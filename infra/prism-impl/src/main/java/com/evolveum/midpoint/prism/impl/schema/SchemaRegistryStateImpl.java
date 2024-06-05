/*
 * Copyright (C) 2010-2024 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.prism.impl.schema;

import static java.util.Collections.emptyList;

import java.io.Serializable;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import com.evolveum.axiom.concepts.CheckedFunction;
import com.evolveum.midpoint.prism.path.ItemName;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.prism.schema.SchemaRegistryState;

import com.evolveum.midpoint.prism.schemaContext.SchemaContextDefinition;
import com.evolveum.midpoint.prism.xml.DynamicNamespacePrefixMapper;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.impl.*;
import com.evolveum.midpoint.prism.schema.DefinitionStoreUtils;
import com.evolveum.midpoint.prism.schema.PrismSchema;
import com.evolveum.midpoint.prism.schema.SchemaDescription;
import com.evolveum.midpoint.prism.xml.XmlTypeConverter;
import com.evolveum.midpoint.prism.xml.XsdTypeMapper;
import com.evolveum.midpoint.util.*;
import com.evolveum.midpoint.util.annotation.Experimental;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;

import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * Contains caches and provides definitions.
 *
 */
public class SchemaRegistryStateImpl extends AbstractFreezable implements DebugDumpable, SchemaRegistryState {

    private static final Trace LOGGER = TraceManager.getTrace(SchemaRegistryStateImpl.class);

    /**
     * Schema that is used for "native" validation of XML files.
     * It is set up during initialization. So any schemas added after that will not be reflected here.
     */
    private javax.xml.validation.Schema javaxSchema;

    /**
     * "Registry" for namespace prefixes. It is used when serializing data as well as schemas.
     * For historical reasons it is kept here -- along with the schemas.
     */
    private final DynamicNamespacePrefixMapper namespacePrefixMapper;

    /**
     * Registered schema descriptions.
     * When the registry is in initialized state, all schema descriptions in this list are frozen.
     */
    private final List<SchemaDescriptionImpl> schemaDescriptions;

    /**
     * Schema descriptions for a given namespace.
     * In case of extension schemas there can be more schema descriptions with the same namespace!
     */
    private final MultiValuedMap<String, SchemaDescription> parsedSchemas;

    /**
     * Parsed prism schemas.
     */
    private Collection<PrismSchemaImpl> parsedPrismSchemas;

    /** Map for fast lookup of prism schema by class or package. */
    private final Map<Package, PrismSchema> prismSchemasMap = new HashMap<>();

    /**
     * Cached value of "isList" for given type.
     */
    private final ConcurrentHashMap<QName, IsList> isListByXsiType = new ConcurrentHashMap<>();

    /**
     * Cached value of "isList" for given item.
     */
    private final ConcurrentHashMap<QName, IsList> isListByElementName = new ConcurrentHashMap<>();

    /**
     * Cached class for given type for {@link #determineClassForType(QName)} method.
     * TODO better name, probably unify with the latter
     */
    @Experimental
    private final ConcurrentHashMap<QName, Class<?>> classForTypeIncludingXsd = new ConcurrentHashMap<>();

    /**
     * Cached class for given type for {@link #determineCompileTimeClass(QName)} method.
     * TODO better name, probably unify with the former
     */
    @Experimental
    private final ConcurrentHashMap<QName, Class<?>> classForTypeExcludingXsd = new ConcurrentHashMap<>();

    /**
     * Cached object definition for given compile-time class.
     */
    @Experimental
    private final ConcurrentHashMap<Class<?>, PrismObjectDefinition<?>> objectDefinitionForClass = new ConcurrentHashMap<>();

    /**
     * Cached object definition for given type name.
     */
    @Experimental
    private final ConcurrentHashMap<QName, PrismObjectDefinition<?>> objectDefinitionForType = new ConcurrentHashMap<>();


    private final ConcurrentHashMap<DerivationKey, Object> derivedObjects = new ConcurrentHashMap<>();

    /**
     * Marker value for "no such class": cached value that indicates that we have executed the search but found
     * no matching class.
     */
    private static final Class<?> NO_CLASS = Void.class;

    /**
     * Marker value for "no object definition": cached value that indicates that we have executed the search
     * but found no matching definition.
     */
    private static final PrismObjectDefinition<?> NO_OBJECT_DEFINITION = new DummyPrismObjectDefinition();

    /**
     * A prism context this schema registry is part of.
     * Should be non-null.
     */
    protected final PrismContext prismContext;

    /**
     * Definition of the value metadata container.
     * It is lazily evaluated, because the schema registry has to be initialized to resolve type name to definition.
     */
    private PrismContainerDefinition<?> valueMetadataDefinition;

    private SchemaRegistryStateImpl(
            DynamicNamespacePrefixMapper namespacePrefixMapper, List<SchemaDescriptionImpl> schemaDescriptions,
            MultiValuedMap<String, SchemaDescription> parsedSchemas, PrismContext prismContext) {
        this.namespacePrefixMapper = namespacePrefixMapper;
        this.schemaDescriptions = schemaDescriptions;
        this.parsedSchemas = parsedSchemas;
        this.prismContext = prismContext;
    }

    void invalidateCaches() {
        isListByXsiType.clear();
        isListByElementName.clear();
        classForTypeIncludingXsd.clear();
        classForTypeExcludingXsd.clear();
        objectDefinitionForClass.clear();
        objectDefinitionForType.clear();
        derivedObjects.clear();
    }

    //region Schemas and type maps (TODO)
    private void putPrismSchemasMap(Map<Package, PrismSchema> mapper) {
        prismSchemasMap.putAll(mapper);
    }

    private void setJavaxSchema(Schema javaxSchema) {
        this.javaxSchema = javaxSchema;
    }

    private void setParsedPrismSchemas(Collection<PrismSchemaImpl> parsedPrismSchemas) {
        this.parsedPrismSchemas = parsedPrismSchemas;
    }

