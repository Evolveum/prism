/*
 * Copyright (C) 2024 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.prism;

@FunctionalInterface
public interface PrismVisitor {

    /**
     * Visit item and decides if the children should be also visited.
    **/
    boolean visit(PrismVisitable visitable);

}
