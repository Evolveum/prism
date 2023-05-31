/*
 * Copyright (C) 2010-2022 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.prism.impl.schema;

import static com.evolveum.midpoint.util.MiscUtil.argCheck;
import static com.evolveum.midpoint.util.MiscUtil.stateCheck;

import java.util.*;
import javax.xml.namespace.QName;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.EntityResolver;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.impl.*;
import com.evolveum.midpoint.prism.schema.DefinitionSupplier;
import com.evolveum.midpoint.prism.schema.MutablePrismSchema;
import com.evolveum.midpoint.prism.schema.PrismSchema;
import com.evolveum.midpoint.prism.schema.SchemaDescription;
import com.evolveum.midpoint.util.DOMUtil;
import com.evolveum.midpoint.util.QNameUtil;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;

/**
 * @author Radovan Semancik
 */
public class PrismSchemaImpl extends AbstractFreezable implements MutablePrismSchema {

    private static final Trace LOGGER = TraceManager.getTrace(PrismSchema.class);

    @NotNull protected final Collection<Definition> definitions = new ArrayList<>();

    // These maps contain the same objects as are in definitions collection.

    /**
     * Global item definitions contained in this schema, stored in a map for faster access.
     *
     * The key is the item name (qualified or unqualified).
     */
    @NotNull private final Map<QName, ItemDefinition<?>> itemDefinitionMap = new HashMap<>();

    /**
     * Global item definitions contained in this schema, stored in a map for faster access.
     *
     * The key is the type name (always qualified).
     */
    @NotNull private final MultiValuedMap<QName, ItemDefinition<?>> itemDefinitionByTypeMap = new ArrayListValuedHashMap<>();

    /**
     * Type definitions contained in this schema, stored in a map for faster access.
     *
     * The key is the type name (always qualified).
     */
    @NotNull private final Map<QName, TypeDefinition> typeDefinitionMap = new HashMap<>();

    /**
     * Namespace for items defined in this schema.
     */
    @NotNull protected final String namespace;

    protected PrismContext prismContext;

    /**
     * Item definitions that couldn't be created when parsing the schema because of unresolvable CTD.
     * (Caused by the fact that the type resides in another schema.)
     * These definitions are to be resolved after parsing the set of schemas.
     */
    @NotNull private final List<DefinitionSupplier> delayedItemDefinitions = new ArrayList<>();

    /**
     * TODO
     */
    private final Multimap<QName, ItemDefinition<?>> substitutions = HashMultimap.create();

    public PrismSchemaImpl(@NotNull String namespace) {
        argCheck(StringUtils.isNotEmpty(namespace), "Namespace can't be null or empty.");
        this.namespace = namespace;
        this.prismContext = PrismContext.get();
    }

    //region Trivia
    @NotNull
    @Override
    public String getNamespace() {
        return namespace;
    }

    @NotNull
    @Override
    public Collection<Definition> getDefinitions() {
        return Collections.unmodifiableCollection(definitions);
    }

    @SuppressWarnings("unchecked")
    @NotNull
    @Override
    public <T extends Definition> List<T> getDefinitions(@NotNull Class<T> type) {
        List<T> list = new ArrayList<>();
        for (Definition def : definitions) {
            if (type.isAssignableFrom(def.getClass())) {
                list.add((T) def);
            }
        }
        return list;
    }

    @Override
    public void addDelayedItemDefinition(DefinitionSupplier supplier) {
        checkMutable();
        delayedItemDefinitions.add(supplier);
    }

    @NotNull
    List<DefinitionSupplier> getDelayedItemDefinitions() {
        return delayedItemDefinitions;
    }

    @Override
    public boolean isEmpty() {
        return definitions.isEmpty();
    }

    @Override
    public void add(@NotNull Definition def) {
        checkMutable();

        definitions.add(def);

        if (def instanceof ItemDefinition) {
            ItemDefinition<?> itemDef = (ItemDefinition<?>) def;
            itemDefinitionMap.put(itemDef.getItemName(), itemDef);
            QName typeName = def.getTypeName();
            if (QNameUtil.isUnqualified(typeName)) {
                throw new IllegalArgumentException("Item definition (" + itemDef + ") of unqualified type " + typeName + " cannot be added to " + this);
            }
            itemDefinitionByTypeMap.put(typeName, itemDef);

            List<SchemaMigration> schemaMigrations = itemDef.getSchemaMigrations();
            if (schemaMigrations != null) {
                for (SchemaMigration schemaMigration : schemaMigrations) {
                    if (SchemaMigrationOperation.RENAMED_PARENT.equals(schemaMigration.getOperation())) {
                        itemDefinitionMap.put(schemaMigration.getElementQName(), itemDef);
                    }
                }
            }

        } else if (def instanceof TypeDefinition) {
            QName typeName = def.getTypeName();
            if (QNameUtil.isUnqualified(typeName)) {
                throw new IllegalArgumentException("Unqualified definition of type " + typeName + " cannot be added to " + this);
            }
            typeDefinitionMap.put(typeName, (TypeDefinition) def);
        }
    }

