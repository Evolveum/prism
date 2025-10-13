/*
 * Copyright (C) 2010-2024 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.prism.schema;

import java.util.function.BiConsumer;
import java.util.function.Function;

import com.evolveum.midpoint.prism.ComplexTypeDefinition.ComplexTypeDefinitionLikeBuilder;
import com.evolveum.midpoint.prism.schema.DefinitionFeatureSerializer.SerializationTarget;

import com.sun.xml.xsom.XSAnnotation;
import com.sun.xml.xsom.XSComponent;
import org.jetbrains.annotations.NotNull;

import com.evolveum.midpoint.util.exception.SchemaException;

import org.jetbrains.annotations.Nullable;

/**
 * An identifiable feature of a item or type definition. Currently, it supports parsing and sometimes also serialization.
 *
 * For parsing, it knows:
 *
 * . How to be parsed from XSOM (via {@link DefinitionFeatureParser}).
 * . How to be applied to the definition object or builder (via {@link #setterMethod()}).
 *
 * For serialization:
 *
 * . How to be taken from the definition (via {@link DefaultImpl#getterMethod})
 * . How to be put into XSD DOM (via {@link #serializer()}).
 *
 * Work in progress.
 *
 * @param <V> type for value of the feature (Boolean, String, or more complex type)
 * @param <DB> definition builder (or mutable definition) object to which it's applicable when parsing
 * @param <XC> source XSOM object that this feature can be parsed from; {@link Object}
 * means {@link XSComponent} or {@link XSAnnotation}
 * @param <SD> serializable definition object to which it's applicable when serializing
 */
public interface DefinitionFeature<V, DB, XC, SD> {

    static <V, DB, XC, SD> DefinitionFeature<V, DB, XC, SD> of(
            @NotNull Class<V> valueType, @NotNull Class<DB> setterInterface, @NotNull BiConsumer<DB, V> setterMethod,
            @NotNull DefinitionFeatureParser<V, XC> processor) {
        return new DefaultImpl<>(valueType, setterInterface, setterMethod, processor, null, null, null);
    }

    static <V, DB, XC, SD> DefinitionFeature<V, DB, XC, SD> of(
            @NotNull Class<V> valueType, @NotNull Class<DB> setterInterface, @NotNull BiConsumer<DB, V> setterMethod,
            @NotNull DefinitionFeatureParser<V, XC> parser,
            @NotNull Class<SD> serializableDefinitionType,
            @NotNull Function<SD, V> getterMethod,
            @NotNull DefinitionFeatureSerializer<V> serializer) {
        return new DefaultImpl<>(valueType, setterInterface, setterMethod, parser, serializableDefinitionType, getterMethod, serializer);
    }

    @NotNull Class<V> valueType();

    @NotNull Class<DB> definitionBuilderType();

    @NotNull BiConsumer<DB, V> setterMethod();

    @SuppressWarnings("unused") // IDE marks this perhaps by mistake, as it has uses
    @NotNull DefinitionFeatureParser<V, XC> xsomParser();

    DefinitionFeatureSerializer<V> serializer();

    default boolean settableOn(@NotNull Object target) {
        return definitionBuilderType().isAssignableFrom(target.getClass());
    }

    default void set(@NotNull DB target, V value) {
        setterMethod().accept(target, value);
    }

    @SuppressWarnings("UnusedReturnValue") // maybe in the future
    default V parseIfApplicable(DB target, XC source) throws SchemaException {
        if (settableOn(target)) {
            return parse(target, source);
        } else {
            return null;
        }
    }

    /** Obtains the value from `source` (e.g. XSOM component) and sets it to `target` definition builder. */
    V parse(@NotNull DB target, @Nullable XC source) throws SchemaException;

    /** Obtains the value from `source` definition and serializes it into `target` (e.g. DOM element in XSD). */
    void serialize(@NotNull SerializableDefinition source, @NotNull SerializationTarget target);

    /**
     * Used when we know that the feature will be applicable to given builder class, e.g., when passing as "extra feature"
     * like in {@link ComplexTypeDefinitionLikeBuilder#getExtraFeaturesToParse()}.
     */
    default <DB2> DefinitionFeature<V, DB2, XC, SD> asForBuilder(Class<DB2> builderClass) {
        //noinspection unchecked
        return (DefinitionFeature<V, DB2, XC, SD>) this;
    }

    class DefaultImpl<V, DB, XC, SD> implements DefinitionFeature<V, DB, XC, SD> {

        @NotNull protected final Class<V> valueType;
        @NotNull final Class<DB> definitionBuilderType;
        @NotNull final BiConsumer<DB, V> setterMethod;
        @NotNull final DefinitionFeatureParser<V, XC> xsomParser;
        final Class<SD> serializableDefinitionType;
        final Function<SD, V> getterMethod;
        final DefinitionFeatureSerializer<V> serializer;

        public DefaultImpl(
                @NotNull Class<V> valueType,
                @NotNull Class<DB> definitionBuilderType,
                @NotNull BiConsumer<DB, V> setterMethod,
                @NotNull DefinitionFeatureParser<V, XC> xsomParser,
                @Nullable Class<SD> serializableDefinitionType,
                @Nullable Function<SD, V> getterMethod,
                @Nullable DefinitionFeatureSerializer<V> serializer) {
            this.valueType = valueType;
            this.definitionBuilderType = definitionBuilderType;
            this.setterMethod = setterMethod;
            this.xsomParser = xsomParser;
            this.serializableDefinitionType = serializableDefinitionType;
            this.getterMethod = getterMethod;
            this.serializer = serializer;
        }

        @Override
        public @NotNull Class<V> valueType() {
            return valueType;
        }

        @Override
        public @NotNull Class<DB> definitionBuilderType() {
            return definitionBuilderType;
        }

        @Override
        public @NotNull BiConsumer<DB, V> setterMethod() {
            return setterMethod;
        }

        @Override
        public @NotNull DefinitionFeatureParser<V, XC> xsomParser() {
            return xsomParser;
        }

        @Override
        public DefinitionFeatureSerializer<V> serializer() {
            return serializer;
        }

        @Override
        public V parse(@NotNull DB target, @Nullable XC source) throws SchemaException {
            DefinitionFeatureParser<V, XC> parser = (DefinitionFeatureParser<V, XC>) xsomParser();
            if (parser.applicableTo(source)) {
                V value = parser.getValue(source);
                if (value != null) {
                    // This is somewhat intentional. We want to avoid re-setting values back to "nothing" in some cases.
                    // For example, current XsomSchemaParser tries to find marks on various annotations, quite wildly.
                    // (Probably not very correct, but...)
                    // We also want to avoid calling interfaces that do not support particular features, if the feature
                    // is missing in XSD.
                    set(target, value);
                    return value;
                }
            }
            return null;
        }

        @Override
        public void serialize(@NotNull SerializableDefinition source, @NotNull SerializationTarget target) {
            if (serializableDefinitionType == null || getterMethod == null) {
                throw new UnsupportedOperationException("Serialization is not supported for this feature: " + this);
            }
            if (!serializableDefinitionType.isInstance(source)) {
                return; // not applicable -> nothing to serialize here
            }
            //noinspection unchecked
            var value = getterMethod.apply((SD) source);
            if (value == null) {
                return; // no value -> nothing to serialize here
            }
            serializer().serialize(value, target);
        }
    }
}
