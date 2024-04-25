/*
 * Copyright (C) 2010-2021 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.prism.impl.schema;

import static java.util.Collections.emptyList;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.xml.namespace.QName;
import javax.xml.validation.Validator;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.xml.resolver.Catalog;
import org.apache.xml.resolver.CatalogManager;
import org.apache.xml.resolver.tools.CatalogResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.EntityResolver;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.delta.ObjectDelta;
import com.evolveum.midpoint.prism.impl.*;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.prism.schema.*;
import com.evolveum.midpoint.prism.schema.SchemaRegistryState.IsList;
import com.evolveum.midpoint.prism.xml.DynamicNamespacePrefixMapper;
import com.evolveum.midpoint.prism.xml.XmlTypeConverter;
import com.evolveum.midpoint.prism.xml.XsdTypeMapper;
import com.evolveum.midpoint.util.*;
import com.evolveum.midpoint.util.annotation.Experimental;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;
import com.evolveum.prism.xml.ns._public.types_3.ObjectType;
import com.evolveum.prism.xml.ns._public.types_3.PolyStringType;

/**
 * Registry and resolver of schema files and resources.
 *
 * @author Radovan Semancik
 */
public class SchemaRegistryImpl implements DebugDumpable, SchemaRegistry {

    private static final Trace LOGGER = TraceManager.getTrace(SchemaRegistryImpl.class);

    private static final String DEFAULT_RUNTIME_CATALOG_RESOURCE = "META-INF/catalog-runtime.xml";

    /**
     * Catalog files to look for when resolving schemas by URI.
     * This has precedence over catalogResourceName.
     */
    private File[] catalogFiles;

    /**
     * Catalog resource to look for when resolving schemas by URI.
     * Overridden by catalog files, if specified.
     */
    private String catalogResourceName = DEFAULT_RUNTIME_CATALOG_RESOURCE;

    /**
     * Resolver for schema files based on catalog files or catalog resource.
     * Created during early stages of the initialization.
     */
    private EntityResolver builtinSchemaResolver;

    /**
     * Advanced entity resolver that uses all registered schemas and built-in catalog-based schema resolver.
     */
    private final XmlEntityResolver entityResolver = new XmlEntityResolverImpl(this);

    /**
     * Registered schema descriptions.
     */
    private final List<SchemaDescriptionImpl> schemaDescriptions = new ArrayList<>();

    /**
     * Registered schema extensions descriptions.
     * When the registry is in initialized state, all schema descriptions in this list are frozen.
     */
    private List<SchemaDescriptionImpl> dynamicSchemaExtensions = List.of();

    /**
     * Current parsed schema state.
     */
    private SchemaRegistryStateImpl schemaRegistryState;

    /**
     * "Registry" for namespace prefixes. It is used when serializing data as well as schemas.
     * For historical reasons it is kept here -- along with the schemas.
     */
    private DynamicNamespacePrefixMapper namespacePrefixMapper;

    /**
     * What namespace is considered "default" for the schema registry. The meaning of this field is unknown.
     * Currently we use this information to provide namespace for "_value" and "_metadata" elements in XML
     * serialization.
     */
    private String defaultNamespace;

    /**
     * A prism context this schema registry is part of.
     * Should be non-null.
     */
    protected PrismContext prismContext;

    /**
     * Listeners to be called when schema-related caches have to be invalidated.
     * This occurs when the registry is initialized or when a new schema is added to the registry.
     * See {@link #invalidateCaches()} method.
     */
    private final Collection<InvalidationListener> invalidationListeners = new ArrayList<>();

    /**
     * Type name for value metadata container. It is set by the application. For example,
     * for midPoint it is c:ValueMetadataType.
     */
    private QName valueMetadataTypeName;

    /**
     * Definition of the value metadata container.
     * It is lazily evaluated, because the schema registry has to be initialized to resolve type name to definition.
     */
    private PrismContainerDefinition<?> valueMetadataDefinition;

    /**
     * Default name for value metadata container. Used to construct ad-hoc definition when no value metadata
     * type name is specified.
     */
    private static final QName DEFAULT_VALUE_METADATA_NAME = new QName("valueMetadata");

    /** Type name for empty metadata. Doesn't exist in the registry. */
    private static final QName DEFAULT_VALUE_METADATA_TYPE_NAME = new QName("EmptyValueMetadataType");

    private PrismNamespaceContext staticNamespaceContext;

    private final PrismNamespaceContext.Builder staticPrefixes = PrismNamespaceContext.builder();

    /**
     * Don't use it for edit of mapper. For editing of mapper use {@link #customizeNamespacePrefixMapper}
     */
    @Override
    public DynamicNamespacePrefixMapper getNamespacePrefixMapper() {
        if (isInitialized()) {
            return schemaRegistryState.getNamespacePrefixMapper();
        }
        return namespacePrefixMapper;
    }

    public void customizeNamespacePrefixMapper(Consumer<DynamicNamespacePrefixMapper> customizer){
        if (isInitialized()) {
            customizer.accept(schemaRegistryState.getNamespacePrefixMapper());
        }
        customizer.accept(namespacePrefixMapper);

    }

    /**
     * Must be called before call to initialize()
     */
    public void setNamespacePrefixMapper(DynamicNamespacePrefixMapper namespacePrefixMapper) {
        this.namespacePrefixMapper = namespacePrefixMapper;
    }

    @Override
    public void registerInvalidationListener(InvalidationListener listener) {
        invalidationListeners.add(listener);
    }

    public void setPrismContext(@NotNull PrismContext prismContext) {
        this.prismContext = prismContext;
    }

    public XmlEntityResolver getEntityResolver() {
        return entityResolver;
    }

