/*
 * Copyright (c) 2010-2020 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.prism.xnode;

import java.util.List;

/**
 *
 */
public interface ListXNode extends XNode {
    @Override
    boolean isEmpty();
    int size();
    XNode get(int i);

    // todo reconsider this
    List<? extends XNode> asList();

    @Override
    ListXNode copy();
}
