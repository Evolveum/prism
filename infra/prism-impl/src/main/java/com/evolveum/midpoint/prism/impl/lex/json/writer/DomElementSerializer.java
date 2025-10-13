/*
 * Copyright (c) 2010-2020 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.prism.impl.lex.json.writer;

import java.io.IOException;

import org.w3c.dom.Node;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ext.DOMSerializer;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;

public class DomElementSerializer extends DOMSerializer {

    @Override
    public void serializeWithType(Node value, JsonGenerator jgen, SerializerProvider provider,
            TypeSerializer typeSer) throws IOException, JsonProcessingException {
        serialize(value, jgen, provider);
    }
}
