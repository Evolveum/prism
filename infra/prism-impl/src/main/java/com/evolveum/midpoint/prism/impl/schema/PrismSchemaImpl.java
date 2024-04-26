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
import java.util.concurrent.ConcurrentHashMap;
import javax.xml.namespace.QName;

import com.evolveum.midpoint.prism.ComplexTypeDefinition.ComplexTypeDefinitionLikeBuilder;
import com.evolveum.midpoint.prism.Definition.DefinitionBuilder;
import com.evolveum.midpoint.prism.schema.*;
import com.evolveum.midpoint.prism.schema.PrismSchema.PrismSchemaMutator;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Document;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.impl.*;
import com.evolveum.midpoint.util.QNameUtil;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;

/**
 * @author Radovan Semancik
 */
public class PrismSchemaImpl
        extends SchemaRegistryStateAware
        implements PrismSchema, PrismSchemaMutator, SchemaBuilder, SerializableSchema {

    private static final Trace LOGGER = TraceManager.getTrace(PrismSchema.class);

    // TEMPORARY
    boolean isRuntime;

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
     * Type definitions contained in this schema, stored in a map for faster access.
     *
     * The key is the compile-time class.
     */
    @NotNull private final Map<Class<?>, TypeDefinition> typeDefinitionByCompileTimeClassMap = new HashMap<>();

    /**
     * Namespace for items defined in this schema.
     */
    @NotNull protected final String namespace;

    @NotNull protected final PrismContextImpl prismContext = (PrismContextImpl) PrismContext.get();

    /**
     * Item definitions that couldn't be created when parsing the schema because of unresolvable CTD.
     * (Caused by the fact that the type resides in another schema.)
     * These definitions are to be resolved after parsing the set of schemas.
     */
    @NotNull private final List<ItemDefinitionSupplier> delayedItemDefinitions = new ArrayList<>();

    /**
     * TODO
     */
    private final Multimap<QName, ItemDefinition<?>> substitutions = HashMultimap.create();

    /**
     * Caches the result of {@link #findItemDefinitionsByCompileTimeClass(Class, Class)} queries.
     *
     * Just to be sure, this map is created as thread-safe. Normally, all additions to a prism schema occur before the
     * regular operation is started. But we cannot assume this will be always the case.
     *
     * Temporary. The method is not quite sound anyway.
     */
    private final Map<Class<?>, List<ItemDefinition<?>>> itemDefinitionsByCompileTimeClassMap = new ConcurrentHashMap<>();

    //TODO PoC
    private String sourceDescription;
    private Package compileTimePackage;

    public PrismSchemaImpl(@NotNull String namespace) {
        argCheck(StringUtils.isNotEmpty(namespace), "Namespace can't be null or empty.");
        this.namespace = namespace;
    }

    public PrismSchemaImpl(@NotNull String namespace, @Nullable Package compileTimePackage) {
        argCheck(StringUtils.isNotEmpty(namespace), "Namespace can't be null or empty.");
        this.namespace = namespace;
        this.compileTimePackage = compileTimePackage;
    }

    private void invalidateCaches() {
        itemDefinitionsByCompileTimeClassMap.clear();
    }

    //region Trivia
    @Override
    public @NotNull String getNamespace() {
        return namespace;
    }

    @NotNull
    @Override
    public Collection<Definition> getDefinitions() {
        return Collections.unmodifiableCollection(definitions);
    }

    @Override
    public @NotNull Collection<? extends SerializableDefinition> getDefinitionsToSerialize() {
        //noinspection unchecked,rawtypes
        return (Collection) getDefinitions(); // TODO resolve the casting
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NotNull <T extends Definition> List<T> getDefinitions(@NotNull Class<T> type) {
        List<T> list = new ArrayList<>();
        for (Definition def : definitions) {
            if (type.isAssignableFrom(def.getClass())) {
                list.add((T) def);
            }
        }
        return List.copyOf(list);
    }

    @Override
    public void addDelayedItemDefinition(ItemDefinitionSupplier supplier) {
        checkMutable();
        delayedItemDefinitions.add(supplier);
        invalidateCaches();
    }

    @NotNull List<ItemDefinitionSupplier> getDelayedItemDefinitions() {
        return delayedItemDefinitions;
    }

    @Override
    public void add(@NotNull Definition def) {
        checkMutable();

        definitions.add(def);

        if (def instanceof ItemDefinition<?> itemDef) {
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

            QName substitutionHead = itemDef.getSubstitutionHead();
            if (substitutionHead != null) {
                addSubstitution(substitutionHead, itemDef);
            }

        } else if (def instanceof TypeDefinition typeDef) { // We hope all types will be of this
            QName typeName = def.getTypeName();
            if (QNameUtil.isUnqualified(typeName)) {
                throw new IllegalArgumentException("Unqualified definition of type " + typeName + " cannot be added to " + this);
            }
            typeDefinitionMap.put(typeName, typeDef);
            if (def instanceof TypeDefinitionImpl typeImplDef && !def.isImmutable()) {
                typeImplDef.setSchemaRegistryState(getSchemaRegistryState());
            }
        } else {
            throw new IllegalArgumentException("Unsupported type to be added to this schema: " + def);
        }

        invalidateCaches();
    }

    void setupCompileTimeClass(@NotNull TypeDefinition typeDef) {
        // Not caching the negative result, as this is called during schema parsing.
        Class<Object> compileTimeClass = getSchemaLookup().determineCompileTimeClassInternal(typeDef.getTypeName(), false);
        if (typeDef instanceof TypeDefinitionImpl typeDefImpl) {
            typeDefImpl.setCompileTimeClass(compileTimeClass); // FIXME do better!
        }
        registerCompileTimeClass(compileTimeClass, typeDef);
    }

    @Override
    public @NotNull ComplexTypeDefinitionLikeBuilder newComplexTypeDefinitionLikeBuilder(String localTypeName) {
        return prismContext.definitionFactory()
                .newComplexTypeDefinition(qualify(localTypeName));
    }

    @Override
    public void add(@NotNull DefinitionBuilder builder) {
        var definition = builder.getObjectBuilt();
        if (definition instanceof Definition typeDef) {
            add(typeDef);
        } else {
            // Only the subclasses can deal with non-prism definitions. This is why the builder was created in the first place.
            throw new IllegalArgumentException("The builder does not produce a prism definition: " + definition);
        }
    }

    @Override
    public SchemaBuilder builder() {
        return this;
    }

    private void registerCompileTimeClass(@Nullable Class<?> compileTimeClass, @NotNull TypeDefinition typeDefinition) {
        // FIXME use more relevant criteria to decide whether to put a definition into this map. Currently we skip
        //  extension types because multiple unrelated types correspond to ExtensionType.class and that causes the insertion
        //  into the map fail (as it takes care to avoid overwriting the entries, for obvious reasons)
        if (compileTimeClass != null && !isExtensionType(typeDefinition)) {
            var previous = typeDefinitionByCompileTimeClassMap.put(compileTimeClass, typeDefinition);
            stateCheck(previous == null,
                    "Multiple type definitions for %s: %s and %s",
                    compileTimeClass, previous, typeDefinition);
        }
    }

    // FIXME put into correct place
    private boolean isExtensionType(TypeDefinition typeDefinition) {
        return typeDefinition instanceof ComplexTypeDefinition ctd
                && ctd.getExtensionForType() != null;
    }

    private void addSubstitution(QName substitutionHead, ItemDefinition<?> definition) {
        this.substitutions.put(substitutionHead, definition);
    }

    @Override
    public Multimap<QName, ItemDefinition<?>> getSubstitutions() {
        return substitutions;
    }

    //endregion

    //region XSD parsing and serialization
    @Override
    public @NotNull Document serializeToXsd() throws SchemaException {
        return new SchemaDomSerializer(this).serializeSchema();
    }
    //endregion

    //region Creating definitions

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
        extendDebugDump(sb, indent);
        return sb.toString();
    }

    protected void extendDebugDump(StringBuilder sb, int indent) {
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
        var cached = itemDefinitionsByCompileTimeClassMap.get(compileTimeClass);
        if (cached != null) {
            return narrowIfNeeded(cached, definitionClass);
        }
        var fresh = findItemDefinitionsByCompileTimeClassInternal(compileTimeClass);
        itemDefinitionsByCompileTimeClassMap.put(compileTimeClass, fresh);
        return narrowIfNeeded(fresh, definitionClass);
    }

    /**
     * Narrows the list of definitions (expected to be short) by given class. We expect that for the most of the time,
     * all members would be matching. Hence, we first check, and only in the case of mismatch we create a narrowed list.
     *
     * TODO get rid of list management altogether
     */
    private <ID extends ItemDefinition<?>> List<ID> narrowIfNeeded(
            @NotNull List<ItemDefinition<?>> definitions, @NotNull Class<ID> definitionClass) {
        for (ItemDefinition<?> definition : definitions) {
            if (!definitionClass.isAssignableFrom(definition.getClass())) {
                return narrow(definitions, definitionClass);
            }
        }
        //noinspection unchecked
        return (List<ID>) definitions;
    }

    private <ID extends ItemDefinition<?>> List<ID> narrow(List<ItemDefinition<?>> definitions, Class<ID> definitionClass) {
        List<ID> narrowed = new ArrayList<>();
        for (ItemDefinition<?> definition : definitions) {
            if (definitionClass.isAssignableFrom(definition.getClass())) {
                //noinspection unchecked
                narrowed.add((ID) definition);
            }
        }
        return narrowed;
    }

    private @NotNull List<ItemDefinition<?>> findItemDefinitionsByCompileTimeClassInternal(@NotNull Class<?> compileTimeClass) {
        List<ItemDefinition<?>> found = new ArrayList<>();
        for (Definition def : definitions) {
            if (def instanceof PrismContainerDefinition) {
                if (compileTimeClass.equals(((PrismContainerDefinition<?>) def).getCompileTimeClass())) {
                    found.add((ItemDefinition<?>) def);
                }
            } else if (def instanceof PrismPropertyDefinition) {
                Class<?> fondClass = getSchemaLookup().determineClassForType(def.getTypeName());
                if (compileTimeClass.equals(fondClass)) {
                    found.add((ItemDefinition<?>) def);
                }
            } else {
                // Skipping the definition, PRD is not supported here.
                // Currently, this does not work sensibly for midPoint because it has multiple top-level
                // elements for ObjectReferenceType (not to mention it's common-3, not Prism ORT).
                // Instead, use findItemDefinitionsByElementName(...) to find the reference definition
                // by well-known top-level element name .
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
            if (def instanceof ComplexTypeDefinition ctd) {
                if (compileTimeClass.equals(ctd.getCompileTimeClass())) {
                    return ctd;
                }
            }
        }
        return null;
    }

    public TypeDefinition findTypeDefinitionByType(@NotNull QName typeName) {
        return findTypeDefinitionByType(typeName, TypeDefinition.class);
    }

    @Nullable
    @Override
    public <TD extends TypeDefinition> TD findTypeDefinitionByType(@NotNull QName typeName, @NotNull Class<TD> definitionClass) {
        Collection<TD> definitions = findTypeDefinitionsByType(typeName, definitionClass);
        return !definitions.isEmpty() ?
                definitions.iterator().next() : null; // TODO treat multiple results somehow
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
        TypeDefinition typeDefinition = typeDefinitionByCompileTimeClassMap.get(compileTimeClass);
        if (typeDefinition != null
                && definitionClass.isAssignableFrom(typeDefinition.getClass())) {
            return (TD) typeDefinition;
        } else {
            return null;
        }
    }
    //endregion

    @Override
    public void performFreeze() {
        super.performFreeze();
        definitions.forEach(Freezable::freeze);
    }

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    public PrismSchemaImpl clone() {
        PrismSchemaImpl clone = new PrismSchemaImpl(namespace, compileTimePackage);
        copyContent(clone);
        return clone;
    }

    @Override
    public PrismSchemaMutator mutator() {
        return this;
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

    protected @NotNull QName qualify(@NotNull String localTypeName) {
        return new QName(namespace, localTypeName);
    }

    @Override
    public boolean isRuntime() {
        return isRuntime;
    }

    public void setRuntime(boolean runtime) {
        checkMutable();
        isRuntime = runtime;
    }

    @Override
    public String getSourceDescription() {
        return sourceDescription;
    }

    public void setSourceDescription(String sourceDescription) {
        this.sourceDescription = sourceDescription;
    }

    public Package getCompileTimePackage() {
        return compileTimePackage;
    }
}
