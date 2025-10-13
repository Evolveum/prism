/*
 * Copyright (c) 2010-2019 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.prism;

import com.evolveum.midpoint.util.annotation.Experimental;

/**
 *  Represents visitor of generated JAXB beans.
 *
 *  EXPERIMENTAL. Consider merging with traditional prism Visitor.
 */
@FunctionalInterface
@Experimental
public interface JaxbVisitor {

    void visit(JaxbVisitable visitable);

}
