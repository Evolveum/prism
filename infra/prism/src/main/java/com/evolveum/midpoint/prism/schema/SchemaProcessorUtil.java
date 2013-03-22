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
package com.evolveum.midpoint.prism.schema;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.evolveum.midpoint.prism.xml.XmlTypeConverter;
import com.evolveum.midpoint.util.DOMUtil;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.sun.xml.xsom.XSAnnotation;
import com.sun.xml.xsom.XSType;

/**
 * Class to be used by schema processor but also by SchemaDefinitionFactory subclasses.
 * 
 * @author Radovan Semancik
 *
 */
public class SchemaProcessorUtil {
	
	public static final String MULTIPLICITY_UNBOUNDED = "unbounded";

	public static boolean hasAnnotation(XSType xsType, QName annotationElementName) {
		if (xsType.getName() == null) {
			return false;
		}
		Element annotationElement = getAnnotationElement(xsType.getAnnotation(), annotationElementName);
		if (annotationElement != null) {
			return true;
		}
		if (xsType.getBaseType() != null && !xsType.getBaseType().equals(xsType)) {
			return hasAnnotation(xsType.getBaseType(), annotationElementName);
		}
		return false;
	}
	
	public static Element getAnnotationElement(XSAnnotation annotation, QName qname) {
		if (annotation == null) {
			return null;
		}
		List<Element> elements = getAnnotationElements(annotation, qname);
		if (elements.isEmpty()) {
			return null;
		}
		return elements.get(0);
	}
	
	public static List<Element> getAnnotationElements(XSAnnotation annotation, QName qname) {
		List<Element> elements = new ArrayList<Element>();
		if (annotation == null) {
			return elements;
		}

		Element xsdAnnotation = (Element) annotation.getAnnotation();
		NodeList list = xsdAnnotation.getElementsByTagNameNS(qname.getNamespaceURI(), qname.getLocalPart());
		if (list != null && list.getLength() > 0) {
			for (int i = 0; i < list.getLength(); i++) {
				elements.add((Element) list.item(i));
			}
		}

		return elements;
	}

	public static QName getAnnotationQName(XSAnnotation annotation, QName qname) {
		Element element = getAnnotationElement(annotation, qname);
		if (element == null) {
			return null;
		}
		return DOMUtil.getQNameValue(element);
	}

	public static Boolean getAnnotationBoolean(XSAnnotation annotation, QName qname) throws SchemaException {
		Element element = getAnnotationElement(annotation, qname);
		if (element == null) {
			return null;
		}
		String textContent = element.getTextContent();
		if (textContent == null || textContent.isEmpty()) {
			return null;
		}
		return XmlTypeConverter.toJavaValue(element, Boolean.class);
	}

	public static Integer parseMultiplicity(String stringMultiplicity) {
		if (stringMultiplicity == null) {
			return null;
		}
		if (stringMultiplicity.equals(MULTIPLICITY_UNBOUNDED)) {
			return -1;
		}
		return Integer.parseInt(stringMultiplicity);
	}
	
}
