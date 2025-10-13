/*
 * Copyright (c) 2010-2017 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.prism;

import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class ParserStringSource implements ParserSource {

    @NotNull private final String data;

    public ParserStringSource(@NotNull String data) {
        this.data = data;
    }

    @NotNull
    public String getData() {
        return data;
    }

    @NotNull
    @Override
    public InputStream getInputStream() {
        return IOUtils.toInputStream(data, StandardCharsets.UTF_8);
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
