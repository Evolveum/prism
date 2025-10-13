/*
 * Copyright (c) 2010-2018 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.prism.impl.schema;

import static com.evolveum.midpoint.prism.PrismConstants.*;
import static com.evolveum.midpoint.prism.SimpleTypeDefinition.DerivationMethod.*;
import static com.evolveum.midpoint.prism.impl.schema.SchemaProcessorUtil.getAnnotationElement;
import static com.evolveum.midpoint.prism.impl.schema.annotation.Annotation.*;
import static com.evolveum.midpoint.prism.impl.schema.features.DefinitionFeatures.*;
import static com.evolveum.midpoint.prism.impl.schema.features.DefinitionFeatures.XsomParsers.*;
import static com.evolveum.midpoint.util.MiscUtil.*;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;
import javax.xml.namespace.QName;

import com.evolveum.midpoint.prism.ComplexTypeDefinition.ComplexTypeDefinitionLikeBuilder;
import com.evolveum.midpoint.prism.Definition.DefinitionBuilder;
import com.evolveum.midpoint.prism.ItemDefinition.ItemDefinitionLikeBuilder;
import com.evolveum.midpoint.prism.PrismPropertyDefinition.PrismPropertyLikeDefinitionBuilder;
import com.evolveum.midpoint.prism.PrismReferenceDefinition.PrismReferenceDefinitionBuilder;
import com.evolveum.midpoint.prism.SimpleTypeDefinition.SimpleTypeDefinitionBuilder;

import com.evolveum.midpoint.prism.impl.schema.features.IsAnyXsomParser.IsAny;

import com.evolveum.midpoint.prism.schema.*;

import com.sun.xml.xsom.*;
import jakarta.xml.bind.annotation.XmlEnumValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Element;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.impl.*;
import com.evolveum.midpoint.prism.impl.schema.features.EnumerationValuesInfoXsomParser.EnumValueInfo;
import com.evolveum.midpoint.prism.path.ItemName;
import com.evolveum.midpoint.prism.xml.XmlTypeConverter;
import com.evolveum.midpoint.prism.xml.XsdTypeMapper;
import com.evolveum.midpoint.util.DOMUtil;
import com.evolveum.midpoint.util.DisplayableValue;
import com.evolveum.midpoint.util.MiscUtil;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;

import org.xml.sax.Locator;

/**
 * Parses {@link PrismSchema} represented in XSOM.
 *
 * @author lazyman
 * @author Radovan Semancik
 */
class SchemaXsomParser {

    private static final Trace LOGGER = TraceManager.getTrace(SchemaXsomParser.class);

    /** Schema XSOM model - the source for parsing. */
    @NotNull private final XSSchemaSet xsSchemaSet;

    /** The schema being created - the target for parsing. */
    @NotNull private final Map<String, SchemaBuilder> schemaBuilderMap;

    /** See {@link PrismSchemaImpl#delayedItemDefinitions}. Used when parsing interconnected set of schemas. */
    private final boolean allowDelayedItemDefinitions;

    /** For diagnostics. */
    private final String shortDescription;

    private final PrismContextImpl prismContext = (PrismContextImpl) PrismContext.get();

    private final SchemaRegistry schemaRegistry = prismContext.getSchemaRegistry();

    private final DefinitionFactoryImpl definitionFactory = prismContext.definitionFactory();

    private final SchemaRegistryState schemaRegistryState;

    SchemaXsomParser(
            @NotNull XSSchemaSet xsSchemaSet,
            @NotNull Collection<SchemaBuilder> schemaBuilders,
            boolean allowDelayedItemDefinitions,
            String shortDescription,
            @Nullable SchemaRegistryState schemaRegistryState) {
        this.xsSchemaSet = xsSchemaSet;
        this.schemaBuilderMap = schemaBuilders.stream()
                .collect(Collectors.toMap(
                        SchemaBuilder::getNamespace,
                        sb -> sb,
                        (sb1, sb2) -> {
                            throw new IllegalStateException("Duplicate schema builder for namespace " + sb1.getNamespace());
                        }
                ));
        this.allowDelayedItemDefinitions = allowDelayedItemDefinitions;
        this.shortDescription = shortDescription;
        this.schemaRegistryState = schemaRegistryState;
    }

    /** Executes the parsing process. Should be called only once per class instantiation. */
    void parseSchema() throws SchemaException {
        parseTopLevelComplexTypeDefinitions(); // parses top level <complexType> elements
        parseTopLevelSimpleTypeDefinitions(); // parses top level <simpleType> elements
        parseTopLevelItemDefinitions(); // parses top level <element> elements
    }

    /**
     * Create {@link ComplexTypeDefinition}s from all top-level `complexType` definitions in the XSD.
     * These definitions are the reused later to fit inside item definitions.
     */
    private void parseTopLevelComplexTypeDefinitions() throws SchemaException {
        Iterator<XSComplexType> iterator = xsSchemaSet.iterateComplexTypes();
        while (iterator.hasNext()) {
            XSComplexType sourceComplexType = iterator.next();
            SchemaBuilder target = getTargetSchema(sourceComplexType);
            if (target != null) {
                LOGGER.trace("### processing complexType {} into {} [{}]", sourceComplexType, target, shortDescription);
                parseComplexTypeDefinition(sourceComplexType);
            }
        }
    }

