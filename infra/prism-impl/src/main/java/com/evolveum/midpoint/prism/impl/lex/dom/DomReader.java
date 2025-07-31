/*
 * Copyright (c) 2020 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism.impl.lex.dom;

import com.evolveum.concepts.SourceLocation;
import com.evolveum.concepts.ValidationLogType;
import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.impl.lex.ValidatorUtil;
import com.evolveum.midpoint.prism.impl.xnode.*;

import com.evolveum.midpoint.prism.schema.SchemaRegistry;
import com.evolveum.midpoint.prism.schema.SchemaRegistryState;
import com.evolveum.midpoint.prism.xnode.MapXNode;
import com.evolveum.midpoint.prism.xnode.MetadataAware;
import com.evolveum.midpoint.util.DOMUtil;
import com.evolveum.midpoint.util.QNameUtil;
import com.evolveum.midpoint.util.exception.SchemaException;

import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * TODO
 */
class DomReader {

    public static final Trace LOGGER = TraceManager.getTrace(DomLexicalProcessor.class);

    private static final QName SCHEMA_ELEMENT_QNAME = DOMUtil.XSD_SCHEMA_ELEMENT;
    static final String VALUE_LOCAL_PART = "_value";
    static final String METADATA_LOCAL_PART = "_metadata";

    private final Element root;
    private final QName rootElementName;
    private final SchemaRegistry schemaRegistry;
    private final XNodeDefinition.Root schema;
    private final PrismNamespaceContext rootContext;

    @NotNull private final QName valueElementName;
    @NotNull private final QName metadataElementName;

    @NotNull private final ParsingContext parsingContext;
    boolean isValidation;

    DomReader(@NotNull Element root, SchemaRegistry schemaRegistry, PrismNamespaceContext rootContext, @NotNull ParsingContext parsingContext) {
        this.root = root;
        this.rootElementName = DOMUtil.getQName(root);
        this.schemaRegistry = schemaRegistry;
        this.valueElementName = new QName(schemaRegistry.getDefaultNamespace(), VALUE_LOCAL_PART);
        this.metadataElementName = new QName(schemaRegistry.getDefaultNamespace(), DomReader.METADATA_LOCAL_PART);
        this.rootContext = rootContext;
        this.schema = XNodeDefinition.root(schemaRegistry);
        this.parsingContext = parsingContext;
        this.isValidation = parsingContext.isValidation();
    }

    DomReader(Document document, SchemaRegistry schemaRegistry, ParsingContext parsingContext) {
        this(document, schemaRegistry, PrismNamespaceContext.EMPTY, parsingContext);
    }

    DomReader(Document document, SchemaRegistry schemaRegistry, PrismNamespaceContext nsContext, ParsingContext parsingContext) {
        this(DOMUtil.getFirstChildElement(document), schemaRegistry, nsContext, parsingContext);
    }

    @NotNull
    List<RootXNodeImpl> readObjects() throws SchemaException {
        if (rootIsNotObjectsMarker()) {
            return Collections.singletonList(read());
        } else {
            List<RootXNodeImpl> rv = new ArrayList<>();
            PrismNamespaceContext context = rootContext.childContext(DOMUtil.getNamespaceDeclarationsNonNull(root));
            for (Element child : DOMUtil.listChildElements(root)) {
                rv.add(new DomReader(child, schemaRegistry, context.inherited(), parsingContext).read());
            }
            return rv;
        }
    }

    @NotNull RootXNodeImpl read() throws SchemaException {
        RootXNodeImpl xroot = new RootXNodeImpl(rootElementName, rootContext);
        XNodeImpl xnode = readElementContent(root, null, schema, rootContext, false);
        xroot.setSubnode(xnode);
        return xroot;
    }

    private boolean rootIsNotObjectsMarker() {
        QName objectsMarker = PrismContext.get().getObjectsElementName();
        return objectsMarker != null && !QNameUtil.match(rootElementName, objectsMarker);
    }

