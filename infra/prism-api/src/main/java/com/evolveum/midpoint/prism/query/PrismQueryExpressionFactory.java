/*
 * Copyright (C) 2021-2022 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.prism.query;

import java.util.Map;

import com.evolveum.midpoint.prism.ExpressionWrapper;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.util.exception.SchemaException;

public interface PrismQueryExpressionFactory {


    ExpressionWrapper parseScript(Map<String, String> namespaceContext, String language, String script);

    default ExpressionWrapper parsePath(ItemPath rightPath) {
        throw new UnsupportedOperationException();
    }

    default void serializeExpression(ExpressionWriter writer, ExpressionWrapper wrapper) throws SchemaException {
        throw new UnsupportedOperationException("Expression serializer not supported.");
    }

    public interface ExpressionWriter {

        void writeVariable(ItemPath path);

        void writeScript(String language, String script);

        void writeConst(String name);

    }

}