    private SchemaBuilder getTargetSchema(XSType type) {
        return getTargetSchema(type.getTargetNamespace());
    }

    private SchemaBuilder getTargetSchema(String namespace) {
        return schemaBuilderMap.get(namespace);
    }

    /**
     * Creates {@link ComplexTypeDefinition} object from a single XSD complexType definition.
     * It may be top-level or embedded in a different XSOM item.
     *
     * As a side effect, it is inserted into the appropriate schema builder (as top-level CTD).
     */
    private void parseComplexTypeDefinition(XSComplexType sourceComplexType) throws SchemaException {

        String localTypeName = sourceComplexType.getName();
        QName typeName = new QName(sourceComplexType.getTargetNamespace(), localTypeName);

        var existingDef = findTypeDefinitionHere(typeName);
        if (existingDef != null) {
            return;
        }

        var target = getTargetSchema(sourceComplexType);
        if (target == null) {
            LOGGER.trace("Skipping complex type {} in namespace {} because there is no schema for that namespace",
                    localTypeName, sourceComplexType.getTargetNamespace());
            return;
        }

        ComplexTypeDefinitionLikeBuilder ctdBuilder = target.newComplexTypeDefinitionLikeBuilder(localTypeName);

        // Add to the schema right now to avoid loops - even if it is not complete yet.
        // The definition may reference itself, directly or indirectly; during the parsing process we will look it up.
        target.add(ctdBuilder);

        ctdBuilder.setAbstract(sourceComplexType.isAbstract());

        boolean isExtension = DF_EXTENSION_REF.parse(ctdBuilder, sourceComplexType) != null;

        // We need to setup the compile time class after "extensionRef" is determine, because it depends on it for extensions
        // CTDs (they have no declared static type, but their type is effectively ExtensionType.class or analogous).
        // But also, we need to know it before the sub-items are parsed, as they can recursively reference this CTD.
        // So we do it here.
        setupCompileTimeClass(target, ctdBuilder);

        var explicitSuperType = XsomParsers.DF_SUPERTYPE_PARSER.getValue(sourceComplexType);
        if (explicitSuperType != null) {
            ctdBuilder.setSuperType(explicitSuperType);
        } else if (isExtension) {
            // All extension containers have a subtype of c:ExtensionType, unless explicitly overridden.
            ctdBuilder.setSuperType(prismContext.getExtensionContainerTypeName());
        }

        if (isExtension) {
            ctdBuilder.setContainerMarker(true); // this is the default for extension containers
        } else {
            DF_CONTAINER_MARKER.parse(ctdBuilder, sourceComplexType);
        }
        DF_OBJECT_MARKER.parse(ctdBuilder, sourceComplexType);
        DF_REFERENCE_MARKER.parse(ctdBuilder, sourceComplexType);
        DF_LIST_MARKER.parse(ctdBuilder, sourceComplexType);

        MERGER.parseIfApplicable(ctdBuilder, sourceComplexType.getAnnotation());
        NATURAL_KEY.parseIfApplicable(ctdBuilder, sourceComplexType.getAnnotation());

        DF_SCHEMA_MIGRATIONS.parse(ctdBuilder, sourceComplexType);
        DF_DIAGRAMS.parse(ctdBuilder, sourceComplexType);

        SCHEMA_CONTEXT.parseIfApplicable(ctdBuilder, sourceComplexType.getAnnotation());

        XSContentType content = sourceComplexType.getContentType();
        if (content != null) {
            XSParticle particle = content.asParticle();
            if (particle != null) {
                var modelGroup = particle.getTerm().asModelGroup();
                if (modelGroup != null) { // contained items ("elements" in XML language)
                    // "explicit content" is the delta against base type, if there's a base type
                    XSContentType explicitContent = sourceComplexType.getExplicitContent();
                    Boolean inherited = explicitContent == null || content == explicitContent ? false : null;
                    parseItemDefinitionsFromGroup(target, ctdBuilder, modelGroup, inherited, explicitContent);
                }
            }
        }

        markRuntime(target, ctdBuilder);

        DF_INSTANTIATION_ORDER.parse(ctdBuilder, sourceComplexType);
        DF_DEFAULT_ITEM_TYPE_NAME.parse(ctdBuilder, sourceComplexType);
        DF_DEFAULT_REFERENCE_TARGET_TYPE_NAME.parse(ctdBuilder, sourceComplexType);
        DF_DEFAULT_NAMESPACE.parse(ctdBuilder, sourceComplexType);
        DF_IGNORED_NAMESPACES.parse(ctdBuilder, sourceComplexType);
        DF_IS_ANY_XSD.parse(ctdBuilder, sourceComplexType);
        DF_DOCUMENTATION.parse(ctdBuilder, sourceComplexType);
        DF_DISPLAY_NAME.parse(ctdBuilder, sourceComplexType.getAnnotation());
        DF_HELP.parse(ctdBuilder, sourceComplexType.getAnnotation());

        parseXmlAttributes(ctdBuilder, sourceComplexType);

        for (var extraFeature : ctdBuilder.getExtraFeaturesToParse()) {
            extraFeature
                    .asForBuilder(ComplexTypeDefinitionLikeBuilder.class)
                    .parse(ctdBuilder, sourceComplexType);
        }
    }