    public MultiValuedMap<String, SchemaDescription> getParsedSchemas() {
        return parsedSchemas;
    }

    public PrismContainerDefinition<?> getValueMetadataDefinition() {
        return valueMetadataDefinition;
    }

    private void setValueMetadataDefinition(PrismContainerDefinition<?> valueMetadataDefinition) {
        this.valueMetadataDefinition = valueMetadataDefinition;
    }

    @Override
    public javax.xml.validation.Schema getJavaxSchema() {
        return javaxSchema;
    }

    @Override
    public DynamicNamespacePrefixMapper getNamespacePrefixMapper() {
        return namespacePrefixMapper;
    }
    //endregion

    //region Finding items (standard cases - core methods)
    @NotNull
    @Override
    public <ID extends ItemDefinition> List<ID> findItemDefinitionsByCompileTimeClass(
            @NotNull Class<?> compileTimeClass, @NotNull Class<ID> definitionClass) {
        PrismSchema schema = findSchemaByCompileTimeClass(compileTimeClass);
        if (schema == null) {
            return emptyList();
        }
        return schema.findItemDefinitionsByCompileTimeClass(compileTimeClass, definitionClass);
    }

    @Nullable
    @Override
    public <ID extends ItemDefinition> ID findItemDefinitionByType(@NotNull QName typeName, @NotNull Class<ID> definitionClass) {
        if (QNameUtil.noNamespace(typeName)) {
            TypeDefinition td = resolveGlobalTypeDefinitionWithoutNamespace(typeName.getLocalPart(), TypeDefinition.class);
            if (td == null) {
                return null;
            }
            typeName = td.getTypeName();
        }
        PrismSchema schema = findSchemaByNamespace(typeName.getNamespaceURI());
        if (schema == null) {
            return null;
        }
        return schema.findItemDefinitionByType(typeName, definitionClass);
    }

    @NotNull
    @Override
    public <ID extends ItemDefinition> List<ID> findItemDefinitionsByElementName(@NotNull QName elementName, @NotNull Class<ID> definitionClass) {
        if (QNameUtil.noNamespace(elementName)) {
            return resolveGlobalItemDefinitionsWithoutNamespace(elementName.getLocalPart(), definitionClass);
        } else {
            PrismSchema schema = findSchemaByNamespace(elementName.getNamespaceURI());
            if (schema == null) {
                return new ArrayList<>();
            }
            return schema.findItemDefinitionsByElementName(elementName, definitionClass);
        }
    }

    @Nullable
    @Override
    public <TD extends TypeDefinition> TD findTypeDefinitionByCompileTimeClass(@NotNull Class<?> compileTimeClass, @NotNull Class<TD> definitionClass) {
        PrismSchema schema = findSchemaByCompileTimeClass(compileTimeClass);
        if (schema == null) {
            return null;
        }
        return schema.findTypeDefinitionByCompileTimeClass(compileTimeClass, definitionClass);
    }

    @Nullable
    @Override
    public <TD extends TypeDefinition> TD findTypeDefinitionByType(@NotNull QName typeName, @NotNull Class<TD> definitionClass) {
        if (QNameUtil.noNamespace(typeName)) {
            return resolveGlobalTypeDefinitionWithoutNamespace(typeName.getLocalPart(), definitionClass);
        }
        PrismSchema schema = findSchemaByNamespace(typeName.getNamespaceURI());
        if (schema == null) {
            return null;
        }
        return schema.findTypeDefinitionByType(typeName, definitionClass);
    }

    @NotNull
    @Override
    public <TD extends TypeDefinition> Collection<? extends TD> findTypeDefinitionsByType(@NotNull QName typeName,
            @NotNull Class<TD> definitionClass) {
        if (QNameUtil.noNamespace(typeName)) {
            return resolveGlobalTypeDefinitionsWithoutNamespace(typeName.getLocalPart(), definitionClass);
        }
        PrismSchema schema = findSchemaByNamespace(typeName.getNamespaceURI());
        if (schema == null) {
            return emptyList();
        }
        return schema.findTypeDefinitionsByType(typeName, definitionClass);
    }
    //endregion

    //region Finding items - cached (frequent cases) - EXPERIMENTAL
    // FIXME: Rework cache function to getOrCreate
    @Experimental
    @Override
    public <O extends Objectable> PrismObjectDefinition<O> findObjectDefinitionByCompileTimeClass(
            @NotNull Class<O> compileTimeClass) {
        PrismObjectDefinition<?> cached = objectDefinitionForClass.get(compileTimeClass);
        if (cached == NO_OBJECT_DEFINITION) {
            return null;
        } else if (cached != null) {
            //noinspection unchecked
            return (PrismObjectDefinition<O>) cached;
        } else {
            //noinspection unchecked
            PrismObjectDefinition<O> found = findItemDefinitionByCompileTimeClass(compileTimeClass, PrismObjectDefinition.class);
            objectDefinitionForClass.put(compileTimeClass, found != null ? found : NO_OBJECT_DEFINITION);
            return found;
        }
    }

    @Override
    public <O extends Objectable> PrismObjectDefinition<O> findObjectDefinitionByType(@NotNull QName typeName) {
        PrismObjectDefinition<?> cached = objectDefinitionForType.get(typeName);
        if (cached == NO_OBJECT_DEFINITION) {
            return null;
        } else if (cached != null) {
            //noinspection unchecked
            return (PrismObjectDefinition<O>) cached;
        } else {
            //noinspection unchecked
            PrismObjectDefinition<O> found = findItemDefinitionByType(typeName, PrismObjectDefinition.class);
            objectDefinitionForType.put(typeName, found != null ? found : NO_OBJECT_DEFINITION);
            return found;
        }
    }
    //endregion

    //region Finding items (nonstandard cases)
    @Override
    public SchemaDescription findSchemaDescriptionByNamespace(String namespaceURI) {
        for (SchemaDescription desc : schemaDescriptions) {
            if (namespaceURI.equals(desc.getNamespace())) {
                return desc;
            }
        }
        return null;
    }

