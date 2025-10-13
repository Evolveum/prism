/*
 * Copyright (c) 2010-2018 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.prism.xnode;

import javax.xml.namespace.QName;

/**
 *
 */
public interface RootXNode extends XNode {
    QName getRootElementName();

    XNode getSubnode();

    MapXNode toMapXNode();

    @Override
    RootXNode copy();
}