    // FIXME this horrible hack
    private static void setupCompileTimeClass(SchemaBuilder schemaBuilder, DefinitionBuilder defBuilder) {
        if (schemaBuilder instanceof PrismSchemaImpl prismSchema) {
            prismSchema.setupCompileTimeClass((TypeDefinition) defBuilder.getObjectBuilt());
        }
    }

    private void parseXmlAttributes(ComplexTypeDefinitionLikeBuilder ctdBuilder, XSComplexType complexType) throws SchemaException {
        for (XSAttributeUse attributeUse : complexType.getAttributeUses()) {
            var attributeDecl = attributeUse.getDecl();
            ItemName name = ItemName.from(ctdBuilder.getTypeName().getNamespaceURI(), attributeDecl.getName());
            QName type = getTypeNameRequired(attributeDecl.getType());
            var attributeDef = new PrismPropertyDefinitionImpl<>(name, type, null);
            attributeDef.mutator().setMinOccurs(0);
            attributeDef.mutator().setMaxOccurs(1);
            ctdBuilder.addXmlAttributeDefinition(attributeDef);
        }
    }

    private void parseTopLevelSimpleTypeDefinitions() throws SchemaException {
        Iterator<XSSimpleType> iterator = xsSchemaSet.iterateSimpleTypes();
        while (iterator.hasNext()) {
            XSSimpleType simpleType = iterator.next();
            var target = getTargetSchema(simpleType);
            if (target != null) {
                LOGGER.trace("### processing STD {} into {} [{}]", simpleType, target, shortDescription);
                processTopSimpleTypeDefinition(target, simpleType);
            }
        }
    }

    private void processTopSimpleTypeDefinition(SchemaBuilder target, XSSimpleType simpleType) throws SchemaException {

        QName typeName = new QName(simpleType.getTargetNamespace(), simpleType.getName());
        var existing = target.findTypeDefinitionByType(typeName);
        if (existing != null) {
            schemaCheck(existing instanceof SimpleTypeDefinition,
                    "Simple type %s already defined as %s", typeName, existing);
            return; // We already have this in schema. So avoid redundant work
        }

        SimpleTypeDefinitionBuilder stdBuilder = createSimpleTypeDefinitionBuilder(typeName, simpleType);

        markRuntime(target, stdBuilder);

        DF_SUPERTYPE.parse(stdBuilder, simpleType);
        DF_INSTANTIATION_ORDER.parse(stdBuilder, simpleType);
        DF_DOCUMENTATION.parse(stdBuilder, simpleType);
        DF_DISPLAY_NAME.parse(stdBuilder, simpleType.getAnnotation());
        DF_HELP.parse(stdBuilder,simpleType.getAnnotation());

        // Here will be extra features parsing, when needed.

        setupCompileTimeClass(target, stdBuilder);

        target.add(stdBuilder);
    }

    /** Returns a builder; but currently it is always a "full" definition. */
    private SimpleTypeDefinitionImpl createSimpleTypeDefinitionBuilder(QName typeName, XSSimpleType simpleType) {
        XSType baseType = simpleType.getBaseType();
        QName baseTypeName = baseType != null ? new QName(baseType.getTargetNamespace(), baseType.getName()) : null;
        var enumerationValues = XsomParsers.DF_ENUMERATION_VALUES_PARSER.getValue(simpleType);
        if (enumerationValues != null) {
            return new EnumerationTypeDefinitionImpl(typeName, baseTypeName, enumerationValues);
        } else {
            return new SimpleTypeDefinitionImpl(
                    typeName,
                    baseTypeName,
                    switch (simpleType.getDerivationMethod()) {
                        case XSSimpleType.EXTENSION -> EXTENSION;
                        case XSSimpleType.RESTRICTION -> RESTRICTION;
                        case XSSimpleType.SUBSTITUTION -> SUBSTITUTION;
                        default -> null; // TODO are combinations allowed? e.g. EXTENSION+SUBSTITUTION?
                    });
        }
    }