    @Override
    public SchemaDescription findSchemaDescriptionByPrefix(String prefix) {
        for (SchemaDescription desc : schemaDescriptions) {
            if (prefix.equals(desc.getUsualPrefix())) {
                return desc;
            }
        }
        return null;
    }
    //endregion

    //region Derived objects

    @Override
    public <R, E extends Exception> R getDerivedObject(DerivationKey<R> derivationKey, CheckedFunction<SchemaRegistryState, R, E> mapping) throws E {
        var maybe = derivedObjects.get(derivationKey);
        if (maybe != null) {
            return (R) maybe;
        }
        var computed = mapping.apply(this);
        if (computed == null) {
            throw new IllegalArgumentException("Mapping returned null.");
        }
        derivedObjects.putIfAbsent(derivationKey, computed);
        return computed;
    }

    //endregion

    //region Unqualified names resolution
    // TODO fix this temporary and inefficient implementation
    @Override
    public QName resolveUnqualifiedTypeName(QName type) throws SchemaException {
        QName typeFound = null;
        for (PrismSchemaImpl desc : parsedPrismSchemas) {
            QName typeInSchema = new QName(desc.getNamespace(), type.getLocalPart());
            if (desc.findComplexTypeDefinitionByType(typeInSchema) != null) {
                if (typeFound != null) {
                    throw new SchemaException("Ambiguous type name: " + type);
                } else {
                    typeFound = typeInSchema;
                }
            }
        }
        if (typeFound == null) {
            throw new SchemaException("Unknown type: " + type);
        } else {
            return typeFound;
        }
    }

    // current implementation tries to find all references to the child CTD and select those that are able to resolve path of 'rest'
    // fails on ambiguity
    // it's a bit fragile, as adding new references to child CTD in future may break existing code
    @Override
    public ComplexTypeDefinition determineParentDefinition(@NotNull ComplexTypeDefinition child, @NotNull ItemPath rest) {
        Map<ComplexTypeDefinition, ItemDefinition<?>> found = new HashMap<>();
        for (PrismSchema schema : getSchemas()) {
            if (schema == null) {
                continue;
            }
            for (ComplexTypeDefinition ctd : schema.getComplexTypeDefinitions()) {
                for (ItemDefinition<?> item : ctd.getDefinitions()) {
                    if (!(item instanceof PrismContainerDefinition)) {
                        continue;
                    }
                    PrismContainerDefinition<?> itemPcd = (PrismContainerDefinition<?>) item;
                    if (itemPcd.getComplexTypeDefinition() == null) {
                        continue;
                    }
                    if (child.getTypeName().equals(itemPcd.getComplexTypeDefinition().getTypeName())) {
                        if (!rest.isEmpty() && ctd.findItemDefinition(rest) == null) {
                            continue;
                        }
                        found.put(ctd, itemPcd);
                    }
                }
            }
        }
        if (found.isEmpty()) {
            throw new IllegalStateException("Couldn't find definition for parent for " + child.getTypeName() + ", path=" + rest);
        } else if (found.size() > 1) {
            Map<ComplexTypeDefinition, ItemDefinition> notInherited = found.entrySet().stream()
                    .filter(e -> !e.getValue().isInherited())
                    .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
            if (notInherited.size() > 1) {
                throw new IllegalStateException(
                        "Couldn't find parent definition for " + child.getTypeName() + ": More than one candidate found: "
                                + notInherited);
            } else if (notInherited.isEmpty()) {
                throw new IllegalStateException(
                        "Couldn't find parent definition for " + child.getTypeName() + ": More than one candidate found - and all are inherited: "
                                + found);
            } else {
                return notInherited.keySet().iterator().next();
            }
        } else {
            return found.keySet().iterator().next();
        }
    }

    <TD extends TypeDefinition> TD resolveGlobalTypeDefinitionWithoutNamespace(String typeLocalName, Class<TD> definitionClass) {
        TD found = null;
        for (PrismSchemaImpl schema : parsedPrismSchemas) {
            TD def = schema.findTypeDefinitionByType(new QName(schema.getNamespace(), typeLocalName), definitionClass);
            if (def != null) {
                if (found != null) {
                    throw new IllegalArgumentException("Multiple possible resolutions for unqualified type name " + typeLocalName + " (e.g. in " +
                            def.getTypeName() + " and " + found.getTypeName());
                }
                found = def;
            }
        }
        return found;
    }

    @NotNull
    <TD extends TypeDefinition> Collection<TD> resolveGlobalTypeDefinitionsWithoutNamespace(String typeLocalName, Class<TD> definitionClass) {
        List<TD> rv = new ArrayList<>();
        for (PrismSchemaImpl schema : parsedPrismSchemas) {
            rv.addAll(schema.findTypeDefinitionsByType(new QName(schema.getNamespace(), typeLocalName), definitionClass));
        }
        return rv;
    }

    <ID extends ItemDefinition> List<ID> resolveGlobalItemDefinitionsWithoutNamespace(String localPart, Class<ID> definitionClass) {
        return resolveGlobalItemDefinitionsWithoutNamespace(localPart, definitionClass, null);
    }

    <ID extends ItemDefinition> ID resolveGlobalItemDefinitionWithoutNamespace(
            String localPart, Class<ID> definitionClass, boolean exceptionIfAmbiguous, @Nullable List<String> ignoredNamespaces) {
        return DefinitionStoreUtils.getOne(
                resolveGlobalItemDefinitionsWithoutNamespace(localPart, definitionClass, ignoredNamespaces),
                exceptionIfAmbiguous,
                "Multiple possible resolutions for unqualified element name '" + localPart + "'");
    }

