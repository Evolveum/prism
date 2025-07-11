package com.evolveum.midpoint.prism.impl.lex.dom;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Created by Dominik.
 */
public class XmlPositionTreeBuilder {

    public static class XmlNode {
        String name;
        int line;
        int column;
        Map<String, String> attributes = new HashMap<>();
        List<XmlNode> children = new ArrayList<>();

        XmlNode parent; // Optional for traversal
        public XmlNode(String name, int line, int column) {
            this.name = name;
            this.line = line;
            this.column = column;
        }

        @Override
        public String toString() {
            return name + " (line " + line + ", column " + column + ")";
        }

        public void printTree(String indent) {
            System.out.println(indent + this);
            for (XmlNode child : children) {
                child.printTree(indent + "  ");
            }
        }

        private void collectNodes(XmlNode node, List<XmlNode> list) {
            list.add(node);
            for (XmlNode child : node.children) {
                collectNodes(child, list);
            }

            node.attributes.forEach((name, value) -> {
                collectNodes(new XmlNode(name, node.line, -1), list);
                collectNodes(new XmlNode(value, node.line, -1), list);
            });
        }

        public List<XmlNode> toNodeList() {
            List<XmlNode> nodeList = new ArrayList<>();
            collectNodes(this, nodeList);
            return nodeList;
        }

    }

    static class PositionHandler extends DefaultHandler {
        private Locator locator;
        private XmlNode root;
        private Deque<XmlNode> stack = new ArrayDeque<>();

        @Override
        public void setDocumentLocator(Locator locator) {
            this.locator = locator;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) {
            XmlNode node = new XmlNode(qName, locator.getLineNumber(), locator.getColumnNumber());
            for (int i = 0; i < attributes.getLength(); i++) {
                node.attributes.put(attributes.getQName(i), attributes.getValue(i));
            }

            if (stack.isEmpty()) {
                root = node;
            } else {
                XmlNode parent = stack.peek();
                parent.children.add(node);
                node.parent = parent;
            }
            stack.push(node);
        }

        @Override
        public void endElement(String uri, String localName, String qName) {
            stack.pop();
        }

        public XmlNode getRoot() {
            return root;
        }
    }

    public static XmlNode buildTree(InputStream is) throws ParserConfigurationException, SAXException, IOException {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser saxParser = factory.newSAXParser();
        PositionHandler handler = new PositionHandler();

        saxParser.parse(is, handler);

        return handler.getRoot();
    }
}