    /**
     * Creates {@link ComplexTypeDefinition} object from a XSModelGroup inside XSD complexType definition.
     *
     * This is a recursive method. The embedded sequences, choices and "all" elements are added to the
     * target {@link ComplexTypeDefinition} in a recursive manner.
     *
     * @param targetCtdBuilder ComplexTypeDefinition that will hold the definitions
     * @param sourceGroup XSD XSModelGroup
     * @param inherited Are these properties inherited? (null means we don't know and
     * we'll determine that from explicitContent)
     * @param explicitContent Explicit (i.e. non-inherited) content of the type being parsed
     * - filled-in only for subtypes!
     */
    private void parseItemDefinitionsFromGroup(
            SchemaBuilder target, ComplexTypeDefinitionLikeBuilder targetCtdBuilder, XSModelGroup sourceGroup,
            Boolean inherited, XSContentType explicitContent) throws SchemaException {

        for (XSParticle particle : sourceGroup.getChildren()) {
            boolean particleInherited = inherited != null ? inherited : particle != explicitContent;
            XSTerm term = particle.getTerm();
            if (term.isModelGroup()) { // sequence, choice, all (sometimes does happen)
                parseItemDefinitionsFromGroup(target, targetCtdBuilder, term.asModelGroup(), particleInherited, explicitContent);
            } else if (term.isElementDecl()) { // xs:element inside complex type (the standard case)
                parseItemDefinition(target, targetCtdBuilder, particle, particleInherited);
            }
        }
    }

    private void parseItemDefinition(
            SchemaBuilder target,
            ComplexTypeDefinitionLikeBuilder ctdBuilder, XSParticle particle, boolean particleInherited)
            throws SchemaException {

        XSElementDecl element = particle.getTerm().asElementDecl();
        QName elementName = new QName(element.getTargetNamespace(), element.getName());
        XSType elementType = element.getType(); // this is the "type" attribute, i.e. the XSD type this xsd:element refers to

        // Via "a:type", one can override the type of the element. The XS type (if even present) is not relevant in such case.
        QName overriddenTypeName = DF_TYPE_OVERRIDE_PROCESSOR.getValue(particle);
        boolean typeIsUnknown = elementType.getName() == null && overriddenTypeName == null;

        @Nullable XSAnnotation annotation = selectAnnotationToUse(particle.getAnnotation(), element.getAnnotation());

        boolean embeddedObject = isObject(elementType) && isEmbeddedObject(annotation);

        if (isReference(annotation) || isReference(elementType)) {

            parseReferenceDefinition(
                    ctdBuilder, particle, elementType, elementName, annotation, particleInherited);

        } else if (isObject(elementType) && !embeddedObject && ctdBuilder.isContainerMarker()) {

            // Skipping any ObjectType object embedded by value, unless any of the following occurs:
            //
            // 1. The object is marked as embedded; this is quite a special case - please do not confuse this
            //    with the composite object reference!
            //
            // 2. The parent is NOT a container. This occurs in a special cases like ObjectListType, ResourceObjectShadowListType,
            //    FindShadowOwnerResponseType, where the object presence is intentional, and must be preserved.
            //
            // (Currently, the ObjectType items should not appear in containers anyway!)

        } else if (typeIsUnknown) {

            IsAny isAny = XsomParsers.DF_IS_ANY_XSD_PARSER.getValue(elementType);
            if (isAny != null) {
                if (isContainer(element)) {
                    addToCtd(ctdBuilder, particleInherited,
                            createContainerOrObjectDefinition(target, ctdBuilder, particle, null));
                } else {
                    // "any" inside non-container (i.e., plain structured) CTD
                    addToCtd(ctdBuilder, particleInherited,
                            createPropertyDefinition(target, ctdBuilder, particle, elementType, elementName, DOMUtil.XSD_ANY, annotation));
                }
            } else {
                // Skipping type-less non-any item.
            }

        } else if (isContainer(element) || embeddedObject) {

            // Create an inner PrismContainerDefinition (never PrismObjectDefinition - unless we aren't a container!).
            // It is assumed that this is a XSD complex type.

            var itemCtd = determineCtd(elementName, elementType, overriddenTypeName);
            addToCtd(ctdBuilder, particleInherited,
                    createContainerOrObjectDefinition(target, ctdBuilder, particle, itemCtd));

        } else {

            // Create a property definition (even if this is a XSD complex type)
            QName typeName = new QName(elementType.getTargetNamespace(), elementType.getName());
            addToCtd(ctdBuilder, particleInherited,
                    createPropertyDefinition(target, ctdBuilder, particle, elementType, elementName, typeName, annotation));
        }
    }

    private @NotNull AbstractTypeDefinition determineCtd(QName elementName, XSType elementType, QName overriddenType)
            throws SchemaException {
        QName actualTypeName = getTypeNameRequired(elementType);
        if (overriddenType != null && !overriddenType.equals(actualTypeName)) {
            // There is a type override annotation. The source "elementType" is useless. We need to locate our own CTD.
            var maybe = findTypeDefinitionHereAndEverywhere(overriddenType);
            if (maybe != null) {
                return maybe;
            }
            // Not found, let us parse it and try to find again.
            var target = getTargetSchema(overriddenType.getNamespaceURI());
            if (target != null) {
                var source =
                        MiscUtil.requireNonNull(
                                xsSchemaSet.getComplexType(overriddenType.getNamespaceURI(), overriddenType.getLocalPart()),
                                "Cannot find definition of complex type %s as specified in type override annotation at %s",
                                overriddenType, elementName);
                parseComplexTypeDefinition(source);
                var maybeAgain = findTypeDefinitionHereAndEverywhere(overriddenType);
                if (maybeAgain != null) {
                    return maybeAgain; // should indeed be the case!
                }
            }
            throw new SchemaException("Cannot find definition of complex type " + overriddenType
                    + " as specified in type override annotation at " + elementName);
        }

        var maybe = findTypeDefinitionHereAndEverywhere(actualTypeName);
        if (maybe != null) {
            return maybe;
        }

        parseComplexTypeDefinition((XSComplexType) elementType);

        var maybeAgain = findTypeDefinitionHereAndEverywhere(actualTypeName);
        if (maybeAgain != null) {
            return maybeAgain;
        }

        throw new IllegalStateException("Newly created complex type definition not found in the schema: " + actualTypeName);
    }