    @NotNull
    private <ID extends ItemDefinition> List<ID> resolveGlobalItemDefinitionsWithoutNamespace(String localPart, Class<ID> definitionClass, @Nullable List<String> ignoredNamespaces) {
        List<ID> found = new ArrayList<>();
        for (PrismSchemaImpl schema : parsedPrismSchemas) {
            if (namespaceMatches(schema.getNamespace(), ignoredNamespaces)) {
                continue;
            }
            ItemDefinition def = schema.findItemDefinitionByElementName(new QName(localPart), definitionClass);
            if (def != null) {
                //noinspection unchecked
                found.add((ID) def);
            }
        }
        return found;
    }

    private boolean namespaceMatches(String namespace, @Nullable List<String> ignoredNamespaces) {
        if (ignoredNamespaces == null) {
            return false;
        }
        for (String ignored : ignoredNamespaces) {
            if (namespace.startsWith(ignored)) {
                return true;
            }
        }
        return false;
    }
    //endregion

    //region Finding schemas
    @Override
    public PrismSchema getPrismSchema(String namespace) {
        List<PrismSchema> schemas = parsedPrismSchemas.stream()
                .filter(s -> namespace.equals(s.getNamespace()))
                .collect(Collectors.toList());
        if (schemas.size() > 1) {
            throw new IllegalStateException("More than one prism schema for namespace " + namespace);
        } else if (schemas.size() == 1) {
            return schemas.get(0);
        } else {
            return null;
        }
    }

    @Override
    public Collection<PrismSchema> getSchemas() {
        return (Collection) parsedPrismSchemas; //TODO
    }

    @Override
    public PrismSchema findSchemaByCompileTimeClass(@NotNull Class<?> compileTimeClass) {
        Package compileTimePackage = compileTimeClass.getPackage();
        if (compileTimePackage == null) {
            return null;
        }

        return prismSchemasMap.get(compileTimePackage);
    }

    @Override
    public PrismSchema findSchemaByNamespace(String namespaceURI) {
        return parsedPrismSchemas.stream()
                .filter(schema -> namespaceURI.equals(schema.getNamespace()))
                .findFirst().orElse(null);
    }
    //endregion

    //region Misc
    @NotNull
    @Override
    public IsList isList(@Nullable QName xsiType, @NotNull QName elementName) {
        if (xsiType != null) {
            return isListByType(xsiType);
        } else {
            return isListByElementName(elementName);
        }
    }

    @NotNull
    private IsList isListByElementName(@NotNull QName elementName) {
        IsList cached = isListByElementName.get(elementName);
        if (cached != null) {
            return cached;
        } else {
            IsList computed = determineIsListFromElementName(elementName);
            isListByElementName.put(elementName, computed);
            return computed;
        }
    }

    @NotNull
    private IsList determineIsListFromElementName(@NotNull QName elementName) {
        Collection<? extends ComplexTypeDefinition> definitions =
                findTypeDefinitionsByElementName(elementName, ComplexTypeDefinition.class);
        // TODO - or allMatch here? - allMatch would mean that if there's an extension (or resource item) with a name
        // of e.g. formItems, pipeline, sequence, ... - it would not be recognizable as list=true anymore. That's why
        // we will use anyMatch here.
        if (definitions.stream().anyMatch(ComplexTypeDefinition::isListMarker)) {
            // we are very hopefully OK -- so let's continue
            return IsList.MAYBE;
        } else {
            return IsList.NO;
        }
    }

    @NotNull
    private IsList isListByType(@NotNull QName xsiType) {
        IsList cached = isListByXsiType.get(xsiType);
        if (cached != null) {
            return cached;
        } else {
            IsList computed = determineIsListFromType(xsiType);
            isListByXsiType.put(xsiType, computed);
            return computed;
        }
    }

    @NotNull
    private IsList determineIsListFromType(@NotNull QName xsiType) {
        Collection<? extends ComplexTypeDefinition> definitions = findTypeDefinitionsByType(xsiType, ComplexTypeDefinition.class);
        if (definitions.isEmpty()) {
            return IsList.NO;    // to be safe (we support this heuristic only for known types)
        }
        if (QNameUtil.hasNamespace(xsiType)) {
            assert definitions.size() <= 1;
            return definitions.iterator().next().isListMarker() ? IsList.YES : IsList.NO;
        } else {
            if (definitions.stream().allMatch(ComplexTypeDefinition::isListMarker)) {
                // great -- we are very probably OK -- so let's continue
                return IsList.MAYBE;
            } else {
                return IsList.NO;    // sorry, there's a possibility of failure
            }
        }
    }
    //endregion

    //region TODO categorize
    @Override
    public <T> Class<T> determineClassForType(QName type) {
        Class<?> cached = classForTypeIncludingXsd.get(type);
        if (cached == NO_CLASS) {
            return null;
        } else if (cached != null) {
            //noinspection unchecked
            return (Class<T>) cached;
        } else {
            Class<?> computed = computeClassForType(type);
            classForTypeIncludingXsd.put(type, Objects.requireNonNullElse(computed, NO_CLASS));
            //noinspection unchecked
            return (Class<T>) computed;
        }
    }

    @Override
    public Collection<Package> getCompileTimePackages() {
        Collection<Package> compileTimePackages = new ArrayList<>(schemaDescriptions.size());
        for (SchemaDescription desc : schemaDescriptions) {
            if (desc.getCompileTimeClassesPackage() != null) {
                compileTimePackages.add(desc.getCompileTimeClassesPackage());
            }
        }
        return compileTimePackages;
    }

    private <T> Class<T> computeClassForType(QName type) {
        if (XmlTypeConverter.canConvert(type)) {
            return XsdTypeMapper.toJavaType(type);
        } else {
            return determineCompileTimeClass(type);
        }
    }

    @Override
    public <T> Class<T> determineCompileTimeClass(QName type) {
        return determineCompileTimeClassInternal(type, true);
    }

    public <T> Class<T> determineCompileTimeClassInternal(QName type, boolean cacheAlsoNegativeResults) {
        Class<?> cached = classForTypeExcludingXsd.get(type);
        if (cached == NO_CLASS) {
            return null;
        } else if (cached != null) {
            //noinspection unchecked
            return (Class<T>) cached;
        } else {
            Class<?> computed = computeCompileTimeClass(type);
            if (computed != null || cacheAlsoNegativeResults) {
                classForTypeExcludingXsd.put(type, Objects.requireNonNullElse(computed, NO_CLASS));
            }
            //noinspection unchecked
            return (Class<T>) computed;
        }
    }

