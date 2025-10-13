/*
 * Copyright (c) 2010-2013 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.util;

import org.w3c.dom.Element;

/**
 * @author Radovan Semancik
 *
 */
@FunctionalInterface
public interface DomElementVisitor {

    void visit(Element element);

}
