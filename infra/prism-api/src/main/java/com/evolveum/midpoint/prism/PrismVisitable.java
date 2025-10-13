/*
 * Copyright (C) 2024 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.prism;

public interface PrismVisitable {

    /**
     * Accept visitor and visits prism item or value and it's children
     *
     *
     * Note: name is not accept, but accept visitor to prevent naming conflict
     * in lambdas dynamic languages such as groovy with {@link Visitable#accept(com.evolveum.midpoint.prism.Visitor)}
     *
     * @param visitor
     * @return return value of {@link PrismVisitor#visit(PrismVisitable)} invocation for this visitable.
     */
    boolean acceptVisitor(PrismVisitor visitor);




}
