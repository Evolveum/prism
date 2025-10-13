/*
 * Copyright (c) 2010-2020 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.prism.impl.lex.json.reader;

import java.io.IOException;
import java.io.Serializable;

import com.evolveum.midpoint.prism.polystring.PolyString;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public class PolyStringDeserializer extends JsonDeserializer<PolyString> implements Serializable {

    @Override
    public PolyString deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        if (jp.getCurrentToken() != JsonToken.VALUE_STRING) {
            throw new IllegalStateException("Cannot deserialize value. Expected string value, but is was " + jp.getCurrentToken() + ". ");
        }
        String str = jp.getText();
        return new PolyString(str);
    }
}