    /**
     * Reads the content of the element.
     *
     * @param knownElementName Pre-fetched element name. Might be null (this is expected if storeElementName is true).
     * @param parentContext
     */
    @NotNull
    private XNodeImpl readElementContent(@NotNull Element element, @Nullable XNodeDefinition itemDef, @NotNull XNodeDefinition parentDef, PrismNamespaceContext parentContext, boolean storeElementName) throws SchemaException {
        XNodeImpl node;
        // If definition is not resolved already, resolve it
        itemDef = itemDef != null ? itemDef : parentDef.child(DOMUtil.getQName(element));

        QName xsiType = DOMUtil.resolveXsiType(element);
        if(xsiType != null) {
            itemDef = itemDef.withType(xsiType);
        }
        QName elementName = itemDef.getName();

        // FIXME: read namespaces
        Map<String, String> localNamespaces = DOMUtil.getNamespaceDeclarationsNonNull(element);
        PrismNamespaceContext localNsCtx = parentContext.childContext(localNamespaces);

        Element valueChild = DOMUtil.getMatchingChildElement(element, valueElementName);

        if (valueChild != null) {
            node = readElementContent(valueChild, itemDef.valueDef(), parentDef, localNsCtx, false);
        } else if (DOMUtil.hasChildElements(element) || DOMUtil.hasApplicationAttributes(element)) {
            if (isList(element, itemDef, xsiType)) {
                node = readElementContentToList(element, itemDef, localNsCtx);
            } else {
                node = readElementContentToMap(element, itemDef, localNsCtx);
            }
        } else if (DOMUtil.isMarkedAsIncomplete(element)) {
            // Note that it is of no use to check for "incomplete" on non-leaf elements. In XML the incomplete attribute
            // must be attached to an empty element.
            node = new IncompleteMarkerXNodeImpl();
        } else {
            node = parsePrimitiveElement(element, localNsCtx);
        }

//        System.out.println(node + " ===>>> " + getSourceLocation(element));
        ValidatorUtil.setPositionToXNode(parsingContext, node, getSourceLocation(element));

        readMetadata(element, node, localNsCtx);
        readMaxOccurs(element, node);

        setTypeAndElementName(xsiType, elementName, node, storeElementName);

        return node;
    }

    private void readMetadata(@NotNull Element element, XNodeImpl node, PrismNamespaceContext parentNsContext) throws SchemaException {
        List<Element> metadataChildren = DOMUtil.getMatchingChildElements(element, metadataElementName);
        for (Element metadataChild : metadataChildren) {
            XNodeImpl metadata = readElementContent(metadataChild, schema.metadataDef(), schema, parentNsContext, false);
            if (metadata instanceof MapXNode) {
                if (node instanceof MetadataAware) {
                    ((MetadataAware) node).addMetadataNode((MapXNode) metadata);
                } else {
                    String msg = "Attempt to add metadata to non-metadata-aware XNode: %s";
                    parsingContext.validationLogger(false, ValidationLogType.ERROR,
                            node.getSourceLocation(), msg.formatted(node),
                            msg, "");
                    throw new SchemaException(String.format(msg, node));
                }
            } else {
                String msg = "Metadata is not of Map type:  %s";
                parsingContext.validationLogger(false, ValidationLogType.ERROR,
                        node.getSourceLocation(), msg.formatted(metadata),
                        msg, "");
                throw new SchemaException(String.format(msg, metadata));
            }
        }
    }

    private void readMaxOccurs(Element element, XNodeImpl xnode) throws SchemaException {
        String maxOccursString = element.getAttributeNS(
                PrismConstants.A_MAX_OCCURS.getNamespaceURI(),
                PrismConstants.A_MAX_OCCURS.getLocalPart());

        if (!StringUtils.isBlank(maxOccursString)) {
            int maxOccurs = parseMultiplicity(maxOccursString, element);
            xnode.setMaxOccurs(maxOccurs);
        }
    }

    private int parseMultiplicity(String maxOccursString, Element element) throws SchemaException {
        if (PrismConstants.MULTIPLICITY_UNBOUNDED.equals(maxOccursString)) {
            return -1;
        }
        if (maxOccursString.startsWith("-")) {
            return -1;
        }
        if (StringUtils.isNumeric(maxOccursString)) {
            return Integer.parseInt(maxOccursString);
        } else {
            String msg = "Expected numeric value for %s attribute on %s but got %s";
            parsingContext.validationLogger(false, ValidationLogType.ERROR,
                    null, msg.formatted(PrismConstants.A_MAX_OCCURS.getLocalPart(), DOMUtil.getQName(element), maxOccursString),
                    msg, PrismConstants.A_MAX_OCCURS.getLocalPart(), "", maxOccursString);
            throw new SchemaException(String.format(msg, PrismConstants.A_MAX_OCCURS.getLocalPart(), DOMUtil.getQName(element), maxOccursString));
        }
    }

