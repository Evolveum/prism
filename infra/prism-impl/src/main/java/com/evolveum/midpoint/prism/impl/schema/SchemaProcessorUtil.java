/*
 * Copyright (c) 2010-2018 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.prism.impl.schema;

import java.util.ArrayList;
import java.util.List;
import javax.xml.namespace.QName;

import com.sun.xml.xsom.XSAnnotation;
import com.sun.xml.xsom.XSComponent;
import com.sun.xml.xsom.XSType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.evolveum.midpoint.prism.xml.XmlTypeConverter;
import com.evolveum.midpoint.util.DOMUtil;
import com.evolveum.midpoint.util.exception.SchemaException;

/**
 * Class to be used by schema processor but also by SchemaDefinitionFactory subclasses.
 *
 * @author Radovan Semancik
 *
 */
public class SchemaProcessorUtil {

    public static Element getAnnotationElementCheckingAncestors(@Nullable XSType type, QName annotationElementName) {
        if (type == null) {
            return null;
        }
        Element annoElement = getAnnotationElement(type, annotationElementName);
        if (annoElement != null) {
            return annoElement;
        }
        if (type.getBaseType() != null && !type.getBaseType().equals(type)) {
            return getAnnotationElementCheckingAncestors(type.getBaseType(), annotationElementName);
        }
        return null;
    }

    public static Element getAnnotationElement(XSComponent component, QName qname) {
        if (component != null) {
            return getAnnotationElement(component.getAnnotation(), qname);
        } else {
            return null;
        }
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

    public static @NotNull List<Element> getAnnotationElements(@Nullable XSComponent component, @NotNull QName qname) {
        if (component != null) {
            return getAnnotationElements(component.getAnnotation(), qname);
        } else {
            return List.of();
        }
    }

    public static @NotNull List<Element> getAnnotationElements(@Nullable XSAnnotation annotation, @NotNull QName qname) {
        List<Element> elements = new ArrayList<>();
        if (annotation == null) {
            return elements;
        }

        Element xsdAnnotation = (Element) annotation.getAnnotation();
        NodeList list = xsdAnnotation.getElementsByTagNameNS(qname.getNamespaceURI(), qname.getLocalPart());
        if (list.getLength() > 0) {
            for (int i = 0; i < list.getLength(); i++) {
                elements.add((Element) list.item(i));
            }
        }

        return elements;
    }

    public static QName getAnnotationQName(Object object, QName qname) {
        Element element = getAnnotationElement(object, qname);
        if (element == null) {
            return null;
        }
        return DOMUtil.getQNameValue(element);
    }

    public static @NotNull List<QName> getAnnotationQNameList(Object object, QName qname) {
        return getAnnotationElements(object, qname).stream()
                .map(DOMUtil::getQNameValue)
                .toList();
    }

    public static <T> T getAnnotationConverted(Object source, QName qname, Class<T> type) throws SchemaException {
        return convert(type, getAnnotationElement(source, qname));
    }

    public static <T> T getAnnotationConvertedInherited(XSType xsType, QName qname, Class<T> type) throws SchemaException {
        return convert(
                type,
                getAnnotationElementCheckingAncestors(xsType, qname));
    }

    @Nullable
    public static <T> T convert(Class<T> type, Element element) throws SchemaException {
        if (element == null) {
            return null;
        }
        String textContent = element.getTextContent();
        if (textContent == null || textContent.isEmpty()) {
            return null;
        }
        return XmlTypeConverter.toJavaValue(element, type);
    }

    public static Boolean getAnnotationBoolean(XSAnnotation annotation, QName qname) throws SchemaException {
        return getAnnotationConverted(annotation, qname, Boolean.class);
    }

    public static String dumpAnnotation(XSAnnotation annotation) {
        if (annotation == null) {
            return null;
        }
        Element xsdAnnotation = (Element) annotation.getAnnotation();
        if (xsdAnnotation == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (Element element : DOMUtil.listChildElements(xsdAnnotation)) {
            sb.append(element.getLocalName()).append(", ");
        }
        return sb.toString();
    }

    public static @Nullable Element getAnnotationElement(@Nullable Object sourceObject, @NotNull QName name) {
        if (sourceObject == null) {
            return null;
        } else if (sourceObject instanceof XSAnnotation annotation) {
            return getAnnotationElement(annotation, name);
        } else if (sourceObject instanceof XSComponent component) {
            return getAnnotationElement(component, name);
        } else {
            throw new IllegalStateException("Unsupported source object: " + sourceObject);
        }
    }

    public static @NotNull List<Element> getAnnotationElements(@Nullable Object sourceObject, @NotNull QName name) {
        if (sourceObject == null) {
            return List.of();
        } else if (sourceObject instanceof XSAnnotation annotation) {
            return getAnnotationElements(annotation, name);
        } else if (sourceObject instanceof XSComponent component) {
            return getAnnotationElements(component, name);
        } else {
            throw new IllegalStateException("Unsupported source object: " + sourceObject);
        }
    }

    public static XSAnnotation getAnnotation(XSComponent component) {
        return component != null ? component.getAnnotation() : null;
    }


}
