/*
 * Copyright (C) 2010-2022 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.prism.impl;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;

import com.evolveum.midpoint.prism.PrismContainerDefinition.PrismContainerDefinitionMutator;
import com.evolveum.midpoint.prism.path.ItemName;

import com.evolveum.midpoint.prism.schema.*;

import com.evolveum.midpoint.prism.schemaContext.SchemaContextDefinition;

import org.jetbrains.annotations.NotNull;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.annotation.ItemDiagramSpecification;
import com.evolveum.midpoint.prism.delta.ContainerDelta;
import com.evolveum.midpoint.prism.impl.delta.ContainerDeltaImpl;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.prism.util.DefinitionUtil;
import com.evolveum.midpoint.util.DOMUtil;
import com.evolveum.midpoint.util.DebugUtil;
import com.evolveum.midpoint.util.QNameUtil;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.google.common.collect.ImmutableSet;
import org.jetbrains.annotations.Nullable;

/**
 * Definition of a property container.
 * <p>
 * Property container groups properties into logical blocks. The reason for
 * grouping may be as simple as better understandability of data structure. But
 * the group usually means different meaning, source or structure of the data.
 * For example, the property container is frequently used to hold properties
 * that are dynamic, not fixed by a static schema. Such grouping also naturally
 * translates to XML and helps to "quarantine" such properties to avoid Unique
 * Particle Attribute problems.
 * <p>
 * Property Container contains a set of (potentially multi-valued) properties.
 * The order of properties is not significant, regardless of the fact that it
 * may be fixed in the XML representation. In the XML representation, each
 * element inside Property Container must be either Property or a Property
 * Container.
 *
 * This class represents schema definition for property container. See
 * {@link Definition} for more details.
 *
 * Don't call constructors of this class directly. Use methods in the {@link DefinitionFactory} instead.
 *
 * @author Radovan Semancik
 */