    public MultiValuedMap<String, SchemaDescription> getParsedSchemas() {
        return schemaRegistryState.getParsedSchemas();
    }

    public EntityResolver getBuiltinSchemaResolver() {
        return builtinSchemaResolver;
    }

    @SuppressWarnings("unused") // consider removal
    public File[] getCatalogFiles() {
        return catalogFiles;
    }

    public void setCatalogFiles(File[] catalogFiles) {
        this.catalogFiles = catalogFiles;
    }

    @SuppressWarnings("unused") // consider removal
    public String getCatalogResourceName() {
        return catalogResourceName;
    }

    public void setCatalogResourceName(String catalogResourceName) {
        this.catalogResourceName = catalogResourceName;
    }

    @Override
    public String getDefaultNamespace() {
        return defaultNamespace;
    }

    /**
     * Must be called before call to initialize()
     */
    public void setDefaultNamespace(String defaultNamespace) {
        this.defaultNamespace = defaultNamespace;
        this.staticPrefixes.defaultNamespace(defaultNamespace);
    }

    //region Registering resources and initialization

    /**
     * Must be called before call to initialize()
     */
    @SuppressWarnings("unused") // consider removal
    public void registerSchemaResource(String resourcePath, String usualPrefix) throws SchemaException {
        SchemaDescriptionImpl desc = SchemaDescriptionParser.parseResource(resourcePath);
        desc.setUsualPrefix(usualPrefix);
        registerSchemaDescription(desc);
    }

    /**
     * Must be called before call to initialize()
     */
    public void registerPrismSchemaResource(String resourcePath, String usualPrefix) throws SchemaException {
        SchemaDescriptionImpl desc = SchemaDescriptionParser.parseResource(resourcePath);
        desc.setUsualPrefix(usualPrefix);
        desc.setPrismSchema(true);
        registerSchemaDescription(desc);
    }

    public void registerWsdlSchemaFile(File file) throws SchemaException, FileNotFoundException {
        List<SchemaDescriptionImpl> descriptions = SchemaDescriptionParser.parseWsdlResource(file);
        registerPrismSchemasFromWsdl(file.toString(), descriptions, null);
    }

    public void registerPrismSchemasFromWsdlResource(String resourcePath, List<Package> compileTimeClassesPackages) throws SchemaException {
        List<SchemaDescriptionImpl> descriptions = SchemaDescriptionParser.parseWsdlResource(resourcePath);
        registerPrismSchemasFromWsdl(resourcePath, descriptions, compileTimeClassesPackages);

    }

    protected void registerPrismSchemasFromWsdl(String resourcePath, List<SchemaDescriptionImpl> descriptions, List<Package> compileTimeClassesPackages) throws SchemaException {
        Iterator<Package> pkgIterator = null;
        if (compileTimeClassesPackages != null) {
            if (descriptions.size() != compileTimeClassesPackages.size()) {
                throw new SchemaException("Mismatch between the size of compileTimeClassesPackages (" + compileTimeClassesPackages.size()
                        + " and schemas in " + resourcePath + " (" + descriptions.size() + ")");
            }
            pkgIterator = compileTimeClassesPackages.iterator();
        }
        for (SchemaDescriptionImpl desc : descriptions) {
            desc.setPrismSchema(true);
            if (pkgIterator != null) {
                desc.setCompileTimeClassesPackage(pkgIterator.next());
            }
            registerSchemaDescription(desc);
        }
    }

    /**
     * Must be called before call to initialize()
     */
    public void registerPrismSchemaResource(String resourcePath, String usualPrefix, Package compileTimeClassesPackage) throws SchemaException {
        registerPrismSchemaResource(resourcePath, usualPrefix, compileTimeClassesPackage, false, false);
    }

    /**
     * Must be called before call to initialize()
     */
    public void registerPrismSchemaResource(String resourcePath, String usualPrefix, Package compileTimeClassesPackage, boolean prefixDeclaredByDefault) throws SchemaException {
        registerPrismSchemaResource(resourcePath, usualPrefix, compileTimeClassesPackage, false, prefixDeclaredByDefault);
    }

    /**
     * Must be called before call to initialize()
     */
    public void registerPrismDefaultSchemaResource(String resourcePath, String usualPrefix, Package compileTimeClassesPackage) throws SchemaException {
        registerPrismSchemaResource(resourcePath, usualPrefix, compileTimeClassesPackage, true, true);
    }

    /**
     * Must be called before call to initialize()
     *
     * @param prefixDeclaredByDefault Whether this prefix will be declared in top element in all XML serializations (MID-2198)
     */
    private void registerPrismSchemaResource(String resourcePath, String usualPrefix, Package compileTimeClassesPackage,
            boolean defaultSchema, boolean prefixDeclaredByDefault) throws SchemaException {
        SchemaDescriptionImpl desc = SchemaDescriptionParser.parseResource(resourcePath);
        desc.setUsualPrefix(usualPrefix);
        desc.setPrismSchema(true);
        desc.setDefault(defaultSchema);
        desc.setDeclaredByDefault(prefixDeclaredByDefault);
        desc.setCompileTimeClassesPackage(compileTimeClassesPackage);
        registerSchemaDescription(desc);
    }

    /**
     * Must be called before call to initialize()
     */
    public void registerSchema(Node node, String sourceDescription) throws SchemaException {
        registerSchemaDescription(SchemaDescriptionParser.parseNode(node, sourceDescription));
    }

    public void registerPrismSchemaFile(File file) throws IOException, SchemaException {
        loadPrismSchemaFileDescription(file);
    }

    public void registerPrismSchema(InputStream input, String sourceDescription) throws SchemaException {
        loadPrismSchemaDescription(input, sourceDescription);
    }