    @Override
    public void addSubstitution(QName substitutionHead, ItemDefinition<?> definition) {
        this.substitutions.put(substitutionHead, definition);
    }

    @Override
    public Multimap<QName, ItemDefinition<?>> getSubstitutions() {
        return substitutions;
    }

    @Override
    public PrismContext getPrismContext() {
        return prismContext;
    }
    //endregion

    //region XSD parsing and serialization

    // TODO: cleanup this chaos
    // Currently used for connector schema (at least in production code)
    public static PrismSchema parse(Element element, boolean isRuntime, String shortDescription, PrismContext prismContext)
            throws SchemaException {
        // We need to synchronize, because the DOM structures are not thread-safe, even for reading.
        // Here, DOMUtil.getSchemaTargetNamespace gets an exception, see MID-8860.
        //
        // We intentionally synchronize on the schema element. Note that synchronizing e.g. on the owning ConnectorType object
        // is not sufficient, because of not cloning the embedded schema (59bee63b1b8eb933db39e8a9b61a4023b25ec4c0 - wrong
        // decision at that time) we usually have different connector objects (in parallel threads) sharing the same schema
        // DOM element.
        //
        // FIXME this should be resolved more seriously; maybe we will have to put the schema cloning back?
        //
        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (element) {
            PrismSchemaImpl schema = new PrismSchemaImpl(DOMUtil.getSchemaTargetNamespace(element));
            return parse(element, ((PrismContextImpl) prismContext).getEntityResolver(), schema, isRuntime, shortDescription,
                    false, prismContext);
        }
    }

    // used for parsing prism schemas; only in exceptional cases
    public static PrismSchema parse(Element element, EntityResolver resolver, boolean isRuntime, String shortDescription,
            boolean allowDelayedItemDefinitions, PrismContext prismContext) throws SchemaException {
        PrismSchemaImpl schema = new PrismSchemaImpl(DOMUtil.getSchemaTargetNamespace(element));
        return parse(element, resolver, schema, isRuntime, shortDescription, allowDelayedItemDefinitions, prismContext);
    }

    // main entry point for parsing standard prism schemas
    static void parseSchemas(Element wrapperElement, XmlEntityResolver resolver,
            List<SchemaDescription> schemaDescriptions,
            boolean allowDelayedItemDefinitions, PrismContext prismContext) throws SchemaException {
        DomToSchemaProcessor processor = new DomToSchemaProcessor(resolver, prismContext);
        processor.parseSchemas(schemaDescriptions, wrapperElement, allowDelayedItemDefinitions, "multiple schemas");
    }

    // used for connector and resource schemas
    @Override
    public void parseThis(Element element, boolean isRuntime, String shortDescription, PrismContext prismContext) throws SchemaException {
        checkMutable();
        parse(element, ((PrismContextImpl) prismContext).getEntityResolver(), this, isRuntime, shortDescription, false, prismContext);
    }

    private static PrismSchema parse(Element element, EntityResolver resolver, PrismSchemaImpl schema, boolean isRuntime,
            String shortDescription, boolean allowDelayedItemDefinitions, PrismContext prismContext) throws SchemaException {
        if (element == null) {
            throw new IllegalArgumentException("Schema element must not be null in " + shortDescription);
        }
        DomToSchemaProcessor processor = new DomToSchemaProcessor(resolver, prismContext);
        processor.parseSchema(schema, element, isRuntime, allowDelayedItemDefinitions, shortDescription);
        return schema;
    }

    @NotNull
    @Override
    public Document serializeToXsd() throws SchemaException {
        SchemaToDomProcessor processor = new SchemaToDomProcessor();
        processor.setPrismContext(prismContext);
        return processor.parseSchema(this);
    }
    //endregion

    //region Creating definitions

    /**
     * Creates a new property container definition and adds it to the schema.
     *
     * This is a preferred way how to create definition in the schema.
     *
     * @param localTypeName type name "relative" to schema namespace
     * @return new property container definition
     */
    @Override
    public MutablePrismContainerDefinition<?> createContainerDefinition(String localTypeName) {
        QName typeName = new QName(getNamespace(), localTypeName);
        QName name = new QName(getNamespace(), toElementName(localTypeName));
        ComplexTypeDefinition cTypeDef = new ComplexTypeDefinitionImpl(typeName);
        PrismContainerDefinitionImpl<?> def = new PrismContainerDefinitionImpl<>(name, cTypeDef);
        add(cTypeDef);
        add(def);
        return def;
    }