    private void setTypeAndElementName(QName xsiType, QName elementName, XNodeImpl xnode, boolean storeElementName) {
        if (xsiType != null) {
            xnode.setTypeQName(xsiType);
            xnode.setExplicitTypeDeclaration(true);
        }
        if (storeElementName) {
            xnode.setElementName(elementName);
        }
    }

    // all the sub-elements should be compatible (this is not enforced here, however)
    private ListXNodeImpl readElementContentToList(Element element, XNodeDefinition parentDef, PrismNamespaceContext parentNsContext) throws SchemaException {
        if (DOMUtil.hasApplicationAttributes(element)) {
            String msg = "List should have no application attributes: %s";
            parsingContext.validationLogger(false, ValidationLogType.ERROR,
                    null, msg.formatted(element),
                    msg, "");
            throw new SchemaException(String.format(msg, element));
        }
        return parseElementList(DOMUtil.listChildElements(element), null, parentDef, parentNsContext, true);
    }

    private MapXNodeImpl readElementContentToMap(Element element, @NotNull XNodeDefinition parentDef, PrismNamespaceContext localNsContext) throws SchemaException {
        MapXNodeImpl xmap = new MapXNodeImpl(localNsContext);

        // Attributes
        for (Attr attr : DOMUtil.listApplicationAttributes(element)) {
            QName attrQName = DOMUtil.getQName(attr);
            XNodeImpl subnode = parseAttributeValue(attr, localNsContext.inherited());
            xmap.put(attrQName, subnode);
        }

        // Sub-elements
        QName lastName = null;
        List<Element> lastElements = null;
        for (Element childElement : DOMUtil.listChildElements(element)) {
            QName childName = DOMUtil.getQName(childElement);
            if (QNameUtil.match(childName, metadataElementName)) {
                continue;
            }
            if (!match(childName, lastName)) {
                parseSubElementsGroupAsMapEntry(xmap, lastElements, lastName, parentDef, localNsContext);
                lastName = childName;
                lastElements = new ArrayList<>();
            }
            lastElements.add(childElement);
        }
        parseSubElementsGroupAsMapEntry(xmap, lastElements, lastName, parentDef, localNsContext);
        return xmap;
    }

    private boolean isList(@NotNull Element element, @NotNull @Nullable XNodeDefinition itemDef, @Nullable QName xsiType) {
        String isListAttribute = DOMUtil.getAttribute(element, DOMUtil.IS_LIST_ATTRIBUTE_NAME);
        if (StringUtils.isNotEmpty(isListAttribute)) {
            return Boolean.parseBoolean(isListAttribute);
        }
        // enable this after schema registry is optional (now it's mandatory)
//        if (schemaRegistry == null) {
//            return false;
//        }

        SchemaRegistryState.IsList fromSchema = schemaRegistry.isList(xsiType, itemDef.getName());
        if (fromSchema != SchemaRegistryState.IsList.MAYBE) {
            return fromSchema == SchemaRegistryState.IsList.YES;
        }

        // checking the content
        if (DOMUtil.hasApplicationAttributes(element)) {
            return false;        // TODO - or should we fail in this case?
        }
        //System.out.println("Elements are compatible: " + DOMUtil.listChildElements(element) + ": " + rv);
        return elementsAreCompatible(DOMUtil.listChildElements(element));
    }

    private boolean elementsAreCompatible(List<Element> elements) {
        QName unified = null;
        for (Element element : elements) {
            QName root = getHierarchyRoot(DOMUtil.getQName(element));
            if (unified == null) {
                unified = root;
            } else if (!QNameUtil.match(unified, root)) {
                return false;
            } else if (QNameUtil.noNamespace(unified) && QNameUtil.hasNamespace(root)) {
                unified = root;
            }
        }
        return true;
    }

