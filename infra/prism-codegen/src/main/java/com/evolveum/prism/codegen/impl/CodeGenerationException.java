/*
 * Copyright (c) 2022 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.prism.codegen.impl;

import com.google.common.base.Strings;

public class CodeGenerationException extends Exception {

    public CodeGenerationException(String message, Throwable cause) {
        super(message, cause);
    }

    public CodeGenerationException(String message) {
        super(message);
    }


    public static void check(boolean condition, String format, Object... args) throws CodeGenerationException {
        if (!condition) {
            throw CodeGenerationException.of(null, format, args);

        }
    }

    public static CodeGenerationException of(Throwable cause, String format, Object... args) {
        return new CodeGenerationException(Strings.lenientFormat(format, args), cause);
    }

    public static <T> T checkNotNull(T value, String format, Object... args) throws CodeGenerationException {
        check(value != null, format, args);
        return value;
    }
}
