/*
 * Copyright (c) 2010-2018 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism;

import com.evolveum.midpoint.util.DisplayableValue;

import org.jetbrains.annotations.NotNull;

import javax.xml.namespace.QName;
import java.util.Collection;

/**
 *  Factory for prism definitions (Definition and all its subtypes in prism-api).
 */
public interface DefinitionFactory {

    ComplexTypeDefinition newComplexTypeDefinition(QName name);

    @Deprecated // used e.g. by Grouper-related code
    default <T> PrismPropertyDefinition<T> createPropertyDefinition(QName name, QName typeName) {
        return newPropertyDefinition(name, typeName);
    }

    <T> PrismPropertyDefinition<T> newPropertyDefinition(QName name, QName typeName);

    default <T> PrismPropertyDefinition<T> newPropertyDefinition(QName name, QName typeName, int minOccurs, int maxOccurs) {
        PrismPropertyDefinition<T> def = newPropertyDefinition(name, typeName);
        def.mutator().setMinOccurs(minOccurs);
        def.mutator().setMaxOccurs(maxOccurs);
        return def;
    }

    default <T> PrismPropertyDefinition<T> newPropertyDefinition(
            QName name, QName typeName, Collection<? extends DisplayableValue<T>> allowedValues, T defaultValue) {
        PrismPropertyDefinition<T> def = newPropertyDefinition(name, typeName);
        def.mutator().setAllowedValues(allowedValues);
        def.mutator().setDefaultValue(defaultValue);
        return def;
    }

    PrismReferenceDefinition newReferenceDefinition(QName name, QName typeName);

    default PrismReferenceDefinition newReferenceDefinition(QName name, QName typeName, int minOccurs, int maxOccurs) {
        PrismReferenceDefinition def = newReferenceDefinition(name, typeName);
        def.mutator().setMinOccurs(minOccurs);
        def.mutator().setMaxOccurs(maxOccurs);
        return def;
    }

    //region Containers and objects
    /** Standard case: creating container with known CTD. */
    <C extends Containerable> @NotNull PrismContainerDefinition<C> newContainerDefinition(
            @NotNull QName name, @NotNull ComplexTypeDefinition ctd);

    /** Quite a special case - no complex type definition is known. */
    @NotNull PrismContainerDefinition<?> newContainerDefinitionWithoutTypeDefinition(@NotNull QName name, @NotNull QName typeName);

    default <C extends Containerable> @NotNull PrismContainerDefinition<C> createContainerDefinition(
            @NotNull QName name, @NotNull ComplexTypeDefinition ctd, int minOccurs, int maxOccurs) {

        var pcd = this.<C>newContainerDefinition(name, ctd);
        pcd.mutator().setMinOccurs(minOccurs);
        pcd.mutator().setMaxOccurs(maxOccurs);
        return pcd;
    }

    <O extends Objectable> @NotNull PrismObjectDefinition<O> newObjectDefinition(
            @NotNull QName name, @NotNull ComplexTypeDefinition ctd);
    //endregion
}
