/*
 * Copyright (c) 2010-2018 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism.impl.marshaller;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.impl.PrismPropertyDefinitionImpl;
import com.evolveum.midpoint.prism.schema.DefinitionStoreUtils;
import com.evolveum.midpoint.prism.schema.SchemaRegistry;
import com.evolveum.midpoint.util.MiscUtil;
import com.evolveum.midpoint.util.QNameUtil;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;
import org.jetbrains.annotations.NotNull;

import javax.xml.namespace.QName;
import java.util.List;

public class ItemInfo<ID extends ItemDefinition> {
    private QName itemName;
    private ID itemDefinition;
    private QName typeName;
    private ComplexTypeDefinition complexTypeDefinition;

    private static final Trace LOGGER = TraceManager.getTrace(ItemInfo.class);

    /**
     * This method is to be called ONLY on the root level, i.e. when unmarshalling starts.
     */
    @NotNull
    public static <ID extends ItemDefinition> ItemInfo determine(ItemDefinition itemDefinitionExplicit,
            QName itemNameFromSource, QName itemNameExplicit, QName itemNameDefault,
            QName typeNameFromSource, QName typeNameExplicit,
            Class<?> classExplicit,
            @NotNull Class<ID> definitionClass, @NotNull ParsingContext pc, @NotNull SchemaRegistry schemaRegistry) throws SchemaException {

        // deriving definition

        ID definition;
        if (itemDefinitionExplicit != null) {
            if (!definitionClass.isAssignableFrom(itemDefinitionExplicit.getClass())) {
                throw new IllegalArgumentException("ItemDefinition of " + itemDefinitionExplicit + " doesn't match expected "
                        + "definition class of " + definitionClass);
            }
            @SuppressWarnings("unchecked")
            ID narrowedDefinitionExplicit = (ID) itemDefinitionExplicit;
            definition = narrowedDefinitionExplicit;
        } else {
            definition = null;
        }
        definition = augmentWithType(definition, definitionClass, schemaRegistry, typeNameExplicit);
        definition = augmentWithType(definition, definitionClass, schemaRegistry, typeNameFromSource);
        definition = augmentWithClass(definition, definitionClass, schemaRegistry, classExplicit);
        definition = augmentWithItemName(definition, definitionClass, schemaRegistry, itemNameFromSource);

        // Sanity check: if typeNameFromSource or typeNameExplicit are not compatible with the type derived from definition,
        // we will NOT use the definition. For example, if client is looking for ConditionalSearchFilterType (has no ItemDefinition)
        // the definition here would be very probably SearchFilterType (based e.g. on the itemNameFromSource). And it has not
        // to be used, as the client explicitly requested ConditionalSearchFilterType.
        //
        // Exception is PrismReferenceDefinition, as there are no subtypes in that case (yet)
        if (definition != null && !(definition instanceof PrismReferenceDefinition)) {
            QName typeName = definition.getTypeName();
            if (typeNameExplicit != null && !schemaRegistry.isAssignableFrom(typeNameExplicit, typeName)
                || typeNameFromSource != null && !schemaRegistry.isAssignableFrom(typeNameFromSource, typeName)) {
                // the result would NOT match typeNameExplicit/typeNameFromSource (very probably)
                // so we have to abandon this definition
                definition = null;
            }
        }

        ItemInfo<ID> info = new ItemInfo<>();
        info.itemDefinition = definition;

        // deriving item name
        if (itemNameExplicit != null) {
            info.itemName = itemNameExplicit;
        } else if (itemNameFromSource != null) {
            if (QNameUtil.noNamespace(itemNameFromSource) && definition != null) {
                info.itemName = new QName(definition.getItemName().getNamespaceURI(), itemNameFromSource.getLocalPart());
            } else {
                info.itemName = itemNameFromSource;
            }
        } else if (definition != null) {
            info.itemName = definition.getItemName();
        } else {
            info.itemName = itemNameDefault;
        }

        // Schema migration
        if (definition != null) {
            if (!info.itemName.equals(definition.getItemName())) {
                List<SchemaMigration> schemaMigrations = definition.getSchemaMigrations();
                if (schemaMigrations != null) {
                    for (SchemaMigration schemaMigration : schemaMigrations) {
                        if (SchemaMigrationOperation.RENAMED_PARENT.equals(schemaMigration.getOperation()) &&
                                info.itemName.equals(schemaMigration.getElementQName())) {
                            LOGGER.warn("Using legacy element name {} for definition {}", info.itemName, definition);
                            info.itemName = definition.getItemName();
                        }
                    }
                }
            }
        }

        // type name
        if (definition != null) {
            info.typeName = definition.getTypeName();
        } else if (typeNameExplicit == null && typeNameFromSource == null && classExplicit != null) {
            info.typeName = schemaRegistry.determineTypeForClass(classExplicit);
            if (info.typeName != null && info.itemName != null) {
                // Create artificial definition. We do NOT mark it as dynamic, because it might come from client code-supplied
                // information (i.e. not from the dynamic one). This might cause problems during serialization; but more probably
                // not - the decision on putting explicit type declaration on the root level is not driven by the 'dynamic' flag.
                // And this method is called only at the root level.
                if (Objectable.class.isAssignableFrom(classExplicit)) {
                    throw new IllegalStateException("Prism object type " + info.typeName + " without definition");
                } else if (Containerable.class.isAssignableFrom(classExplicit)) {
                    @SuppressWarnings("unchecked")
                    Class<Containerable> containerableClass = (Class<Containerable>) classExplicit;
                    ComplexTypeDefinition ctd =
                            MiscUtil.requireNonNull(
                                    schemaRegistry.findComplexTypeDefinitionByCompileTimeClass(containerableClass),
                                    "No complex type definition for %s", containerableClass);
                    @SuppressWarnings("unchecked")
                    ID id = (ID) PrismContext.get().definitionFactory().newContainerDefinition(info.itemName, ctd);
                    info.itemDefinition = id;
                } else {
                    @SuppressWarnings("unchecked")
                    ID id = (ID) new PrismPropertyDefinitionImpl<>(info.itemName, info.typeName);
                    info.itemDefinition = id;
                }
            }
        } else if (typeNameExplicit == null) {
            info.typeName = typeNameFromSource;
        } else if (typeNameFromSource == null) {
            info.typeName = typeNameExplicit;
        } else if (schemaRegistry.isAssignableFrom(typeNameExplicit, typeNameFromSource)) {
            info.typeName = typeNameFromSource;
        } else {
            info.typeName = typeNameExplicit;
        }

        // complex type definition
        if (info.itemDefinition != null) {
            if (info.itemDefinition instanceof PrismContainerDefinition) {
                info.complexTypeDefinition = ((PrismContainerDefinition) info.itemDefinition).getComplexTypeDefinition();
            } else {
                info.complexTypeDefinition = null;
            }
        } else if (info.typeName != null) {
            // TODO probably optimize (e.g. if we already know the class ... )
            info.complexTypeDefinition = schemaRegistry.findComplexTypeDefinitionByType(info.typeName);
        } else {
            info.complexTypeDefinition = null;
        }

        return info;
    }

    private static <ID extends ItemDefinition> ID augmentWithType(ID definition, Class<ID> definitionClass,
            SchemaRegistry schemaRegistry, QName typeName) throws SchemaException {
        if (typeName == null) {
            return definition;
        }
        if (definition != null && QNameUtil.match(definition.getTypeName(), typeName)) {        // just an optimization to avoid needless lookups
            return definition;
        }
        ItemDefinition rawDefFromType = schemaRegistry.findItemDefinitionByType(typeName);
        if (rawDefFromType == null) {
            return definition;          // TODO warning if wrong type?
        }
        if (!definitionClass.isAssignableFrom(rawDefFromType.getClass())) {
            throw new SchemaException("Wrong type name " + typeName + " (not a " + definitionClass.getClass().getSimpleName() + ")");       // TODO context of error
        }
        @SuppressWarnings("unchecked")
        ID defFromType = (ID) rawDefFromType;
        if (definition instanceof PrismReferenceDefinition && defFromType instanceof PrismObjectDefinition) {
            return definition;        // nothing to do; this is a reference holding a composite object
        }
        return schemaRegistry.selectMoreSpecific(definition, defFromType);
    }

    private static <ID extends ItemDefinition> ID augmentWithItemName(ID definition, Class<ID> definitionClass,
            SchemaRegistry schemaRegistry, QName itemName) throws SchemaException {
        if (itemName == null || definition != null && QNameUtil.match(definition.getItemName(), itemName)) {        // just an optimization to avoid needless lookups
            return definition;
        }
        ID defFromItemName = DefinitionStoreUtils.getOne(
                schemaRegistry.findItemDefinitionsByElementName(itemName, definitionClass),
                false, null);
        if (defFromItemName == null) {        // zero or more than one
            return definition;
        }
        if (definition == null) {
            return defFromItemName;
        }
        SchemaRegistry.ComparisonResult comparisonResult = schemaRegistry.compareDefinitions(definition, defFromItemName);
        switch (comparisonResult) {
            case EQUAL: return definition;                // definition is generally considered 'at least as good' as the new one
            case NO_STATIC_CLASS: return definition;    // play safe, select original definition (item name is not very trustworthy source of information)
            case INCOMPATIBLE: return definition;        // TODO probably we should signal a problem
            case FIRST_IS_CHILD: return definition;
            case SECOND_IS_CHILD: return defFromItemName;
            default: throw new AssertionError("Invalid result: " + comparisonResult);
        }
    }

    private static <ID extends ItemDefinition> ID augmentWithClass(ID definition, Class<ID> definitionClass,
            SchemaRegistry schemaRegistry, Class<?> clazz) throws SchemaException {
        if (clazz == null) {
            return definition;
        }
        if (definition instanceof PrismContainerDefinition && clazz.equals(((PrismContainerDefinition) definition).getCompileTimeClass())) {
            return definition;
        }
        QName typeNameFromClass = schemaRegistry.determineTypeForClass(clazz);
        if (typeNameFromClass == null) {
            throw new SchemaException("Unknown class " + clazz.getName());
        }
        if (definition != null && definition.getTypeName().equals(typeNameFromClass)) {
            return definition;
        }
        @SuppressWarnings("unchecked")
        List<ID> defFromClass = schemaRegistry.findItemDefinitionsByCompileTimeClass(clazz, (Class) definitionClass);
        ID singleDef = DefinitionStoreUtils.getOne(defFromClass, false, null);
        if (singleDef == null) {
            return definition;
        } else {
            return schemaRegistry.selectMoreSpecific(definition, singleDef);
        }
    }

    public QName getItemName() {
        return itemName;
    }

    public ID getItemDefinition() {
        return itemDefinition;
    }

    public ComplexTypeDefinition getComplexTypeDefinition() {
        return complexTypeDefinition;
    }

    public QName getTypeName() {
        return typeName;
    }

    @NotNull
    public static ItemInfo determineFromValue(@NotNull PrismValue value, QName itemName, ItemDefinition itemDefinition,
            @NotNull SchemaRegistry schemaRegistry) {
        ItemInfo info = new ItemInfo();

        // definition
        info.itemDefinition = itemDefinition;
        if (info.itemDefinition == null && value.getParent() != null) {
            info.itemDefinition = itemDefinition = value.getParent().getDefinition();
            if (info.itemDefinition == null) {
                info.itemDefinition = schemaRegistry.findItemDefinitionByElementName(value.getParent().getElementName());
            }
        }
        if (info.itemDefinition == null) {
            Class<?> realClass = value.getRealClass();
            if (realClass != null) {
                List<ItemDefinition> definitions = schemaRegistry.findItemDefinitionsByCompileTimeClass(realClass, ItemDefinition.class);
                if (definitions.size() == 1) {
                    info.itemDefinition = definitions.get(0);
                }
            }
        }

        // item name
        info.itemName = itemName;
        if (info.itemName == null && value.getParent() != null) {
            info.itemName = value.getParent().getElementName();
        }
        if (info.itemName == null && info.itemDefinition != null) {
            info.itemName = info.itemDefinition.getItemName();
        }

        // item type
        if (itemDefinition != null) {
            info.typeName = itemDefinition.getTypeName();
        } else {
            Class<?> realClass = value.getRealClass();
            if (realClass != null) {
                info.typeName = schemaRegistry.determineTypeForClass(realClass);
            }
        }

        return info;
    }

    @Override
    public String toString() {
        return "ItemInfo{" +
                "itemName=" + itemName +
                ", itemDefinition=" + itemDefinition +
                ", typeName=" + typeName +
                ", complexTypeDefinition=" + complexTypeDefinition +
                '}';
    }
}