    private QName getHierarchyRoot(QName name) {
        ItemDefinition<?> def = schemaRegistry.findItemDefinitionByElementName(name);
        if (def == null || !def.isHeterogeneousListItem()) {
            return name;
        } else {
            return def.getSubstitutionHead();
        }
    }

    private boolean match(QName name, QName existing) {
        if (existing == null) {
            return false;
        } else {
            return QNameUtil.match(name, existing);
        }
    }

    // All elements share the same elementName
    private void parseSubElementsGroupAsMapEntry(MapXNodeImpl xmap, List<Element> elements, QName itemName, @NotNull XNodeDefinition parentDef, PrismNamespaceContext parentNsContext) throws SchemaException {
        if (elements == null || elements.isEmpty()) {
            return;
        }
        XNodeImpl xsub;
        XNodeDefinition itemDef = parentDef.child(itemName);

        // We really want to have equals here, not match
        // we want to be very explicit about namespace here
        if (itemDef.getName().equals(SCHEMA_ELEMENT_QNAME)) {
            if (elements.size() == 1) {
                xsub = parseSchemaElement(elements.iterator().next(), parentNsContext);
            } else {
                String msg = "Too many schema elements";
                parsingContext.validationLogger(false, ValidationLogType.ERROR,
                        null, "", msg);
                throw new SchemaException(msg);
            }
        } else if (elements.size() == 1) {
            xsub = readElementContent(elements.get(0), itemDef, parentDef, parentNsContext, false);
        } else {
            xsub = parseElementList(elements, itemDef, parentDef, parentNsContext, false);
        }
        xmap.merge(itemDef.getName(), xsub);
    }

    /**
     * Parses elements that should form the list.
     * <p>
     * Either they have the same element name, or they are stored as a sub-elements of "list" parent element.
     * @param parentNsContext
     * @param parentDef
     */
    @NotNull
    @Contract("!null, null, false -> fail")
    private ListXNodeImpl parseElementList(List<Element> elements, @Nullable XNodeDefinition itemDef, @NotNull XNodeDefinition parentDef, PrismNamespaceContext parentNsContext, boolean storeElementNames) throws SchemaException {
        if (!storeElementNames && itemDef == null) {
            String msg = "When !storeElementNames the element name must be specified";
            parsingContext.validationLogger(false, ValidationLogType.ERROR,
                    null, "", msg);

            throw new IllegalArgumentException(msg);
        }
        ListXNodeImpl xlist = new ListXNodeImpl(parentNsContext);
        for (Element element : elements) {
            xlist.add(readElementContent(element, itemDef, parentDef, parentNsContext, storeElementNames));
        }
        return xlist;
    }

    // @pre element has no children nor application attributes
    private <T> PrimitiveXNodeImpl<T> parsePrimitiveElement(@NotNull Element element, PrismNamespaceContext localNsContext) {
        PrimitiveXNodeImpl<T> xnode = new PrimitiveXNodeImpl<>(localNsContext);
        xnode.setValueParser(new NamespaceAwareValueParser<>(element.getTextContent(), localNsContext));
        return xnode;
    }

    private <T> PrimitiveXNodeImpl<T> parseAttributeValue(@NotNull Attr attr, PrismNamespaceContext localNsContext) {
        PrimitiveXNodeImpl<T> xnode = new PrimitiveXNodeImpl<>(localNsContext);
        xnode.setValueParser(new NamespaceAwareValueParser<>(attr.getTextContent(), localNsContext));
        xnode.setAttribute(true);
        return xnode;
    }

    @NotNull
    private SchemaXNodeImpl parseSchemaElement(Element schemaElement, PrismNamespaceContext localNsContext) {
        SchemaXNodeImpl xschema = new SchemaXNodeImpl(localNsContext);
        xschema.setSchemaElement(schemaElement);
        return xschema;
    }

    private SourceLocation getSourceLocation(Element element) {
        if (isValidation && element != null) {
            var sourceLocation = element.getUserData(ValidatorUtil.SOURCE_LOCATION_OF_ELEMENT_KEY);
            if (sourceLocation == null) {
                sourceLocation = SourceLocation.unknown();
            }

            return (SourceLocation) sourceLocation;
        }

        return null;
    }
}
