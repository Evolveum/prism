/*
 * Copyright (c) 2010-2018 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism.impl.schema;

import static com.evolveum.midpoint.prism.impl.schema.features.DefinitionFeatures.DF_EXTENSION_REF;

import static javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI;

import static com.evolveum.midpoint.prism.PrismConstants.*;
import static com.evolveum.midpoint.prism.impl.schema.features.DefinitionFeatures.DF_ACCESS;
import static com.evolveum.midpoint.util.MiscUtil.argNonNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.impl.schema.features.DefinitionFeatures;
import com.evolveum.midpoint.prism.impl.schema.features.EnumerationValuesInfoXsomParser;
import com.evolveum.midpoint.prism.impl.schema.features.EnumerationValuesXsomParser;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.evolveum.midpoint.prism.path.ItemName;
import com.evolveum.midpoint.prism.schema.*;
import com.evolveum.midpoint.prism.schema.DefinitionFeatureSerializer.SerializationTarget;
import com.evolveum.midpoint.prism.xml.DynamicNamespacePrefixMapper;
import com.evolveum.midpoint.prism.xml.XsdTypeMapper;
import com.evolveum.midpoint.util.DOMUtil;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;

/**
 * Takes a midPoint Schema definition and produces a XSD schema (in a DOM form).
 *
 * Great pains were taken to make sure that the output XML is "nice" and human readable.
 * E.g. the namespace prefixes are unified using the definitions in {@link SchemaRegistry}.
 * Please do not ruin this if you would update this class.
 *
 * Single use class. Not thread safe. Create new instance for each run.
 *
 * TODO continue with cleanup of this class (using {@link DefinitionFeatures}) eventually
 *
 * @author lazyman
 * @author Radovan Semancik
 */
public class SchemaDomSerializer {

    private static final Trace LOGGER = TraceManager.getTrace(SchemaDomSerializer.class);

    private static final String TNS_PREFIX = "tns";

    @NotNull private final DynamicNamespacePrefixMapper namespacePrefixMapper;

    /** Schema that is being serialized. */
    @NotNull private final SerializableSchema schema;

    @NotNull private final String schemaNamespace;

    /** Document being created. */
    @NotNull private final Document document;

    /** Topmost element of {@link #document}. */
    @NotNull private final Element documentRootElement;

    @NotNull private final Set<String> importNamespaces = new HashSet<>();

    public SchemaDomSerializer(SerializableSchema schema) throws SchemaException {
        this.schema = argNonNull(schema, "schema");
        this.schemaNamespace = schema.getNamespace();

        // We clone, because we don't want the "tns" prefix to be kept in the mapper.
        namespacePrefixMapper = PrismContext.get().getSchemaRegistry().getNamespacePrefixMapper().clone();
        namespacePrefixMapper.registerPrefixLocal(schemaNamespace, TNS_PREFIX);

        LOGGER.trace("Using namespace prefix mapper to serialize schema:\n{}", namespacePrefixMapper.debugDumpLazily(1));

        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(true);
            documentBuilderFactory.setValidating(false);
            // XXE
            documentBuilderFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            documentBuilderFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            documentBuilderFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            documentBuilderFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            documentBuilderFactory.setXIncludeAware(false);
            documentBuilderFactory.setExpandEntityReferences(false);
            DocumentBuilder db = documentBuilderFactory.newDocumentBuilder();
            document = db.newDocument();
        } catch (ParserConfigurationException e) {
            throw new SchemaException("Couldn't initialize schema parser: " + e.getMessage(), e);
        }

        documentRootElement = createRootElement(new QName(W3C_XML_SCHEMA_NS_URI, "schema"));
        documentRootElement.setAttribute("targetNamespace", schemaNamespace);
        documentRootElement.setAttribute("elementFormDefault", "qualified");
        DOMUtil.setNamespaceDeclaration(documentRootElement, TNS_PREFIX, schemaNamespace);

