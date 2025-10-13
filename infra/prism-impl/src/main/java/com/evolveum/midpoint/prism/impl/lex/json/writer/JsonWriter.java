/*
 * Copyright (c) 2010-2020 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.prism.impl.lex.json.writer;

import org.jetbrains.annotations.NotNull;

import com.evolveum.midpoint.prism.SerializationContext;
import com.evolveum.midpoint.prism.schema.SchemaRegistry;

public class JsonWriter extends AbstractWriter {

    public JsonWriter(@NotNull SchemaRegistry schemaRegistry) {
        super(schemaRegistry);
    }

    @Override
    WritingContext createWritingContext(SerializationContext prismSerializationContext) {
        return new JsonWritingContext(prismSerializationContext);
    }
}