    private void addToCtd(ComplexTypeDefinitionLikeBuilder ctdBuilder, boolean particleInherited, ItemDefinitionLikeBuilder child) {
        child.setInherited(particleInherited);
        ctdBuilder.add(child);
    }

    private ItemDefinitionLikeBuilder parseReferenceDefinition(
            @Nullable ComplexTypeDefinitionLikeBuilder targetCtdBuilder,
            XSParticle elementParticle, XSType xsType, QName elementName, XSAnnotation annotation,
            boolean inherited) throws SchemaException {

        QName typeName = new QName(xsType.getTargetNamespace(), xsType.getName());

        PrismReferenceDefinitionBuilder prdBuilder = null;
        if (targetCtdBuilder != null) {
            if (targetCtdBuilder.getObjectBuilt() instanceof ComplexTypeDefinition ctdBuilt) {
                prdBuilder = (PrismReferenceDefinitionImpl) // TODO does this find anything?
                        ctdBuilt.findItemDefinition(ItemName.fromQName(elementName), PrismReferenceDefinition.class);
            }
        }
        if (prdBuilder == null) {
            var definedInType = targetCtdBuilder != null ? targetCtdBuilder.getTypeName() : null;
            prdBuilder = new PrismReferenceDefinitionImpl(elementName, typeName, definedInType);
            prdBuilder.setInherited(inherited);
            if (targetCtdBuilder != null) {
                targetCtdBuilder.add(prdBuilder);
            }
        }

        parseMultiplicity(prdBuilder, elementParticle, annotation, false);

        OBJECT_REFERENCE_TARGET_TYPE.parseIfApplicable(prdBuilder, annotation);
        DF_COMPOSITE_MARKER.parse(prdBuilder, annotation);

        parseItemDefinitionAnnotations(prdBuilder, annotation);

        DF_INDEXED.parse(prdBuilder, annotation);

        // Here will be extra features parsing, when needed.

        return prdBuilder;
    }

    private void parseMultiplicity(
            PrismItemBasicDefinition.Mutable itemDef, XSParticle particle, XSAnnotation annotation, boolean topLevel) {
        if (topLevel || particle == null) {
            itemDef.setMinOccurs(0);
            Element maxOccursAnnotation = getAnnotationElement(annotation, A_MAX_OCCURS);
            if (maxOccursAnnotation != null) {
                itemDef.setMaxOccurs(
                        XsdTypeMapper.multiplicityToInteger(
                                maxOccursAnnotation.getTextContent()));
            } else {
                itemDef.setMaxOccurs(-1);
            }
        } else {
            itemDef.setMinOccurs(particle.getMinOccurs().intValue());
            itemDef.setMaxOccurs(particle.getMaxOccurs().intValue());
        }
    }