        document.appendChild(documentRootElement);
    }

    /**
     * Main entry point.
     *
     * @return XSD schema in DOM form
     * @throws SchemaException error parsing the midPoint schema or converting values
     */
    public @NotNull Document serializeSchema() throws SchemaException {

        try {

            // Putting CTDs first is maybe not strictly required, but it's definitely nice.
            for (var definition : schema.getDefinitionsToSerialize()) {
                if (definition instanceof SerializableComplexTypeDefinition ctd) {
                    serializeComplexTypeDefinition(ctd, documentRootElement);
                } else if (definition instanceof EnumerationTypeDefinition etd) {
                    serializeEnumerationTypeDefinition(etd, document.getDocumentElement());
                }
            }

            for (var definition : schema.getDefinitionsToSerialize()) {
                if (definition instanceof SerializableContainerDefinition pcd) {
                    // Container definitions are serialized as <complexType> and top-level <element> definitions in XSD.
                    serializeContainerDefinition(pcd, documentRootElement);
                } else if (definition instanceof SerializablePropertyDefinition ppd) {
                    // Add top-level property definition. It will create <element> XSD definition
                    serializePropertyDefinition(ppd, documentRootElement);
                } else if (definition instanceof SerializableComplexTypeDefinition
                        || definition instanceof EnumerationTypeDefinition) {
                    // Skip this. Already processed above.
                } else {
                    throw new IllegalArgumentException("Encountered unsupported definition in schema: " + definition);
                }
                // TODO: what about root-level references and simple types?
                // TODO: process unprocessed ComplexTypeDefinitions
            }

            // Add import definition. These were accumulated during previous processing.
            addImports();

        } catch (Exception ex) {
            throw new SchemaException("Couldn't serialize schema: " + ex.getMessage(), ex);
        }
        return document;
    }

    /**
     * Serializes container definition. This is complexType + element.
     *
     * @param containerDef {@link PrismContainerDefinition} to process
     * @param parent element under which the XSD definition will be added
     */
    private void serializeContainerDefinition(SerializableContainerDefinition containerDef, Element parent) {

        // Note that we don't need to serialize the complex type definition. If it belongs to this schema, it was already
        // serialized at the beginning. If it does not, there's no need to serialize it here.

        Element itemElement = serializeContainerItemDefinition(containerDef, parent);

        SerializableComplexTypeDefinition complexTypeDefinition = containerDef.getComplexTypeDefinitionToSerialize();
        if (complexTypeDefinition == null || !complexTypeDefinition.isContainerMarker()) {
            // Need to add a:container annotation to the element as there is no complex type definition to put it on
            // (or it's not a container definition - however, that is probably quite strange)
            addAnnotationToDefinition(itemElement, A_CONTAINER);
        }
    }

    /**
     * Adds root XSD element definition for a container.
     *
     * TODO why it this different from other item definitions serializations?
     *
     * @param parent element under which the definition will be added
     */
    private Element serializeContainerItemDefinition(SerializableContainerDefinition containerDef, Element parent) {
        Element itemElement = createElement(new QName(W3C_XML_SCHEMA_NS_URI, "element"));
        parent.appendChild(itemElement);

        var itemName = containerDef.getItemName();
        var typeName = containerDef.getTypeName();
        if (schemaNamespace.equals(itemName.getNamespaceURI())) {
            itemElement.setAttribute("name", itemName.getLocalPart());
            if (typeName.equals(DOMUtil.XSD_ANY)) {
                addSequenceXsdAnyDefinition(itemElement);
            } else {
                setQNameAttribute(itemElement, "type", typeName);
            }
        } else {
            // Need to create "ref" instead of "name"
            setAttribute(itemElement, "ref", itemName);
            // Type cannot be stored directly, XSD does not allow it with "ref"s.
            addAnnotationToDefinition(itemElement, A_TYPE, typeName);
        }

        setMultiplicity(containerDef, itemElement);

        var annTarget = createAppInfoAnnotationsTarget(itemElement);

        addCommonItemDefinitionAnnotations(containerDef, annTarget);
        addExtraFeatures(containerDef, annTarget);

        annTarget.removeIfNotNeeded();

        return itemElement;
    }

    /**
     * Serializes a property definition.
     *
     * @param propertyDef midPoint PropertyDefinition
     * @param parent element under which the definition will be added
     */
    private void serializePropertyDefinition(SerializablePropertyDefinition propertyDef, Element parent) {
        Element itemElement = createItemElement(propertyDef, parent);

        var appInfo = createAppInfoAnnotationsTarget(itemElement);

        addCommonItemDefinitionAnnotations(propertyDef, appInfo);

        addAnnotation(A_MATCHING_RULE, propertyDef.getMatchingRuleQName(), appInfo.appInfoElement);
        addAnnotation(A_VALUE_ENUMERATION_REF, propertyDef.getValueEnumerationRef(), appInfo.appInfoElement);

        addExtraFeatures(propertyDef, appInfo);

        appInfo.removeIfNotNeeded();
    }

    /**
     * Serializes a reference definition.
     */
    private void serializeReferenceDefinition(SerializableReferenceDefinition referenceDef, Element parent) {
        Element itemElement = createItemElement(referenceDef, parent);

        var appInfo = createAppInfoAnnotationsTarget(itemElement);

        addCommonItemDefinitionAnnotations(referenceDef, appInfo);
        addAnnotation(A_OBJECT_REFERENCE_TARGET_TYPE, referenceDef.getTargetTypeName(), appInfo.appInfoElement);
        addTrueAnnotation(A_COMPOSITE, referenceDef.isComposite(), appInfo.appInfoElement);

        addExtraFeatures(referenceDef, appInfo);

        appInfo.removeIfNotNeeded();
    }

    private void addCommonItemDefinitionAnnotations(SerializableItemDefinition itemDefinition, AppInfoSerializationTarget target) {
        addCommonDefinitionAnnotations(itemDefinition, target);
        DF_ACCESS.serialize(itemDefinition, target);
        target.addAnnotation(A_INDEXED, itemDefinition.isIndexed());
        if (itemDefinition.getProcessing() != null) {
            target.addAnnotation(A_PROCESSING, itemDefinition.getProcessing().getValue());
        }
    }

    /** Serializes item name+type or reference. Returns the {@link Element} created for the item. */
    private Element createItemElement(SerializableItemDefinition itemDef, Element parent) {
        Element itemElement = createElement(new QName(W3C_XML_SCHEMA_NS_URI, "element"));
        // Add to document first, so following methods will be able to resolve namespaces
        parent.appendChild(itemElement);

        ItemName itemName = itemDef.getItemName();
        String itemNamespace = itemName.getNamespaceURI();
        if (itemNamespace != null && itemNamespace.equals(schemaNamespace)) {
            itemElement.setAttribute("name", itemName.getLocalPart());
            setQNameAttribute(itemElement, "type", itemDef.getTypeName());
        } else {
            setQNameAttribute(itemElement, "ref", itemName);
        }

        setMultiplicity(itemDef, itemElement);

        return itemElement;
    }

    private void setMultiplicity(SerializableItemDefinition itemDef, Element itemElement) {
        setMultiplicityAttribute(itemElement, "minOccurs", itemDef.getMinOccurs());
        setMultiplicityAttribute(itemElement, "maxOccurs", itemDef.getMaxOccurs());
    }

    private void addSequenceXsdAnyDefinition(Element elementDef) {
        Element complexContextElement = createElement(new QName(W3C_XML_SCHEMA_NS_URI, "complexType"));
        elementDef.appendChild(complexContextElement);
        Element sequenceElement = createElement(new QName(W3C_XML_SCHEMA_NS_URI, "sequence"));
        complexContextElement.appendChild(sequenceElement);
        addXsdAnyDefinition(sequenceElement);
    }

    private void addXsdAnyDefinition(Element elementDef) {
        Element anyElement = createElement(new QName(W3C_XML_SCHEMA_NS_URI, "any"));
        elementDef.appendChild(anyElement);
        anyElement.setAttribute("namespace", "##other");
        anyElement.setAttribute("minOccurs", "0");
        anyElement.setAttribute("maxOccurs", "unbounded");
        anyElement.setAttribute("processContents", "lax");
    }

    /**
     * Adds XSD complexType definition from {@link SerializableComplexTypeDefinition} object.
     *
     * @param parent element under which the definition will be added
     */
    private void serializeComplexTypeDefinition(SerializableComplexTypeDefinition ctd, Element parent) {
        if (ctd == null) {
            return; // Nothing to do
        }
        QName typeName = ctd.getTypeName();

        Element complexType = createElement(new QName(W3C_XML_SCHEMA_NS_URI, "complexType"));
        parent.appendChild(complexType);
        complexType.setAttribute("name", typeName.getLocalPart());

        Element definitionsParent;
        QName superType = ctd.getSuperType();
        if (superType != null) {
            Element complexContent = createElement(new QName(W3C_XML_SCHEMA_NS_URI, "complexContent"));
            complexType.appendChild(complexContent);
            Element extension = createElement(new QName(W3C_XML_SCHEMA_NS_URI, "extension"));
            complexContent.appendChild(extension);
            setQNameAttribute(extension, "base", superType);
            definitionsParent = extension;
        } else {
            definitionsParent = complexType;
        }

        // Must come before "<sequence>".
        var appInfo = createAppInfoAnnotationsTarget(complexType);

        Element sequence = createElement(new QName(W3C_XML_SCHEMA_NS_URI, "sequence"));
        definitionsParent.appendChild(sequence);

        for (var itemDef : ctd.getDefinitionsToSerialize()) {
            if (itemDef instanceof SerializablePropertyDefinition ppd) {
                serializePropertyDefinition(ppd, sequence);
            } else if (itemDef instanceof SerializableContainerDefinition pcd) {
                serializeContainerDefinition(pcd, sequence);
            } else if (itemDef instanceof SerializableReferenceDefinition prd) {
                serializeReferenceDefinition(prd, sequence);
            } else {
                throw new IllegalArgumentException(
                        "Unknown definition %s (%s) in complex type definition %s".formatted(
                                itemDef, itemDef.getClass().getName(), ctd));
            }
        }

        if (ctd.isXsdAnyMarker()) {
            addXsdAnyDefinition(sequence);
        }

        if (ctd.isObjectMarker()) {
            addAnnotation(A_OBJECT, true, appInfo.appInfoElement);
        } else if (ctd.isContainerMarker()) {
            addAnnotation(A_CONTAINER, true, appInfo.appInfoElement);
        }

        DF_EXTENSION_REF.serialize(ctd, appInfo);

        addCommonDefinitionAnnotations(ctd, appInfo);

        addExtraFeatures(ctd, appInfo);

        appInfo.removeIfNotNeeded();
    }

    /** TODO reconcile with (move to) {@link EnumerationValuesXsomParser} and {@link EnumerationValuesInfoXsomParser} one day */
    private void serializeEnumerationTypeDefinition(EnumerationTypeDefinition definition, Element parent) {
        if (definition == null) {
            // Nothing to do
            return;
        }

        Element simpleType = createElement(new QName(W3C_XML_SCHEMA_NS_URI, "simpleType"));
        parent.appendChild(simpleType);
        // "typeName" should be used instead of "name" when defining a XSD type
        setAttribute(simpleType, "name", definition.getTypeName().getLocalPart());

        var appInfo = createAppInfoAnnotationsTarget(simpleType);
        addCommonDefinitionAnnotations(definition, appInfo);
        appInfo.appInfoElement.appendChild(createElement(EnumerationValuesXsomParser.TYPESAFE_ENUM_CLASS));
        appInfo.removeIfNotNeeded();

        Element restriction = createElement(new QName(W3C_XML_SCHEMA_NS_URI, "restriction"));
        setAttribute(restriction, "base", definition.getBaseTypeName());
        simpleType.appendChild(restriction);

        Collection<EnumerationTypeDefinition.ValueDefinition> valueDefinitions = definition.getValues();
        for (EnumerationTypeDefinition.ValueDefinition valueDefinition : valueDefinitions) {
            restriction.appendChild(createValueDefinitionChild(valueDefinition));
        }
    }

    private Element createValueDefinitionChild(EnumerationTypeDefinition.ValueDefinition valueDefinition) {
        Element enumeration = createElement(new QName(W3C_XML_SCHEMA_NS_URI, "enumeration"));
        setAttribute(enumeration, "value", valueDefinition.getValue());

        AppInfoSerializationTarget aia = createAppInfoAnnotationsTarget(enumeration);

        if (valueDefinition.getDocumentation().isPresent()) {
            aia.documentation.setTextContent(DOMUtil.getContentOfDocumentation(valueDefinition.getDocumentation().get()));
        }

        if (valueDefinition.getConstantName().isPresent()) {
            Element typeSafeEnum = createElement(EnumerationValuesXsomParser.TYPESAFE_ENUM_MEMBER);
            setAttribute(typeSafeEnum, "name", valueDefinition.getConstantName().get());
            aia.appInfoElement.appendChild(typeSafeEnum);
        }

        aia.removeIfNotNeeded();

        return enumeration;
    }

    private static void addExtraFeatures(SerializableDefinition definition, AppInfoSerializationTarget appInfo) {
        for (DefinitionFeature<?, ?, ?, ?> extraFeature : definition.getExtraFeaturesToSerialize()) {
            extraFeature.serialize(definition, appInfo);
        }
    }

    private void addCommonDefinitionAnnotations(SerializableDefinition definition, AppInfoSerializationTarget aie) {
        var appInfo = aie.appInfoElement;

        boolean isOperational = (definition instanceof SerializableItemDefinition itemDef) && itemDef.isOperational();
        addTrueAnnotation(A_OPERATIONAL, isOperational, appInfo);

        addAnnotation(A_DISPLAY_NAME, definition.getDisplayName(), appInfo);
        addToStringAnnotation(A_DISPLAY_ORDER, definition.getDisplayOrder(), appInfo);
        addAnnotation(A_HELP, definition.getHelp(), appInfo);
        addDisplayHint(definition, appInfo);

        addDocumentation(definition, aie);
    }

    private void addDocumentation(SerializableDefinition definition, AppInfoSerializationTarget ais) {
        if (definition.getDocumentation() != null) {
            ais.documentation.setTextContent(DOMUtil.getContentOfDocumentation(definition.getDocumentation()));
        }
    }

    private void addDisplayHint(SerializableDefinition definition, Element parent) {
        DisplayHint displayHint = definition.getDisplayHint();
        if (displayHint != null) {
            addAnnotation(A_DISPLAY_HINT, displayHint.getValue(), parent);
            if (displayHint.equals(DisplayHint.EMPHASIZED)) {
                return;
            }
        }

        addTrueAnnotation(A_EMPHASIZED, definition.isEmphasized(), parent);
    }

    /**
     * Add generic annotation element.
     *
     * @param qname QName of the element
     * @param value string value of the element
     * @param parent element under which the definition will be added
     */
    public void addAnnotation(QName qname, String value, Element parent) {
        if (value != null) {
            Element annotation = createElement(qname);
            parent.appendChild(annotation);
            annotation.setTextContent(value);
        }
    }

    @SuppressWarnings("SameParameterValue")
    private void addToStringAnnotation(QName qname, Object value, Element parent) {
        if (value != null) {
            addAnnotation(qname, value.toString(), parent);
        }
    }

    public void addTrueAnnotation(QName qname, boolean value, Element parent) {
        if (value) {
            addAnnotation(qname, "true", parent);
        }
    }

    public void addAnnotation(QName qname, Boolean value, Element parent) {
        if (value != null) {
            Element annotation = createElement(qname);
            parent.appendChild(annotation);
            annotation.setTextContent(Boolean.toString(value));
        }
    }

    public void addEmptyAnnotation(QName qname, Element parent) {
        parent.appendChild(
                createElement(qname));
    }

    private void addAnnotation(QName qname, QName value, Element parent) {
        if (value != null) {
            Element annotation = createElement(qname);
            parent.appendChild(annotation);
            DOMUtil.setQNameValue(annotation, value);
        }
    }

    @SuppressWarnings("SameParameterValue")
    private void addAnnotation(QName qname, PrismReferenceValue value, Element parent) {
        if (value != null) {
            Element annotation = createElement(qname);
            parent.appendChild(annotation);
            annotation.setAttribute(ATTRIBUTE_OID_LOCAL_NAME, value.getOid());
            DOMUtil.setQNameAttribute(annotation, ATTRIBUTE_REF_TYPE_LOCAL_NAME, value.getTargetType());
        }
    }

    @SuppressWarnings("SameParameterValue")
    private void addAnnotationToDefinition(Element definitionElement, QName qname) {
        addAnnotationToDefinition(definitionElement, qname, null);
    }

    private void addAnnotationToDefinition(Element definitionElement, QName qname, QName value) {
        Element annotationElement = getOrCreateElement(new QName(W3C_XML_SCHEMA_NS_URI, "annotation"), definitionElement);
        Element appinfoElement = getOrCreateElement(new QName(W3C_XML_SCHEMA_NS_URI, "appinfo"), annotationElement);
        if (value == null) {
            addEmptyAnnotation(qname, appinfoElement);
        } else {
            addAnnotation(qname, value, appinfoElement);
        }
    }

    private Element getOrCreateElement(QName qName, Element parentElement) {
        NodeList elements = parentElement.getElementsByTagNameNS(qName.getNamespaceURI(), qName.getLocalPart());
        if (elements.getLength() == 0) {
            Element element = createElement(qName);
            Element refChild = DOMUtil.getFirstChildElement(parentElement);
            parentElement.insertBefore(element, refChild);
            return element;
        }
        return (Element)elements.item(0);
    }

    /** Create XML element with the correct namespace prefix and namespace definition. */
    private Element createElement(QName qname) {
        QName qnameWithPrefix = namespacePrefixMapper.setQNamePrefix(qname);
        addToImports(qname.getNamespaceURI());
        return DOMUtil.createElement(document, qnameWithPrefix, documentRootElement, documentRootElement);
    }

    private Element createRootElement(QName qname) {
        return DOMUtil.createElement(document, namespacePrefixMapper.setQNamePrefix(qname));
    }

    private void setMultiplicityAttribute(Element itemElement, String attrName, int attrValue) {
        if (attrValue != 1) {
            itemElement.setAttribute(attrName, XsdTypeMapper.multiplicityToString(attrValue));
        }
    }

    /** As {@link #setQNameAttribute(Element, String, QName)} but does not deal with QName prefix. */
    @SuppressWarnings("SameParameterValue")
    private void setAttribute(Element element, String attrName, QName attrValue) {
        DOMUtil.setQNameAttribute(element, attrName, attrValue, documentRootElement);
    }

    private void setAttribute(Element element, String attrName, String attrValue) {
        DOMUtil.setAttributeValue(element, attrName, attrValue);
    }

    /**
     * Set attribute in the DOM element to a QName value. This will make sure that the
     * appropriate namespace definition for the QName exists.
     *
     * @param element element element element where to set attribute
     * @param attrName attribute name (String)
     * @param value attribute value (Qname)
     */
    private void setQNameAttribute(Element element, String attrName, QName value) {
        QName valueWithPrefix = namespacePrefixMapper.setQNamePrefix(value);
        DOMUtil.setQNameAttribute(element, attrName, valueWithPrefix, documentRootElement);
        addToImports(value.getNamespaceURI());
    }

    /**
     * Make sure that the namespace will be added to import definitions.
     * @param namespace namespace to import
     */
    private void addToImports(String namespace) {
        importNamespaces.add(namespace);
    }

    /**
     * Adds import definition to XSD.
     * It adds imports of namespaces that accumulated during schema processing in the importNamespaces list.
     */
    private void addImports() {
        for (String namespace : importNamespaces) {
            if (W3C_XML_SCHEMA_NS_URI.equals(namespace)) {
                continue;
            }
            if (schemaNamespace.equals(namespace)) {
                continue; //we don't want to import target namespace
            }
            documentRootElement.insertBefore(createImport(namespace), documentRootElement.getFirstChild());
        }
    }

    /** Creates single import XSD element. */
    private Element createImport(String namespace) {
        Element element = createElement(new QName(W3C_XML_SCHEMA_NS_URI, "import"));
        element.setAttribute("namespace", namespace);
        return element;
    }

    private AppInfoSerializationTarget createAppInfoAnnotationsTarget(Element element) {
        Element annotation = createElement(new QName(W3C_XML_SCHEMA_NS_URI, "annotation"));
        // We need to append this element to the tree for QName resolution to work.
        // Even if we will remove it later (if it's empty) - in "removeIfNotNeeded" method below.
        element.appendChild(annotation);
        Element documentation = createElement(new QName(W3C_XML_SCHEMA_NS_URI, "documentation"));
        annotation.appendChild(documentation);
        Element appinfo = createElement(new QName(W3C_XML_SCHEMA_NS_URI, "appinfo"));
        annotation.appendChild(appinfo);
        return new AppInfoSerializationTarget(element, annotation, documentation, appinfo, this);
    }

    private record AppInfoSerializationTarget(
            Element parent,
            Element annotationElement,
            Element documentation,
            Element appInfoElement,
            SchemaDomSerializer schemaSerializer)
            implements SerializationTarget {

        @Override
        public void addAnnotation(QName qname, Boolean value) {
            if (value != null) {
                addNewElement(qname)
                        .setTextContent(Boolean.toString(value));
            }
        }

        @Override
        public void addAnnotation(QName qname, String value) {
            if (value != null) {
                addNewElement(qname)
                        .setTextContent(value);
            }
        }

        @Override
        public void addAnnotation(QName qname, QName value) {
            if (value != null) {
                DOMUtil.setQNameValue(
                        addNewElement(qname),
                        value);
            }
        }

        @Override
        public void addRefAnnotation(QName qname, QName value) {
            if (value != null) {
                DOMUtil.setQNameAttribute(
                        addNewElement(qname),
                        A_REF.getLocalPart(),
                        value);
            }
        }

        private Element addNewElement(QName qname) {
            Element annotation = schemaSerializer.createElement(qname);
            appInfoElement.appendChild(annotation);
            return annotation;
        }

        void removeIfNotNeeded() {
            if (!appInfoElement.hasChildNodes()) {
                annotationElement.removeChild(appInfoElement);
            }
            if (StringUtils.isEmpty(documentation.getTextContent())) {
                annotationElement.removeChild(documentation);
            }
            if (!annotationElement.hasChildNodes()) {
                parent.removeChild(annotationElement); // remove empty <annotation> element
            }
        }
    }
}
