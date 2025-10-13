/*
 * Copyright (c) 2010-2017 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.prism;

import com.evolveum.midpoint.prism.xnode.RootXNode;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;

public class ParserXNodeSource implements ParserSource {

    @NotNull private final RootXNode xnode;

    public ParserXNodeSource(@NotNull RootXNode xnode) {
        this.xnode = xnode;
    }

    @NotNull
    public RootXNode getXNode() {
        return xnode;
    }

    @NotNull
    @Override
    public InputStream getInputStream() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean closeStreamAfterParsing() {
        return true;
    }

    @Override
    public boolean throwsIOException() {
        return false;
    }
}
