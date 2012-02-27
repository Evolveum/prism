/**
 * Copyright (c) 2011 Evolveum
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * http://www.opensource.org/licenses/cddl1 or
 * CDDLv1.0.txt file in the source code distribution.
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * Portions Copyrighted 2011 [name of copyright owner]
 */
package com.evolveum.midpoint.prism.dom;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.namespace.QName;

import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * @author semancik
 *
 */
public class AttributeNamedNodeMapImpl implements NamedNodeMap {
	
	private List<AttributeNodeImpl> attributeList;
	private ElementPrismAbstractImpl parent;

	AttributeNamedNodeMapImpl(Map<String, String> attributeMap, String namespace, ElementPrismAbstractImpl parent) {
		this.attributeList = convert(attributeMap, namespace);
		this.parent = parent;
	}

	private List<AttributeNodeImpl> convert(Map<String, String> map, String namespace) {
		List<AttributeNodeImpl> list = new ArrayList<AttributeNodeImpl>();
		if (map != null) {
			int i = 0;
			for (Entry<String, String> entry: map.entrySet()) {
				list.add(createAttr(entry.getKey(), entry.getValue(), namespace, i));
				i++;
			}
		}
		return list;
	}

	private AttributeNodeImpl createAttr(String name, String value, String namespace, int index) {
		QName attrQName = new QName(namespace, name);
		AttributeNodeImpl attr = new AttributeNodeImpl(attrQName, value);
		attr.setIndex(index);
		attr.setNodeMap(this);
		attr.setParent(parent);
		return attr;
	}

	/* (non-Javadoc)
	 * @see org.w3c.dom.NamedNodeMap#getNamedItem(java.lang.String)
	 */
	@Override
	public Node getNamedItem(String name) {
		for (AttributeNodeImpl attr: attributeList) {
			if (name.equals(attr.getName())) {
				return attr;
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.w3c.dom.NamedNodeMap#setNamedItem(org.w3c.dom.Node)
	 */
	@Override
	public Node setNamedItem(Node arg) throws DOMException {
		throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR, "This kind of modification is not supported (yet)");
	}

	/* (non-Javadoc)
	 * @see org.w3c.dom.NamedNodeMap#removeNamedItem(java.lang.String)
	 */
	@Override
	public Node removeNamedItem(String name) throws DOMException {
		throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR, "This kind of modification is not supported (yet)");
	}

	/* (non-Javadoc)
	 * @see org.w3c.dom.NamedNodeMap#item(int)
	 */
	@Override
	public Node item(int index) {
		return attributeList.get(index);
	}

	/* (non-Javadoc)
	 * @see org.w3c.dom.NamedNodeMap#getLength()
	 */
	@Override
	public int getLength() {
		return attributeList.size();
	}

	/* (non-Javadoc)
	 * @see org.w3c.dom.NamedNodeMap#getNamedItemNS(java.lang.String, java.lang.String)
	 */
	@Override
	public Node getNamedItemNS(String namespaceURI, String localName) throws DOMException {
		// TODO: not really correct
		for (AttributeNodeImpl attr: attributeList) {
			if (localName.equals(attr.getName()) && namespaceURI.equals(attr.getNamespaceURI())) {
				return attr;
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.w3c.dom.NamedNodeMap#setNamedItemNS(org.w3c.dom.Node)
	 */
	@Override
	public Node setNamedItemNS(Node arg) throws DOMException {
		throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR, "This kind of modification is not supported (yet)");
	}

	/* (non-Javadoc)
	 * @see org.w3c.dom.NamedNodeMap#removeNamedItemNS(java.lang.String, java.lang.String)
	 */
	@Override
	public Node removeNamedItemNS(String namespaceURI, String localName) throws DOMException {
		throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR, "This kind of modification is not supported (yet)");
	}

	public Node getPrevious(AttributeNodeImpl attributeNodeImpl) {
		int index = attributeNodeImpl.getIndex();
		if (index == 0) {
			return null;
		}
		return attributeList.get(index - 1);
	}

	public Node getNext(AttributeNodeImpl attributeNodeImpl) {
		int index = attributeNodeImpl.getIndex();
		if (index > (attributeList.size() - 1)) {
			return null;
		}
		return attributeList.get(index + 1);
	}

}
