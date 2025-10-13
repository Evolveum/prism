/*
 * Copyright (C) 2010-2023 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.prism;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import javax.xml.namespace.QName;

import com.evolveum.midpoint.prism.match.MatchingRuleRegistry;
import com.evolveum.midpoint.prism.normalization.Normalizer;

import com.evolveum.midpoint.prism.schema.SchemaLookup;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.evolveum.midpoint.prism.crypto.Protector;
import com.evolveum.midpoint.prism.crypto.ProtectorCreator;
import com.evolveum.midpoint.prism.delta.DeltaFactory;
import com.evolveum.midpoint.prism.delta.ObjectDelta;
import com.evolveum.midpoint.prism.delta.builder.S_ItemEntry;
import com.evolveum.midpoint.prism.equivalence.EquivalenceStrategy;
import com.evolveum.midpoint.prism.marshaller.ParsingMigrator;
import com.evolveum.midpoint.prism.metadata.ValueMetadataFactory;
import com.evolveum.midpoint.prism.path.CanonicalItemPath;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.prism.path.UniformItemPath;
import com.evolveum.midpoint.prism.polystring.PolyStringNormalizer;
import com.evolveum.midpoint.prism.query.*;
import com.evolveum.midpoint.prism.query.builder.S_FilterEntry;
import com.evolveum.midpoint.prism.query.builder.S_FilterEntryOrEmpty;
import com.evolveum.midpoint.prism.schema.SchemaRegistry;
import com.evolveum.midpoint.prism.util.PrismMonitor;
import com.evolveum.midpoint.prism.xnode.RootXNode;
import com.evolveum.midpoint.prism.xnode.XNodeFactory;
import com.evolveum.midpoint.prism.xnode.XNodeMutator;
import com.evolveum.midpoint.util.annotation.Experimental;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.prism.xml.ns._public.types_3.ItemPathType;
import com.evolveum.prism.xml.ns._public.types_3.PolyStringNormalizerConfigurationType;

/**
 * @author semancik
 */
public interface PrismContext extends ProtectorCreator {

    String LANG_XML = "xml";
    String LANG_JSON = "json";
    String LANG_YAML = "yaml";

    /**
     * Initializes the prism context, e.g. loads and parses all the schemas.
     */
    void initialize() throws SchemaException, SAXException, IOException;
    void reload() throws SchemaException;

    void configurePolyStringNormalizer(PolyStringNormalizerConfigurationType configuration) throws ClassNotFoundException, InstantiationException, IllegalAccessException;

    /** Creates a configured poly string normalizer for the use by the client. Does not set anything in {@link PrismContext}. */
    @NotNull PolyStringNormalizer createConfiguredPolyStringNormalizer(PolyStringNormalizerConfigurationType configuration)
            throws InstantiationException, IllegalAccessException, ClassNotFoundException;

    /**
     * Returns the schema registry.
     */
    @NotNull SchemaRegistry getSchemaRegistry();

    @NotNull MatchingRuleRegistry getMatchingRuleRegistry();

    /**
     * Returns the default PolyString normalizer.
     */
    @NotNull PolyStringNormalizer getDefaultPolyStringNormalizer();

    /** TODO */
    <T> @NotNull Normalizer<T> getNoOpNormalizer();

    /**
     * Returns the default protector.
     */
    Protector getDefaultProtector();

    @NotNull
    QueryConverter getQueryConverter();

    //region Parsing

    /**
     * Creates a parser ready to process the given file.
     *
     * @param file File to be parsed.
     * @return Parser that can be invoked to retrieve the (parsed) content of the file.
     */
    @NotNull
    PrismParser parserFor(@NotNull File file);

    /**
     * Creates a parser ready to process data from the given input stream.
     *
     * @param stream Input stream to be parsed.
     * @return Parser that can be invoked to retrieve the (parsed) content of the input stream.
     */
    @NotNull
    PrismParser parserFor(@NotNull InputStream stream);