    /**
     * Create PropertyContainer (and possibly also Property) definition from the
     * top-level elements in XSD. Each top-level element will be interpreted as
     * a potential PropertyContainer. The element name will be set as name of
     * the PropertyContainer, element type will become type (indirectly through
     * ComplexTypeDefinition).
     *
     * No need to recurse here. All the work was already done while creating
     * ComplexTypeDefinitions.
     */
    private void parseTopLevelItemDefinitions() throws SchemaException {
        Iterator<XSElementDecl> iterator = xsSchemaSet.iterateElementDecls();
        while (iterator.hasNext()) {
            XSElementDecl xsElementDecl = iterator.next();
            if (DF_DEPRECATED.hasMark(xsElementDecl)) {
                // Safe to ignore. We want it in the XSD schema only. The real definition will be parsed
                // from the non-deprecated variant.
                // FIXME ... but we are not ignoring anything here!
            }
            var target = getTargetSchema(xsElementDecl.getTargetNamespace());
            if (target == null) {
                // The top-level element is from a different namespace. We ignore it.
                continue;
            }

            QName elementName = new QName(xsElementDecl.getTargetNamespace(), xsElementDecl.getName());
            LOGGER.trace("### processing item {} into {} [{}]", elementName, target, shortDescription);
            XSType xsType = xsElementDecl.getType();
            if (xsType == null) {
                throw new SchemaException("Found element " + elementName + " without type definition");
            }
            QName typeQName = determineType(xsElementDecl);
            if (typeQName == null) {
                continue; // No type defined, safe to skip
            }
            XSAnnotation annotation = xsElementDecl.getAnnotation();
            ItemDefinitionLikeBuilder itemDefBuilder;

            if (isContainer(xsElementDecl) || isObject(xsType)) {
                var typeDefinition = findTypeDefinitionHereAndEverywhere(typeQName);
                if (typeDefinition == null) {
                    if (!allowDelayedItemDefinitions) {
                        throw new SchemaException("Couldn't parse prism container " + elementName + " of type " + typeQName
                                + " because complex type definition couldn't be found and delayed item definitions are not allowed.");
                    }
                    itemDefBuilder = null; // the item definition is delayed
                    target.addDelayedItemDefinition(
                            (ItemDefinitionSupplier) () -> {
                                // parent CTD is null, because we are at the top leve
                                var ctd = SchemaXsomParser.this.findTypeDefinitionHereAndEverywhere(typeQName);
                                // here we take the risk that ctd is null
                                var builder = SchemaXsomParser.this.createContainerOrObjectDefinition(
                                        target, null, xsType, xsElementDecl, ctd, annotation, null,
                                        VIRTUAL_SCHEMA_ROOT, false, true);
                                // Non-prism definitions are never added as delayed item definitions.
                                return castSafely(builder.getObjectBuilt(), ItemDefinition.class);
                            });
                } else {
                    itemDefBuilder = createContainerOrObjectDefinition(
                            target, null, xsType, xsElementDecl, typeDefinition, annotation, null, PrismConstants.VIRTUAL_SCHEMA_ROOT, false, true);
                }
            } else if (isReference(xsType)) {
                itemDefBuilder = parseReferenceDefinition(null, null, xsType, elementName, annotation, false);
            } else {
                // Create a top-level property definition (even if this is a XSD complex type)
                itemDefBuilder = createPropertyDefinition(
                        target, null, null, xsType, elementName, typeQName, annotation);
            }
            if (itemDefBuilder != null) {
                itemDefBuilder.setSubstitutionHead(
                        getSubstitutionHead(xsElementDecl));
                target.add(itemDefBuilder);
            }
        }
    }

    // We first try to find the definition locally, because in schema registry we don't have the current schema yet.
    private AbstractTypeDefinition findTypeDefinitionHereAndEverywhere(QName typeQName) throws SchemaException {
        var local = findTypeDefinitionHere(typeQName);
        if (local != null) {
            return local;
        }
        var target = getTargetSchema(typeQName.getNamespaceURI());
        if (target != null) {
            var maybe = target.findTypeDefinitionByType(typeQName);
            if (maybe != null) {
                return maybe;
            }
        }
        // Last attempt, maybe the definition is already in the registry
        if (schemaRegistryState == null) {
            return schemaRegistry.findTypeDefinitionByType(typeQName);
        }
        return schemaRegistryState.findTypeDefinitionByType(typeQName);
    }

    private QName getSubstitutionHead(XSElementDecl element) {
        XSElementDecl head = element.getSubstAffiliation();
        if (head == null) {
            return null;
        } else {
            return new QName(head.getTargetNamespace(), head.getName());
        }
    }

    private QName determineType(XSElementDecl xsElementDecl) throws SchemaException {
        // Check for a:type annotation. If present, this overrides the type
        QName type = DF_TYPE_OVERRIDE_PROCESSOR.getValue(xsElementDecl);
        if (type != null) {
            return type;
        }
        XSType xsType = xsElementDecl.getType();
        if (xsType == null) {
            return null;
        }
        return getTypeName(xsType);
    }

    private QName getTypeNameRequired(XSType xsType) throws SchemaException {
        return requireNonNull(
                getTypeName(xsType),
                () -> "Unnamed type " + xsType + " @" + location(xsType));
    }

    private QName getTypeName(XSType xsType) {
        if (xsType.getName() != null) {
            return new QName(xsType.getTargetNamespace(), xsType.getName());
        } else {
            return null;
        }
    }

    private String location(XSComponent component) {
        Locator locator = component.getLocator();
        if (locator == null) {
            return "unknown location";
        }
        return locator.getSystemId() + ":" + locator.getLineNumber() + "," + locator.getColumnNumber();
    }

    private boolean isContainer(XSElementDecl xsElementDecl) {
        return XsomParsers.DF_CONTAINER_MARKER_PROCESSOR.hasMark(xsElementDecl)
                || XsomParsers.DF_CONTAINER_MARKER_PROCESSOR.hasMark(xsElementDecl.getType());
    }

    private boolean isReference(XSAnnotation annotation) {
        return XsomParsers.DF_REFERENCE_MARKER_PROCESSOR.hasMark(annotation);
    }

    private boolean isReference(XSType xsType) {
        return XsomParsers.DF_REFERENCE_MARKER_PROCESSOR.hasMark(xsType);
    }

    private static boolean isObject(XSType xsType) {
        return XsomParsers.DF_OBJECT_MARKER_PROCESSOR.hasMark(xsType);
    }

    private static boolean isEmbeddedObject(XSAnnotation annotation) {
        return DF_EMBEDDED_OBJECT_MARKER_PROCESSOR.hasMark(annotation);
    }

