/*
 * Copyright (c) 2010-2018 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.prism.impl.lex;

import com.evolveum.midpoint.prism.impl.xnode.RootXNodeImpl;
import com.evolveum.midpoint.prism.impl.xnode.XNodeImpl;
import org.jetbrains.annotations.NotNull;

import javax.xml.namespace.QName;

public class LexicalUtils {

    @NotNull
    public static RootXNodeImpl createRootXNode(XNodeImpl xnode, QName rootElementName) {
        if (xnode instanceof RootXNodeImpl) {
            return (RootXNodeImpl) xnode;
        } else {
            RootXNodeImpl xroot = new RootXNodeImpl(rootElementName);
            xroot.setSubnode(xnode);
            return xroot;
        }
    }
}
