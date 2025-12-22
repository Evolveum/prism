package com.evolveum.midpoint.prism.impl.lex.dom;

import com.evolveum.concepts.SourceLocation;
import com.evolveum.midpoint.prism.impl.lex.ValidatorUtil;

import com.evolveum.midpoint.util.DOMUtil;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.*;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by Dominik.
 */
public class StreamDomBuilder {

    private final DocumentBuilder documentBuilder;

    public StreamDomBuilder() throws ParserConfigurationException {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        documentBuilder = documentBuilderFactory.newDocumentBuilder();
    }

    /**
     * Building XML dom tree with exact position to store as UserData of Element of key {@link com.evolveum.midpoint.prism.impl.lex.ValidatorUtil.#SOURCE_LOCATION_OF_ELEMENT_KEY}.)
     * @param is
     * @return
     * @throws Exception
     */
    public Document parse(InputStream is) throws XMLStreamException {
        Document doc = documentBuilder.newDocument();

        XMLInputFactory factory = XMLInputFactory.newInstance();
        XMLStreamReader reader = factory.createXMLStreamReader(new InputStreamReader(is));

        Element current = null;

        while (reader.hasNext()) {
            int event = reader.next();

            switch (event) {
                case XMLStreamConstants.START_ELEMENT -> {
                    String localName = reader.getLocalName();
                    String prefix = reader.getPrefix();
                    String qName = (prefix != null && !prefix.isEmpty()) ? prefix + ":" + localName : localName;
                    String namespaceURI = reader.getNamespaceURI();

                    Element elem = doc.createElementNS(namespaceURI, qName);

                    Location location = reader.getLocation();
                    elem.setUserData(ValidatorUtil.SOURCE_LOCATION_OF_ELEMENT_KEY,
                            SourceLocation.from(SourceLocation.unknown().getSource(), location.getLineNumber(), location.getColumnNumber()), null);

                    // Add attributes (with namespace and prefix)
                    for (int i = 0; i < reader.getAttributeCount(); i++) {
                        String attrLocal = reader.getAttributeLocalName(i);
                        String attrPrefix = reader.getAttributePrefix(i);
                        String attrQName = (attrPrefix != null && !attrPrefix.isEmpty())
                                ? attrPrefix + ":" + attrLocal : attrLocal;
                        String attrNS = reader.getAttributeNamespace(i);
                        String attrValue = reader.getAttributeValue(i);

                        Attr attr = doc.createAttributeNS(attrNS, attrQName);
                        attr.setValue(attrValue);
                        elem.setAttributeNodeNS(attr);
                    }

                    // Add namespace with prefix
                    for (int i = 0; i < reader.getNamespaceCount(); i++) {
                        String attrNamespacePrefix = reader.getNamespacePrefix(i);
                        String attrQName = (attrNamespacePrefix != null && !attrNamespacePrefix.isEmpty())
                                ? DOMUtil.W3C_XML_SCHEMA_XMLNS_PREFIX + ":" + attrNamespacePrefix : DOMUtil.W3C_XML_SCHEMA_XMLNS_PREFIX;

                        Attr attr = doc.createAttributeNS(DOMUtil.W3C_XML_SCHEMA_XMLNS_URI, attrQName);
                        attr.setValue(reader.getNamespaceURI(i));
                        elem.setAttributeNodeNS(attr);
                    }

                    if (current != null) {
                        current.appendChild(elem);
                    } else {
                        doc.appendChild(elem);
                    }

                    current = elem;
                }

                case XMLStreamConstants.CHARACTERS -> {
                    if (current != null) {
                        String text = reader.getText();
                        if (!text.isBlank()) {
                            current.appendChild(doc.createTextNode(text));
                        }
                    }
                }

                case XMLStreamConstants.END_ELEMENT -> {
                    if (current != null) {
                        Node parent = current.getParentNode();
                        if (parent instanceof Element) {
                            current = (Element) parent;
                        } else {
                            current = null;
                        }
                    }
                }
            }
        }

        reader.close();
        return doc;
    }
}