    /**
     * Creates a parser ready to process data from the given string.
     * Format/language of the data will be auto-detected, so the typically following
     * {@link PrismParser#language(String)} can be omitted.
     *
     * @param data String with the data to be parsed.
     * @return Parser that can be invoked to retrieve the (parsed) content.
     */
    @NotNull
    PrismParserNoIO parserFor(@NotNull String data);

    /**
     * Creates a parser ready to process data from the given XNode tree.
     *
     * @param xnode XNode tree with the data to be parsed.
     * @return Parser that can be invoked to retrieve the (parsed) content.
     */
    @NotNull
    PrismParserNoIO parserFor(@NotNull RootXNode xnode);

    /**
     * Creates a parser ready to process data from the given DOM element.
     *
     * @param element Element with the data to be parsed.
     * @return Parser that can be invoked to retrieve the (parsed) content.
     */
    @NotNull
    PrismParserNoIO parserFor(@NotNull Element element);

    @NotNull
    String detectLanguage(@NotNull File file) throws IOException;

    default <T extends Objectable> PrismObject<T> parseObject(File file) throws SchemaException, IOException {
        return parserFor(file).parse();
    }

    default <T extends Objectable> PrismObject<T> parseObject(String dataString) throws SchemaException {
        return parserFor(dataString).parse();
    }

    ParsingMigrator getParsingMigrator();

    void setParsingMigrator(ParsingMigrator migrator);

    //endregion

    //region Adopt methods
    <C extends Containerable> void adopt(PrismContainer<C> object, Class<C> declaredType) throws SchemaException;

    <T extends Containerable> void adopt(PrismContainer<T> object) throws SchemaException;

    void adopt(Objectable objectable) throws SchemaException;

    void adopt(Containerable containerable) throws SchemaException;

    void adopt(PrismContainerValue<?> value) throws SchemaException;

    <T extends Objectable> void adopt(ObjectDelta<T> delta) throws SchemaException;

    <C extends Containerable, O extends Objectable> void adopt(C containerable, Class<O> type, ItemPath path) throws SchemaException;

    <C extends Containerable, O extends Objectable> void adopt(PrismContainerValue<C> prismContainerValue, Class<O> type,
            ItemPath path) throws SchemaException;

    <C extends Containerable> void adopt(
            PrismContainerValue<C> prismContainerValue, QName typeName, ItemPath path)
            throws SchemaException;
    //endregion

    //region Serializing
    /**
     * Creates a serializer for the given language.
     *
     * @param language Language (like xml, json, yaml).
     * @return The serializer.
     */
    @NotNull
    PrismSerializer<String> serializerFor(@NotNull String language);

    /**
     * Creates a serializer for XML language.
     *
     * @return The serializer.
     */
    @NotNull
    PrismSerializer<String> xmlSerializer();

    /**
     * Creates a serializer for JSON language.
     *
     * @return The serializer.
     */
    @NotNull
    PrismSerializer<String> jsonSerializer();

    /**
     * Creates a serializer for YAML language.
     *
     * @return The serializer.
     */
    @NotNull
    PrismSerializer<String> yamlSerializer();

    /**
     * Creates a serializer for DOM. The difference from XML serializer is that XML produces String output
     * whereas this one produces a DOM Element.
     *
     * @return The serializer.
     */
    @NotNull
    PrismSerializer<Element> domSerializer();

    /**
     * Creates a serializer for XNode. The output of this serializer is intermediate XNode representation.
     *
     * @return The serializer.
     */
    @NotNull
    PrismSerializer<RootXNode> xnodeSerializer();

    //endregion

    /**
     * Creates a new PrismObject of a given type.
     *
     * @param clazz Static type of the object to be created.
     * @return New PrismObject.
     * @throws SchemaException If a definition for the given class couldn't be found.
     */
    @NotNull <O extends Objectable> PrismObject<O> createObject(@NotNull Class<O> clazz) throws SchemaException;

    /**
     * Creates a new Objectable of a given type.
     *
     * @param clazz Static type of the object to be created.
     * @return New PrismObject's objectable content.
     * @throws SchemaException If a definition for the given class couldn't be found.
     */
    @NotNull <O extends Objectable> O createObjectable(@NotNull Class<O> clazz) throws SchemaException;

