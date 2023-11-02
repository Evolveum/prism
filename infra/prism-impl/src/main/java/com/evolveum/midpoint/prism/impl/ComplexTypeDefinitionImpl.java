/*
 * Copyright (c) 2010-2018 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism.impl;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.path.*;
import com.evolveum.midpoint.prism.schema.SchemaRegistry;
import com.evolveum.midpoint.util.*;
import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import javax.xml.namespace.QName;

import static com.evolveum.midpoint.prism.DeepCloneOperation.notUltraDeep;

/**
 * Direct implementation of {@link ComplexTypeDefinition}.
 *
 * @author Radovan Semancik
 */
public class ComplexTypeDefinitionImpl extends TypeDefinitionImpl implements MutableComplexTypeDefinition {

    private static final Trace LOGGER = TraceManager.getTrace(ComplexTypeDefinitionImpl.class);

    private static final long serialVersionUID = -9142629126376258513L;

    /**
     * Collection of constituents of this complex type.
     *
     * These are directly applicable when this CTD is instantiated into PrismContainer.
     *
     * TODO Define their meaning if this CTD is instantiated as PrismProperty.
     *
     * FIXME: This should be probably Map
     */
    @NotNull private final List<ItemDefinition<?>> itemDefinitions = new ArrayList<>();

    /** @see ComplexTypeDefinition#isReferenceMarker() */
    private boolean referenceMarker;

    /** @see ComplexTypeDefinition#isContainerMarker() */
    private boolean containerMarker;

    /** @see ComplexTypeDefinition#isObjectMarker() */
    private boolean objectMarker;

    /** @see ComplexTypeDefinition#isXsdAnyMarker() */
    private boolean xsdAnyMarker;

    /** @see ComplexTypeDefinition#isListMarker() */
    private boolean listMarker;

    /** @see ComplexTypeDefinition#getExtensionForType() */
    private QName extensionForType;

    /** @see ComplexTypeDefinition#getDefaultNamespace() */
    private String defaultNamespace;

    /** @see ComplexTypeDefinition#getIgnoredNamespaces() */
    @NotNull private List<String> ignoredNamespaces = new ArrayList<>();

    /**
     * Cache for {@link #findLocalItemDefinition(QName)} queries.
     *
     * BEWARE! Ugly hack, just to see the performance effect.
     *
     * TODO Consider whether we should keep this feature.
     */
    @NotNull private final TransientCache<QName, Object> cachedLocalDefinitionQueries = new TransientCache<>();

    /** Special value corresponding that no definition was found - used in {@link #cachedLocalDefinitionQueries}. */
    private static final Object NO_DEFINITION = new Object();

    /** TODO */
    private final @NotNull Map<QName, ItemDefinition<?>> substitutions = new HashMap<>();

    private transient List<PrismPropertyDefinition<?>> attributeDefinitions;

    private boolean strictAnyMarker;

    public ComplexTypeDefinitionImpl(@NotNull QName typeName) {
        super(typeName);
    }

    //region Trivia
    protected String getSchemaNamespace() {
        return getTypeName().getNamespaceURI();
    }

    /**
     * Returns set of item definitions.
     *
     * The set contains all item definitions of all types that were parsed.
     * Order of definitions is insignificant.
     *
     * @return set of definitions
     */
    @Override
    public @NotNull List<? extends ItemDefinition<?>> getDefinitions() {
        return Collections.unmodifiableList(itemDefinitions);
    }

    @Override
    public void add(ItemDefinition<?> definition) {
        checkMutable();
        itemDefinitions.add(definition);
        invalidateCaches();
    }

    private void invalidateCaches() {
        cachedLocalDefinitionQueries.invalidate();
    }

    @Override
    public QName getExtensionForType() {
        return extensionForType;
    }

    @Override
    public void setExtensionForType(QName extensionForType) {
        checkMutable();
        this.extensionForType = extensionForType;
    }

    @Override
    public boolean isReferenceMarker() {
        return referenceMarker;
    }