    private <T> Class<T> computeCompileTimeClass(QName typeName) {
        if (QNameUtil.noNamespace(typeName)) {
            TypeDefinition td = resolveGlobalTypeDefinitionWithoutNamespace(typeName.getLocalPart(), TypeDefinition.class);
            if (td == null) {
                return null;
            }
            if (QNameUtil.noNamespace(td.getTypeName())) {
                //noinspection unchecked
                return (Class<T>) td.getCompileTimeClass(); // This is the best we can do
            }
            typeName = td.getTypeName();
        }
        SchemaDescription desc = findSchemaDescriptionByNamespace(typeName.getNamespaceURI());
        if (desc == null) {
            return null;
        }
        Package pkg = desc.getCompileTimeClassesPackage();
        if (pkg != null) {
            var fromJaxb = JAXBUtil.findClassForType(typeName, pkg);
            if (fromJaxb != null) {
                //noinspection unchecked
                return (Class<T>) fromJaxb;
            }
        }

        // Last attempt: is this an extension? HACK HACK HACK
        TypeDefinition typeDefinition = findTypeDefinitionByType(typeName);
        if (typeDefinition instanceof ComplexTypeDefinition ctd) {
            QName extensionForType = ctd.getExtensionForType();
            QName extContainerTypeName = PrismContext.get().getExtensionContainerTypeName();
            if (extensionForType != null && extContainerTypeName != null) {
                //noinspection unchecked
                return (Class<T>) Objects.requireNonNull(
                                findComplexTypeDefinitionByType(extContainerTypeName), "No definition for extension container type")
                        .getCompileTimeClass();
            }
        }

        return null;
    }

    @Override
    protected void performFreeze() {
        parsedPrismSchemas.forEach(Freezable::freeze);
        schemaDescriptions.forEach(Freezable::freeze);
    }

    @Override
    public String debugDump(int indent) {
        StringBuilder sb = new StringBuilder();
        DebugUtil.indentDebugDump(sb, indent);
        sb.append("SchemaRegistryState:\n");
        sb.append("  Parsed Schemas:");
        for (SchemaDescription schema : schemaDescriptions) {
            sb.append("\n");
            DebugUtil.indentDebugDump(sb, indent + 1);
            sb.append(schema.getNamespace());
            sb.append(": ");
            sb.append(schema);
        }
        return sb.toString();
    }

    /**
     * Builder for SchemaRegistryState. Builder parse schemas to prism parsed schemas.
     */
    public static class Builder {

        /**
         * A prism context this schema registry is part of.
         * Should be non-null.
         */
        protected PrismContext prismContextBuilder;

        /**
         * Registered schema descriptions.
         * When the registry is in initialized state, all schema descriptions in this list are frozen.
         */
        private List<SchemaDescriptionImpl> schemaDescriptionsBuilder;

        /**
         * "Registry" for namespace prefixes. It is used when serializing data as well as schemas.
         * For historical reasons it is kept here -- along with the schemas.
         */
        private DynamicNamespacePrefixMapper namespacePrefixMapperBuilder;

        /**
         * Extension CTDs for base types.
         * Key is the type being extended: UserType, RoleType, and so on.
         * Value is (merged) extension complex type definition.
         */
        private final Map<QName, ComplexTypeDefinition> extensionSchemasBuilder = new HashMap<>();

        private final Multimap<QName, ItemDefinition<?>> substitutionsBuilder = HashMultimap.create();

        /**
         * Type name for value metadata container. It is set by the application. For example,
         * for midPoint it is c:ValueMetadataType.
         */
        private QName valueMetadataTypeNameBuilder;

        /**
         * Default name for value metadata container. Used to construct ad-hoc definition when no value metadata
         * type name is specified.
         */
        private static final QName DEFAULT_VALUE_METADATA_NAME = new QName("valueMetadata");

        /** Type name for empty metadata. Doesn't exist in the registry. */
        private static final QName DEFAULT_VALUE_METADATA_TYPE_NAME = new QName("EmptyValueMetadataType");

        public Builder prismContext(@NotNull PrismContext prismContext) {
            this.prismContextBuilder = prismContext;
            return this;
        }

        public Builder schemaDescriptions(@NotNull List<SchemaDescriptionImpl> schemaDescriptions) {
            this.schemaDescriptionsBuilder = schemaDescriptions;
            return this;
        }

        public Builder namespacePrefixMapper(DynamicNamespacePrefixMapper namespacePrefixMapper) {
            this.namespacePrefixMapperBuilder = namespacePrefixMapper;
            return this;
        }

