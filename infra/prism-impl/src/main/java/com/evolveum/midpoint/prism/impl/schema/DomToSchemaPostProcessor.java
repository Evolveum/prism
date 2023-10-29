/*
 * Copyright (c) 2010-2018 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism.impl.schema;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.annotation.DiagramElementFormType;
import com.evolveum.midpoint.prism.annotation.DiagramElementInclusionType;
import com.evolveum.midpoint.prism.annotation.ItemDiagramSpecification;
import com.evolveum.midpoint.prism.impl.*;
import com.evolveum.midpoint.prism.path.ItemName;
import com.evolveum.midpoint.prism.schema.MutablePrismSchema;
import com.evolveum.midpoint.prism.schema.SchemaRegistry;
import com.evolveum.midpoint.prism.xml.XmlTypeConverter;
import com.evolveum.midpoint.prism.xml.XsdTypeMapper;
import com.evolveum.midpoint.util.DOMUtil;
import com.evolveum.midpoint.util.DisplayableValue;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;
import com.sun.xml.xsom.*;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import jakarta.xml.bind.annotation.XmlEnumValue;
import javax.xml.namespace.QName;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static com.evolveum.midpoint.prism.PrismConstants.*;

/**
 * Parser for DOM-represented XSD, creates midPoint Schema representation.
 *
 * It will parse schema in several passes. It has special handling if the schema
 * is "resource schema", which will create ResourceObjectDefinition and
 * ResourceObjectAttributeDefinition instead of PropertyContainer and Property.
 *
 * @author lazyman
 * @author Radovan Semancik
 */
class DomToSchemaPostProcessor {

    private static final Trace LOGGER = TraceManager.getTrace(DomToSchemaPostProcessor.class);


    private static final String ENUMERATION = "enumeration";


    private final XSSchemaSet xsSchemaSet;
    private final PrismContext prismContext;
    private MutablePrismSchema schema;
    private String shortDescription;
    private boolean isRuntime;
    private boolean allowDelayedItemDefinitions;

    DomToSchemaPostProcessor(XSSchemaSet xsSchemaSet, PrismContext prismContext) {
        this.xsSchemaSet = xsSchemaSet;
        this.prismContext = prismContext;
    }

    private SchemaRegistry getSchemaRegistry() {
        return this.prismContext.getSchemaRegistry();
    }

    private SchemaDefinitionFactory getDefinitionFactory() {
        return ((PrismContextImpl) prismContext).getDefinitionFactory();
    }

    private String getNamespace() {
        return schema.getNamespace();
    }

    private boolean isMyNamespace(QName qname) {
        return getNamespace().equals(qname.getNamespaceURI());
    }

    /**
     * Main entry point.
     */
    void postprocessSchema(MutablePrismSchema prismSchema, boolean isRuntime, boolean allowDelayedItemDefinitions, String shortDescription) throws SchemaException {
        this.schema = prismSchema;
        this.isRuntime = isRuntime;
        this.allowDelayedItemDefinitions = allowDelayedItemDefinitions;
        this.shortDescription = shortDescription;

        // Create ComplexTypeDefinitions from all top-level complexType
        // definition in the XSD
        processComplexTypeDefinitions(xsSchemaSet);

        // Create SimpleTypeDefinitions from all top-level simpleType
        // definition in the XSD
        processSimpleTypeDefinitions(xsSchemaSet);

        // Create PropertyContainer (and possibly also Property) definition from
        // the top-level elements in XSD
        // This also creates ResourceObjectDefinition in some cases
        createDefinitionsFromElements(xsSchemaSet);
    }

    /**
     * Create ComplexTypeDefinitions from all top-level complexType definitions
     * in the XSD.
     *
     * These definitions are the reused later to fit inside PropertyContainer
     * definitions.
     *
     * @param set
     *            XS Schema Set
     */
    private void processComplexTypeDefinitions(XSSchemaSet set) throws SchemaException {
        Iterator<XSComplexType> iterator = set.iterateComplexTypes();
        while (iterator.hasNext()) {
            XSComplexType complexType = iterator.next();
            if (complexType.getTargetNamespace().equals(schema.getNamespace())) {
                LOGGER.trace("### processing CTD {} into {} [{}]", complexType, schema, shortDescription);
                processComplexTypeDefinition(complexType);
            }
        }
    }

    private ComplexTypeDefinition getOrProcessComplexType(QName typeName) throws SchemaException {
        ComplexTypeDefinition complexTypeDefinition = schema.findComplexTypeDefinitionByType(typeName);
        if (complexTypeDefinition != null) {
            return complexTypeDefinition;
        }
        // The definition is not yet processed (or does not exist). Let's try to
        // process it.
        XSComplexType complexType = xsSchemaSet.getComplexType(typeName.getNamespaceURI(),
                typeName.getLocalPart());
        return processComplexTypeDefinition(complexType);
    }