    /**
     * Creates appropriate instance of {@link PrismContainerDefinition} or {@link PrismObjectDefinition}.
     * This method also takes care of parsing all the annotations and similar fancy stuff.
     */
    private ItemDefinitionLikeBuilder createContainerOrObjectDefinition(
            SchemaBuilder target,
            @NotNull ComplexTypeDefinitionLikeBuilder ctdBuilder,
            @NotNull XSParticle elementParticle,
            @Nullable AbstractTypeDefinition elementTypeDef) throws SchemaException {
        @NotNull XSType elementType = elementParticle.getTerm().asElementDecl().getType();
        return createContainerOrObjectDefinition(
                target,
                ctdBuilder,
                elementType,
                elementParticle.getTerm().asElementDecl(),
                elementTypeDef,
                elementType.getAnnotation(),
                elementParticle,
                ctdBuilder.getTypeName(),
                ctdBuilder.isContainerMarker(),
                false);
    }

    /**
     * @param containerInsteadOfObject If true, we never create a {@link PrismObjectDefinition} (mainly because we
     * cannot have object inside a container)
     */
    private ItemDefinitionLikeBuilder createContainerOrObjectDefinition(
            SchemaBuilder target,
            ComplexTypeDefinitionLikeBuilder ctdBuilder,
            XSType xsType,
            XSElementDecl elementDecl,
            AbstractTypeDefinition typeDefinition,
            XSAnnotation annotation,
            XSParticle elementParticle,
            QName definedInType,
            boolean containerInsteadOfObject,
            boolean objectMultiplicityIsOne) throws SchemaException {

        QName elementName = new QName(elementDecl.getTargetNamespace(), elementDecl.getName());

        boolean isObject = isObject(xsType);
        ItemDefinitionLikeBuilder pcdBuilder;
        if (isObject && !containerInsteadOfObject) {
            pcdBuilder = ctdBuilder != null ?
                    ctdBuilder.newObjectLikeDefinition(elementName, typeDefinition) :
                    definitionFactory.newObjectDefinition(elementName, (ComplexTypeDefinition) typeDefinition);
        } else {
            pcdBuilder = ctdBuilder != null ?
                    ctdBuilder.newContainerLikeDefinition(elementName, typeDefinition) :
                    definitionFactory.newContainerDefinition(elementName, (ComplexTypeDefinition) typeDefinition);
        }

        if (isObject && objectMultiplicityIsOne) {
            pcdBuilder.setMinOccurs(1);
            pcdBuilder.setMaxOccurs(1);
        } else {
            parseMultiplicity(pcdBuilder, elementParticle, elementDecl.getAnnotation(), PrismConstants.VIRTUAL_SCHEMA_ROOT.equals(definedInType));
        }

        markRuntime(target, pcdBuilder);

        ALWAYS_USE_FOR_EQUALS.parseIfApplicable(pcdBuilder, annotation);

        // This is quite a wild attempt to collect annotations from somewhere (provided by the caller),
        // the element declaration (relevant in the case of "xsd:element ref" constructs), and the element particle.
        // We must take care not to overwrite the feature values by each other. We rely on the fact that there should be
        // no "default values", i.e. the parser should return non-null value only if there is something really present there.
        parseItemDefinitionAnnotations(pcdBuilder, annotation);
        parseItemDefinitionAnnotations(pcdBuilder, elementDecl.getAnnotation());
        if (elementParticle != null) {
            parseItemDefinitionAnnotations(pcdBuilder, elementParticle.getAnnotation());
        }

        return pcdBuilder;
    }

    /**
     * Creates appropriate instance of PropertyDefinition. It creates either
     * PropertyDefinition itself or one of its subclasses
     * (ResourceObjectAttributeDefinition). The behavior depends of the "mode"
     * of the schema. This method is also processing annotations and other fancy
     * property-relates stuff.
     */
    private <T> PrismPropertyLikeDefinitionBuilder<T> createPropertyDefinition(
            @NotNull SchemaBuilder target,
            @Nullable ComplexTypeDefinitionLikeBuilder targetCtd,
            XSParticle elementParticle, XSType xsType, QName elementName, QName typeName,
            XSAnnotation annotation)
            throws SchemaException {

        PrismPropertyLikeDefinitionBuilder<T> ppdBuilder = targetCtd != null ?
                targetCtd.newPropertyLikeDefinition(elementName, typeName) :
                definitionFactory.newPropertyDefinition(elementName, typeName);

        //noinspection unchecked
        ppdBuilder.setDefaultValue(
                (T) parseDefaultValue(elementParticle, typeName));
        //noinspection unchecked
        ppdBuilder.setAllowedValues(
                (Collection<? extends DisplayableValue<T>>) parseEnumAllowedValues(targetCtd, typeName, xsType));

        parseMultiplicity(ppdBuilder, elementParticle, annotation, targetCtd == null);

        markRuntime(target, ppdBuilder);

        parseItemDefinitionAnnotations(ppdBuilder, annotation);

        DF_INDEXED.parse(ppdBuilder, annotation);
        DF_INDEX_ONLY.parse(ppdBuilder, annotation);
        DF_MATCHING_RULE.parse(ppdBuilder, annotation);
        DF_VALUE_ENUMERATION_REF.parse(ppdBuilder, annotation);

        return ppdBuilder;
    }