    protected void loadPrismSchemaFileDescription(File file) throws SchemaException, IOException {
        if (!(file.getName().matches(".*\\.xsd$"))) {
            LOGGER.trace("Skipping registering {}, because it is not schema definition.", file.getAbsolutePath());
        } else {
            LOGGER.info("Loading schema from file {}", file);
            SchemaDescriptionImpl desc = SchemaDescriptionParser.parseFile(file);
            desc.setPrismSchema(true);
            registerSchemaDescription(desc);
        }
    }

    private void loadPrismSchemaDescription(InputStream input, String sourceDescription)
            throws SchemaException {
        SchemaDescriptionImpl desc = SchemaDescriptionParser.parseInputStream(input, sourceDescription);
        desc.setPrismSchema(true);
        registerSchemaDescription(desc);
    }

    private void registerSchemaDescription(SchemaDescriptionImpl desc) throws SchemaException {
        registerDynamicPrefix(desc, this.namespacePrefixMapper);

        addStaticPrefix(desc);

        schemaDescriptions.add(desc);

        desc.setRegistered();

        if (isInitialized()) {
            reload();
        }

        invalidateCaches();
    }

    private void addStaticPrefix(SchemaDescriptionImpl desc) {
        String defaultPrefix = desc.getDefaultPrefix();
        if (defaultPrefix != null) {
            staticPrefixes.addPrefix(defaultPrefix, desc.getNamespace());
        }
    }

    private void registerDynamicPrefix(SchemaDescriptionImpl desc, DynamicNamespacePrefixMapper namespacePrefixMapper) {
        String usualPrefix = desc.getUsualPrefix();
        if (usualPrefix != null) {
            namespacePrefixMapper.registerPrefix(desc.getNamespace(), usualPrefix, desc.isDefault());
            if (desc.isDeclaredByDefault()) {
                namespacePrefixMapper.addDeclaredByDefault(usualPrefix);
            }
        }
    }

    private boolean isInitialized() {
        return schemaRegistryState != null;
    }

    public void registerPrismSchemasFromDirectory(File directory) throws IOException, SchemaException {
        registerPrismSchemasFromDirectory(directory, emptyList());
    }

    public void registerPrismSchemasFromDirectory(File directory, @NotNull Collection<String> extensionFilesToIgnore)
            throws IOException, SchemaException {
        File[] fileArray = directory.listFiles();
        if (fileArray != null) {
            List<File> files = Arrays.asList(fileArray);
            // Sort the filenames so we have deterministic order of loading
            // This is useful in tests but may come handy also during customization
            Collections.sort(files);
            for (File file : files) {
                String name = file.getName();
                if (name.startsWith(".")) {
                    // skip dotfiles. this will skip SVN data and similar things
                    continue;
                }
                if (extensionFilesToIgnore.contains(name)) {
                    continue;
                }
                if (file.isDirectory()) {
                    registerPrismSchemasFromDirectory(file);
                }
                if (file.isFile()) {
                    registerPrismSchemaFile(file);
                }
            }
        }
    }

    @VisibleForTesting
    public void loadPrismSchemaResource(String resourcePath) throws SchemaException {
        SchemaDescriptionImpl desc = SchemaDescriptionParser.parseResource(resourcePath);
        desc.setPrismSchema(true);
        registerSchemaDescription(desc);
    }

    public void reload() throws SchemaException {
        try {
            initialize();
        } catch (SAXException | IOException e) {
            throw new SchemaException(e.getMessage(), e);
        }
    }

    /**
     * This can be used to read additional schemas even after the registry was initialized.
     */
    @Override
    public void initialize() throws SAXException, IOException, SchemaException {
        if (prismContext == null) {
            throw new IllegalStateException("Prism context not set");
        }
        if (namespacePrefixMapper == null) {
            throw new IllegalStateException("Namespace prefix mapper not set");
        }
        try {
            LOGGER.trace("initialize() starting");
            long start = System.currentTimeMillis();

            initResolver();
            long resolverDone = System.currentTimeMillis();
            LOGGER.trace("initResolver() done in {} ms", resolverDone - start);

            List<SchemaDescriptionImpl> schemaDescriptions = new ArrayList<>();
            schemaDescriptions.addAll(this.schemaDescriptions);
            schemaDescriptions.addAll(this.dynamicSchemaExtensions);

            DynamicNamespacePrefixMapper namespacePrefixMapper = this.namespacePrefixMapper.clone();
            dynamicSchemaExtensions.forEach(schema -> registerDynamicPrefix(schema, namespacePrefixMapper));

            SchemaRegistryStateImpl schemaRegistryStateLocale = new SchemaRegistryStateImpl.Builder()
                    .prismContext(prismContext)
                    .schemaDescriptions(schemaDescriptions)
                    .namespacePrefixMapper(namespacePrefixMapper)
                    .build();
            long schemaRegistryStateDone = System.currentTimeMillis();
            LOGGER.trace("init schemaRegistryState done in {} ms", schemaRegistryStateDone - resolverDone);

            parseAdditionalSchemas(schemaRegistryStateLocale);

            schemaRegistryStateLocale.freeze();

            this.schemaRegistryState = schemaRegistryStateLocale;

            invalidateCaches();
            staticNamespaceContext = staticPrefixes.build();
        } catch (SAXException ex) {
            if (ex instanceof SAXParseException sex) {
                throw new SchemaException("Error parsing schema " + sex.getSystemId() + " line " + sex.getLineNumber() + ": " + sex.getMessage(), sex);
            }
            throw ex;
        }
    }