    /**
     * Creates ComplexTypeDefinition object from a single XSD complexType
     * definition.
     *
     * @param complexType
     *            XS complex type definition
     */
    private ComplexTypeDefinition processComplexTypeDefinition(XSComplexType complexType)
            throws SchemaException {

        SchemaDefinitionFactory definitionFactory = getDefinitionFactory();
        MutableComplexTypeDefinition ctd = definitionFactory.createComplexTypeDefinition(complexType, prismContext, complexType.getAnnotation());

        ComplexTypeDefinition existingComplexTypeDefinition = schema.findComplexTypeDefinitionByType(ctd.getTypeName());
        if (existingComplexTypeDefinition != null) {
            // We already have this in schema. So avoid redundant work and
            // infinite loops;
            return existingComplexTypeDefinition;
        }
        // Add to the schema right now to avoid loops - even if it is not
        // complete yet
        // The definition may reference itself
        schema.add(ctd);

        XSContentType content = complexType.getContentType();
        XSContentType explicitContent = complexType.getExplicitContent();
        if (content != null) {
            XSParticle particle = content.asParticle();


            // We need this in order for properly distinguish between containers and plain complex types
            if (isPropertyContainer(complexType)) {
                ctd.setContainerMarker(true);
            }

            if (particle != null) {
                XSTerm term = particle.getTerm();

                if (term.isModelGroup()) {
                    Boolean inherited = null;
                    if (explicitContent == null || content == explicitContent) {
                        inherited = false;
                    }
                    addItemDefinitionListFromGroup(term.asModelGroup(), ctd, inherited, explicitContent);
                }
            }

            XSAnnotation annotation = complexType.getAnnotation();
            Element extensionAnnotationElement = SchemaProcessorUtil.getAnnotationElement(annotation, A_EXTENSION);
            if (extensionAnnotationElement != null) {
                QName extensionType = DOMUtil.getQNameAttribute(extensionAnnotationElement,
                        A_EXTENSION_REF.getLocalPart());
                if (extensionType == null) {
                    throw new SchemaException("The " + A_EXTENSION + "annotation on " + ctd.getTypeName()
                            + " complex type does not have " + A_EXTENSION_REF.getLocalPart() + " attribute",
                            A_EXTENSION_REF);
                }
                ctd.setContainerMarker(true);
                ctd.setExtensionForType(extensionType);
                ctd.setSuperType(prismContext.getExtensionContainerTypeName());
            }

            parseSchemaMigrations(ctd, annotation);
            parseDiagrams(ctd, annotation);
        }

        markRuntime(ctd);

        if (complexType.isAbstract()) {
            ctd.setAbstract(true);
        }

        QName superType = determineSupertype(complexType);
        if (superType != null) {
            ctd.setSuperType(superType);
        }

        setInstantiationOrder(ctd, complexType.getAnnotation());

        if (isObjectDefinition(complexType)) {
            ctd.setObjectMarker(true);
        }
        if (isObjectReference(complexType)) {
            ctd.setReferenceMarker(true);
        }

        ctd.setDefaultNamespace(getDefaultNamespace(complexType));
        ctd.setIgnoredNamespaces(getIgnoredNamespaces(complexType));

        if (isAny(complexType, Optional.empty())) {
            ctd.setXsdAnyMarker(true);
            if (isAny(complexType, Optional.of(XSWildcard.STRTICT))) {
                ctd.setStrictAnyMarker(true);
            }

        }

        if (isList(complexType)) {
            ctd.setListMarker(true);
        }

        extractDocumentation(ctd, complexType.getAnnotation());

        Class<?> compileTimeClass = getSchemaRegistry().determineCompileTimeClass(ctd.getTypeName());
        ctd.setCompileTimeClass(compileTimeClass);
        schema.registerCompileTimeClass(compileTimeClass, ctd);

        definitionFactory.finishComplexTypeDefinition(ctd, complexType, prismContext,
                complexType.getAnnotation());

        // Attempt to create object or container definition from this complex
        // type

        PrismContainerDefinition<?> defFromComplexType = getDefinitionFactory()
                .createExtraDefinitionFromComplexType(complexType, ctd, prismContext,
                        complexType.getAnnotation());

        if (defFromComplexType != null) {
            markRuntime(defFromComplexType);
            schema.add(defFromComplexType);
        }

        parseAttributes(ctd, complexType);

        return ctd;

    }

    private void parseAttributes(MutableComplexTypeDefinition ctd, XSComplexType complexType) throws SchemaException {
        // TODO Auto-generated method stub
        List<PrismPropertyDefinition<?>> definitions = new ArrayList<>();
        for(XSAttributeUse attributeUse : complexType.getAttributeUses()) {

            var attributeDecl = attributeUse.getDecl();
            ItemName name = new ItemName(ctd.getTypeName().getNamespaceURI(), attributeDecl.getName());
            QName type = getType(attributeDecl.getType());
            var attributeDef = getDefinitionFactory().createPropertyDefinition(name, type, null, prismContext, null,  null);
            attributeDef.toMutable().setMinOccurs(0);
            attributeDef.toMutable().setMaxOccurs(1);
            definitions.add(attributeDef);
        }
        ctd.setAttributeDefinitions(definitions);

    }

    private void setInstantiationOrder(MutableTypeDefinition typeDefinition, XSAnnotation annotation) throws SchemaException {
        typeDefinition.setInstantiationOrder(
                SchemaProcessorUtil.getAnnotationInteger(annotation, A_INSTANTIATION_ORDER));
    }

    private void processSimpleTypeDefinitions(XSSchemaSet set) throws SchemaException {
        Iterator<XSSimpleType> iterator = set.iterateSimpleTypes();
        while (iterator.hasNext()) {
            XSSimpleType simpleType = iterator.next();
            if (simpleType.getTargetNamespace().equals(schema.getNamespace())) {
                LOGGER.trace("### processing STD {} into {} [{}]", simpleType, schema, shortDescription);
                processSimpleTypeDefinition(simpleType);
            }
        }
    }

    private SimpleTypeDefinition processSimpleTypeDefinition(XSSimpleType simpleType)
            throws SchemaException {

        SimpleTypeDefinitionImpl std;
        SchemaDefinitionFactory definitionFactory = getDefinitionFactory();
        if (isEnumeration(simpleType)) {
            std = definitionFactory.createEnumerationTypeDefinition(simpleType, prismContext,
                    simpleType.getAnnotation());


        } else {
            std = (SimpleTypeDefinitionImpl) definitionFactory.createSimpleTypeDefinition(simpleType, prismContext,
                    simpleType.getAnnotation());
        }

        SimpleTypeDefinition existingSimpleTypeDefinition = schema.findSimpleTypeDefinitionByType(std.getTypeName());
        if (existingSimpleTypeDefinition != null) {
            // We already have this in schema. So avoid redundant work
            return existingSimpleTypeDefinition;
        }
        markRuntime(std);

        QName superType = determineSupertype(simpleType);
        if (superType != null) {
            std.setSuperType(superType);
        }

        setInstantiationOrder(std, simpleType.getAnnotation());

        extractDocumentation(std, simpleType.getAnnotation());

        if (getSchemaRegistry() != null) {
            Class<?> compileTimeClass = getSchemaRegistry().determineCompileTimeClass(std.getTypeName());
            std.setCompileTimeClass(compileTimeClass);
        }

        schema.add(std);
        return std;
    }

    private boolean isEnumeration(XSSimpleType simpleType) {
        if (simpleType instanceof XSRestrictionSimpleType) {
            XSRestrictionSimpleType restType = (XSRestrictionSimpleType) simpleType;
            Collection<? extends XSFacet> facets = restType.getDeclaredFacets();
            if (facets.isEmpty() || restType.getDerivationMethod() != XSType.RESTRICTION) {
                return false;
            }
            return facets.stream().allMatch(f -> ENUMERATION.equals(f.getName()));
        }
        return false;
    }