    @Override
    public MutablePrismContainerDefinition<?> createContainerDefinition(String localItemName, String localTypeName) {
        QName typeName = new QName(getNamespace(), localTypeName);
        QName name = new QName(getNamespace(), localItemName);
        ComplexTypeDefinition cTypeDef = findComplexTypeDefinitionByType(typeName);
        if (cTypeDef == null) {
            cTypeDef = new ComplexTypeDefinitionImpl(typeName);
            add(cTypeDef);
        }
        PrismContainerDefinitionImpl<?> def = new PrismContainerDefinitionImpl<>(name, cTypeDef);
        add(def);
        return def;
    }

    @Override
    public ComplexTypeDefinition createComplexTypeDefinition(QName typeName) {
        ComplexTypeDefinition cTypeDef = new ComplexTypeDefinitionImpl(typeName);
        add(cTypeDef);
        return cTypeDef;
    }

    /**
     * Creates a top-level property definition and adds it to the schema.
     *
     * This is a preferred way how to create definition in the schema.
     *
     * @param localName element name "relative" to schema namespace
     * @param typeName XSD type name of the element
     * @return new property definition
     */
    @Override
    public PrismPropertyDefinition<?> createPropertyDefinition(String localName, QName typeName) {
        QName name = new QName(getNamespace(), localName);
        return createPropertyDefinition(name, typeName);
    }

    /**
     * Creates a top-level property definition and adds it to the schema.
     *
     * This is a preferred way how to create definition in the schema.
     *
     * @param name element name
     * @param typeName XSD type name of the element
     * @return new property definition
     */
    @Override
    public PrismPropertyDefinition<?> createPropertyDefinition(QName name, QName typeName) {
        PrismPropertyDefinition<?> def = new PrismPropertyDefinitionImpl<>(name, typeName);
        add(def);
        return def;
    }

    /**
     * Internal method to create a "nice" element name from the type name.
     */
    private String toElementName(String localTypeName) {
        String elementName = StringUtils.uncapitalize(localTypeName);
        if (elementName.endsWith("Type")) {
            return elementName.substring(0, elementName.length() - 4);
        } else {
            return elementName;
        }
    }
    //endregion

