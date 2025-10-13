/*
 * Copyright (C) 2010-2024 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.prism;

import java.util.Objects;

/**
 * Root of all those definition builders.
 *
 * Work in progress. Consider renaming.
 */
public interface DefinitionFragmentBuilder {

    Object getObjectBuilt();

    /** Useful for builders that intentionally want to reject changing a property to other than prescribed value. */
    static void fixed(String name, Object value, Object fixedValue) {
        if (!Objects.equals(value, fixedValue)) {
            throw new UnsupportedOperationException(
                    "Attempted to set the value of the fixed feature '%s' (to: '%s', while it must be '%s')".formatted(
                            name, value, fixedValue));
        }
    }

    /** Useful for builders that do not want to change a property at all. */
    static void unsupported(String name, Object value) {
        if (value != null) {
            throw new UnsupportedOperationException(
                    "Attempted to set the value of the unsupported feature '%s' (to: '%s')".formatted(name, value));
        }
    }

    /** Useful for builders that want to keep a Boolean property turned off. */
    static void unsupported(String name, boolean value) {
        if (value) {
            throw new UnsupportedOperationException(
                    "Attempted to set the value of the unsupported feature '%s' (to: '%s')".formatted(name, value));
        }
    }
}