    private Object parseDefaultValue(XSParticle elementParticle, QName typeName) {
        if (elementParticle == null) {
            return null;
        }
        XSTerm term = elementParticle.getTerm();
        if (term == null) {
            return null;
        }

        XSElementDecl elementDecl = term.asElementDecl();
        if (elementDecl == null) {
            return null;
        }
        if (elementDecl.getDefaultValue() != null) {
            if (XmlTypeConverter.canConvert(typeName)) {
                return XmlTypeConverter.toJavaValue(elementDecl.getDefaultValue().value, typeName);
            }
            return elementDecl.getDefaultValue().value;
        }
        return null;
    }

    private Collection<? extends DisplayableValue<?>> parseEnumAllowedValues(
            @Nullable ComplexTypeDefinitionLikeBuilder ctdBuilder, QName typeName, XSType xsType) {
        List<EnumValueInfo> valuesInfo = XsomParsers.DF_ENUMERATION_VALUES_INFO_PROCESSOR.getValue(xsType);
        if (valuesInfo == null) {
            return null;
        }
        Collection<DisplayableValue<?>> rv = new ArrayList<>();
        for (EnumValueInfo info : valuesInfo) {
            Object adjustedValue =
                    ctdBuilder != null && !ctdBuilder.isRuntimeSchema() ?
                            adjustEnumValueFromStaticInfo(typeName, info.value()) : info.value();
            rv.add(new DisplayableValueImpl<>(adjustedValue, info.label(), info.documentation()));
        }
        return rv.isEmpty() ? null : List.copyOf(rv);
    }

    private Object adjustEnumValueFromStaticInfo(QName typeName, Object original) {
        Class<?> compileTimeClass;
        if (schemaRegistryState == null) {
            compileTimeClass = prismContext.getSchemaRegistry().getCompileTimeClass(typeName);
        } else {
            compileTimeClass = schemaRegistryState.determineCompileTimeClass(typeName);
        }

        if (compileTimeClass == null) {
            return original;
        }
        for (Field field : compileTimeClass.getDeclaredFields()) {
            XmlEnumValue xmlEnumValue = field.getAnnotation(XmlEnumValue.class);
            if (xmlEnumValue != null && xmlEnumValue.value() != null && xmlEnumValue.value().equals(original)) {
                //noinspection unchecked,rawtypes
                return Enum.valueOf((Class<Enum>) compileTimeClass, field.getName());
            }
        }
        return original;
    }

    private void parseItemDefinitionAnnotations(ItemDefinitionLikeBuilder builder, XSAnnotation sourceAnnotation)
            throws SchemaException {
        if (sourceAnnotation == null || sourceAnnotation.getAnnotation() == null) {
            return;
        }

        parseAllAnnotations(builder, sourceAnnotation);

        DF_ACCESS.parse(builder, sourceAnnotation);
        DF_DOCUMENTATION.parse(builder, sourceAnnotation);
        DF_SCHEMA_MIGRATIONS.parse(builder, sourceAnnotation);
        DF_DIAGRAMS.parse(builder, sourceAnnotation);

        for (var extraFeature : builder.getExtraFeaturesToParse()) {
            extraFeature
                    .asForBuilder(ItemDefinitionLikeBuilder.class)
                    .parse(builder, sourceAnnotation);
        }
    }

    private XSAnnotation selectAnnotationToUse(XSAnnotation particleAnnotation, XSAnnotation termAnnotation) {
        // It is not quite clear when particle vs. term annotation is present.
        // So let's do the selection intuitively.
        if (particleAnnotation == null || particleAnnotation.getAnnotation() == null) {
            return termAnnotation;
        } else if (termAnnotation == null || termAnnotation.getAnnotation() == null) {
            return particleAnnotation;
        } else {
            // both are non-null; let's decide by appinfo presence
            return hasAnnotationAppinfo(particleAnnotation) ? particleAnnotation : termAnnotation;
        }
    }

    private boolean hasAnnotationAppinfo(XSAnnotation annotation) {
        return getAnnotationElement(annotation, SCHEMA_APP_INFO) != null;
    }

    private void markRuntime(SchemaBuilder target, Object def) {
        if (target.isRuntime()) {
            // It is interesting, but the same definition can be processed multiple times here,
            // with different values of `isRuntime`. So we want to make a logical OR of them.
            // FIXME research this

            if (def instanceof ComplexTypeDefinitionLikeBuilder builder) {
                builder.setRuntimeSchema(true);
            } else if (def instanceof Definition definition) {
                definition.mutator().setRuntimeSchema(true);
            } else {
                //throw new IllegalStateException("Not applicable to: " + def);
            }
        }
    }

    private AbstractTypeDefinition findTypeDefinitionHere(QName typeName) throws SchemaException {
        var target = getTargetSchema(typeName.getNamespaceURI());
        if (target == null) {
            return null;
        }
        return target.findTypeDefinitionByType(typeName);
    }
}
