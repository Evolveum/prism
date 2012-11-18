/*
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
 *
 * Portions Copyrighted 2011 [name of copyright owner]
 */
package com.evolveum.midpoint.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlSchema;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;

/**
 * 
 * @author lazyman
 * 
 */
public final class JAXBUtil {

	private static final Trace LOGGER = TraceManager.getTrace(JAXBUtil.class);

	public static String getSchemaNamespace(Package pkg) {
		XmlSchema xmlSchemaAnn = pkg.getAnnotation(XmlSchema.class);
		if (xmlSchemaAnn == null) {
			return null;
		}
		return xmlSchemaAnn.namespace();
	}

	public static <T> String getTypeLocalName(Class<T> type) {
		XmlType xmlTypeAnn = type.getAnnotation(XmlType.class);
		if (xmlTypeAnn == null) {
			return null;
		}
		return xmlTypeAnn.name();
	}

	public static <T> QName getTypeQName(Class<T> type) {
		String namespace = getSchemaNamespace(type.getPackage());
		String localPart = getTypeLocalName(type);
		if (localPart == null) {
			return null;
		}
		return new QName(namespace, localPart);
	}

	public static boolean isElement(Object element) {
		if (element == null) {
			return false;
		}
		if (element instanceof Element) {
			return true;
		} else if (element instanceof JAXBElement) {
			return true;
		} else {
			return false;
		}
	}
	
	public static QName getElementQName(Object element) {
		if (element == null) {
			return null;
		}
		if (element instanceof Element) {
			return DOMUtil.getQName((Element) element);
		} else if (element instanceof JAXBElement) {
			return ((JAXBElement<?>) element).getName();
		} else {
			throw new IllegalArgumentException("Not an element: " + element);
		}
	}

	public static String getElementLocalName(Object element) {
		if (element == null) {
			return null;
		}
		if (element instanceof Element) {
			return ((Element) element).getLocalName();
		} else if (element instanceof JAXBElement) {
			return ((JAXBElement<?>) element).getName().getLocalPart();
		} else {
			throw new IllegalArgumentException("Not an element: " + element);
		}
	}

	/**
	 * Returns short description of element content for diagnostics use (logs,
	 * dumps).
	 * 
	 * Works with DOM and JAXB elements.
	 * 
	 * @param element
	 *            DOM or JAXB element
	 * @return short description of element content
	 */
	public static String getTextContentDump(Object element) {
		if (element == null) {
			return null;
		}
		if (element instanceof Element) {
			return ((Element) element).getTextContent();
		} else {
			return element.toString();
		}
	}

	/**
	 * @param element
	 * @return
	 */
	public static Document getDocument(Object element) {
		if (element instanceof Element) {
			return ((Element) element).getOwnerDocument();
		} else {
			return DOMUtil.getDocument();
		}
	}

	/**
	 * Looks for an element with specified name. Considers both DOM and JAXB
	 * elements. Assumes single element instance in the list.
	 * 
	 * @param elements
	 * @param elementName
	 */
	public static Object findElement(List<Object> elements, QName elementName) {
		if (elements == null) {
			return null;
		}
		for (Object element : elements) {
			if (elementName.equals(getElementQName(element))) {
				return element;
			}
		}
		return null;
	}

	/**
	 * @param parentElement
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static List<Object> listChildElements(Object parentElement) {
		if (parentElement == null) {
			return null;
		}
		List<Object> childElements = new ArrayList<Object>();
		if (parentElement instanceof Element) {
			Element parentEl = (Element) parentElement;
			NodeList childNodes = parentEl.getChildNodes();
			for (int i = 0; i < childNodes.getLength(); i++) {
				Node item = childNodes.item(i);
				if (item.getNodeType() == Node.ELEMENT_NODE) {
					childElements.add(item);
				}
			}
		} else if (parentElement instanceof JAXBElement) {
			JAXBElement<?> jaxbElement = (JAXBElement<?>)parentElement;
			Object jaxbObject = jaxbElement.getValue();
			Method xsdAnyMethod = lookForXsdAnyElementMethod(jaxbObject);
			if (xsdAnyMethod == null) {
				throw new IllegalArgumentException("No xsd any method in "+jaxbObject);
			}
			Object result = null;
			try {
				result = xsdAnyMethod.invoke(jaxbObject);
			} catch (IllegalArgumentException e) {
				throw new IllegalStateException("Unable to invoke xsd any method "+xsdAnyMethod.getName()+" on "+jaxbObject+": "+e.getMessage(),e);
			} catch (IllegalAccessException e) {
				throw new IllegalStateException("Unable to invoke xsd any method "+xsdAnyMethod.getName()+" on "+jaxbObject+": "+e.getMessage(),e);
			} catch (InvocationTargetException e) {
				throw new IllegalStateException("Unable to invoke xsd any method "+xsdAnyMethod.getName()+" on "+jaxbObject+": "+e.getMessage(),e);
			}
			try {
				childElements = (List<Object>)result;
			} catch (ClassCastException e) {
				throw new IllegalStateException("Xsd any method "+xsdAnyMethod.getName()+" on "+jaxbObject+" returned unexpected type "+result.getClass(),e);
			}
		} else {
			throw new IllegalArgumentException("Not an element: " + parentElement + " ("
					+ parentElement.getClass().getName() + ")");
		}
		return childElements;
	}
	
	private static Method lookForXsdAnyElementMethod(Object jaxbObject) {
		Class<? extends Object> jaxbClass = jaxbObject.getClass();
		for (Method method: jaxbClass.getMethods()) {
			for (Annotation annotation: method.getAnnotations()) {
				if (annotation.annotationType().isAssignableFrom(XmlAnyElement.class)) {
					return method;
				}
			}
		}
		return null;
	}

	public static Class findClassForType(QName typeName, Package pkg) {
		XmlSchema xmlSchemaAnnotation = pkg.getAnnotation(XmlSchema.class);
		String namespace = xmlSchemaAnnotation.namespace();
		if (namespace == null) {
			throw new IllegalArgumentException("No namespace annotation in "+pkg);
		}
		if (!namespace.equals(typeName.getNamespaceURI())) {
			throw new IllegalArgumentException("Looking for type in namespace " + typeName.getNamespaceURI() +
					", but the package annotation indicates namespace " + namespace);
		}
		for (Class clazz: ClassPathUtil.listClasses(pkg)) {
			QName foundTypeQName = getTypeQName(clazz);
			if (typeName.equals(foundTypeQName)) {
				return clazz;
			}
		}
		return null;
	}
	
	public static boolean compareElementList(List<Object> aList, List<Object> bList, boolean considerNamespacePrefixes) {
		if (aList.size() != bList.size()) {
			return false;
		}
		Iterator<Object> bIterator = bList.iterator();
		for (Object a: aList) {
			Object b = bIterator.next();
			if (a instanceof Element) {
				if (!(b instanceof Element)) {
					return false;
				}
				if (!DOMUtil.compareElement((Element)a, (Element)b, considerNamespacePrefixes)) {
					return false;
				}
			} else {
				if (!a.equals(b)) {
					return false;
				}
			}
			
		}
		return true;
	}

}