    @Override
    public void setReferenceMarker(boolean referenceMarker) {
        checkMutable();
        this.referenceMarker = referenceMarker;
    }

    @Override
    public boolean isContainerMarker() {
        return containerMarker;
    }

    @Override
    public void setContainerMarker(boolean containerMarker) {
        checkMutable();
        this.containerMarker = containerMarker;
    }

    @Override
    public boolean isObjectMarker() {
        return objectMarker;
    }

    @Override
    public boolean isXsdAnyMarker() {
        return xsdAnyMarker;
    }

    @Override
    public void setXsdAnyMarker(boolean xsdAnyMarker) {
        checkMutable();
        this.xsdAnyMarker = xsdAnyMarker;
    }

    @Override
    public boolean isListMarker() {
        return listMarker;
    }

    @Override
    public void setListMarker(boolean listMarker) {
        checkMutable();
        this.listMarker = listMarker;
    }

    @Override
    public String getDefaultNamespace() {
        return defaultNamespace;
    }

    @Override
    public void setDefaultNamespace(String defaultNamespace) {
        checkMutable();
        this.defaultNamespace = defaultNamespace;
    }

    @Override
    @NotNull
    public List<String> getIgnoredNamespaces() {
        return ignoredNamespaces;
    }

    @Override
    public void setIgnoredNamespaces(@NotNull List<String> ignoredNamespaces) {
        checkMutable();
        this.ignoredNamespaces = ignoredNamespaces;
    }

    @Override
    public void setObjectMarker(boolean objectMarker) {
        checkMutable();
        this.objectMarker = objectMarker;
    }

    //endregion

    //region Creating definitions
    @Override
    public PrismPropertyDefinitionImpl<?> createPropertyDefinition(QName name, QName typeName) {
        PrismPropertyDefinitionImpl<?> propDef = new PrismPropertyDefinitionImpl<>(name, typeName);
        add(propDef);
        return propDef;
    }

    // Creates reference to other schema
    // TODO: maybe check if the name is in different namespace
    // TODO: maybe create entirely new concept of property reference?
    public PrismPropertyDefinition<?> createPropertyDefinition(QName name) {
        PrismPropertyDefinition<?> propDef = new PrismPropertyDefinitionImpl<>(name, null);
        add(propDef);
        return propDef;
    }

    @Override
    public PrismPropertyDefinitionImpl<?> createPropertyDefinition(String localName, QName typeName) {
        return createPropertyDefinition(
                new QName(getSchemaNamespace(), localName),
                typeName);
    }

    public PrismPropertyDefinition<?> createPropertyDefinition(String localName, String localTypeName) {
        return createPropertyDefinition(
                new QName(getSchemaNamespace(), localName),
                new QName(getSchemaNamespace(), localTypeName));
    }
    //endregion

    //region Finding definitions

    @Override
    public <ID extends ItemDefinition<?>> ID findLocalItemDefinition(@NotNull QName name) {
        Object cached = cachedLocalDefinitionQueries.get(name);
        if (cached == NO_DEFINITION) {
            return null;
        } else if (cached != null) {
            //noinspection unchecked
            return (ID) cached;
        } else {
            //noinspection unchecked
            ID found = (ID) findLocalItemDefinition(name, ItemDefinition.class, false);
            cachedLocalDefinitionQueries.put(name, found != null ? found : NO_DEFINITION);
            return found;
        }
    }

