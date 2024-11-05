/*
 * Copyright (C) 2024 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.prism;

@FunctionalInterface
public interface PrismVisitor {

    /**
     * Visit item and decides if the children should be also visited.
    **/
    boolean visit(PrismVisitable visitable);

}
