/*
 * Copyright (c) 2010-2020 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
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
