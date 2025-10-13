/*
 * Copyright (c) 2010-2018 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.prism;

/**
 *  Allows a visitor to follow the path along the "parent" relationship.
 */
@FunctionalInterface
public interface ParentVisitable {

    void acceptParentVisitor(Visitor visitor);

}
