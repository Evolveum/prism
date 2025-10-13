/*
 * Copyright (c) 2010-2024 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.prism.schema;

import java.util.Collection;
import java.util.Objects;
import javax.xml.namespace.QName;

import com.evolveum.axiom.concepts.CheckedFunction;
import com.evolveum.midpoint.prism.path.ItemPath;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.xml.DynamicNamespacePrefixMapper;
import com.evolveum.midpoint.util.DebugDumpable;
import com.evolveum.midpoint.util.exception.SchemaException;

/**
 * Maintains system-wide parsed schemas.
 */
public interface SchemaRegistryState extends DebugDumpable, GlobalDefinitionsStore {

    Collection<PrismSchema> getSchemas();

    PrismSchema getPrismSchema(String namespace);

    javax.xml.validation.Schema getJavaxSchema();

    /**
     //     * @return System-wide "standard prefixes" registry.
     //     */
    DynamicNamespacePrefixMapper getNamespacePrefixMapper();

    // TODO fix this temporary and inefficient implementation
    QName resolveUnqualifiedTypeName(QName type) throws SchemaException;

    // current implementation tries to find all references to the child CTD and select those that are able to resolve path of 'rest'
    // fails on ambiguity
    // it's a bit fragile, as adding new references to child CTD in future may break existing code
    ComplexTypeDefinition determineParentDefinition(@NotNull ComplexTypeDefinition child, @NotNull ItemPath rest);

    <T> Class<T> determineCompileTimeClass(QName typeName);

    PrismSchema findSchemaByCompileTimeClass(@NotNull Class<?> compileTimeClass);

    SchemaDescription findSchemaDescriptionByNamespace(String namespaceURI);

    SchemaDescription findSchemaDescriptionByPrefix(String prefix);

    PrismSchema findSchemaByNamespace(String namespaceURI);

    // Takes XSD types into account as well
    <T> Class<T> determineClassForType(QName type);

    <T> Class<T> determineCompileTimeClassInternal(QName type, boolean cacheAlsoNegativeResults);

    Collection<Package> getCompileTimePackages();

    enum IsList {
        YES, NO, MAYBE
    }

    /**
     * Checks whether element with given (declared) xsi:type and name can be a heterogeneous list.
     *
     * @return YES if it is a list,
     *         NO if it's not,
     *         MAYBE if it probably is a list but some further content-based checks are needed
     */
    @NotNull
    IsList isList(@Nullable QName xsiType, @NotNull QName elementName);

    static <R> DerivationKey<R> derivationKeyFrom(Class<?> keyOwner, String keyName) {
        return new DerivationKey(keyOwner, keyName);
    }

    /**
     * Returns derived value specific to this schema context state, supplied derivation key and mapping.
     *
     * Returns cached value if it was already computed and is available. If value is not available, it is computed by supplied
     * mapping function.
     *
     * Do not use for short-lived values. Use-cases should be like caching parser instances with already done lookups for
     * repository or other long-lived components, which needs to be recomputed when schemas are changed.
     *
     *
     * @param derivationKey
     * @param mapping
     * @return
     * @param <R>
     * @param <E>
     * @throws E
     */
    default <R,E extends Exception> R getDerivedObject(DerivationKey<R> derivationKey, CheckedFunction<SchemaRegistryState, R, E> mapping)
        throws E {
        return mapping.apply(this);
    }


    /**
     * Derivation key is used for caching computed values, which depends on schema context state.
     *
     * @param <R>
     */
    public static class DerivationKey<R> {
        private final Class<?> keyOwner;
        private final String keyName;

        private DerivationKey(Class<?> keyOwner, String keyName) {
            this.keyOwner = keyOwner;
            this.keyName = keyName;
        }



        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;

            DerivationKey<?> that = (DerivationKey<?>) o;

            if (!Objects.equals(keyOwner, that.keyOwner))
                return false;
            return Objects.equals(keyName, that.keyName);
        }

        @Override
        public int hashCode() {
            int result = keyOwner != null ? keyOwner.hashCode() : 0;
            result = 31 * result + (keyName != null ? keyName.hashCode() : 0);
            return result;
        }
    }
}