    private void extractDocumentation(Definition definition, XSAnnotation annotation) {
        if (annotation == null) {
            return;
        }
        Element documentationElement = SchemaProcessorUtil.getAnnotationElement(annotation, DOMUtil.XSD_DOCUMENTATION_ELEMENT);
        if (documentationElement != null) {
            // The documentation may be HTML-formatted. Therefore we want to
            // keep the formatting and tag names
            String documentationText = DOMUtil.serializeElementContent(documentationElement);
            definition.toMutable().setDocumentation(documentationText);
        }
    }

    /**
     * Creates ComplexTypeDefinition object from a XSModelGroup inside XSD
     * complexType definition. This is a recursive method. It can create
     * "anonymous" internal PropertyContainerDefinitions. The definitions will
     * be added to the ComplexTypeDefinition provided as parameter.
     *
     * @param group
     *            XSD XSModelGroup
     * @param ctd
     *            ComplexTypeDefinition that will hold the definitions
     * @param inherited
     *            Are these properties inherited? (null means we don't know and
     *            we'll determine that from explicitContent)
     * @param explicitContent
     *            Explicit (i.e. non-inherited) content of the type being parsed
     *            - filled-in only for subtypes!
     */
    private void addItemDefinitionListFromGroup(XSModelGroup group, MutableComplexTypeDefinition ctd,
            Boolean inherited, XSContentType explicitContent) throws SchemaException {

        XSParticle[] particles = group.getChildren();
        for (XSParticle particle : particles) {
            boolean particleInherited = inherited != null ? inherited : particle != explicitContent;
            XSTerm pterm = particle.getTerm();
            if (pterm.isModelGroup()) {
                addItemDefinitionListFromGroup(pterm.asModelGroup(), ctd, particleInherited, explicitContent);
            }

            // xs:element inside complex type
            if (pterm.isElementDecl()) {
                XSAnnotation annotation = selectAnnotationToUse(particle.getAnnotation(), pterm.getAnnotation());

                XSElementDecl elementDecl = pterm.asElementDecl();
                QName elementName = new QName(elementDecl.getTargetNamespace(), elementDecl.getName());
                QName typeFromAnnotation = getTypeAnnotation(particle.getAnnotation());

                XSType xsType = elementDecl.getType();

                boolean embeddedObject =
                        isObjectDefinition(xsType)
                                && SchemaProcessorUtil.getAnnotationElement(annotation, A_EMBEDDED_OBJECT) != null;

                if (isObjectReference(xsType, annotation)) {

                    processObjectReferenceDefinition(
                            xsType, elementName, annotation, ctd, particle, particleInherited);

                } else if (ctd.isContainerMarker() && isObjectDefinition(xsType) && !embeddedObject) {

                    // We can not skip properties with object type in case of non-containers complex types
                    // (this should not affect normal container behaviour, where we skip them in favour of *Ref)

                    // In case of structural complex types (types used as basic PrismPropertyValue), these
                    // needs to be present in order to correctly generate binding code
                    // e.g ObjectListType, ResourceObjectShadowListType, FindShadowOwnerResponseType

                } else if (xsType.getName() == null && typeFromAnnotation == null) {

                    if (isAny(xsType, Optional.empty())) {
                        if (isPropertyContainer(elementDecl)) {
                            XSAnnotation containerAnnotation = xsType.getAnnotation();
                            PrismContainerDefinition<?> containerDefinition = createContainerOrObjectDefinition(
                                    xsType,
                                    particle,
                                    null,
                                    containerAnnotation,
                                    ctd.getTypeName(),
                                    ctd.isContainerMarker()
                            );
                            containerDefinition.toMutable().setInherited(particleInherited);
                            ctd.add(containerDefinition);
                        } else {
                            MutablePrismPropertyDefinition<?> propDef =
                                    createPropertyDefinition(xsType, elementName, DOMUtil.XSD_ANY, ctd, annotation, particle);
                            propDef.setInherited(particleInherited);
                            ctd.add(propDef);
                        }
                    }

                } else if (isPropertyContainer(elementDecl) || embeddedObject) {

                    // Create an inner PrismContainerDefinition (never PrismObjectDefinition - unless we aren't a container!).
                    // It is assumed that this is a XSD complex type.

                    XSComplexType complexType = (XSComplexType) xsType;
                    ComplexTypeDefinition complexTypeDefinition;
                    if (typeFromAnnotation != null && !typeFromAnnotation.equals(getType(xsType))) {
                        // There is a type override annotation. The type that
                        // the schema parser determined is useless
                        // We need to locate our own complex type definition
                        if (isMyNamespace(typeFromAnnotation)) {
                            complexTypeDefinition = getOrProcessComplexType(typeFromAnnotation);
                        } else {
                            complexTypeDefinition = prismContext.getSchemaRegistry()
                                    .findComplexTypeDefinitionByType(typeFromAnnotation);
                        }
                        if (complexTypeDefinition == null) {
                            throw new SchemaException(
                                    "Cannot find definition of complex type " + typeFromAnnotation
                                            + " as specified in type override annotation at " + elementName);
                        }
                    } else {
                        complexTypeDefinition = processComplexTypeDefinition(complexType);
                    }
                    XSAnnotation containerAnnotation = complexType.getAnnotation();
                    PrismContainerDefinition<?> containerDefinition = createContainerOrObjectDefinition(
                            xsType,
                            particle,
                            complexTypeDefinition,
                            containerAnnotation,
                            ctd.getTypeName(),
                            ctd.isContainerMarker()
                    );
                    containerDefinition.toMutable().setInherited(particleInherited);
                    ctd.add(containerDefinition);

                } else {

                    // Create a property definition (even if this is a XSD complex type)
                    QName typeName = new QName(xsType.getTargetNamespace(), xsType.getName());

                    MutablePrismPropertyDefinition<?> propDef =
                            createPropertyDefinition(xsType, elementName, typeName, ctd, annotation, particle);
                    propDef.setInherited(particleInherited);
                    ctd.add(propDef);
                }
            }
        }
    }