public class PrismContainerDefinitionImpl<C extends Containerable>
        extends ItemDefinitionImpl<PrismContainer<C>>
        implements PrismContainerDefinition<C>, PrismContainerDefinitionMutator<C>, SerializableContainerDefinition {

    @Serial private static final long serialVersionUID = -5068923696147960699L;

    // There are situations where CTD is (maybe) null but class is defined.
    // TODO clean up this.
    protected ComplexTypeDefinition complexTypeDefinition;
    protected Class<C> compileTimeClass;
    private Set<QName> alwaysUseForEquals = Collections.emptySet();

    /** Standard case, instantiating from known {@link ComplexTypeDefinition} (with or without compile time class). */
    protected PrismContainerDefinitionImpl(@NotNull QName itemName, @NotNull ComplexTypeDefinition complexTypeDefinition) {
        //noinspection unchecked
        this(itemName, complexTypeDefinition.getTypeName(), complexTypeDefinition,
                (Class<C>) complexTypeDefinition.getCompileTimeClass(), null, complexTypeDefinition.schemaLookup());
    }

    PrismContainerDefinitionImpl(@NotNull QName itemName, @NotNull ComplexTypeDefinition complexTypeDefinition, QName definedInType) {
        //noinspection unchecked
        this(itemName, complexTypeDefinition.getTypeName(), complexTypeDefinition,
                (Class<C>) complexTypeDefinition.getCompileTimeClass(), definedInType, complexTypeDefinition.schemaLookup());
    }

    /** Special case, having no complex type definition. */
    PrismContainerDefinitionImpl(@NotNull QName itemName, @NotNull QName typeName, SchemaLookup lookup) {
        this(itemName, typeName, null, null, null, lookup);
    }

    PrismContainerDefinitionImpl(
            @NotNull QName name,
            @NotNull QName typeName,
            ComplexTypeDefinition complexTypeDefinition,
            Class<C> compileTimeClass,
            QName definedInType,
            SchemaLookup lookup) {
        super(name, typeName, definedInType);
        this.complexTypeDefinition = complexTypeDefinition;
        if (complexTypeDefinition == null) {
            isRuntimeSchema = true;
            super.setDynamic(true);             // todo is this really ok?
        } else {
            isRuntimeSchema = complexTypeDefinition.isXsdAnyMarker();
            //super.setDynamic(isRuntimeSchema);  // todo is this really ok?
        }
        this.compileTimeClass = compileTimeClass;
        setSchemaLookup(lookup);
    }

    private static QName determineTypeName(ComplexTypeDefinition complexTypeDefinition) {
        if (complexTypeDefinition == null) {
            // Property container without type: xsd:any
            // FIXME: this is kind of hack, but it works now
            return DOMUtil.XSD_ANY;
        }
        return complexTypeDefinition.getTypeName();
    }

    @Override
    public Class<C> getCompileTimeClass() {
        if (compileTimeClass != null) {
            return compileTimeClass;
        }
        if (complexTypeDefinition == null) {
            return null;
        }
        //noinspection unchecked
        return (Class<C>) complexTypeDefinition.getCompileTimeClass();
    }

    @Override
    public void setCompileTimeClass(Class<C> compileTimeClass) {
        checkMutable();
        this.compileTimeClass = compileTimeClass;
    }

    @Override
    public Class<C> getTypeClass() {
        return compileTimeClass;
    }

    protected String getSchemaNamespace() {
        return getItemName().getNamespaceURI();
    }

    @Override
    public ComplexTypeDefinition getComplexTypeDefinition() {
        return complexTypeDefinition;
    }

    @Override
    public SerializableComplexTypeDefinition getComplexTypeDefinitionToSerialize() {
        if (complexTypeDefinition == null) {
            return null;
        } else if (complexTypeDefinition instanceof SerializableComplexTypeDefinition sctd) {
            return sctd;
        } else {
            throw new IllegalStateException("Unserializable CTD? " + complexTypeDefinition);
        }
    }

    @Override
    public void setComplexTypeDefinition(ComplexTypeDefinition complexTypeDefinition) {
        checkMutable();
        this.complexTypeDefinition = complexTypeDefinition;
    }

    @Override
    public boolean isAbstract() {
        return super.isAbstract() || complexTypeDefinition != null && complexTypeDefinition.isAbstract();
    }

    @Override
    public <ID extends ItemDefinition<?>> ID findItemDefinition(@NotNull ItemPath path, @NotNull Class<ID> clazz) {
        for (; ; ) {
            if (path.isEmpty()) {
                if (clazz.isAssignableFrom(PrismContainerDefinition.class)) {
                    //noinspection unchecked
                    return (ID) this;
                } else {
                    return null;
                }
            }
            Object first = path.first();
            if (ItemPath.isName(first)) {
                return findNamedItemDefinition(ItemPath.toName(first), path.rest(), clazz);
            } else if (ItemPath.isId(first)) {
                path = path.rest();
            } else if (ItemPath.isParent(first)) {
                ItemPath rest = path.rest();
                ComplexTypeDefinition parent = PrismContext.get().getSchemaRegistry().determineParentDefinition(getComplexTypeDefinition(), rest);
                if (rest.isEmpty()) {
                    // requires that the parent is defined as an item (container, object)
                    //noinspection unchecked
                    return (ID) PrismContext.get().getSchemaRegistry().findItemDefinitionByType(parent.getTypeName());
                } else {
                    return parent.findItemDefinition(rest, clazz);
                }
            } else if (ItemPath.isObjectReference(first)) {
                throw new IllegalStateException("Couldn't use '@' path segment in this context. PCD=" + getTypeName() + ", path=" + path);
            } else if (ItemPath.isIdentifier(first)) {
                if (!clazz.isAssignableFrom(PrismPropertyDefinition.class)) {
                    return null;
                }
                PrismPropertyDefinitionImpl<?> oidDefinition;
                if (this instanceof PrismObjectDefinition) {
                    oidDefinition = new PrismPropertyDefinitionImpl<>(PrismConstants.T_ID, DOMUtil.XSD_STRING);
                } else {
                    oidDefinition = new PrismPropertyDefinitionImpl<>(PrismConstants.T_ID, DOMUtil.XSD_INTEGER);
                }
                oidDefinition.setMaxOccurs(1);
                //noinspection unchecked
                return (ID) oidDefinition;
            } else {
                throw new IllegalStateException("Unexpected path segment: " + first + " in " + path);
            }
        }
    }

    private <ID extends ItemDefinition<?>> ID findNamedItemDefinition(
            @NotNull QName firstName, @NotNull ItemPath rest, @NotNull Class<ID> clazz) {

        var ctd = complexTypeDefinition;
        if (ctd != null) {
            var def = ctd.findLocalItemDefinition(firstName);
            if (def != null) {
                return def.findItemDefinition(rest, clazz);
            }
        }

        if (ctd != null && ctd.isXsdAnyMarker()) {
            SchemaRegistry schemaRegistry = PrismContext.get().getSchemaRegistry();
            ItemDefinition<?> def = schemaRegistry.findItemDefinitionByElementName(firstName);
            if (def != null) {
                return def.findItemDefinition(rest, clazz);
            }
        }

        return null;
    }

    @Override
    public <ID extends ItemDefinition<?>> ID findLocalItemDefinition(
            @NotNull QName name, @NotNull Class<ID> clazz, boolean caseInsensitive) {
        return findLocalItemDefinitionByIteration(name, clazz, caseInsensitive);
    }

    /**
     * FIXME this looks like outdated description
     *
     * Returns set of property definitions.
     * <p>
     * WARNING: This may return definitions from the associated complex type.
     * Therefore changing the returned set may influence also the complex type definition.
     * <p>
     * The set contains all property definitions of all types that were parsed.
     * Order of definitions is insignificant.
     *
     * @return list of definitions
     */
    @Override
    public @NotNull List<? extends ItemDefinition<?>> getDefinitions() {
        if (complexTypeDefinition == null) {
            // e.g. for xsd:any containers
            // FIXME
            return new ArrayList<>();
        }
        return complexTypeDefinition.getDefinitions();
    }

    @Override
    public void checkMutable() {

    }

    @Override
    public List<PrismPropertyDefinition<?>> getPropertyDefinitions() {
        return complexTypeDefinition != null ? complexTypeDefinition.getPropertyDefinitions() : List.of();
    }

    @NotNull
    @Override
    public PrismContainer<C> instantiate() throws SchemaException {
        return instantiate(getItemName());
    }

    @NotNull
    @Override
    public PrismContainer<C> instantiate(QName elementName) throws SchemaException {
        elementName = DefinitionUtil.addNamespaceIfApplicable(elementName, this.itemName);
        return new PrismContainerImpl<>(elementName, this);
    }

    @Override
    public @NotNull ContainerDelta<C> createEmptyDelta(ItemPath path) {
        return new ContainerDeltaImpl<>(path, this);
    }

    @Override
    public boolean accept(Visitor<Definition> visitor, SmartVisitation<Definition> visitation) {
        if (super.accept(visitor, visitation)) {
            if (complexTypeDefinition != null) {
                complexTypeDefinition.accept(visitor, visitation);
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * Shallow clone
     */
    @NotNull
    @Override
    public PrismContainerDefinitionImpl<C> clone() {
        PrismContainerDefinitionImpl<C> clone = // TODO should we copy also "defined in type"?
                new PrismContainerDefinitionImpl<>(itemName, typeName, complexTypeDefinition, compileTimeClass, null, schemaLookup());
        clone.copyDefinitionDataFrom(this);
        return clone;
    }

    @Override
    public @NotNull PrismContainerDefinition<?> cloneWithNewType(
            @NotNull QName newTypeName, @NotNull ComplexTypeDefinition newCtd) {
        // Note that we need the explicit type of PrismContainerDefinitionImpl<C> in order to invoke "copyDefinitionDataFrom"
        // on this object. If we use implicit "var" here, we get PrismContainerDefinitionImpl<?>, and the copier method invoked
        // will be on DefinitionImpl, skipping cloning of item definition aspects!
        //noinspection unchecked
        PrismContainerDefinitionImpl<C> clone =
                (PrismContainerDefinitionImpl<C>)
                        new PrismContainerDefinitionImpl<>(itemName, newTypeName, newCtd,
                                (Class<? extends Containerable>) newCtd.getCompileTimeClass(), null, schemaLookup());
        clone.copyDefinitionDataFrom(this);
        return clone;
    }

    @Override
    public @NotNull PrismContainerDefinition<C> cloneWithNewName(@NotNull ItemName newItemName) {
        var clone = new PrismContainerDefinitionImpl<>(newItemName, typeName, complexTypeDefinition, compileTimeClass, null, schemaLookup());
        clone.copyDefinitionDataFrom(this);
        return clone;
    }

    protected void copyDefinitionDataFrom(PrismContainerDefinition<C> source) {
        super.copyDefinitionDataFrom(source);
        // nothing to do here
    }

    @Override
    public ItemDefinition<PrismContainer<C>> deepClone(@NotNull DeepCloneOperation operation) {
        PrismContainerDefinitionImpl<C> clone = clone();
        ComplexTypeDefinition ctd = getComplexTypeDefinition();
        if (ctd != null) {
            ctd = ctd.deepClone(operation);
            clone.setComplexTypeDefinition(ctd);
        }
        return clone;
    }

    @Override
    @NotNull
    public PrismContainerDefinition<C> cloneWithNewDefinition(QName newItemName, ItemDefinition<?> newDefinition) {
        PrismContainerDefinitionImpl<C> clone = clone();
        clone.replaceDefinition(newItemName, newDefinition);
        return clone;
    }

    @Override
    public void replaceDefinition(QName itemName, ItemDefinition<?> newDefinition) {
        ComplexTypeDefinition originalComplexTypeDefinition = getComplexTypeDefinition();
        ComplexTypeDefinition cloneComplexTypeDefinition = originalComplexTypeDefinition.clone();
        setComplexTypeDefinition(cloneComplexTypeDefinition);
        ((ComplexTypeDefinitionImpl) cloneComplexTypeDefinition).replaceDefinition(itemName, newDefinition);
    }

    /**
     * Creates new instance of property definition and adds it to the container.
     * <p>
     * This is the preferred method of creating a new definition.
     *
     * @param name name of the property (element name)
     * @param typeName XSD type of the property
     * @return created property definition
     */
    @Override
    public PrismPropertyDefinitionImpl<?> createPropertyDefinition(QName name, QName typeName) {
        PrismPropertyDefinitionImpl<?> propDef = new PrismPropertyDefinitionImpl<>(name, typeName);
        addDefinition(propDef);
        return propDef;
    }

    private void addDefinition(ItemDefinition<?> itemDef) {
        checkMutable();
        if (complexTypeDefinition == null) {
            throw new UnsupportedOperationException("Cannot add an item definition because there's no complex type definition");
        } else if (!(complexTypeDefinition instanceof ComplexTypeDefinitionImpl)) {
            throw new UnsupportedOperationException("Cannot add an item definition into complex type definition of type " + complexTypeDefinition.getClass().getName());
        } else {
            ((ComplexTypeDefinitionImpl) complexTypeDefinition).add(itemDef);
        }
    }

    /**
     * Creates new instance of property definition and adds it to the container.
     * <p>
     * This is the preferred method of creating a new definition.
     *
     * @param name name of the property (element name)
     * @param typeName XSD type of the property
     * @param minOccurs minimal number of occurrences
     * @param maxOccurs maximal number of occurrences (-1 means unbounded)
     * @return created property definition
     */
    @Override
    public PrismPropertyDefinition<?> createPropertyDefinition(QName name, QName typeName,
            int minOccurs, int maxOccurs) {
        PrismPropertyDefinitionImpl<?> propDef = new PrismPropertyDefinitionImpl<>(name, typeName);
        propDef.setMinOccurs(minOccurs);
        propDef.setMaxOccurs(maxOccurs);
        addDefinition(propDef);
        return propDef;
    }

    // Creates reference to other schema
    // TODO: maybe check if the name is in different namespace
    // TODO: maybe create entirely new concept of property reference?
    public PrismPropertyDefinition<?> createPropertyDefinition(QName name) {
        PrismPropertyDefinition<?> propDef = new PrismPropertyDefinitionImpl<>(name, null);
        addDefinition(propDef);
        return propDef;
    }

    /**
     * Creates new instance of property definition and adds it to the container.
     * <p>
     * This is the preferred method of creating a new definition.
     *
     * @param localName name of the property (element name) relative to the schema namespace
     * @param typeName XSD type of the property
     * @return created property definition
     */
    @Override
    public PrismPropertyDefinition<?> createPropertyDefinition(String localName, QName typeName) {
        QName name = new QName(getSchemaNamespace(), localName);
        return createPropertyDefinition(name, typeName);
    }

    /**
     * Creates new instance of property definition and adds it to the container.
     * <p>
     * This is the preferred method of creating a new definition.
     *
     * @param localName name of the property (element name) relative to the schema namespace
     * @param localTypeName XSD type of the property
     * @return created property definition
     */
    public PrismPropertyDefinition<?> createPropertyDefinition(String localName, String localTypeName) {
        QName name = new QName(getSchemaNamespace(), localName);
        QName typeName = new QName(getSchemaNamespace(), localTypeName);
        return createPropertyDefinition(name, typeName);
    }

    /**
     * Creates new instance of property definition and adds it to the container.
     * <p>
     * This is the preferred method of creating a new definition.
     *
     * @param localName name of the property (element name) relative to the schema namespace
     * @param localTypeName XSD type of the property
     * @param minOccurs minimal number of occurrences
     * @param maxOccurs maximal number of occurrences (-1 means unbounded)
     * @return created property definition
     */
    public PrismPropertyDefinition<?> createPropertyDefinition(String localName, String localTypeName,
            int minOccurs, int maxOccurs) {
        QName name = new QName(getSchemaNamespace(), localName);
        QName typeName = new QName(getSchemaNamespace(), localTypeName);
        PrismPropertyDefinitionImpl<?> propertyDefinition = createPropertyDefinition(name, typeName);
        propertyDefinition.setMinOccurs(minOccurs);
        propertyDefinition.setMaxOccurs(maxOccurs);
        return propertyDefinition;
    }

    public PrismContainerDefinition<?> createContainerDefinition(QName name, QName typeName) {
        return createContainerDefinition(name, typeName, 1, 1);
    }

    @Override
    public PrismContainerDefinition<?> createContainerDefinition(
            QName name, QName typeName, int minOccurs, int maxOccurs) {
        PrismSchema typeSchema = PrismContext.get().getSchemaRegistry().findSchemaByNamespace(typeName.getNamespaceURI());
        if (typeSchema == null) {
            throw new IllegalArgumentException("Schema for namespace " + typeName.getNamespaceURI() + " is not known in the prism context");
        }
        ComplexTypeDefinition typeDefinition = typeSchema.findComplexTypeDefinitionByType(typeName);
        if (typeDefinition == null) {
            throw new IllegalArgumentException("Type " + typeName + " is not known in the schema");
        }
        return createContainerDefinition(name, typeDefinition, minOccurs, maxOccurs);
    }

    @Override
    public PrismContainerDefinition<?> createContainerDefinition(
            @NotNull QName name, @NotNull ComplexTypeDefinition ctd, int minOccurs, int maxOccurs) {
        PrismContainerDefinition<C> def =
                PrismContext.get().definitionFactory().createContainerDefinition(name, ctd, minOccurs, maxOccurs);
        addDefinition(def);
        return def;
    }

    @Override
    public boolean canRepresent(@NotNull QName typeName) {
        return QNameUtil.match(this.typeName, typeName) || complexTypeDefinition.canRepresent(typeName);
    }

    @Override
    public PrismContainerValue<C> createValue() {
        return new PrismContainerValueImpl<>();
    }

    @Override
    public List<ItemDiagramSpecification> getDiagrams() {
        List<ItemDiagramSpecification> diagrams = super.getDiagrams();
        if (diagrams != null) {
            return diagrams;
        }
        return complexTypeDefinition.getDiagrams();
    }

    @Override
    public String debugDump(int indent) {
        return debugDump(indent, new IdentityHashMap<>());
    }

    @Override
    public String debugDump(int indent, IdentityHashMap<Definition, Object> seen) {
        StringBuilder sb = DebugUtil.createIndentedStringBuilder(indent);
        sb.append(this);
        extendDumpHeader(sb);
        if (isRuntimeSchema()) {
            sb.append(" dynamic");
        }
        if (seen.containsKey(this) || complexTypeDefinition != null && seen.containsKey(complexTypeDefinition)) {
            sb.append(" (already shown)");
        } else {
            seen.put(this, null);
            if (complexTypeDefinition != null) {
                seen.put(complexTypeDefinition, null);
            }
            for (Definition def : getDefinitions()) {
                sb.append("\n");
                sb.append(def.debugDump(indent + 1, seen));
            }
        }
        return sb.toString();
    }

    @Override
    public boolean isEmpty() {
        return complexTypeDefinition.isEmpty();
    }

    /**
     * Return a human readable name of this class suitable for logs.
     */
    @Override
    public String getDebugDumpClassName() {
        return "PCD";
    }

    @Override
    public String getDocClassName() {
        return "container";
    }

    @Override
    public PrismContainerDefinitionMutator<C> mutator() {
        checkMutableOnExposing();
        return this;
    }

    @Override
    public void setAlwaysUseForEquals(@NotNull Collection<QName> keysElem) {
        alwaysUseForEquals = ImmutableSet.copyOf(keysElem);
    }

    @Override
    public Collection<QName> getAlwaysUseForEquals() {
        return alwaysUseForEquals;
    }

    @Override
    public void performFreeze() {
        // We do not "own" complex type definition so we do not freeze it here
        super.performFreeze();
    }

//    @Override
//    protected boolean skipFreeze() {
//        return itemName.getLocalPart().equals(PrismConstants.EXTENSION_LOCAL_NAME);
//
//    }

    @Override
    public SchemaContextDefinition getSchemaContextDefinition() {
        return super.getSchemaContextDefinition() != null ?
                super.getSchemaContextDefinition() :
                getComplexTypeDefinition().getSchemaContextDefinition();
    }

    @Override
    public @Nullable List<QName> getNaturalKeyConstituents() {
        List<QName> constituents = super.getNaturalKeyConstituents();
        if (constituents != null) {
            return constituents;
        }

        return getComplexTypeDefinition().getNaturalKeyConstituents();
    }
}