        SchemaRegistryStateImpl build() throws SAXException, SchemaException {
            LOGGER.trace("build() starting");
            long start = System.currentTimeMillis();

            MultiValuedMap<String, SchemaDescription> parsedSchemas = createMapForSchemaDescByNamespace(schemaDescriptionsBuilder);
            long parsedSchemasDone = System.currentTimeMillis();
            LOGGER.trace("createMapForSchemaDescByNamespace() done in {} ms", parsedSchemasDone - start);

            SchemaRegistryStateImpl schemaRegistryState = new SchemaRegistryStateImpl(
                    namespacePrefixMapperBuilder,
                    schemaDescriptionsBuilder,
                    parsedSchemas,
                    prismContextBuilder);

            ParsePrismSchemasState parsedPrismSchemaState = parsePrismSchemas(schemaDescriptionsBuilder, schemaRegistryState);
            schemaRegistryState.setParsedPrismSchemas(parsedPrismSchemaState.parsedPrismSchemas);

            //we need split whole schema parsing process to two method, because second method already use result of first
            finishParsingPrismSchemas(parsedPrismSchemaState, schemaRegistryState);
            long prismSchemasDone = System.currentTimeMillis();
            LOGGER.trace("parsePrismSchemas() done in {} ms", prismSchemasDone - parsedSchemasDone);

            Map<Package, PrismSchema> mapper = initPackageMapper(schemaRegistryState);
            schemaRegistryState.putPrismSchemasMap(mapper);
            long initPackageMapperDone = System.currentTimeMillis();
            LOGGER.trace("initPackageMapper() done in {} ms", initPackageMapperDone - prismSchemasDone);

            javax.xml.validation.Schema javaxSchema = parseJavaxSchema(schemaRegistryState);
            schemaRegistryState.setJavaxSchema(javaxSchema);
            long javaxSchemasDone = System.currentTimeMillis();
            LOGGER.trace("parseJavaxSchema() done in {} ms", javaxSchemasDone - initPackageMapperDone);

            PrismContainerDefinition<?> valueMetadataDefinition = resolveValueMetadataDefinition(schemaRegistryState);
            schemaRegistryState.setValueMetadataDefinition(valueMetadataDefinition);
            long valueMetadataDefinitionDone = System.currentTimeMillis();
            LOGGER.trace("resolveValueMetadataDefinition() done in {} ms", valueMetadataDefinitionDone - javaxSchemasDone);

            LOGGER.trace("build() finish");
            return schemaRegistryState;
        }

        private PrismContainerDefinition<?> resolveValueMetadataDefinition(SchemaRegistryStateImpl schemaRegistryState) {
            if (valueMetadataTypeNameBuilder != null) {
                return Objects.requireNonNull(
                        schemaRegistryState.findContainerDefinitionByType(valueMetadataTypeNameBuilder),
                        () -> "no definition for value metadata type " + valueMetadataTypeNameBuilder);
            } else {
                return createDefaultValueMetadataDefinition();
            }
        }

        private PrismContainerDefinition<?> createDefaultValueMetadataDefinition() {
            var pcd = prismContextBuilder.definitionFactory().newContainerDefinitionWithoutTypeDefinition(
                    DEFAULT_VALUE_METADATA_NAME, DEFAULT_VALUE_METADATA_TYPE_NAME);
            pcd.mutator().setMinOccurs(0);
            pcd.mutator().setMaxOccurs(1);
            return pcd;
        }

        private MultiValuedMap<String, SchemaDescription> createMapForSchemaDescByNamespace(List<SchemaDescriptionImpl> schemaDescriptions) {
            MultiValuedMap<String, SchemaDescription> parsedSchemas = new ArrayListValuedHashMap<>();
            schemaDescriptions.forEach(schema -> parsedSchemas.put(schema.getNamespace(), schema));
            return parsedSchemas;
        }

        private Map<Package, PrismSchema> initPackageMapper(SchemaRegistryStateImpl schemaRegistryState) {
            Map<Package, PrismSchema> mapper = new HashMap<>();
            for (PrismSchemaImpl schema : schemaRegistryState.parsedPrismSchemas) {
                Package compileTimePackage = schema.getCompileTimePackage();
                if (compileTimePackage != null) {
                    mapper.put(compileTimePackage, schema);
                }
            }
            return mapper;
        }

        // see https://stackoverflow.com/questions/14837293/xsd-circular-import
        private ParsePrismSchemasState parsePrismSchemas(
                List<SchemaDescriptionImpl> schemaDescriptions, SchemaRegistryStateImpl schemaRegistryState) {
            List<SchemaDescriptionImpl> prismSchemaDescriptions = schemaDescriptions.stream()
                    .filter(SchemaDescriptionImpl::isPrismSchema)
                    .toList();
            Element schemaElement = DOMUtil.createElement(DOMUtil.XSD_SCHEMA_ELEMENT);
            schemaElement.setAttribute("targetNamespace", "http://dummy/");
            schemaElement.setAttribute("elementFormDefault", "qualified");

            // These fragmented namespaces should not be included in wrapper XSD because they are defined in multiple XSD files.
            // We have to process them one by one.
            MultiValuedMap<String, SchemaDescriptionImpl> filteredSchemasByNamespace = new ArrayListValuedHashMap<>();
            prismSchemaDescriptions.forEach(sd -> filteredSchemasByNamespace.put(sd.getNamespace(), sd));
            List<String> fragmentedNamespaces = filteredSchemasByNamespace.keySet().stream()
                    .filter(ns -> filteredSchemasByNamespace.get(ns).size() > 1)
                    .collect(Collectors.toList());
            LOGGER.trace("Fragmented namespaces: {}", fragmentedNamespaces);

            List<PrismSchemaImpl> wrappedSchemas = new ArrayList<>();
            Collection<PrismSchemaImpl> parsedPrismSchemas = new ArrayList<>();
            MultiValuedMap<String, SchemaDescription> schemasByNamespace = new ArrayListValuedHashMap<>();
            for (SchemaDescriptionImpl description : prismSchemaDescriptions) {
                String namespace = description.getNamespace();
                schemasByNamespace.put(namespace, description);
                if (!fragmentedNamespaces.contains(namespace)) {
                    Element importElement = DOMUtil.createSubElement(schemaElement, DOMUtil.XSD_IMPORT_ELEMENT);
                    importElement.setAttribute(DOMUtil.XSD_ATTR_NAMESPACE.getLocalPart(), namespace);
                    PrismSchemaImpl schema = new PrismSchemaImpl(namespace, description.getCompileTimeClassesPackage());
                    schema.setRuntime(description.getCompileTimeClassesPackage() == null);
                    schema.setSourceDescription(description.getSourceDescription());
                    schema.setSchemaRegistryState(schemaRegistryState);
                    parsedPrismSchemas.add(schema);
                    wrappedSchemas.add(schema);
                }
            }
            return new ParsePrismSchemasState()
                    .schemaElement(schemaElement)
                    .wrappedSchemas(wrappedSchemas)
                    .fragmentedNamespaces(fragmentedNamespaces)
                    .parsedPrismSchemas(parsedPrismSchemas)
                    .filteredSchemasByNamespace(filteredSchemasByNamespace)
                    .schemasByNamespace(schemasByNamespace);
        }