    protected void parseAdditionalSchemas(SchemaRegistryState schemaRegistryStateLocale) throws SchemaException {
    }

    private void invalidateCaches() {
        if (isInitialized()) {
            schemaRegistryState.invalidateCaches();
        }
        invalidationListeners.forEach(InvalidationListener::invalidate);
    }

    private void initResolver() throws IOException {
        CatalogManager catalogManager = new CatalogManager();
        catalogManager.setUseStaticCatalog(true);
        catalogManager.setIgnoreMissingProperties(true);
        catalogManager.setVerbosity(1);
        catalogManager.setPreferPublic(true);
        CatalogResolver catalogResolver = new CatalogResolver(catalogManager);
        Catalog catalog = catalogResolver.getCatalog();

        if (catalogFiles != null && catalogFiles.length > 0) {
            for (File catalogFile : catalogFiles) {
                LOGGER.trace("Using catalog file {}", catalogFile);
                catalog.parseCatalog(catalogFile.getPath());
            }
        } else if (catalogResourceName != null) {
            LOGGER.trace("Using catalog from resource: {}", catalogResourceName);
            Enumeration<URL> catalogs = SchemaRegistryImpl.class.getClassLoader().getResources(catalogResourceName);
            while (catalogs.hasMoreElements()) {
                URL catalogResourceUrl = catalogs.nextElement();
                LOGGER.trace("Parsing catalog from URL: {}", catalogResourceUrl);
                catalog.parseCatalog(catalogResourceUrl);
            }
        } else {
            throw new IllegalStateException("Catalog is not defined");
        }

        builtinSchemaResolver = catalogResolver;
    }
    //endregion

    //region Schemas and type maps (TODO)
    @Override
    public javax.xml.validation.Schema getJavaxSchema() {
        return schemaRegistryState.getJavaxSchema();
    }

    @Override
    public Validator getJavaxSchemaValidator() {
        Validator validator = getJavaxSchema().newValidator();
        validator.setResourceResolver(entityResolver);
        return validator;
    }

    @Override
    public Collection<Package> getCompileTimePackages() {
        return schemaRegistryState.getCompileTimePackages();
    }
    //endregion

    @Override
    public String debugDump(int indent) {
        StringBuilder sb = new StringBuilder();
        DebugUtil.indentDebugDump(sb, indent);
        sb.append("SchemaRegistry:\n")
                .append("  Parsed Schema state:")
                .append("\n")
                .append(schemaRegistryState == null ? null : schemaRegistryState.debugDump(indent));
        return sb.toString();
    }

    //region applyDefinition(..) methods
    @Override
    public <C extends Containerable> void applyDefinition(PrismContainer<C> container, Class<C> type) throws SchemaException {
        applyDefinition(container, type, true);
    }

    @Override
    public <C extends Containerable> void applyDefinition(PrismContainer<C> container, Class<C> compileTimeClass, boolean force) throws SchemaException {
        //noinspection unchecked
        PrismContainerDefinition<C> definition = determineDefinitionFromClass(compileTimeClass);
        if (definition != null) {
            container.applyDefinition(definition, force);
        }
    }

    @Override
    public <O extends Objectable> void applyDefinition(ObjectDelta<O> objectDelta, Class<O> compileTimeClass, boolean force) throws SchemaException {
        //noinspection unchecked
        PrismObjectDefinition<O> objectDefinition = determineDefinitionFromClass(compileTimeClass);
        if (objectDefinition != null) {
            objectDelta.applyDefinition(objectDefinition, force);
        }
    }

    @Override
    public <C extends Containerable, O extends Objectable> void applyDefinition(PrismContainerValue<C> prismContainerValue,
            Class<O> compileTimeClass, ItemPath path, boolean force) throws SchemaException {
        //noinspection unchecked
        PrismObjectDefinition<O> objectDefinition = determineDefinitionFromClass(compileTimeClass);
        PrismContainerDefinition<C> containerDefinition = objectDefinition.findContainerDefinition(path);
        if (containerDefinition != null) {
            prismContainerValue.applyDefinition(containerDefinition, force);
        }
    }