    @Override
    public <ID extends ItemDefinition<?>> ID findItemDefinition(@NotNull ItemPath path, @NotNull Class<ID> clazz) {
        for (;;) {
            if (path.isEmpty()) {
                throw new IllegalArgumentException("Cannot resolve empty path on complex type definition "+this);
            }
            Object first = path.first();
            if (ItemPath.isName(first)) {
                QName firstName = ItemPath.toName(first);
                return findNamedItemDefinition(firstName, path.rest(), clazz);
            } else if (ItemPath.isId(first)) {
                path = path.rest();
            } else if (ItemPath.isParent(first)) {
                ItemPath rest = path.rest();
                ComplexTypeDefinition parent = getSchemaRegistry().determineParentDefinition(this, rest);
                if (rest.isEmpty()) {
                    // requires that the parent is defined as an item (container, object)
                    //noinspection unchecked
                    return (ID) getSchemaRegistry().findItemDefinitionByType(parent.getTypeName());
                } else {
                    return parent.findItemDefinition(rest, clazz);
                }
            } else if (ItemPath.isObjectReference(first)) {
                throw new IllegalStateException("Couldn't use '@' path segment in this context. CTD=" + getTypeName() + ", path=" + path);
            } else if (ItemPath.isIdentifier(first)) {
                if (!clazz.isAssignableFrom(PrismPropertyDefinition.class)) {
                    return null;
                }
                PrismPropertyDefinitionImpl<?> oidDefinition;
                // experimental
                if (objectMarker) {
                    oidDefinition = new PrismPropertyDefinitionImpl<>(PrismConstants.T_ID, DOMUtil.XSD_STRING);
                } else if (containerMarker) {
                    oidDefinition = new PrismPropertyDefinitionImpl<>(PrismConstants.T_ID, DOMUtil.XSD_INTEGER);
                } else {
                    throw new IllegalStateException("No identifier for complex type " + this);
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
            @NotNull QName firstName,
            @NotNull ItemPath rest,
            @NotNull Class<ID> clazz) {
        ID found = null;
        for (ItemDefinition<?> def : getDefinitions()) {

            if (QNameUtil.match(def.getItemName(), firstName, false)) {
                if (found != null) {
                    throw new IllegalStateException("More definitions found for " + firstName + "/" + rest + " in " + this);
                }
                found = def.findItemDefinition(rest, clazz);
                if (QNameUtil.hasNamespace(firstName)) {
                    break;            // if qualified then there's no risk of matching more entries
                }
            }
        }
        if (found != null) {
            if (clazz.isInstance(found)) {
                return found;
            } else {
                return null;
            }
        }
        if (isXsdAnyMarker()) {
            SchemaRegistry schemaRegistry = getSchemaRegistry();
            if (schemaRegistry != null) {
                ItemDefinition<?> def = schemaRegistry.findItemDefinitionByElementName(firstName);
                if (def != null) {
                    return def.findItemDefinition(rest, clazz);
                }
            }
        }
        return null;
    }
    //endregion

    /**
     * Merge provided definition into this definition.
     */
    @Override
    public void merge(ComplexTypeDefinition otherComplexTypeDef) {
        for (ItemDefinition<?> otherItemDef: otherComplexTypeDef.getDefinitions()) {
            ItemDefinition<?> existingItemDef = findItemDefinition(otherItemDef.getItemName());
            if (existingItemDef != null) {
                LOGGER.warn("Overwriting existing definition {} by {} (in {})", existingItemDef, otherItemDef, this);
                replaceDefinition(otherItemDef.getItemName(), otherItemDef.clone());
            } else {
                add(otherItemDef.clone());
            }
        }
    }

    @Override
    public void revive(PrismContext prismContext) {
        for (ItemDefinition<?> def: itemDefinitions) {
            def.revive(prismContext);
        }
    }

    @Override
    public boolean isEmpty() {
        return itemDefinitions.isEmpty();
    }

    @Override
    public boolean accept(Visitor<Definition> visitor, SmartVisitation<Definition> visitation) {
        if (super.accept(visitor, visitation)) {
            for (ItemDefinition<?> itemDefinition : itemDefinitions) {
                itemDefinition.accept(visitor, visitation);
            }
            return true;
        } else {
            return false;
        }
    }

    @NotNull
    @Override
    public ComplexTypeDefinitionImpl clone() {
        ComplexTypeDefinitionImpl clone = new ComplexTypeDefinitionImpl(this.typeName);
        clone.copyDefinitionDataFrom(this);
        return clone;
    }

    public ComplexTypeDefinition deepClone() {
        return deepClone(
                notUltraDeep());
    }

    @NotNull
    @Override
    public ComplexTypeDefinition deepClone(DeepCloneOperation operation) {
        return operation.execute(this,
                this::clone,
                clone -> {
                    ((ComplexTypeDefinitionImpl) clone).itemDefinitions.clear();
                    for (ItemDefinition<?> itemDef : itemDefinitions) {
                        ItemDefinition<?> itemClone = itemDef.deepClone(operation);
                        ((ComplexTypeDefinitionImpl) clone).itemDefinitions.add(itemClone);
                        operation.executePostCloneAction(itemClone);
            }
        });
    }

    protected void copyDefinitionDataFrom(ComplexTypeDefinition source) {
        super.copyDefinitionDataFrom(source);
        containerMarker = source.isContainerMarker();
        objectMarker = source.isObjectMarker();
        xsdAnyMarker = source.isXsdAnyMarker();
        extensionForType = source.getExtensionForType();
        defaultNamespace = source.getDefaultNamespace();
        ignoredNamespaces = new ArrayList<>(source.getIgnoredNamespaces());
        itemDefinitions.addAll(source.getDefinitions());
    }

    @Override
    public void replaceDefinition(@NotNull QName itemName, ItemDefinition<?> newDefinition) {
        checkMutable();
        invalidateCaches();
        for (int i=0; i<itemDefinitions.size(); i++) {
            ItemDefinition<?> itemDef = itemDefinitions.get(i);
            if (itemDef.getItemName().equals(itemName)) {
                if (!itemDef.getClass().isAssignableFrom(newDefinition.getClass())) {
                    throw new IllegalArgumentException("The provided definition of class "+newDefinition.getClass().getName()+" does not match existing definition of class "+itemDef.getClass().getName());
                }
                if (!itemDef.getItemName().equals(newDefinition.getItemName())) {
                    newDefinition = newDefinition.clone();
                    ((ItemDefinitionImpl<?>) newDefinition).setItemName(itemName);
                }
                // Make sure this is set, not add. set will keep correct ordering
                itemDefinitions.set(i, newDefinition);
                return;
            }
        }
        throw new IllegalArgumentException("The definition with name "+ itemName +" was not found in complex type "+getTypeName());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (containerMarker ? 1231 : 1237);
        result = prime * result + ((extensionForType == null) ? 0 : extensionForType.hashCode());
        //noinspection ConstantConditions [seems to be null during readObject i.e. while deserializing]
        result = prime * result + ((itemDefinitions == null) ? 0 : itemDefinitions.hashCode());
        result = prime * result + (objectMarker ? 1231 : 1237);
        result = prime * result + (xsdAnyMarker ? 1231 : 1237);
        return result;
    }

    @SuppressWarnings("RedundantIfStatement")
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ComplexTypeDefinitionImpl other = (ComplexTypeDefinitionImpl) obj;
        if (containerMarker != other.containerMarker) {
            return false;
        }
        if (extensionForType == null) {
            if (other.extensionForType != null) {
                return false;
            }
        } else if (!extensionForType.equals(other.extensionForType)) {
            return false;
        }
        // itemDefinitions may be null during Java object deserialization, hence "Objects.equals"
        if (!Objects.equals(itemDefinitions, other.itemDefinitions)) {
            return false;
        }
        if (objectMarker != other.objectMarker) {
            return false;
        }
        if (xsdAnyMarker != other.xsdAnyMarker) {
            return false;
        }
        // TODO ignored and default namespaces
        return true;
    }

    @Override
    public String debugDump(int indent) {
        return debugDump(indent, new IdentityHashMap<>());
    }

    @Override
    public String debugDump(int indent, IdentityHashMap<Definition, Object> seen) {
        StringBuilder sb = DebugUtil.createIndentedStringBuilder(indent);
        sb.append(this);
        if (extensionForType != null) {
            sb.append(",ext:");
            sb.append(PrettyPrinter.prettyPrint(extensionForType));
        }
        ItemProcessing processing = getProcessing();
        if (processing != null) {
            sb.append(",").append(processing);
        }
        if (containerMarker) {
            sb.append(",Mc");
        }
        if (objectMarker) {
            sb.append(",Mo");
        }
        if (xsdAnyMarker) {
            sb.append(",Ma");
        }
        if (instantiationOrder != null) {
            sb.append(",o:").append(instantiationOrder);
        }
        if (!staticSubTypes.isEmpty()) {
            sb.append(",st:").append(staticSubTypes.size());
        }
        extendDumpHeader(sb);
        if (seen.containsKey(this)) {
            sb.append(" (already shown)");
        } else {
            seen.put(this, null);
            for (ItemDefinition<?> def : getDefinitions()) {
                sb.append("\n");
                sb.append(def.debugDump(indent + 1));
                extendItemDumpDefinition(sb, def);
            }
        }
        return sb.toString();
    }

    protected void extendItemDumpDefinition(StringBuilder sb, ItemDefinition<?> def) {
        // Do nothing
    }

    /**
     * Return a human readable name of this class suitable for logs.
     */
    @Override
    protected String getDebugDumpClassName() {
        return "CTD";
    }

    @Override
    public String getDocClassName() {
        return "complex type";
    }

    @Override
    public void trimTo(@NotNull Collection<ItemPath> paths) {
        checkMutable();
        for (Iterator<ItemDefinition<?>> iterator = itemDefinitions.iterator(); iterator.hasNext(); ) {
            ItemDefinition<?> itemDef = iterator.next();
            ItemPath itemPath = itemDef.getItemName();
            if (!ItemPathCollectionsUtil.containsSuperpathOrEquivalent(paths, itemPath)) {
                iterator.remove();
            } else if (itemDef instanceof PrismContainerDefinition) {
                PrismContainerDefinition<?> itemPcd = (PrismContainerDefinition<?>) itemDef;
                if (itemPcd.getComplexTypeDefinition() != null) {
                    itemPcd.getComplexTypeDefinition().trimTo(ItemPathCollectionsUtil.remainder(paths, itemPath, false));
                }
            }
        }
    }

    @Override
    public void delete(QName itemName) {
        checkMutable();
        itemDefinitions.removeIf(def -> def.getItemName().equals(itemName));
        cachedLocalDefinitionQueries.remove(itemName);
    }

    @Override
    public MutableComplexTypeDefinition toMutable() {
        checkMutableOnExposing();
        return this;
    }

    @Override
    public void performFreeze() {
        itemDefinitions.forEach(Freezable::freeze);
        super.performFreeze();
    }

    @Override
    public void addSubstitution(ItemDefinition<?> itemDef, ItemDefinition<?> maybeSubst) {
        substitutions.put(maybeSubst.getItemName(),maybeSubst);
        // Also with default namespace to allow search in local
        substitutions.put(new QName(maybeSubst.getItemName().getLocalPart()), maybeSubst);

    }

    @Override
    public Optional<ItemDefinition<?>> substitution(QName name) {
        return Optional.ofNullable(substitutions.get(name));
    }

    @Override
    public boolean hasSubstitutions() {
        return !substitutions.isEmpty();
    }

    @Override
    public boolean hasSubstitutions(QName itemName) {
        for(ItemDefinition<?> substition : substitutions.values()) {
            if (itemName.equals(substition.getSubstitutionHead())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void setAttributeDefinitions(List<PrismPropertyDefinition<?>> definitions) {
        this.attributeDefinitions = definitions;
    }

    @Override
    public List<PrismPropertyDefinition<?>> getXmlAttributeDefinitions() {
        return attributeDefinitions != null ? attributeDefinitions : Collections.emptyList();
    }

    @Override
    public boolean isStrictAnyMarker() {
        return strictAnyMarker;
    }

    @Override
    public void setStrictAnyMarker(boolean marker) {
        strictAnyMarker = marker;
    }
}