    /**
     * Creates a new PrismObject of a given static type. It is expected that the type exists, so any SchemaExceptions
     * will be thrown as run-time exception.
     *
     * @param clazz Static type of the object to be created.
     * @return New PrismObject.
     */
    @NotNull <O extends Objectable> PrismObject<O> createKnownObject(@NotNull Class<O> clazz);

    /**
     * Creates a new Objectable of a given static type. It is expected that the type exists, so any SchemaExceptions
     * will be thrown as run-time exception.
     *
     * @param clazz Static type of the object to be created.
     * @return New PrismObject's objectable content.
     */
    @NotNull <O extends Objectable> O createKnownObjectable(@NotNull Class<O> clazz);

    PrismMonitor getMonitor();

    void setMonitor(PrismMonitor monitor);

    /**
     * If defined, it is considered to be the same as the relation of 'null'. Currently in midPoint, it is the value of org:default.
     */
    QName getDefaultRelation();

    void setDefaultRelation(QName name);

    /**
     * If defined, marks the 'multiple objects' element.
     */
    QName getObjectsElementName();

    /**
     * Type name for serialization of Referencable that's not of XML type (e.g. DefaultReferencableImpl).
     * In midPoint it's c:ObjectReferenceType.
     * <p>
     * VERY EXPERIMENTAL. Maybe we should simply use t:ObjectReferenceType in such cases.
     */
    @Experimental
    QName getDefaultReferenceTypeName();

    boolean isDefaultRelation(QName relation);

    /**
     * Define default reference target type for cases, when missing it in schema for reference item.
     */
    QName getDefaultReferenceTargetType();

    // TODO improve this method to avoid false positives when unqualified relations are defined (minor priority, as that's unsupported anyway)
    boolean relationsEquivalent(QName relation1, QName relation2);

    boolean relationMatches(QName relationQuery, QName relation);

    /**
     * Returns true of any of the relation in the relationQuery list matches specified relation.
     */
    boolean relationMatches(@NotNull List<QName> relationQuery, QName relation);

    /**
     * @return Name of the generic type for object/container extension (e.g. c:ExtensionType).
     */
    @Experimental
    QName getExtensionContainerTypeName();

    void setExtensionContainerTypeName(QName typeName);

    ParsingContext getDefaultParsingContext();

    ParsingContext createParsingContextForAllowMissingRefTypes();

    ParsingContext createParsingContextForCompatibilityMode();

    UniformItemPath emptyPath();

    UniformItemPath path(Object... namesOrIdsOrSegments);

    Hacks hacks();

    XNodeFactory xnodeFactory();

    XNodeMutator xnodeMutator();

    /**
     * Temporary
     */
    @NotNull
    UniformItemPath toUniformPath(ItemPath path);
    @Nullable
    UniformItemPath toUniformPathKeepNull(ItemPath path);
    UniformItemPath toUniformPath(ItemPathType path);
    default ItemPath toPath(ItemPathType path) {
        return path != null ? path.getItemPath() : null;
    }

    /**
     * Temporary
     */
    CanonicalItemPath createCanonicalItemPath(ItemPath itemPath, QName objectType);

    /**
     * Temporary
     */
    CanonicalItemPath createCanonicalItemPath(ItemPath itemPath);

    /** Starts a delta builder, with the default item definition resolution (i.e. from the system-wide schema). */
    <C extends Containerable> S_ItemEntry deltaFor(Class<C> objectClass) throws SchemaException;

    /**
     * Starts a delta builder, with a custom item definition resolver (e.g. for resource-specific deltas).
     * Usually not called directly from a general client code.
     */
    <C extends Containerable> S_ItemEntry deltaFor(Class<C> objectClass, ItemDefinitionResolver itemDefinitionResolver)
            throws SchemaException;