    @Override
    public <C extends Containerable> void applyDefinition(PrismContainerValue<C> prismContainerValue, QName typeName,
            ItemPath path, boolean force) throws SchemaException {
        PrismObjectDefinition objectDefinition = findObjectDefinitionByType(typeName);
        if (objectDefinition != null) {
            PrismContainerDefinition<C> containerDefinition = objectDefinition.findContainerDefinition(path);
            prismContainerValue.applyDefinition(containerDefinition, force);
            return;
        }
        PrismContainerDefinition typeDefinition = findContainerDefinitionByType(typeName);
        if (typeDefinition != null) {
            PrismContainerDefinition<C> containerDefinition = typeDefinition.findContainerDefinition(path);
            prismContainerValue.applyDefinition(containerDefinition, force);
            return;
        }
        ComplexTypeDefinition complexTypeDefinition = findComplexTypeDefinitionByType(typeName);
        if (complexTypeDefinition != null) {
            PrismContainerDefinition<C> containerDefinition = complexTypeDefinition.findContainerDefinition(path);
            prismContainerValue.applyDefinition(containerDefinition, force);
            return;
        }
        throw new SchemaException("No definition for container " + path + " in type " + typeName);
    }
    //endregion

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
            TypeDefinition td = schemaRegistryState.resolveGlobalTypeDefinitionWithoutNamespace(typeName.getLocalPart(), TypeDefinition.class);
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
            return schemaRegistryState.resolveGlobalItemDefinitionsWithoutNamespace(elementName.getLocalPart(), definitionClass);
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
            return schemaRegistryState.resolveGlobalTypeDefinitionWithoutNamespace(typeName.getLocalPart(), definitionClass);
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
            return schemaRegistryState.resolveGlobalTypeDefinitionsWithoutNamespace(typeName.getLocalPart(), definitionClass);
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
        return schemaRegistryState.findObjectDefinitionByCompileTimeClass(compileTimeClass);
    }

    @Override
    public <O extends Objectable> PrismObjectDefinition<O> findObjectDefinitionByType(@NotNull QName typeName) {
        return schemaRegistryState.findObjectDefinitionByType(typeName);
    }

    //endregion

    //region Finding items (nonstandard cases)
    @Override
    public <T extends ItemDefinition> T findItemDefinitionByFullPath(Class<? extends Objectable> objectClass, Class<T> defClass,
            QName... itemNames)
            throws SchemaException {
        PrismObjectDefinition objectDefinition = findObjectDefinitionByCompileTimeClass(objectClass);
        if (objectDefinition == null) {
            throw new SchemaException("No object definition for " + objectClass);
        }
        ItemPath path = ItemPath.create((Object[]) itemNames);
        //noinspection unchecked
        return (T) ((ItemDefinition) objectDefinition).findItemDefinition(path, defClass);
    }

    @Override
    public ItemDefinition findItemDefinitionByElementName(QName elementName, @Nullable List<String> ignoredNamespaces) {
        if (StringUtils.isEmpty(elementName.getNamespaceURI())) {
            return schemaRegistryState.resolveGlobalItemDefinitionWithoutNamespace(
                    elementName.getLocalPart(), ItemDefinition.class, true, ignoredNamespaces);
        }
        PrismSchema schema = findSchemaByNamespace(elementName.getNamespaceURI());
        if (schema == null) {
            return null;
        }
        return schema.findItemDefinitionByElementName(elementName, ItemDefinition.class);
    }

    @Override
    public Class<? extends ObjectType> getCompileTimeClassForObjectType(QName objectType) {
        PrismObjectDefinition definition = findObjectDefinitionByType(objectType);
        if (definition == null) {
            return null;
        } else {
            //noinspection unchecked
            return definition.getCompileTimeClass();
        }
    }

    @Override
    public PrismObjectDefinition determineDefinitionFromClass(Class compileTimeClass) {
        //noinspection unchecked
        PrismObjectDefinition def = findObjectDefinitionByCompileTimeClass(compileTimeClass);
        if (def != null) {
            return def;
        }
        Class<?> superclass = compileTimeClass.getSuperclass();
        if (superclass == null || superclass == Object.class) {
            return null;
        }
        return determineDefinitionFromClass(superclass);
    }

    @Override
    public ItemDefinition locateItemDefinition(
            @NotNull QName itemName,
            @Nullable QName explicitTypeName,
            @Nullable ComplexTypeDefinition complexTypeDefinition,
            @Nullable Function<QName, ItemDefinition> dynamicDefinitionProvider) {
        if (complexTypeDefinition != null) {
            ItemDefinition<?> def = complexTypeDefinition.findLocalItemDefinition(itemName);
            if (def != null) {
                return def;
            }
        }
        // not sure about this: shouldn't extension schemas have xsdAnyMarker set?
        if (complexTypeDefinition == null || complexTypeDefinition.isXsdAnyMarker() || complexTypeDefinition.getExtensionForType() != null) {
            ItemDefinition<?> def = resolveGlobalItemDefinition(itemName, complexTypeDefinition);
            if (def != null) {
                // The definition must not contradict the declared type, like icfs:name (=string) in the case of
                // normalization-aware repository shadows.
                if (explicitTypeName == null || isAssignableFrom(def.getTypeName(), explicitTypeName)) {
                    return def;
                }
            }
        }
        if (dynamicDefinitionProvider != null) {
            return dynamicDefinitionProvider.apply(itemName);
        } else {
            return null;
        }
    }
    //endregion

    @Override
    public QName resolveUnqualifiedTypeName(QName type) throws SchemaException {
        return schemaRegistryState.resolveUnqualifiedTypeName(type);
    }

    @Override
    public QName qualifyTypeName(QName typeName) throws SchemaException {
        if (typeName == null || !QNameUtil.isUnqualified(typeName)) {
            return typeName;
        }
        return resolveUnqualifiedTypeName(typeName);
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

    @Override
    public PrismObjectDefinition determineReferencedObjectDefinition(@NotNull QName targetTypeName, ItemPath rest) {
        // TEMPORARY HACK -- TODO FIXME
        PrismObjectDefinition def = findObjectDefinitionByType(targetTypeName);
        if (def == null) {
            throw new IllegalStateException("Couldn't find definition for referenced object for " + targetTypeName + ", path=" + rest);
        }
        return def;    }

    @Override
    public ItemDefinition resolveGlobalItemDefinition(QName itemName, @Nullable ComplexTypeDefinition complexTypeDefinition) {
        if (QNameUtil.noNamespace(itemName)) {
            if (complexTypeDefinition != null && complexTypeDefinition.getDefaultNamespace() != null) {
                itemName = new QName(complexTypeDefinition.getDefaultNamespace(), itemName.getLocalPart());
            } else {
                List<String> ignoredNamespaces = complexTypeDefinition != null ?
                        complexTypeDefinition.getIgnoredNamespaces() :
                        null;
                return schemaRegistryState.resolveGlobalItemDefinitionWithoutNamespace(
                        itemName.getLocalPart(), ItemDefinition.class, true, ignoredNamespaces);
            }
        }
        PrismSchema schema = findSchemaByNamespace(itemName.getNamespaceURI());
        if (schema == null) {
            return null;
        }
        return schema.findItemDefinitionByElementName(itemName, ItemDefinition.class);
    }
    //endregion

    //region Finding schemas
    @Override
    public PrismSchema getPrismSchema(String namespace) {
        return schemaRegistryState.getPrismSchema(namespace);
    }

    @Override
    public Collection<PrismSchema> getSchemas() {
        return schemaRegistryState.getSchemas();
    }

    @Override
    public Collection<SchemaDescription> getSchemaDescriptions() {
        return getParsedSchemas().values();
    }

    @Override
    public PrismSchema findSchemaByCompileTimeClass(@NotNull Class<?> compileTimeClass) {
        return schemaRegistryState.findSchemaByCompileTimeClass(compileTimeClass);
    }

    @Override
    public PrismSchema findSchemaByNamespace(String namespaceURI) {
        return schemaRegistryState.findSchemaByNamespace(namespaceURI);
    }

    @Override
    public SchemaDescription findSchemaDescriptionByNamespace(String namespaceURI) {
        return schemaRegistryState.findSchemaDescriptionByNamespace(namespaceURI);
    }

    @Override
    public SchemaDescription findSchemaDescriptionByPrefix(String prefix) {
        return schemaRegistryState.findSchemaDescriptionByPrefix(prefix);
    }

    //endregion

    //region Misc
    @NotNull
    @Override
    public IsList isList(@Nullable QName xsiType, @NotNull QName elementName) {
        return schemaRegistryState.isList(xsiType, elementName);
    }

    public synchronized void setValueMetadataTypeName(QName typeName) {
        valueMetadataTypeName = typeName;
        valueMetadataDefinition = null;
    }

    @Override
    @NotNull
    public synchronized PrismContainerDefinition<?> getValueMetadataDefinition() {
        if (valueMetadataDefinition == null) {
            valueMetadataDefinition = resolveValueMetadataDefinition();
        }
        return valueMetadataDefinition;
    }

    private PrismContainerDefinition<?> resolveValueMetadataDefinition() {
        if (valueMetadataTypeName != null) {
            return Objects.requireNonNull(
                    schemaRegistryState.findContainerDefinitionByType(valueMetadataTypeName),
                    () -> "no definition for value metadata type " + valueMetadataTypeName);
        } else {
            return createDefaultValueMetadataDefinition();
        }
    }

    private PrismContainerDefinition<?> createDefaultValueMetadataDefinition() {
        var pcd = prismContext.definitionFactory().newContainerDefinitionWithoutTypeDefinition(
                DEFAULT_VALUE_METADATA_NAME, DEFAULT_VALUE_METADATA_TYPE_NAME);
        pcd.mutator().setMinOccurs(0);
        pcd.mutator().setMaxOccurs(1);
        return pcd;
    }
    //endregion

    //region TODO categorize

    /**
     * Answers the question: "If the receiver would get itemName without any other information, will it be able to
     * derive suitable typeName from it?" If not, we have to provide explicit type definition for serialization.
     * <p>
     * By suitable we mean such that can be used to determine specific object type.
     */
    @Override
    public boolean hasImplicitTypeDefinition(@NotNull QName itemName, @NotNull QName typeName) {
        List<ItemDefinition> definitions = findItemDefinitionsByElementName(itemName, ItemDefinition.class);
        if (definitions.size() != 1) {
            return false;
        }
        ItemDefinition definition = definitions.get(0);
        if (definition.isAbstract()) {
            return false;
        }
        // TODO other conditions?
        return definition.getTypeName().equals(typeName);
    }

    @Override
    public QName determineTypeForClass(Class<?> clazz) {
        if (XmlTypeConverter.canConvert(clazz)) {
            return XsdTypeMapper.toXsdType(clazz);
        } else {
            return ((PrismContextImpl) prismContext).getBeanMarshaller().determineTypeForClass(clazz);
        }
    }

    @Override
    public <T> Class<T> determineClassForType(QName type) {
        return schemaRegistryState.determineClassForType(type);
    }

    @Override
    public <T> Class<T> determineCompileTimeClass(QName type) {
        return schemaRegistryState.determineCompileTimeClass(type);
    }

    public <T> Class<T> determineCompileTimeClassInternal(QName type, boolean cacheAlsoNegativeResults) {
        return schemaRegistryState.determineCompileTimeClassInternal(type, cacheAlsoNegativeResults);
    }

    @Override
    public Class<?> determineClassForItemDefinition(ItemDefinition<?> itemDefinition) {
        if (itemDefinition instanceof PrismContainerDefinition) {
            Class<?> cls = ((PrismContainerDefinition) itemDefinition).getCompileTimeClass();
            if (cls != null) {
                return cls;
            }
        }
        return determineClassForType(itemDefinition.getTypeName());
    }

    @Override
    public <ID extends ItemDefinition> ID selectMoreSpecific(ID def1, ID def2)
            throws SchemaException {
        if (def1 == null) {
            return def2;
        }
        if (def2 == null) {
            return def1;
        }
        if (QNameUtil.match(def1.getTypeName(), def2.getTypeName())) {
            return def1;
        }
        Class<?> cls1 = determineClassForItemDefinition(def1);
        Class<?> cls2 = determineClassForItemDefinition(def2);
        if (cls1 == null || cls2 == null) {
            throw new SchemaException("Couldn't find more specific type from " + def1.getTypeName()
                    + " (" + cls1 + ") and " + def2.getTypeName() + " (" + cls2 + ")");
        }
        if (cls1.isAssignableFrom(cls2)) {
            return def2;
        }
        if (cls2.isAssignableFrom(cls1)) {
            return def1;
        }
        throw new SchemaException("Couldn't find more specific type from " + def1.getTypeName()
                + " (" + cls1 + ") and " + def2.getTypeName() + " (" + cls2 + ")");
    }

    @Override
    public QName selectMoreSpecific(QName type1, QName type2) {
        if (type1 == null) {
            return type2;
        }
        if (type2 == null) {
            return type1;
        }
        if (QNameUtil.match(type1, type2)) {
            return type1;
        }

        // These two checks are moved after type1/type2 comparison, in order to spare some QNameUtil.match calls
        if (QNameUtil.match(type1, DOMUtil.XSD_ANYTYPE)) {
            return type2;
        }
        if (QNameUtil.match(type2, DOMUtil.XSD_ANYTYPE)) {
            return type1;
        }

        Class<?> cls1 = determineClassForType(type1);
        Class<?> cls2 = determineClassForType(type2);
        if (cls1 == null || cls2 == null) {
            return null;
        }
        if (cls1.isAssignableFrom(cls2)) {
            return type2;
        }
        if (cls2.isAssignableFrom(cls1)) {
            return type1;
        }
        // poly string vs string FIXME is this correct? e.g., shouldn't there be '&&' instead of '||'?
        if (PolyStringType.class.equals(cls1) || String.class.equals(cls2)) {
            return type1;
        }
        if (PolyStringType.class.equals(cls2) || String.class.equals(cls1)) {
            return type2;
        }
        return null;
    }

    @Override
    public <ID extends ItemDefinition> ComparisonResult compareDefinitions(@NotNull ID def1, @NotNull ID def2) {
        if (QNameUtil.match(def1.getTypeName(), def2.getTypeName())) {
            return ComparisonResult.EQUAL;
        }
        Class<?> cls1 = determineClassForItemDefinition(def1);
        Class<?> cls2 = determineClassForItemDefinition(def2);
        if (cls1 == null || cls2 == null) {
            return ComparisonResult.NO_STATIC_CLASS;
        }
        boolean cls1AboveOrEqualCls2 = cls1.isAssignableFrom(cls2);
        boolean cls2AboveOrEqualCls1 = cls2.isAssignableFrom(cls1);
        if (cls1AboveOrEqualCls2 && cls2AboveOrEqualCls1) {
            return ComparisonResult.EQUAL;
        } else if (cls1AboveOrEqualCls2) {
            return ComparisonResult.SECOND_IS_CHILD;
        } else if (cls2AboveOrEqualCls1) {
            return ComparisonResult.FIRST_IS_CHILD;
        } else {
            return ComparisonResult.INCOMPATIBLE;
        }
    }

    @Override
    public boolean isAssignableFrom(@NotNull QName superType, @NotNull QName subType) {
        if (QNameUtil.match(superType, subType) || QNameUtil.match(DOMUtil.XSD_ANYTYPE, superType)) {
            return true;
        }
        if (QNameUtil.match(DOMUtil.XSD_ANYTYPE, subType)) {
            return false;
        }
        Class<?> superClass = determineClassForType(superType);
        // TODO consider implementing "strict mode" that would throw an exception in the case of nullness
        return superClass != null && isAssignableFrom(superClass, subType);
    }

    @Override
    public boolean isAssignableFromGeneral(@NotNull QName superType, @NotNull QName subType) {
        if (QNameUtil.match(superType, subType) || QNameUtil.match(DOMUtil.XSD_ANYTYPE, superType)) {
            return true;
        }
        if (QNameUtil.match(DOMUtil.XSD_ANYTYPE, subType)) {
            return false;
        }
        return isAssignableFromGeneral(superType, getSuperType(subType));
    }

    // TODO or should we return null if there is no explicit supertype?
    private QName getSuperType(QName subType) {
        TypeDefinition definition = MiscUtil.requireNonNull(findTypeDefinitionByType(subType),
                () -> new IllegalArgumentException("Unknown type " + subType));
        if (definition.getSuperType() != null) {
            return definition.getSuperType();
        } else {
            return DOMUtil.XSD_ANYTYPE;
        }
    }

    @Override
    public boolean isAssignableFrom(@NotNull Class<?> superClass, @NotNull QName subType) {
        Class<?> subClass = determineClassForType(subType);
        // TODO consider implementing "strict mode" that would throw an exception in the case of nullness
        return subClass != null && superClass.isAssignableFrom(subClass);
    }

    @Override
    public QName unifyTypes(QName type1, QName type2) {
        if (type1 == null) {
            return type2;
        } else if (type2 == null) {
            return type1;
        }

        if (isAssignableFrom(type1, type2)) {
            return type1;
        } else if (isAssignableFrom(type2, type1)) {
            return type2;
        } else {
            return null;
        }
    }

    @Override
    public boolean isContainerable(QName typeName) {
        Class<?> clazz = determineClassForType(typeName);
        return clazz != null && Containerable.class.isAssignableFrom(clazz);
    }

    @Override
    public ItemDefinition<?> createAdHocDefinition(QName elementName, QName typeName, int minOccurs, int maxOccurs) {
        Collection<? extends TypeDefinition> typeDefinitions = findTypeDefinitionsByType(typeName);
        if (typeDefinitions.isEmpty()) {
            // wild guess: create a prism property definition; maybe it will fit
            return createAdHocPropertyDefinition(elementName, typeName, minOccurs, maxOccurs);
        } else if (typeDefinitions.size() == 1) {
            TypeDefinition typeDefinition = typeDefinitions.iterator().next();
            if (typeDefinition instanceof SimpleTypeDefinition) {
                return createAdHocPropertyDefinition(elementName, typeName, minOccurs, maxOccurs);
            } else if (typeDefinition instanceof ComplexTypeDefinition ctd) {
                if (ctd.isObjectMarker()) {
                    return createAdHocObjectDefinition(elementName, ctd, minOccurs, maxOccurs);
                } else if (ctd.isContainerMarker()) {
                    return createAdHocContainerDefinition(elementName, ctd, minOccurs, maxOccurs);
                } else if (ctd.isReferenceMarker()) {
                    return createAdHocReferenceDefinition(elementName, ctd, minOccurs, maxOccurs);
                } else {
                    return createAdHocPropertyDefinition(elementName, typeName, minOccurs, maxOccurs);
                }
            } else {
                throw new IllegalStateException("Creation of ad-hoc definition from this type definition is not supported: " + typeDefinition);
            }
        } else {
            // TODO check if these definitions are compatible
            throw new IllegalStateException("More than one definition for type: " + typeName);
        }
    }

    private PrismPropertyDefinition<?> createAdHocPropertyDefinition(QName elementName, QName typeName, int minOccurs, int maxOccurs) {
        PrismPropertyDefinitionImpl<?> def = new PrismPropertyDefinitionImpl<>(elementName, typeName);
        def.setMinOccurs(minOccurs);
        def.setMaxOccurs(maxOccurs);
        return def;
    }

    private PrismReferenceDefinition createAdHocReferenceDefinition(QName elementName, ComplexTypeDefinition ctd, int minOccurs, int maxOccurs) {
        PrismReferenceDefinitionImpl def = new PrismReferenceDefinitionImpl(elementName, ctd.getTypeName());
        def.setMinOccurs(minOccurs);
        def.setMaxOccurs(maxOccurs);
        return def;
    }

    private PrismContainerDefinition<?> createAdHocContainerDefinition(
            QName elementName, @NotNull ComplexTypeDefinition ctd, int minOccurs, int maxOccurs) {
        return prismContext.definitionFactory().createContainerDefinition(elementName, ctd, minOccurs, maxOccurs);
    }

    private PrismObjectDefinition<?> createAdHocObjectDefinition(QName elementName, ComplexTypeDefinition ctd, int minOccurs, int maxOccurs) {
        PrismObjectDefinition<?> def = prismContext.definitionFactory().newObjectDefinition(elementName, ctd);
        def.mutator().setMinOccurs(minOccurs); // not much relevant for POD
        def.mutator().setMaxOccurs(maxOccurs); // not much relevant for POD
        return def;
    }
    //endregion

    public QName getValueMetadataTypeName() {
        return valueMetadataTypeName;
    }

    @Override
    public PrismNamespaceContext staticNamespaceContext() {
        PrismNamespaceContext ret = staticNamespaceContext;
        if (ret == null) {
            // Temporary version, initialize was not yet called on schema registry
            return staticPrefixes.build();
        }
        return ret;
    }

    @Override
    public void registerDynamicSchemaExtensions(Map<String, Element> dynamicSchemaExtensions) throws SchemaException {
        List<SchemaDescriptionImpl> registeredSchemasExtensions = new ArrayList<>();
        for (Entry<String, Element> entry : dynamicSchemaExtensions.entrySet()) {
            SchemaDescriptionImpl desc = SchemaDescriptionParser.parseNode(entry.getValue(), entry.getKey());
            desc.setRegistered();
            registeredSchemasExtensions.add(desc);

            addStaticPrefix(desc);
        }

        if (registeredSchemasExtensions.isEmpty()) {
            return;
        }

        this.dynamicSchemaExtensions = registeredSchemasExtensions;
    }

    public void registerStaticNamespace(String ns, String prefix, boolean declaredByDefault) {
        staticPrefixes.addPrefix(prefix, ns);
        customizeNamespacePrefixMapper(namespacePrefixMapper -> namespacePrefixMapper.registerPrefix(ns, prefix, declaredByDefault));
    }

    public List<TypeDefinition> getAllSubTypesByTypeDefinition(List<TypeDefinition> typeDefinitions) {
        TypeDefinition typeDefinition;
        List<TypeDefinition> subTypesAll = new ArrayList<>();
        List<TypeDefinition> subTypes = new ArrayList<>();

        // find subtypes for ObjectType
        List<TypeDefinition> objectSubTypes = new ArrayList<>();
        PrismObjectDefinition<?> objectTypeDefinition = findObjectDefinitionByType(prismContext.getDefaultReferenceTargetType());
        objectSubTypes.addAll(findTypeDefinitionByCompileTimeClass(objectTypeDefinition.getCompileTimeClass(), TypeDefinition.class).getStaticSubTypes());
        subTypes.addAll(objectSubTypes);

        while (!objectSubTypes.isEmpty()) {
            objectSubTypes.clear();

            for (TypeDefinition td : subTypes) {
                objectSubTypes.addAll(td.getStaticSubTypes().stream().toList());
            }

            subTypesAll.addAll(subTypes);
            subTypes.clear();
            subTypes.addAll(objectSubTypes);
        }

        objectSubTypes.addAll(subTypesAll);
        subTypesAll.clear();
        subTypes.clear();

        // find subtypes for other type
        for (TypeDefinition td : typeDefinitions) {
            if (objectSubTypes.contains(td)) {
                subTypesAll.addAll(objectSubTypes);
            } else {
                typeDefinition = findTypeDefinitionByCompileTimeClass(td.getCompileTimeClass(), TypeDefinition.class);
                subTypesAll.addAll(typeDefinition.getStaticSubTypes());
                subTypes.addAll(typeDefinition.getStaticSubTypes());
            }
        }

        if (!subTypes.isEmpty()) {
            subTypesAll.addAll(getAllSubTypesByTypeDefinition(subTypes));
        }

        return subTypesAll;
    }
}
