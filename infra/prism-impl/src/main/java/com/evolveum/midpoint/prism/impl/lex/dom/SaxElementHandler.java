package com.evolveum.midpoint.prism.impl.lex.dom;

import com.evolveum.concepts.SourceLocation;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.util.*;

/**
 * Created by Dominik.
 */
public class SaxElementHandler extends DefaultHandler {

    private Locator locator;
    private final Document document;
    private final Stack<Element> elementStack = new Stack<>();
    private final String sourceLocationKey;

    public SaxElementHandler(String sourceLocationOfElementKey) throws ParserConfigurationException {
        DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        document = docBuilder.newDocument();
        sourceLocationKey = sourceLocationOfElementKey;
    }

    @Override
    public void setDocumentLocator(Locator locator) {
        this.locator = locator;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        Element element;

        if (uri != null && !uri.isEmpty()) {
            element = document.createElementNS(uri, qName);
        } else {
            element = document.createElement(qName);
        }

        element.setUserData(sourceLocationKey,
                SourceLocation.from(null, locator.getLineNumber(), locator.getColumnNumber()),
                null);

        for (int i = 0; i < attributes.getLength(); i++) {
            String attrUri = attributes.getURI(i);
            String attrQName = attributes.getQName(i);
            String value = attributes.getValue(i);

            if (attrUri != null && !attrUri.isEmpty()) {
                element.setAttributeNS(attrUri, attrQName, value);
            } else {
                element.setAttribute(attrQName, value);
            }
        }

        if (!elementStack.isEmpty()) {
            Element parent = elementStack.peek();
            parent.appendChild(element);
        } else {
            document.appendChild(element);
        }

        elementStack.push(element);
    }

    @Override
    public void characters(char[] ch, int start, int length) {
        if (!elementStack.isEmpty()) {
            String text = new String(ch, start, length);
            if (!text.trim().isEmpty()) {
                Text textNode = document.createTextNode(text);
                elementStack.peek().appendChild(textNode);
            }
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) {
        elementStack.pop();
    }

    public Document getDocument() {
        return document;
    }
}