    /**
     * Starts a query builder with the goal of creating a query targeted at given object or container type.
     * The resolution of items (properties, references, containers) used in the query formulation is done by the default process,
     * i.e. from the system-wide schema.
     *
     * @param type The type of object or container values queried. This information is used to resolve the definitions of items
     * used in query formulation. It is _not_ meant to restrict the objects returned when the query is eventually applied.
     * If you want to restrict the type of objects returned right in the query (and not just when making e.g. the `searchObjects`
     * call), please consider using {@link S_FilterEntry#type(Class)} or {@link S_FilterEntry#type(QName)}.
     */
    S_FilterEntryOrEmpty queryFor(Class<? extends Containerable> type);

    /**
     * Starts a query builder, with a custom item definition resolver (e.g. for resource-specific queries).
     * Usually not called directly from a general client code.
     *
     * @see #queryFor(Class)
     */
    S_FilterEntryOrEmpty queryFor(Class<? extends Containerable> type, ItemDefinitionResolver itemDefinitionResolver);

    /**
     * Starts a query builder for reference search, with the default item definition resolution (i.e. from the system-wide schema).
     * After this call the mandatory owned-by filter based on the provided parameters is initiated.
     * The next step can be:
     *
     * * writing a filter (e.g. item+condition call) which will be interpreted as a nested owner filter;
     * * starting a block, which will, again, specify the nested owner filter;
     * * continuing with other filter using and/or (which finishes the owned-by filter) - unless a logical filter, the next filter should be a ref filter;
     * * or calling {@link S_FilterEntryOrEmpty#build()} to finish the query builder.
     */
    S_FilterEntryOrEmpty queryForReferenceOwnedBy(Class<? extends Containerable> ownerClass, ItemPath referencePath);

    /**
     * Access point to the "old" way of creating deltas. It is generally considered deprecated.
     * DeltaBuilder (accessed via deltaFor method) should be used instead.
     * <p>
     * However, because there is some functionality (like creation of empty deltas) that is not covered by the delta
     * builder, we keep this method not marked as deprecated. Only particular parts of DeltaFactory are marked as deprecated.
     */
    @NotNull
    DeltaFactory deltaFactory();

    /**
     * Access point to the "old" way of creating queries, filters and paging instructions.
     * It is generally considered deprecated. QueryBuilder (accessed via queryFor method) should be used instead.
     * <p>
     * However, because there is some functionality (like creation of standalone paging instructions) that is not covered
     * by the query builder, we keep this method not marked as deprecated. Only particular parts of QueryFactory are marked
     * as deprecated.
     */
    @NotNull
    QueryFactory queryFactory();

    @NotNull
    ItemFactory itemFactory();

    @NotNull
    DefinitionFactory definitionFactory();

    @NotNull
    ItemPathParser itemPathParser();

    @NotNull ItemPathSerializer itemPathSerializer();

    // TEMPORARY/EXPERIMENTAL
    @Experimental
    void setExtraValidation(boolean value);

    @Experimental
    void setValueMetadataFactory(ValueMetadataFactory factory);

    @Experimental
    ValueMetadataFactory getValueMetadataFactory();

    @Experimental
    EquivalenceStrategy getProvenanceEquivalenceStrategy();

    /**
     *
     * @return Prism Query Language Parser with static default namespaces declared
     */
    default PrismQueryLanguageParser createQueryParser() {
        return createQueryParser(getSchemaRegistry().staticNamespaceContext().allPrefixes());
    }

    void registerQueryExpressionFactory(PrismQueryExpressionFactory factory);

    @Experimental
    void registerValueBasedDefinitionLookup(ValueBasedDefinitionLookupHelper lookup);

    @Experimental
    Collection<ValueBasedDefinitionLookupHelper> valueBasedDefinitionLookupsForType(QName typeName);

    PrismQueryLanguageParser createQueryParser(Map<String, String> prefixToNamespace);

    PrismQuerySerializer querySerializer();

    @NotNull
    ItemMergerFactory itemMergerFactory();

    static PrismContext get() {
        return PrismService.get().prismContext();
    }

    default SchemaLookup.Mutable getDefaultSchemaLookup() {
        return getSchemaRegistry().getCurrentLookup();
    }
}
