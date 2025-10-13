/*
 * Copyright (c) 2010-2019 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.prism.impl.lex.json.yaml;

import java.io.Reader;

import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.core.util.BufferRecycler;
import com.fasterxml.jackson.dataformat.yaml.YAMLParser;

public class MidpointYAMLParser extends YAMLParser {

    public MidpointYAMLParser(IOContext ctxt, BufferRecycler br, int parserFeatures, int csvFeatures,
            ObjectCodec codec, Reader reader) {
        super(ctxt, br, parserFeatures, csvFeatures, codec, reader);
        // TODO Auto-generated constructor stub
    }
}
