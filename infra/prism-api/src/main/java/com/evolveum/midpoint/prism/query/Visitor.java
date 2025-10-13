/*
 * Copyright (c) 2013 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.prism.query;

@FunctionalInterface
public interface Visitor {

    void visit(ObjectFilter filter);

}
