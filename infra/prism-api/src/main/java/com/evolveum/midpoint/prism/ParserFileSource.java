/*
 * Copyright (c) 2010-2017 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.prism;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class ParserFileSource implements ParserSource {

    @NotNull private final File file;

    public ParserFileSource(@NotNull File file) {
        this.file = file;
    }

    @NotNull
    public File getFile() {
        return file;
    }

    @NotNull
    @Override
    public InputStream getInputStream() throws FileNotFoundException {
        return new FileInputStream(file);
    }

    @Override
    public boolean closeStreamAfterParsing() {
        return true;
    }

    @Override
    public boolean throwsIOException() {
        return true;
    }
}
