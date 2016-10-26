/*
 * Copyright (c) 2010-2016 Evolveum
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.evolveum.midpoint.prism.lex.dom;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.marshaller.XNodeProcessorEvaluationMode;
import com.evolveum.midpoint.prism.marshaller.XPathHolder;
import com.evolveum.midpoint.prism.lex.LexicalProcessor;
import com.evolveum.midpoint.prism.lex.LexicalUtils;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.prism.schema.SchemaRegistry;
import com.evolveum.midpoint.prism.xml.XmlTypeConverter;
import com.evolveum.midpoint.prism.xnode.*;
import com.evolveum.midpoint.util.DOMUtil;
import com.evolveum.midpoint.util.PrettyPrinter;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;
import com.evolveum.prism.xml.ns._public.types_3.ItemPathType;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DomLexicalProcessor implements LexicalProcessor<String> {

	public static final Trace LOGGER = TraceManager.getTrace(DomLexicalProcessor.class);

	private static final QName SCHEMA_ELEMENT_QNAME = DOMUtil.XSD_SCHEMA_ELEMENT;
	
	private SchemaRegistry schemaRegistry;

	public DomLexicalProcessor(SchemaRegistry schemaRegistry) {
		super();
		this.schemaRegistry = schemaRegistry;
	}
	
	@Deprecated
	public XNode read(File file, ParsingContext parsingContext) throws SchemaException, IOException {
		return read(new ParserFileSource(file), parsingContext);
	}

	@NotNull
	@Override
	public RootXNode read(@NotNull ParserSource source, @NotNull ParsingContext parsingContext) throws SchemaException, IOException {
		if (source instanceof ParserElementSource) {
			return read(((ParserElementSource) source).getElement());
		}

		InputStream is = source.getInputStream();
		try {
			Document document = DOMUtil.parse(is);
			return read(document);
		} finally {
			if (source.closeStreamAfterParsing()) {
				IOUtils.closeQuietly(is);
			}
		}
	}

	@NotNull
	@Override
	public List<RootXNode> readObjects(ParserSource source, ParsingContext parsingContext) throws SchemaException, IOException {
		InputStream is = source.getInputStream();
		try {
			Document document = DOMUtil.parse(is);
			return readObjects(document);
		} finally {
			if (source.closeStreamAfterParsing()) {
				IOUtils.closeQuietly(is);
			}
		}
	}

	private List<RootXNode> readObjects(Document document) throws SchemaException{
		Element root = DOMUtil.getFirstChildElement(document);
		// TODO: maybe some check if this is a collection of other objects???
		List<Element> children = DOMUtil.listChildElements(root);
		List<RootXNode> nodes = new ArrayList<>();
		for (Element child : children){
			RootXNode xroot = read(child);
			nodes.add(xroot);
		}
		return nodes;
	}

	@NotNull
	public RootXNode read(Document document) throws SchemaException {
		Element rootElement = DOMUtil.getFirstChildElement(document);
		return read(rootElement);
	}

	@NotNull
	public RootXNode read(Element rootElement) throws SchemaException{
		RootXNode xroot = new RootXNode(DOMUtil.getQName(rootElement));
		extractCommonMetadata(rootElement, xroot);
		XNode xnode = parseElementContent(rootElement);
		xroot.setSubnode(xnode);
		return xroot;
	}
	
	private void extractCommonMetadata(Element element, XNode xnode) throws SchemaException {
		QName xsiType = DOMUtil.resolveXsiType(element);
		if (xsiType != null) {
			xnode.setTypeQName(xsiType);
			xnode.setExplicitTypeDeclaration(true);
		}

		String maxOccursString = element.getAttributeNS(
				PrismConstants.A_MAX_OCCURS.getNamespaceURI(),
				PrismConstants.A_MAX_OCCURS.getLocalPart());
		if (!StringUtils.isBlank(maxOccursString)) {
			int maxOccurs = parseMultiplicity(maxOccursString, element);
			xnode.setMaxOccurs(maxOccurs);
		}
	}
	
	private int parseMultiplicity(String maxOccursString, Element element) throws SchemaException {
		if (PrismConstants.MULTIPLICITY_UNBONUNDED.equals(maxOccursString)) {
			return -1;
		}
		if (maxOccursString.startsWith("-")) {
			return -1;
		}
		if (StringUtils.isNumeric(maxOccursString)) {
			return Integer.valueOf(maxOccursString);
		} else {
			throw new SchemaException("Expecetd numeric value for " + PrismConstants.A_MAX_OCCURS.getLocalPart()
					+ " attribute on " + DOMUtil.getQName(element) + " but got " + maxOccursString);
		}
	}

	/**
	 * Parses the element into a RootXNode. 
	 */
	public RootXNode parseElementAsRoot(Element element) throws SchemaException {
		RootXNode xroot = new RootXNode(DOMUtil.getQName(element));
		extractCommonMetadata(element, xroot);
		xroot.setSubnode(parseElementContent(element));
		return xroot;
	}
	
	/**
	 * Parses the element in a single-entry MapXNode. 
	 */
	public MapXNode parseElementAsMap(Element element) throws SchemaException {
		MapXNode xmap = new MapXNode();
		extractCommonMetadata(element, xmap);
		xmap.put(DOMUtil.getQName(element), parseElementContent(element));
		return xmap;
	}
	
	/**
	 * Parses the content of the element (the name of the provided element is ignored, only the content is parsed).
	 */
	@Nullable
	public XNode parseElementContent(Element element) throws SchemaException {
		if (DOMUtil.isNil(element)) {
			return null;
		}
		if (DOMUtil.hasChildElements(element) || DOMUtil.hasApplicationAttributes(element)) {
			return parseSubElemets(element);
		} else {
			return parsePrimitiveElement(element);
		}
	}

	private MapXNode parseSubElemets(Element element) throws SchemaException {
		MapXNode xmap = new MapXNode();
		extractCommonMetadata(element, xmap);
		
		// Attributes
		for (Attr attr: DOMUtil.listApplicationAttributes(element)) {
			QName attrQName = DOMUtil.getQName(attr);
			XNode subnode = parseAttributeValue(attr);
			xmap.put(attrQName, subnode);
		}

		// Subelements
		QName lastElementQName = null;
		List<Element> lastElements = null;
		for (Element childElement: DOMUtil.listChildElements(element)) {
			QName childQName = DOMUtil.getQName(childElement);
			if (childQName == null) {
				throw new IllegalArgumentException("Null qname in element "+childElement+", subelement of "+element);
			}
			if (childQName.equals(lastElementQName)) {
				lastElements.add(childElement);
			} else {
				parseElementGroup(xmap, lastElementQName, lastElements);
				lastElementQName = childQName;
				lastElements = new ArrayList<Element>();
				lastElements.add(childElement);
			}
		}
		parseElementGroup(xmap, lastElementQName, lastElements);
				
		return xmap;
	}
	
	private void parseElementGroup(MapXNode xmap, QName elementQName, List<Element> elements) throws SchemaException {
		if (elements == null || elements.isEmpty()) {
			return;
		}
		XNode xsub;
		// We really want to have equals here, not match
		// we want to be very explicit about namespace here
		if (elementQName.equals(SCHEMA_ELEMENT_QNAME)) {
			if (elements.size() == 1) {
				xsub = parseSchemaElement(elements.iterator().next());
			} else {
				throw new SchemaException("Too many schema elements");
			}
		} else if (elements.size() == 1) {
			xsub = parseElementContent(elements.get(0));
		} else {
			xsub = parseElementList(elements); 
		}
		xmap.merge(elementQName, xsub);
	}

	/**
	 * Parses elements that all have the same element name. 
	 */
	private ListXNode parseElementList(List<Element> elements) throws SchemaException {
		ListXNode xlist = new ListXNode();
		for (Element element: elements) {
			XNode xnode = parseElementContent(element);
			xlist.add(xnode);
		}
		return xlist;
	}

    // changed from anonymous to be able to make it static (serializable independently of DomParser)
    private static class PrimitiveValueParser<T> implements ValueParser<T>, Serializable {

        private Element element;

        private PrimitiveValueParser(Element element) {
            this.element = element;
        }

        @Override
        public T parse(QName typeName, XNodeProcessorEvaluationMode mode) throws SchemaException {
            return parsePrimitiveElementValue(element, typeName, mode);
        }
        @Override
        public boolean isEmpty() {
            return DOMUtil.isEmpty(element);
        }
        @Override
        public String getStringValue() {
            return element.getTextContent();
        }
        @Override
        public Map<String, String> getPotentiallyRelevantNamespaces() {
            return DOMUtil.getAllVisibleNamespaceDeclarations(element);
        }
        @Override
        public String toString() {
            return "ValueParser(DOMe, "+PrettyPrinter.prettyPrint(DOMUtil.getQName(element))+": "+element.getTextContent()+")";
        }
    };
    
    private static class PrimitiveAttributeParser<T> implements ValueParser<T>, Serializable {
   			
    	private Attr attr;
    	
    	public PrimitiveAttributeParser(Attr attr) {
			this.attr = attr;
		}
		@Override
		public T parse(QName typeName, XNodeProcessorEvaluationMode mode) throws SchemaException {
			return parsePrimitiveAttrValue(attr, typeName, mode);
		}

		@Override
		public boolean isEmpty() {
			return DOMUtil.isEmpty(attr);
		}

		@Override
		public String getStringValue() {
			return attr.getValue();
		}

		@Override
		public String toString() {
			return "ValueParser(DOMa, " + PrettyPrinter.prettyPrint(DOMUtil.getQName(attr)) + ": "
					+ attr.getTextContent() + ")";
		}

        @Override
        public Map<String, String> getPotentiallyRelevantNamespaces() {
            return DOMUtil.getAllVisibleNamespaceDeclarations(attr);
        }
    }

	private <T> PrimitiveXNode<T> parsePrimitiveElement(final Element element) throws SchemaException {
		PrimitiveXNode<T> xnode = new PrimitiveXNode<T>();
		extractCommonMetadata(element, xnode);
		xnode.setValueParser(new PrimitiveValueParser<T>(element));
		return xnode;
	}
		
	private static <T> T parsePrimitiveElementValue(Element element, QName typeName, XNodeProcessorEvaluationMode mode) throws SchemaException {
		try {
			if (ItemPathType.COMPLEX_TYPE.equals(typeName)) {
				return (T) parsePath(element);
			} else if (DOMUtil.XSD_QNAME.equals(typeName)) {
				return (T) DOMUtil.getQNameValue(element);
			} else if (XmlTypeConverter.canConvert(typeName)) {
				return (T) XmlTypeConverter.toJavaValue(element, typeName);
			} else if (DOMUtil.XSD_ANYTYPE.equals(typeName)) {
				return (T) element.getTextContent();                // if parsing primitive as xsd:anyType, we can safely parse it as string
			} else {
				throw new SchemaException("Cannot convert element '" + element + "' to " + typeName);
			}
		} catch (IllegalArgumentException e) {
			return processIllegalArgumentException(element.getTextContent(), typeName, e, mode);		// primitive way of ensuring compatibility mode
		}
	}

	private static <T> T processIllegalArgumentException(String value, QName typeName, IllegalArgumentException e, XNodeProcessorEvaluationMode mode) {
		if (mode != XNodeProcessorEvaluationMode.COMPAT) {
			throw e;
		}
		LOGGER.warn("Value of '{}' couldn't be parsed as '{}' -- interpreting as null because of COMPAT mode set", value, typeName, e);
		return null;
	}

	private <T> PrimitiveXNode<T> parseAttributeValue(final Attr attr) {
		PrimitiveXNode<T> xnode = new PrimitiveXNode<T>();
		xnode.setValueParser(new PrimitiveAttributeParser<T>(attr));
		xnode.setAttribute(true);
		return xnode;
	}

	private static <T> T parsePrimitiveAttrValue(Attr attr, QName typeName, XNodeProcessorEvaluationMode mode) throws SchemaException {
		if (DOMUtil.XSD_QNAME.equals(typeName)) {
			try {
				return (T) DOMUtil.getQNameValue(attr);
			} catch (IllegalArgumentException e) {
				return processIllegalArgumentException(attr.getTextContent(), typeName, e, mode);		// primitive way of ensuring compatibility mode
			}
		}
		if (XmlTypeConverter.canConvert(typeName)) {
			String stringValue = attr.getTextContent();
			try {
				return XmlTypeConverter.toJavaValue(stringValue, typeName);
			} catch (IllegalArgumentException e) {
				return processIllegalArgumentException(attr.getTextContent(), typeName, e, mode);		// primitive way of ensuring compatibility mode
			}
		} else {
			throw new SchemaException("Cannot convert attribute '"+attr+"' to "+typeName);
		}
	}

	@NotNull
	private static ItemPathType parsePath(Element element) {
		XPathHolder holder = new XPathHolder(element);
		return new ItemPathType(holder.toItemPath());
	}

	private SchemaXNode parseSchemaElement(Element schemaElement) {
		SchemaXNode xschema = new SchemaXNode();
		xschema.setSchemaElement(schemaElement);
		return xschema;
	}

	@Override
	public boolean canRead(@NotNull File file) throws IOException {
		return file.getName().endsWith(".xml");
	}

	@Override
	public boolean canRead(@NotNull String dataString) {
		if (dataString.startsWith("<?xml")) {
			return true;
		}
		Pattern p = Pattern.compile("\\A\\s*?<\\w+");
		Matcher m = p.matcher(dataString);
		if (m.find()) {
			return true;
		}
		return false;
	}

	@NotNull
	@Override
	public String write(@NotNull XNode xnode, @NotNull QName rootElementName, SerializationContext serializationContext) throws SchemaException {
		DomLexicalWriter serializer = new DomLexicalWriter(this, schemaRegistry);
		RootXNode xroot = LexicalUtils.createRootXNode(xnode, rootElementName);
		Element element = serializer.serialize(xroot);
		return DOMUtil.serializeDOMToString(element);
	}

	@NotNull
	@Override
	public String write(@NotNull RootXNode xnode, SerializationContext serializationContext) throws SchemaException {
		DomLexicalWriter serializer = new DomLexicalWriter(this, schemaRegistry);
		Element element = serializer.serialize(xnode);
		return DOMUtil.serializeDOMToString(element);
	}
	
	public Element serializeUnderElement(XNode xnode, QName rootElementName, Element parentElement) throws SchemaException {
		DomLexicalWriter serializer = new DomLexicalWriter(this, schemaRegistry);
		RootXNode xroot = LexicalUtils.createRootXNode(xnode, rootElementName);
		return serializer.serializeUnderElement(xroot, parentElement);
	}

    public Element serializeXMapToElement(MapXNode xmap, QName elementName) throws SchemaException {
		DomLexicalWriter serializer = new DomLexicalWriter(this, schemaRegistry);
		return serializer.serializeToElement(xmap, elementName);
	}

	private Element serializeXPrimitiveToElement(PrimitiveXNode<?> xprim, QName elementName) throws SchemaException {
		DomLexicalWriter serializer = new DomLexicalWriter(this, schemaRegistry);
		return serializer.serializeXPrimitiveToElement(xprim, elementName);
	}

	@NotNull
	public Element writeXRootToElement(@NotNull RootXNode xroot) throws SchemaException {
		DomLexicalWriter serializer = new DomLexicalWriter(this, schemaRegistry);
		return serializer.serialize(xroot);
	}

    private Element serializeToElement(XNode xnode, QName elementName) throws SchemaException {
        Validate.notNull(xnode);
        Validate.notNull(elementName);
		if (xnode instanceof MapXNode) {
			return serializeXMapToElement((MapXNode) xnode, elementName);
		} else if (xnode instanceof PrimitiveXNode<?>) {
			return serializeXPrimitiveToElement((PrimitiveXNode<?>) xnode, elementName);
		} else if (xnode instanceof RootXNode) {
			return writeXRootToElement((RootXNode)xnode);
		} else if (xnode instanceof ListXNode) {
			ListXNode xlist = (ListXNode) xnode;
			if (xlist.size() == 0) {
				return null;
			} else if (xlist.size() > 1) {
				throw new IllegalArgumentException("Cannot serialize list xnode with more than one item: "+xlist);
			} else {
				return serializeToElement(xlist.get(0), elementName);
			}
		} else {
			throw new IllegalArgumentException("Cannot serialized "+xnode+" to element");
		}
	}
	
	public Element serializeSingleElementMapToElement(MapXNode xmap) throws SchemaException {
		if (xmap == null || xmap.isEmpty()) {
			return null;
		}
		Entry<QName, XNode> subEntry = xmap.getSingleSubEntry(xmap.toString());
		Element parent = serializeToElement(xmap, subEntry.getKey());
		return DOMUtil.getFirstChildElement(parent);
	}

	
}
