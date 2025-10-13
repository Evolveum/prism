/*
 * Copyright (c) 2010-2018 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.prism.impl;

import com.evolveum.midpoint.prism.ParserSource;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Element;

import java.io.IOException;
import java.io.InputStream;

public class ParserElementSource implements ParserSource {

    @NotNull private final Element element;

    public ParserElementSource(@NotNull Element element) {
        this.element = element;
    }

    @NotNull
    public Element getElement() {
        return element;
    }

    @NotNull
    @Override
    public InputStream getInputStream() throws IOException {
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
