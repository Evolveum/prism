/*
 * Copyright (C) 2010-2024 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism.schema;

import java.util.Objects;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.util.exception.SystemException;

/**
 * Parses a schema definition feature value from the source form (currently, XSD/XSOM) to the real value.
 *
 * Most of these are annotations: For example, "a:container" annotation is converted to a {@link Boolean} value.
 * However, more complex features are possible, such as "isAny" feature that is determined by analyzing the complex type
 * definition, and produces a tri-state information about the presence and kind of "any" content present.
 *
 * @param <V> feature value
 * @param <S> source object (like XSOM component, annotation, and so on)
 */
public interface DefinitionFeatureParser<V, S> {

    /**
     * Returns the value of the definition feature for the given (usually XSOM) source.
     * The source is intentionally nullable, because there are situations where this method is called
     * with annotations that are often missing.
     */
    @Nullable V getValue(@Nullable S source) throws SchemaException;

    // TODO decide on the fate of this method
    default @Nullable V getValueIfApplicable(@NotNull Object source) throws SchemaException {
        //noinspection unchecked
        return applicableTo(source) ? getValue((S) source) : null;
    }

    // TODO decide on the fate of this method
    default boolean applicableTo(Object sourceComponent) {
        return true;
    }

    /**
     * Returns this parser, but restricted to a more specific source type. For example, if original parser takes
     * any {@link Object} (e.g., `XSComponent` or `XSAnnotation`), we may restrict it to take only `XSAnnotation`.
     * This is useful e.g. for annotation-based features.
     */
    default @NotNull <RS> DefinitionFeatureParser<V, RS> restrictToSource(Class<RS> restrictedSourceType) {
        return new DefinitionFeatureParser<>() {
            @Override
            public @Nullable V getValue(@Nullable RS source) throws SchemaException {
                if (source == null || DefinitionFeatureParser.this.applicableTo(source)) {
                    //noinspection unchecked
                    return DefinitionFeatureParser.this.getValue((S) source);
                } else {
                    throw new IllegalStateException("This parser is not applicable to " + source.getClass());
                }
            }

            @Override
            public boolean applicableTo(Object source) {
                return source != null && restrictedSourceType.isAssignableFrom(source.getClass());
            }
        };
    }

    /** Special parser for "marker" boolean values, like `a:container`. */
    interface Marker<X> extends DefinitionFeatureParser<Boolean, X> {

        /** Returns true if the source has the mark, false otherwise. So, it's not nullable. */
        default boolean hasMark(@Nullable X source) {
            try {
                return Objects.requireNonNullElse(getValue(source), false);
            } catch (SchemaException e) {
                throw SystemException.unexpected(e);
            }
        }
    }

    /** Marks the parser as always returning a value, and provides appropriate getter method. */
    interface NonNull<V, X> extends DefinitionFeatureParser<V, X> {

        // TODO do we need this?
        default @NotNull V getValueRequired(@Nullable X source) {
            try {
                return Objects.requireNonNull(getValue(source));
            } catch (SchemaException e) {
                throw SystemException.unexpected(e);
            }
        }
    }
}
