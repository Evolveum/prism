/*
 * Copyright (c) 2010-2017 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.prism;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;

/**
 * Source for prism parser (file, input stream, string, DOM tree, ...).
 */
public interface ParserSource {

    /**
     * Presents the input data in the form of an InputStream.
     * For some special cases might not be implemented, and the data could be accessed in another way.
     */
    @NotNull
    InputStream getInputStream() throws IOException;

    /**
     * Should the stream be closed after parsing? Useful for sources that create/open the stream themselves.
     */
    boolean closeStreamAfterParsing();

    /**
     * Is the source expected to throw IOExceptions?
     */
    boolean throwsIOException();
}