        void finishParsingPrismSchemas(ParsePrismSchemasState parsePrismSchemasDto, SchemaRegistryStateImpl schemaRegistryState) throws SchemaException {
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("Wrapper XSD:\n{}", DOMUtil.serializeDOMToString(parsePrismSchemasDto.schemaElement));
            }

            long started = System.currentTimeMillis();
            LOGGER.trace("Parsing {} schemas wrapped in single XSD", parsePrismSchemasDto.wrappedSchemas.size());
            SchemaParsingUtil.parseSchemas(parsePrismSchemasDto.schemaElement, parsePrismSchemasDto.wrappedSchemas, schemaRegistryState);
            LOGGER.trace("Parsed {} schemas in {} ms",
                    parsePrismSchemasDto.wrappedSchemas.size(), System.currentTimeMillis() - started);

            for (PrismSchemaImpl parsedSchema : parsePrismSchemasDto.parsedPrismSchemas) {
                detectAugmentations(parsedSchema, schemaRegistryState);
            }

            for (String namespace : parsePrismSchemasDto.fragmentedNamespaces) {
                Collection<SchemaDescriptionImpl> fragments = parsePrismSchemasDto.filteredSchemasByNamespace.get(namespace);
                LOGGER.trace("Parsing {} schemas for fragmented namespace {}", fragments.size(), namespace);
                for (SchemaDescriptionImpl schemaDescription : fragments) {
                    parsePrismSchema(parsePrismSchemasDto.parsedPrismSchemas, schemaDescription, schemaRegistryState);
                }
            }

