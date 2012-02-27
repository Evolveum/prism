package com.evolveum.midpoint.prism.dom;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.evolveum.midpoint.prism.Item;
import com.evolveum.midpoint.prism.PrismConstants;
import com.evolveum.midpoint.prism.PrismContainer;
import com.evolveum.midpoint.prism.PrismContainerValue;
import com.evolveum.midpoint.prism.PrismProperty;
import com.evolveum.midpoint.prism.PrismPropertyValue;
import com.evolveum.midpoint.prism.PrismValue;
import com.evolveum.midpoint.prism.xml.XmlTypeConverter;

public class ElementPrismContainerImpl<T> extends ElementPrismAbstractImpl {
	
	private NodeListPrismContainerImpl<T> valueNodeList;
	
	public ElementPrismContainerImpl(PrismContainerValue<T> value) {
		super(value);
		this.valueNodeList = new NodeListPrismContainerImpl<T>(this);
	}
	
	protected PrismContainerValue<T> getValue() {
		return (PrismContainerValue<T>)super.getValue();
	}
	
	protected PrismContainer<T> getContainer() {
		return (PrismContainer<T>)getItem();
	}

	@Override
	public String getTextContent() throws DOMException {
		return "";
	}
			
	protected NodeList getValueNodeList() {
		return valueNodeList;
	}

	@Override
	public NodeList getElementsByTagName(String name) {
		return new NodeListPrismContainerImpl(this, null, name);
	}

	@Override
	public NodeList getElementsByTagNameNS(String namespaceURI, String localName) throws DOMException {
		return new NodeListPrismContainerImpl(this, namespaceURI, localName);
	}

	@Override
	public Node insertBefore(Node newChild, Node refChild) throws DOMException {
		throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR, "This kind of modification is not supported (yet)");
	}

	@Override
	public Node replaceChild(Node newChild, Node oldChild) throws DOMException {
		throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR, "This kind of modification is not supported (yet)");
	}

	@Override
	public Node removeChild(Node oldChild) throws DOMException {
		throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR, "This kind of modification is not supported (yet)");
	}

	@Override
	public Node appendChild(Node newChild) throws DOMException {
		throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR, "This kind of modification is not supported (yet)");
	}

	@Override
	public boolean hasChildNodes() {
		return (!valueNodeList.isEmpty());
	}

	@Override
	public Node cloneNode(boolean deep) {
		throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "DOM clonning is not supported (yet)");
	}

	// ATTRIBUTES
	
	// TODO: namespace declarations for qnames
	
	protected Map<String,String> getAttributeMap() {
		Map<String,String> map = new HashMap<String, String>();
		if (getValue().getId() != null) {
			map.put(PrismConstants.ATTRIBUTE_ID_LOCAL_NAME, getValue().getId());
		}
		return map;
	}
	
}