    private PrismReferenceDefinitionImpl processObjectReferenceDefinition(XSType xsType, QName elementName,
            XSAnnotation annotation, ComplexTypeDefinition containingCtd, XSParticle elementParticle,
            boolean inherited) throws SchemaException {
        QName typeName = new QName(xsType.getTargetNamespace(), xsType.getName());
        QName primaryElementName = elementName;
        Element objRefAnnotationElement = SchemaProcessorUtil.getAnnotationElement(annotation,
                A_OBJECT_REFERENCE);
        boolean hasExplicitPrimaryElementName = (objRefAnnotationElement != null
                && !StringUtils.isEmpty(objRefAnnotationElement.getTextContent()));
        if (hasExplicitPrimaryElementName) {
            primaryElementName = DOMUtil.getQNameValue(objRefAnnotationElement);
        }
        PrismReferenceDefinitionImpl definition = null;
        if (containingCtd != null) {
            definition = (PrismReferenceDefinitionImpl) containingCtd.findItemDefinition(ItemName.fromQName(primaryElementName), PrismReferenceDefinition.class);
        }
        if (definition == null) {
            SchemaDefinitionFactory definitionFactory = getDefinitionFactory();
            definition = (PrismReferenceDefinitionImpl) definitionFactory.createReferenceDefinition(primaryElementName, typeName,
                    containingCtd, prismContext, annotation, elementParticle);
            definition.setInherited(inherited);
            if (containingCtd != null) {
                containingCtd.toMutable().add(definition);
            }
        }
        if (hasExplicitPrimaryElementName) {
            // The elements that have explicit type name determine the target
            // type name (if not yet set)
            if (definition.getTargetTypeName() == null) {
                definition.setTargetTypeName(typeName);
            }
            if (definition.getCompositeObjectElementName() == null) {
                definition.setCompositeObjectElementName(elementName);
            }
        } else {
            // The elements that use default element names override type
            // definition
            // as there can be only one such definition, therefore the behavior
            // is deterministic
            definition.setTypeName(typeName);
        }
        Annotation.processAnnotation(definition, annotation, Annotation.OBJECT_REFERENCE_TARGET_TYPE);

        Element targetTypeAnnotationElement = SchemaProcessorUtil.getAnnotationElement(annotation,
                A_OBJECT_REFERENCE_TARGET_TYPE);
        if (targetTypeAnnotationElement != null
                && !StringUtils.isEmpty(targetTypeAnnotationElement.getTextContent())) {
            // Explicit definition of target type overrides previous logic
            QName targetType = DOMUtil.getQNameValue(targetTypeAnnotationElement);
            definition.setTargetTypeName(targetType);
        }
        setMultiplicity(definition, elementParticle, annotation, false);
        Boolean composite = SchemaProcessorUtil.getAnnotationBooleanMarker(annotation, A_COMPOSITE);
        if (composite != null) {
            definition.setComposite(composite);
        }

        parseItemDefinitionAnnotations(definition, annotation);
        // extractDocumentation(definition, annotation);
        return definition;
    }

