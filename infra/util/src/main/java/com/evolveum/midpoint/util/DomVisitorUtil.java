/*
 * Copyright (c) 2010-2013 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.util;

import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @author Radovan Semancik
 *
 */
public class DomVisitorUtil {

    public static void visitElements(Node node, DomElementVisitor visitor) {
        if (node instanceof Element) {
            visitor.visit((Element)node);
        }
        List<Element> childElements = DOMUtil.listChildElements(node);
        for (Element childElement: childElements) {
            visitElements(childElement, visitor);
        }
    }

}