    //region Pretty printing
    @Override
    public String debugDump(int indent) {
        IdentityHashMap<Definition, Object> seen = new IdentityHashMap<>();
        StringBuilder sb = new StringBuilder();
        //noinspection StringRepeatCanBeUsed
        for (int i = 0; i < indent; i++) {
            sb.append(INDENT_STRING);
        }
        sb.append(this).append("\n");
        Iterator<Definition> i = definitions.iterator();
        while (i.hasNext()) {
            Definition def = i.next();
            sb.append(def.debugDump(indent + 1, seen));
            if (i.hasNext()) {
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return "Schema(ns=" + namespace + ")";
    }
    //endregion

    //region Finding definitions

    // items

    @Override
    @NotNull
    public <ID extends ItemDefinition> List<ID> findItemDefinitionsByCompileTimeClass(
            @NotNull Class<?> compileTimeClass, @NotNull Class<ID> definitionClass) {
        List<ID> found = new ArrayList<>();
        for (Definition def : definitions) {
            if (definitionClass.isAssignableFrom(def.getClass())) {
                if (def instanceof PrismContainerDefinition) {
                    @SuppressWarnings("unchecked")
                    ID contDef = (ID) def;
                    if (compileTimeClass.equals(((PrismContainerDefinition<?>) contDef).getCompileTimeClass())) {
                        found.add(contDef);
                    }
                } else if (def instanceof PrismPropertyDefinition) {
                    if (compileTimeClass.equals(prismContext.getSchemaRegistry().determineClassForType(def.getTypeName()))) {
                        @SuppressWarnings("unchecked")
                        ID itemDef = (ID) def;
                        found.add(itemDef);
                    }
                } else {
                    // Skipping the definition, PRD is not supported here.
                    // Currently, this does not work sensibly for midPoint because it has multiple top-level
                    // elements for ObjectReferenceType (not to mention it's common-3, not Prism ORT).
                    // Instead, use findItemDefinitionsByElementName(...) to find the reference definition
                    // by well-known top-level element name .
                }
            }
        }
        return found;
    }

    @Override
    public <ID extends ItemDefinition> ID findItemDefinitionByType(@NotNull QName typeName, @NotNull Class<ID> definitionClass) {
        // TODO: check for multiple definition with the same type
        if (QNameUtil.isQualified(typeName)) {
            Collection<ItemDefinition<?>> definitions = itemDefinitionByTypeMap.get(typeName);
            if (definitions != null) {
                for (Definition definition : definitions) {
                    if (definitionClass.isAssignableFrom(definition.getClass())) {
                        //noinspection unchecked
                        return (ID) definition;
                    }
                }
            }
            return null;
        } else {
            return findItemDefinitionsByUnqualifiedTypeName(typeName, definitionClass);
        }
    }

    @Nullable
    private <ID extends ItemDefinition> ID findItemDefinitionsByUnqualifiedTypeName(
            @NotNull QName typeName, @NotNull Class<ID> definitionClass) {
        LOGGER.warn("Looking for item definition by unqualified type name: {} in {}", typeName, this);
        for (Definition definition : definitions) {     // take from itemDefinitionsMap.values?
            if (definitionClass.isAssignableFrom(definition.getClass())) {
                @SuppressWarnings("unchecked")
                ID itemDef = (ID) definition;
                if (QNameUtil.match(typeName, itemDef.getTypeName())) {
                    return itemDef;
                }
            }
        }
        return null;
    }

    @NotNull
    @Override
    @SuppressWarnings("unchecked")
    public <ID extends ItemDefinition> List<ID> findItemDefinitionsByElementName(@NotNull QName elementName,
            @NotNull Class<ID> definitionClass) {
        List<Definition> matching = new ArrayList<>();
        CollectionUtils.addIgnoreNull(matching, itemDefinitionMap.get(elementName));
        if (QNameUtil.hasNamespace(elementName)) {
            CollectionUtils.addIgnoreNull(matching, itemDefinitionMap.get(QNameUtil.unqualify(elementName)));
        } else {
            CollectionUtils.addIgnoreNull(matching, itemDefinitionMap.get(new QName(namespace, elementName.getLocalPart())));
        }
        List<ID> list = new ArrayList<>();
        for (Definition d : matching) {
            if (definitionClass.isAssignableFrom(d.getClass())) {
                ID id = (ID) d;
                list.add(id);
            }
        }
        return list;
    }

    @Override
    public <C extends Containerable> ComplexTypeDefinition findComplexTypeDefinitionByCompileTimeClass(@NotNull Class<C> compileTimeClass) {
        for (Definition def : definitions) {
            if (def instanceof ComplexTypeDefinition) {
                ComplexTypeDefinition ctd = (ComplexTypeDefinition) def;
                if (compileTimeClass.equals(ctd.getCompileTimeClass())) {
                    return ctd;
                }
            }
        }
        return null;
    }

    @Nullable
    @Override
    public <TD extends TypeDefinition> TD findTypeDefinitionByType(@NotNull QName typeName, @NotNull Class<TD> definitionClass) {
        Collection<TD> definitions = findTypeDefinitionsByType(typeName, definitionClass);
        return !definitions.isEmpty() ?
                definitions.iterator().next() : null;        // TODO treat multiple results somehow
    }

    @NotNull
    @Override
    public <TD extends TypeDefinition> Collection<TD> findTypeDefinitionsByType(
            @NotNull QName typeName, @NotNull Class<TD> definitionClass) {
        List<TD> rv = new ArrayList<>();
        addMatchingTypeDefinitions(rv, typeDefinitionMap.get(typeName), definitionClass);
        if (QNameUtil.isUnqualified(typeName)) {
            addMatchingTypeDefinitions(rv, typeDefinitionMap.get(new QName(namespace, typeName.getLocalPart())), definitionClass);
        }
        return rv;
    }

    private <TD extends TypeDefinition> void addMatchingTypeDefinitions(List<TD> matching, TypeDefinition typeDefinition,
            Class<TD> definitionClass) {
        if (typeDefinition != null && definitionClass.isAssignableFrom(typeDefinition.getClass())) {
            //noinspection unchecked
            matching.add((TD) typeDefinition);
        }
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public <TD extends TypeDefinition> TD findTypeDefinitionByCompileTimeClass(
            @NotNull Class<?> compileTimeClass, @NotNull Class<TD> definitionClass) {
        // TODO: check for multiple definition with the same type
        for (Definition definition : definitions) {
            if (definitionClass.isAssignableFrom(definition.getClass())
                    && compileTimeClass.equals(((TD) definition).getCompileTimeClass())) {
                return (TD) definition;
            }
        }
        return null;
    }

    //endregion

    @Override
    public void performFreeze() {
        definitions.forEach(Freezable::freeze);
    }

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    public PrismSchemaImpl clone() {
        PrismSchemaImpl clone = new PrismSchemaImpl(namespace);
        copyContent(clone);
        return clone;
    }

    protected void copyContent(PrismSchemaImpl target) {
        assertNoDelayedDefinitionsOnClone();
        for (Definition definition : definitions) {
            target.add(definition.clone());
        }
        // TODO ok?
        substitutions.forEach(
                (name, definition) -> target.addSubstitution(name, definition.clone()));
    }

    protected void assertNoDelayedDefinitionsOnClone() {
        stateCheck(delayedItemDefinitions.isEmpty(),
                "Cannot clone schema with delayed definitions: %s (%s)", delayedItemDefinitions.size(), this);
    }
}