    private void setMultiplicity(MutableItemDefinition itemDef, XSParticle particle, XSAnnotation annotation, boolean topLevel) {
        if (topLevel || particle == null) {
            itemDef.setMinOccurs(0);
            Element maxOccursAnnotation = SchemaProcessorUtil.getAnnotationElement(annotation, A_MAX_OCCURS);
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
     *
     * @param set
     *            XS Schema Set
     */
    private void createDefinitionsFromElements(XSSchemaSet set) throws SchemaException {
        Iterator<XSElementDecl> iterator = set.iterateElementDecls();
        while (iterator.hasNext()) {
            XSElementDecl xsElementDecl = iterator.next();
            if (isDeprecated(xsElementDecl)) {
                // Safe to ignore. We want it in the XSD schema only. The real
                // definition will be
                // parsed from the non-deprecated variant
            }

            if (xsElementDecl.getTargetNamespace().equals(schema.getNamespace())) {

                QName elementName = new QName(xsElementDecl.getTargetNamespace(), xsElementDecl.getName());
                LOGGER.trace("### processing item {} into {} [{}]", elementName, schema, shortDescription);
                XSType xsType = xsElementDecl.getType();
                if (xsType == null) {
                    throw new SchemaException("Found element " + elementName + " without type definition");
                }
                QName typeQName = determineType(xsElementDecl);
                if (typeQName == null) {
                    // No type defined, safe to skip
                    continue;
                    // throw new SchemaException("Found element "+elementName+"
                    // with incomplete type name:
                    // {"+xsType.getTargetNamespace()+"}"+xsType.getName());
                }
                XSAnnotation annotation = xsElementDecl.getAnnotation();
                MutableItemDefinition<?> definition;

                if (isPropertyContainer(xsElementDecl) || isObjectDefinition(xsType)) {
                    ComplexTypeDefinition complexTypeDefinition = findComplexTypeDefinition(typeQName);
                    if (complexTypeDefinition == null) {
                        if (!allowDelayedItemDefinitions) {
                            throw new SchemaException("Couldn't parse prism container " + elementName + " of type " + typeQName
                                + " because complex type definition couldn't be found and delayed item definitions are not allowed.");
                        }
                        definition = null;
                        schema.addDelayedItemDefinition(() -> {
                            ComplexTypeDefinition ctd = findComplexTypeDefinition(typeQName);
                            // here we take the risk that ctd is null
                            return createContainerOrObjectDefinition(
                                    xsType, xsElementDecl, ctd, annotation, null, PrismConstants.VIRTUAL_SCHEMA_ROOT, false, true);
                        });
                    } else {
                        definition = createContainerOrObjectDefinition(
                                xsType, xsElementDecl, complexTypeDefinition, annotation, null, PrismConstants.VIRTUAL_SCHEMA_ROOT, false, true);
                    }
                } else if (isObjectReference(xsElementDecl, xsType)) {
                    definition = processObjectReferenceDefinition(xsType, elementName,
                            annotation, null, null, false);
                } else {
                    // Create a top-level property definition (even if this is a XSD complex type)
                    definition = createPropertyDefinition(
                            xsType, elementName, typeQName, null, annotation, null);
                }
                if (definition != null) {
                    QName substitutionHead = getSubstitutionHead(xsElementDecl);
                    if (substitutionHead != null) {
                        definition.setSubstitutionHead(substitutionHead);
                        schema.addSubstitution(substitutionHead, definition);
                    }
                    schema.add(definition);
                }

            } else { //if (xsElementDecl.getTargetNamespace().equals(XMLConstants.W3C_XML_SCHEMA_NS_URI)) {
                // This is OK to ignore. These are imported elements from other
                // schemas
                // } else {
                // throw new SchemaException("Found element
                // "+xsElementDecl.getName()+" with wrong namespace
                // "+xsElementDecl.getTargetNamespace()+" while expecting
                // "+schema.getNamespace());
            }
        }
    }

    // We first try to find the definition locally, because in schema registry we don't have the current schema yet.
    private ComplexTypeDefinition findComplexTypeDefinition(QName typeQName) {
        ComplexTypeDefinition complexTypeDefinition = schema.findComplexTypeDefinitionByType(typeQName);
        if (complexTypeDefinition == null) {
            complexTypeDefinition = getSchemaRegistry().findComplexTypeDefinitionByType(typeQName);
        }
        return complexTypeDefinition;
    }

    private QName getSubstitutionHead(XSElementDecl element) {
        XSElementDecl head = element.getSubstAffiliation();
        if (head == null) {
            return null;
        } else {
            return new QName(head.getTargetNamespace(), head.getName());
        }
    }

    private QName determineType(XSElementDecl xsElementDecl) {
        // Check for a:type annotation. If present, this overrides the type
        QName type = getTypeAnnotation(xsElementDecl);
        if (type != null) {
            return type;
        }
        XSType xsType = xsElementDecl.getType();
        if (xsType == null) {
            return null;
        }
        return getType(xsType);
    }

    private QName getType(XSType xsType) {
        if (xsType.getName() == null) {
            return null;
        }
        return new QName(xsType.getTargetNamespace(), xsType.getName());
    }

    private QName getTypeAnnotation(XSElementDecl xsElementDecl) {
        XSAnnotation annotation = xsElementDecl.getAnnotation();
        return getTypeAnnotation(annotation);
    }

    private QName getTypeAnnotation(XSAnnotation annotation) {
        return SchemaProcessorUtil.getAnnotationQName(annotation, A_TYPE);
    }

    /**
     * Determine whether the definition contains xsd:any (directly or indirectly)
     */
    private boolean isAny(XSType xsType, Optional<Integer> mode) {
        if (xsType instanceof XSComplexType) {
            XSComplexType complexType = (XSComplexType) xsType;
            XSContentType contentType = complexType.getContentType();
            if (contentType != null) {
                XSParticle particle = contentType.asParticle();
                if (particle != null) {
                    XSTerm term = particle.getTerm();
                    if (term != null) {
                        return isAny(term, mode);
                    }
                }
            }
        }
        return false;
    }

    /**
     * Determine whether the definition contains "list" attribute (directly or indirectly)
     */
    private boolean isList(XSComplexType complexType) {
        Collection<? extends XSAttributeUse> attributeUses = complexType.getAttributeUses();
        return attributeUses != null && attributeUses.stream()
                .anyMatch(au -> au.getDecl() != null && DOMUtil.IS_LIST_ATTRIBUTE_NAME.equals(au.getDecl().getName()));
    }

    // not much tested
    private void applyToDeclarations(XSComponent component, Consumer<XSDeclaration> consumer) {
        if (component == null) {
            return;
        }
        if (component instanceof XSDeclaration) {
            consumer.accept((XSDeclaration) component);
        }
        // recursion (if needed)
        if (component instanceof XSParticle) {
            applyToDeclarations(((XSParticle) component).getTerm(), consumer);
        } else if (component instanceof XSModelGroup) {
            for (XSParticle particle : ((XSModelGroup) component).getChildren()) {
                applyToDeclarations(particle, consumer);
            }
        } else if (component instanceof XSModelGroupDecl) {
            applyToDeclarations(((XSModelGroupDecl) component).getModelGroup(), consumer);
        }
    }

    private QName determineSupertype(XSType type) {
        XSType baseType = type.getBaseType();
        if (baseType == null) {
            return null;
        }
        if (baseType.getName().equals("anyType")) {
            return null;
        }
        return new QName(baseType.getTargetNamespace(), baseType.getName());
    }

    /**
     * Determine whether the definition contains xsd:any (directly or indirectly)
     */
    private boolean isAny(XSTerm term, Optional<Integer> mode) {
        if (term.isWildcard()) {
            if (mode.isPresent()) {

                return mode.get().equals(term.asWildcard().getMode());
            } else {
                return true;
            }
        }
        if (term.isModelGroup()) {
            XSParticle[] children = term.asModelGroup().getChildren();
            if (children != null) {
                for (XSParticle childParticle : children) {
                    XSTerm childTerm = childParticle.getTerm();
                    if (childTerm != null) {
                        if (isAny(childTerm, mode)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean isPropertyContainer(XSElementDecl xsElementDecl) {
        return SchemaProcessorUtil.getAnnotationElement(xsElementDecl.getAnnotation(), A_CONTAINER) != null
                || isPropertyContainer(xsElementDecl.getType());
    }

    /**
     * Returns true if provides XSD type is a property container. It looks for
     * annotations.
     */
    private boolean isPropertyContainer(XSType xsType) {
        if (SchemaProcessorUtil.getAnnotationElement(xsType.getAnnotation(), A_CONTAINER) != null) {
            return true;
        }
        if (xsType.getBaseType() != null && !xsType.getBaseType().equals(xsType)) {
            return isPropertyContainer(xsType.getBaseType());
        }
        return false;
    }

    private String getDefaultNamespace(XSType xsType) {
        Element annoElement = SchemaProcessorUtil.getAnnotationElement(xsType.getAnnotation(), A_DEFAULT_NAMESPACE);
        if (annoElement != null) {
            return annoElement.getTextContent();
        }
        if (xsType.getBaseType() != null && !xsType.getBaseType().equals(xsType)) {
            return getDefaultNamespace(xsType.getBaseType());
        }
        return null;
    }

    @NotNull
    private List<String> getIgnoredNamespaces(XSType xsType) {
        List<String> rv = new ArrayList<>();
        List<Element> annoElements = SchemaProcessorUtil.getAnnotationElements(xsType.getAnnotation(), A_IGNORED_NAMESPACE);
        for (Element annoElement : annoElements) {
            rv.add(annoElement.getTextContent());
        }
        if (xsType.getBaseType() != null && !xsType.getBaseType().equals(xsType)) {
            rv.addAll(getIgnoredNamespaces(xsType.getBaseType()));
        }
        return rv;
    }

    private boolean isObjectReference(XSElementDecl xsElementDecl, XSType xsType) {
        XSAnnotation annotation = xsType.getAnnotation();
        return isObjectReference(xsType, annotation);
    }

    private boolean isObjectReference(XSType xsType, XSAnnotation annotation) {
        return isObjectReference(annotation) || isObjectReference(xsType);
    }

    private boolean isObjectReference(XSAnnotation annotation) {
        return SchemaProcessorUtil.getAnnotationElement(annotation, A_OBJECT_REFERENCE) != null;
    }

    private boolean isObjectReference(XSType xsType) {
        return SchemaProcessorUtil.hasAnnotation(xsType, A_OBJECT_REFERENCE);
    }

    /**
     * Returns true if provides XSD type is an object definition.
     */
    private boolean isObjectDefinition(XSType xsType) {
        return SchemaProcessorUtil.hasAnnotation(xsType, A_OBJECT);
    }

    /**
     * Creates appropriate instance of {@link PrismContainerDefinition} or {@link PrismObjectDefinition}.
     * This method also takes care of parsing all the annotations and similar fancy stuff.
     *
     * @param containerInsteadOfObject If true, we never create a {@link PrismObjectDefinition} (mainly because we
     * cannot have object inside a container)
     */
    private PrismContainerDefinition<?> createContainerOrObjectDefinition(
            XSType xsType,
            XSParticle elementParticle,
            ComplexTypeDefinition complexTypeDefinition,
            XSAnnotation annotation,
            QName declaredInType,
            boolean containerInsteadOfObject) throws SchemaException {
        XSTerm elementTerm = elementParticle.getTerm();
        XSElementDecl elementDecl = elementTerm.asElementDecl();

        return createContainerOrObjectDefinition(
                xsType,
                elementDecl,
                complexTypeDefinition,
                annotation,
                elementParticle,
                declaredInType,
                containerInsteadOfObject,
                false);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private MutablePrismContainerDefinition<?> createContainerOrObjectDefinition(
            XSType xsType,
            XSElementDecl elementDecl,
            ComplexTypeDefinition complexTypeDefinition,
            XSAnnotation annotation,
            XSParticle elementParticle,
            QName definedInType,
            boolean containerInsteadOfObject,
            boolean objectMultiplicityIsOne) throws SchemaException {

        QName elementName = new QName(elementDecl.getTargetNamespace(), elementDecl.getName());
        MutablePrismContainerDefinition<?> pcd;

        SchemaDefinitionFactory definitionFactory = getDefinitionFactory();

        Class compileTimeClass;
        if (complexTypeDefinition != null) {
            compileTimeClass = getSchemaRegistry().determineCompileTimeClass(complexTypeDefinition.getTypeName());
        } else {
            compileTimeClass = null;
        }
        boolean isObject = isObjectDefinition(xsType);
        if (isObject && !containerInsteadOfObject) {
            pcd = definitionFactory.createObjectDefinition(elementName, complexTypeDefinition, compileTimeClass);
        } else {
            pcd = definitionFactory.createContainerDefinition(elementName, complexTypeDefinition, compileTimeClass, definedInType);
        }

        if (isObject && objectMultiplicityIsOne) {
            pcd.setMinOccurs(1);
            pcd.setMaxOccurs(1);
        } else {
            setMultiplicity(pcd, elementParticle, elementDecl.getAnnotation(), PrismConstants.VIRTUAL_SCHEMA_ROOT.equals(definedInType));
        }

        markRuntime(pcd);

        var keysElem =  SchemaProcessorUtil.getAnnotationQNames(elementDecl.getAnnotation(), A_ALWAYS_USE_FOR_EQUALS);
        pcd.setAlwaysUseForEquals(keysElem);
        parseItemDefinitionAnnotations(pcd, annotation);
        parseItemDefinitionAnnotations(pcd, elementDecl.getAnnotation());
        if (elementParticle != null) {
            parseItemDefinitionAnnotations(pcd, elementParticle.getAnnotation());
        }

        return pcd;
    }

    /**
     * Creates appropriate instance of PropertyDefinition. It creates either
     * PropertyDefinition itself or one of its subclasses
     * (ResourceObjectAttributeDefinition). The behavior depends of the "mode"
     * of the schema. This method is also processing annotations and other fancy
     * property-relates stuff.
     */
    private <T> MutablePrismPropertyDefinition<T> createPropertyDefinition(XSType xsType, QName elementName,
            QName typeName, ComplexTypeDefinition ctd, XSAnnotation annotation, XSParticle elementParticle)
                    throws SchemaException {
        MutablePrismPropertyDefinition<T> propDef;

        SchemaDefinitionFactory definitionFactory = getDefinitionFactory();

        Collection<? extends DisplayableValue<T>> allowedValues = parseEnumAllowedValues(typeName, ctd, xsType);

        Object defaultValue = parseDefaultValue(elementParticle, typeName);

        propDef = definitionFactory.createPropertyDefinition(elementName, typeName, ctd, prismContext,
                annotation, elementParticle, allowedValues, (T) defaultValue);
        setMultiplicity(propDef, elementParticle, annotation, ctd == null);

        // Process generic annotations
        parseItemDefinitionAnnotations(propDef, annotation);

        List<Element> accessElements = SchemaProcessorUtil.getAnnotationElements(annotation, A_ACCESS);
        if (accessElements.isEmpty()) {
            // Default access is read-write-create
            propDef.setCanAdd(true);
            propDef.setCanModify(true);
            propDef.setCanRead(true);
        } else {
            propDef.setCanAdd(false);
            propDef.setCanModify(false);
            propDef.setCanRead(false);
            for (Element e : accessElements) {
                String access = e.getTextContent();
                if (access.equals(A_ACCESS_CREATE)) {
                    propDef.setCanAdd(true);
                }
                if (access.equals(A_ACCESS_UPDATE)) {
                    propDef.setCanModify(true);
                }
                if (access.equals(A_ACCESS_READ)) {
                    propDef.setCanRead(true);
                }
            }
        }

        markRuntime(propDef);

        Element indexableElement = SchemaProcessorUtil.getAnnotationElement(annotation, A_INDEXED);
        if (indexableElement != null) {
            Boolean indexable = XmlTypeConverter.toJavaValue(indexableElement, Boolean.class);
            propDef.setIndexed(indexable);
        }

        Element indexOnlyElement = SchemaProcessorUtil.getAnnotationElement(annotation, A_INDEX_ONLY);
        if (indexOnlyElement != null) {
            propDef.setIndexOnly(Boolean.TRUE.equals(XmlTypeConverter.toJavaValue(indexOnlyElement, Boolean.class)));
            if (propDef.isIndexOnly()) {
                propDef.setIndexed(true);
            }
        }

        Element matchingRuleElement = SchemaProcessorUtil.getAnnotationElement(annotation, A_MATCHING_RULE);
        if (matchingRuleElement != null) {
            QName matchingRule = XmlTypeConverter.toJavaValue(matchingRuleElement, QName.class);
            propDef.setMatchingRuleQName(matchingRule);
        }

        Element valueEnumerationRefElement = SchemaProcessorUtil.getAnnotationElement(annotation, A_VALUE_ENUMERATION_REF);
        if (valueEnumerationRefElement != null) {
            String oid = valueEnumerationRefElement.getAttribute(PrismConstants.ATTRIBUTE_OID_LOCAL_NAME);
            if (oid != null) {
                QName targetType = DOMUtil.getQNameAttribute(valueEnumerationRefElement, PrismConstants.ATTRIBUTE_REF_TYPE_LOCAL_NAME);
                PrismReferenceValue valueEnumerationRef = new PrismReferenceValueImpl(oid, targetType);
                propDef.setValueEnumerationRef(valueEnumerationRef);
            }
        }

        return propDef;
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

    private <T> Collection<? extends DisplayableValue<T>> parseEnumAllowedValues(QName typeName,
            ComplexTypeDefinition ctd, XSType xsType) {
        if (xsType.isSimpleType()) {
            if (xsType.asSimpleType().isRestriction()) {
                XSRestrictionSimpleType restriction = xsType.asSimpleType().asRestriction();
                List<XSFacet> enumerations = restriction.getDeclaredFacets(XSFacet.FACET_ENUMERATION);
                List<DisplayableValueImpl<T>> enumValues = new ArrayList<>(
                    enumerations.size());
                for (XSFacet facet : enumerations) {
                    String value = facet.getValue().value;
                    Element descriptionE = SchemaProcessorUtil.getAnnotationElement(facet.getAnnotation(), SCHEMA_DOCUMENTATION);
                    Element appInfo = SchemaProcessorUtil.getAnnotationElement(facet.getAnnotation(), SCHEMA_APP_INFO);
                    Element valueE = null;
                    if (appInfo != null) {
                        NodeList list = appInfo.getElementsByTagNameNS(
                                PrismConstants.A_LABEL.getNamespaceURI(),
                                PrismConstants.A_LABEL.getLocalPart());
                        if (list.getLength() != 0) {
                            valueE = (Element) list.item(0);
                        }
                    }
                    String label = null;
                    if (valueE != null) {
                        label = valueE.getTextContent();
                    } else {
                        label = value;
                    }
                    DisplayableValueImpl<T> edv = null;
                    Class compileTimeClass = prismContext.getSchemaRegistry().getCompileTimeClass(typeName);
                    if (ctd != null && !ctd.isRuntimeSchema() && compileTimeClass != null) {

                        String fieldName = null;
                        for (Field field : compileTimeClass.getDeclaredFields()) {
                            XmlEnumValue xmlEnumValue = field.getAnnotation(XmlEnumValue.class);
                            if (xmlEnumValue != null && xmlEnumValue.value() != null
                                    && xmlEnumValue.value().equals(value)) {
                                fieldName = field.getName();
                            }

                        }
                        if (fieldName != null) {
                            T enumValue = (T) Enum.valueOf((Class<Enum>) compileTimeClass, fieldName);
                            edv = new DisplayableValueImpl(enumValue, label,
                                    descriptionE != null ? descriptionE.getTextContent() : null);
                        } else {
                            edv = new DisplayableValueImpl(value, label,
                                    descriptionE != null ? descriptionE.getTextContent() : null);
                        }


                    } else {
                    edv = new DisplayableValueImpl(value, label,
                            descriptionE != null ? descriptionE.getTextContent() : null);
                    }
                    enumValues.add(edv);

                }
                if (enumValues != null && !enumValues.isEmpty()) {
                    return enumValues;
                }

            }
        }
        return null;
    }

    private void parseItemDefinitionAnnotations(MutableItemDefinition itemDef, XSAnnotation annotation) throws SchemaException {
        if (annotation == null || annotation.getAnnotation() == null) {
            return;
        }

        Annotation.processAnnotations(itemDef, annotation);

        // ignore
        Boolean ignore = SchemaProcessorUtil.getAnnotationBooleanMarker(annotation, A_IGNORE);
        if (ignore != null) {
            itemDef.setProcessing(ItemProcessing.IGNORE);
        }

        Element processing = SchemaProcessorUtil.getAnnotationElement(annotation, A_PROCESSING);
        if (processing != null) {
            itemDef.setProcessing(ItemProcessing.findByValue(processing.getTextContent()));
        }

        // deprecated
        Boolean deprecated = SchemaProcessorUtil.getAnnotationBooleanMarker(annotation, A_DEPRECATED);
        if (deprecated != null) {
            itemDef.setDeprecated(deprecated);
        }

        // deprecated since
        Element deprecatedSince = SchemaProcessorUtil.getAnnotationElement(annotation, A_DEPRECATED_SINCE);
        if (deprecatedSince != null) {
            itemDef.setDeprecatedSince(deprecatedSince.getTextContent());
        }

        // removed
        Boolean removed = SchemaProcessorUtil.getAnnotationBooleanMarker(annotation, A_REMOVED);
        if (removed != null) {
            itemDef.setRemoved(removed);
        }

        // removed since
        Element removedSince = SchemaProcessorUtil.getAnnotationElement(annotation, A_REMOVED_SINCE);
        if (removedSince != null) {
            itemDef.setRemovedSince(removedSince.getTextContent());
        }

        // experimental
        Boolean experimental = SchemaProcessorUtil.getAnnotationBooleanMarker(annotation, A_EXPERIMENTAL);
        if (experimental != null) {
            itemDef.setExperimental(experimental);
        }

        // planned removal
        Element plannedRemoval = SchemaProcessorUtil.getAnnotationElement(annotation, A_PLANNED_REMOVAL);
        if (plannedRemoval != null) {
            itemDef.setPlannedRemoval(plannedRemoval.getTextContent());
        }

        // elaborate
        Boolean elaborate = SchemaProcessorUtil.getAnnotationBooleanMarker(annotation, A_ELABORATE);
        if (elaborate != null) {
            itemDef.setElaborate(elaborate);
        }

        // operational
        Boolean operational = SchemaProcessorUtil.getAnnotationBooleanMarker(annotation, A_OPERATIONAL);
        if (operational != null) {
            itemDef.setOperational(operational);
        }

        // displayName
        Element attributeDisplayName = SchemaProcessorUtil.getAnnotationElement(annotation, A_DISPLAY_NAME);
        if (attributeDisplayName != null) {
            itemDef.setDisplayName(attributeDisplayName.getTextContent());
        }

        // displayOrder
        Element displayOrderElement = SchemaProcessorUtil.getAnnotationElement(annotation, A_DISPLAY_ORDER);
        if (displayOrderElement != null) {
            Integer displayOrder = DOMUtil.getIntegerValue(displayOrderElement);
            itemDef.setDisplayOrder(displayOrder);
        }

        // help
        Element help = SchemaProcessorUtil.getAnnotationElement(annotation, A_HELP);
        if (help != null) {
            itemDef.setHelp(help.getTextContent());
        }

        // emphasized
        Boolean emphasized = SchemaProcessorUtil.getAnnotationBooleanMarker(annotation, A_EMPHASIZED);
        if (emphasized != null) {
            itemDef.setEmphasized(emphasized);
        }

        Boolean searchable = SchemaProcessorUtil.getAnnotationBooleanMarker(annotation, A_SEARCHABLE);
        if (searchable != null) {
            itemDef.setSearchable(searchable);
        }

        // documentation
        extractDocumentation(itemDef, annotation);

        Boolean heterogeneousListItem = SchemaProcessorUtil.getAnnotationBooleanMarker(annotation, A_HETEROGENEOUS_LIST_ITEM);
        if (heterogeneousListItem != null) {
            itemDef.setHeterogeneousListItem(heterogeneousListItem);
        }

        // schemaMigration
        parseSchemaMigrations(itemDef, annotation);

        // diagram
        parseDiagrams(itemDef, annotation);
    }

    private void parseSchemaMigrations(MutableDefinition def, XSAnnotation annotation) throws SchemaException {
        for (Element schemaMigrationElement : SchemaProcessorUtil.getAnnotationElements(annotation, A_SCHEMA_MIGRATION)) {
            SchemaMigration schemaMigration = parseSchemaMigration(def, schemaMigrationElement);
            def.addSchemaMigration(schemaMigration);
        }
    }

    private SchemaMigration parseSchemaMigration(MutableDefinition def, Element schemaMigrationElement) throws SchemaException {
        Element elementElement = DOMUtil.getChildElement(schemaMigrationElement, A_SCHEMA_MIGRATION_ELEMENT);
        if (elementElement == null) {
            throw new SchemaException("Missing schemaMigration element in "+def);
        }
        QName elementName = DOMUtil.getQNameValue(elementElement);
        Element versionElement = DOMUtil.getChildElement(schemaMigrationElement, A_SCHEMA_MIGRATION_VERSION);
        if (versionElement == null) {
            throw new SchemaException("Missing schemaMigration version in "+def);
        }
        Element operationElement = DOMUtil.getChildElement(schemaMigrationElement, A_SCHEMA_MIGRATION_OPERATION);
        if (operationElement == null) {
            throw new SchemaException("Missing schemaMigration operation in "+def);
        }

        Element replacementElement = DOMUtil.getChildElement(schemaMigrationElement, A_SCHEMA_MIGRATION_REPLACEMENT);
        QName replacementName = replacementElement != null ? DOMUtil.getQNameValue(replacementElement) : null;

        SchemaMigrationOperation op = SchemaMigrationOperation.parse(org.apache.commons.lang3.StringUtils.trim(operationElement.getTextContent()));
        return new SchemaMigration(elementName, org.apache.commons.lang3.StringUtils.trim(versionElement.getTextContent()), op, replacementName);
    }

    private void parseDiagrams(MutableDefinition def, XSAnnotation annotation) throws SchemaException {
        for (Element diagramElement : SchemaProcessorUtil.getAnnotationElements(annotation, A_DIAGRAM)) {
            ItemDiagramSpecification diagram = parseDiagram(def, diagramElement);
            def.addDiagram(diagram);
        }
    }

    private ItemDiagramSpecification parseDiagram(MutableDefinition def, Element diagramElement) throws SchemaException {
        Element nameElement = DOMUtil.getChildElement(diagramElement, A_DIAGRAM_NAME);
        if (nameElement == null) {
            throw new SchemaException("Missing name element in "+def);
        }
        String name = org.apache.commons.lang3.StringUtils.trim(nameElement.getTextContent());

        Element formElement = DOMUtil.getChildElement(diagramElement, A_DIAGRAM_FORM);

        DiagramElementFormType form = null;
        if (formElement != null) {
            form = DiagramElementFormType.parse(org.apache.commons.lang3.StringUtils.trim(formElement.getTextContent()));
        }

        Element inclusionElement = DOMUtil.getChildElement(diagramElement, A_DIAGRAM_INCLUSION);
        DiagramElementInclusionType inclusion = null;
        if (inclusionElement != null) {
            inclusion = DiagramElementInclusionType.parse(org.apache.commons.lang3.StringUtils.trim(inclusionElement.getTextContent()));
        }

        Element subitemInclusionElement = DOMUtil.getChildElement(diagramElement, A_DIAGRAM_SUBITEM_INCLUSION);
        DiagramElementInclusionType subitemInclusion = null;
        if (subitemInclusionElement != null) {
            subitemInclusion = DiagramElementInclusionType.parse(org.apache.commons.lang3.StringUtils.trim(subitemInclusionElement.getTextContent()));
        }

        return new ItemDiagramSpecification(name, form, inclusion, subitemInclusion);
    }

    private boolean isDeprecated(XSElementDecl xsElementDecl) throws SchemaException {
        XSAnnotation annotation = xsElementDecl.getAnnotation();
        Boolean deprecated = SchemaProcessorUtil.getAnnotationBooleanMarker(annotation, A_DEPRECATED);
        return (deprecated != null && deprecated);
    }

    private boolean containsAccessFlag(String flag, List<Element> accessList) {
        for (Element element : accessList) {
            if (flag.equals(element.getTextContent())) {
                return true;
            }
        }

        return false;
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
        return SchemaProcessorUtil.getAnnotationElement(annotation, SCHEMA_APP_INFO) != null;
    }

    private void markRuntime(Definition def) {
        if (isRuntime) {
            def.toMutable().setRuntimeSchema(true);
        }
    }
}