            applyAugmentations(schemaRegistryState);
            for (PrismSchemaImpl schema : parsePrismSchemasDto.parsedPrismSchemas) {
                resolveMissingTypeDefinitionsInGlobalItemDefinitions(schema);
                processTypes(schemaRegistryState, schema);
            }
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("====================================== Dumping prism schemas ======================================\n");
                for (PrismSchemaImpl prismSchema : schemaRegistryState.parsedPrismSchemas) {
                    LOGGER.trace("************************************************************* {} (in {})",
                            prismSchema.getNamespace(), prismSchema.getSourceDescription());
                    LOGGER.trace("{}", prismSchema.debugDump());
                }
            }
        }

        private void parsePrismSchema(
                Collection<PrismSchemaImpl> parsedPrismSchemas,
                SchemaDescriptionImpl schemaDescription,
                SchemaRegistryStateImpl schemaRegistryState) throws SchemaException {
            String namespace = schemaDescription.getNamespace();

            Element domElement = schemaDescription.getDomElement();
            boolean isRuntime = schemaDescription.getCompileTimeClassesPackage() == null;
            long started = System.currentTimeMillis();
            LOGGER.trace("Parsing schema {}, namespace: {}, isRuntime: {}",
                    schemaDescription.getSourceDescription(), namespace, isRuntime);
            PrismSchemaImpl schema = new PrismSchemaImpl(DOMUtil.getSchemaTargetNamespace(domElement));
            schema.setSchemaRegistryState(schemaRegistryState);
            SchemaParsingUtil.parse(schema, domElement, isRuntime, schemaDescription.getSourceDescription(), true, schemaRegistryState);
            if (StringUtils.isEmpty(namespace)) {
                namespace = schema.getNamespace();
            }
            LOGGER.trace("Parsed schema {}, namespace: {}, isRuntime: {} in {} ms",
                    schemaDescription.getSourceDescription(), namespace, isRuntime, System.currentTimeMillis() - started);
            parsedPrismSchemas.add(schema);
            detectAugmentations(schema, schemaRegistryState);
        }

        // global item definitions may refer to types that are not yet available
        private void resolveMissingTypeDefinitionsInGlobalItemDefinitions(PrismSchemaImpl schema) throws SchemaException {
            for (var iterator = schema.getDelayedItemDefinitions().iterator(); iterator.hasNext(); ) {
                schema.add(iterator.next().get());
                iterator.remove();
            }
        }

        private void processTypes(SchemaRegistryStateImpl schemaRegistryState, PrismSchemaImpl schema) {
            for (TypeDefinition typeDefinition : schema.getDefinitions(TypeDefinition.class)) {
                processSubstitutionGroups(typeDefinition);
                fillInSubtype(schemaRegistryState, typeDefinition);
                schemaContextDefinitionInherited(schemaRegistryState, typeDefinition);
            }
        }

        private void fillInSubtype(SchemaRegistryStateImpl schemaRegistryState, TypeDefinition typeDefinition) {
            if (typeDefinition.getSuperType() == null) {
                return;
            }
            TypeDefinition superTypeDef = schemaRegistryState.findTypeDefinitionByType(typeDefinition.getSuperType(), TypeDefinition.class);
            if (superTypeDef instanceof TypeDefinitionImpl) {
                ((TypeDefinitionImpl) superTypeDef).addStaticSubType(typeDefinition);
            }
        }

        private void processSubstitutionGroups(TypeDefinition typeDefinition) {
            if(!(typeDefinition instanceof ComplexTypeDefinition complex)) {
                return;
            }
            for(ItemDefinition<?> itemDef : complex.getDefinitions()) {
                Collection<ItemDefinition<?>> maybeSubst = substitutionsBuilder.get(itemDef.getItemName());
                if (!maybeSubst.isEmpty()) {
                    addSubstitutionsToComplexType(complex.mutator(), itemDef, maybeSubst);
                }
            }
        }

        private void addSubstitutionsToComplexType(
                ComplexTypeDefinition.ComplexTypeDefinitionMutator ctdMutator, ItemDefinition<?> itemDef, Collection<ItemDefinition<?>> maybeSubst) {
            for(ItemDefinition<?> substitution : maybeSubst) {
                if (isSubstitution(itemDef, substitution)) {
                    ctdMutator.addSubstitution(itemDef, substitution);
                }
            }
        }

        private boolean isSubstitution(ItemDefinition<?> itemDef, ItemDefinition<?> maybeSubst) {
            return itemDef.getItemName().equals(maybeSubst.getSubstitutionHead());
        }

        private void detectAugmentations(PrismSchema schema, SchemaRegistryStateImpl schemaRegistryState) {
            detectSubstitutions(schema);
            detectExtensionSchema(schema, schemaRegistryState);
        }

        private void detectSubstitutions(PrismSchema schema) {
            substitutionsBuilder.putAll(schema.getSubstitutions());
        }

        private void detectExtensionSchema(PrismSchema schema, SchemaRegistryStateImpl schemaRegistryState) {
            for (ComplexTypeDefinition def : schema.getComplexTypeDefinitions()) {
                QName typeBeingExtended = def.getExtensionForType(); // e.g. c:UserType
                if (typeBeingExtended != null) {
                    LOGGER.trace("Processing {} as an extension for {}", def, typeBeingExtended);
                    if (extensionSchemasBuilder.containsKey(typeBeingExtended)) {
                        ComplexTypeDefinition existingExtension = extensionSchemasBuilder.get(typeBeingExtended);
                        existingExtension.merge(def);
                    } else {
                        @NotNull ComplexTypeDefinition clone = def.clone();
                        if (clone instanceof ComplexTypeDefinitionImpl clonedComplexDef) {
                            clonedComplexDef.setSchemaRegistryState(schemaRegistryState);
                        }
                        extensionSchemasBuilder.put(typeBeingExtended, clone);
                    }
                }
            }
        }

        private void applyAugmentations(SchemaRegistryStateImpl schemaRegistryState) throws SchemaException {
            for (Entry<QName, ComplexTypeDefinition> entry : extensionSchemasBuilder.entrySet()) {
                QName typeQName = entry.getKey();
                ComplexTypeDefinition extensionCtd = entry.getValue();
                ComplexTypeDefinition primaryCtd = schemaRegistryState.findComplexTypeDefinitionByType(typeQName);
                PrismContainerDefinition<?> extensionContainer = primaryCtd.findContainerDefinition(
                        new ItemName(primaryCtd.getTypeName().getNamespaceURI(), PrismConstants.EXTENSION_LOCAL_NAME));
                if (extensionContainer == null) {
                    throw new SchemaException(
                            "Attempt to extend type '%s' with '%s' but the original type does not have extension container".formatted(
                                    typeQName, extensionCtd.getTypeClass()));
                }
                var clone = extensionContainer.cloneWithNewType(extensionCtd.getTypeName(), extensionCtd.clone());
                primaryCtd.mutator().replaceDefinition(extensionContainer.getItemName(), clone);
            }
        }

        private javax.xml.validation.Schema parseJavaxSchema(@NotNull SchemaRegistryStateImpl schemaRegistryState) throws SAXException {
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Source[] sources = new Source[schemaDescriptionsBuilder.size()];
            int i = 0;
            for (SchemaDescription schemaDescription : schemaDescriptionsBuilder) {
                Source source = schemaDescription.getSource();
                sources[i] = source;
                i++;
            }
            schemaFactory.setResourceResolver(new XmlEntityResolverImpl(
                    (SchemaRegistryImpl) schemaRegistryState.prismContext.getSchemaRegistry(),
                    schemaRegistryState));
            return schemaFactory.newSchema(sources);
        }

        private void schemaContextDefinitionInherited(SchemaRegistryStateImpl schemaRegistryState, TypeDefinition currentTypeDefinition) {
            if (currentTypeDefinition.getSchemaContextDefinition() != null) return;

            TypeDefinition typeDefinition = currentTypeDefinition;
            SchemaContextDefinition schemaContextDefinition = null;

            while(schemaContextDefinition == null) {
                schemaContextDefinition = typeDefinition.getSchemaContextDefinition();
                if (typeDefinition.getSuperType() == null) break;
                typeDefinition = schemaRegistryState.findTypeDefinitionByType(typeDefinition.getSuperType(), TypeDefinition.class);
                if (typeDefinition == null) break;
            }

            ((TypeDefinitionImpl) currentTypeDefinition).setSchemaContextDefinition(schemaContextDefinition);
        }
    }

    private static class ParsePrismSchemasState implements Serializable {

        Element schemaElement;
        List<PrismSchemaImpl> wrappedSchemas;
        Collection<PrismSchemaImpl> parsedPrismSchemas;
        List<String> fragmentedNamespaces;
        MultiValuedMap<String, SchemaDescriptionImpl> filteredSchemasByNamespace;
        MultiValuedMap<String, SchemaDescription> schemasByNamespace;

        public ParsePrismSchemasState schemaElement(Element schemaElement) {
            this.schemaElement = schemaElement;
            return this;
        }

        public ParsePrismSchemasState wrappedSchemas(List<PrismSchemaImpl> wrappedSchemas) {
            this.wrappedSchemas = wrappedSchemas;
            return this;
        }

        public ParsePrismSchemasState fragmentedNamespaces(List<String> fragmentedNamespaces) {
            this.fragmentedNamespaces = fragmentedNamespaces;
            return this;
        }

        public ParsePrismSchemasState parsedPrismSchemas(Collection<PrismSchemaImpl> parsedPrismSchemas) {
            this.parsedPrismSchemas = parsedPrismSchemas;
            return this;
        }

        public ParsePrismSchemasState filteredSchemasByNamespace(MultiValuedMap<String, SchemaDescriptionImpl> filteredSchemasByNamespace) {
            this.filteredSchemasByNamespace = filteredSchemasByNamespace;
            return this;
        }

        public ParsePrismSchemasState schemasByNamespace(MultiValuedMap<String, SchemaDescription> schemasByNamespace) {
            this.schemasByNamespace = schemasByNamespace;
            return this;
        }
    }
}
