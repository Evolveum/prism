/*
 * Copyright (c) 2010-2018 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism;

import java.util.Collection;

import javax.xml.namespace.QName;

import org.jetbrains.annotations.NotNull;

import com.evolveum.midpoint.util.annotation.Experimental;

/**
 *
 */
public interface MutablePrismContainerDefinition<C extends Containerable> extends PrismContainerDefinition<C>, MutableItemDefinition<PrismContainer<C>> {

    void setCompileTimeClass(Class<C> compileTimeClass);

    MutablePrismPropertyDefinition<?> createPropertyDefinition(QName name, QName propType, int minOccurs, int maxOccurs);

    MutablePrismPropertyDefinition<?> createPropertyDefinition(QName name, QName propType);

    MutablePrismPropertyDefinition<?> createPropertyDefinition(String localName, QName propType);

    MutablePrismContainerDefinition<?> createContainerDefinition(QName name, QName typeName, int minOccurs, int maxOccurs);

    MutablePrismContainerDefinition<?> createContainerDefinition(QName name, ComplexTypeDefinition ctd, int minOccurs, int maxOccurs);

    void setComplexTypeDefinition(ComplexTypeDefinition complexTypeDefinition);

    /**
     *
     * Experimental: USe only with care, this overrides behavior of listed operational=true items in equivalence strategies
     * for containers.
     *
     */
    @Experimental
    default void setAlwaysUseForEquals(@NotNull Collection<QName> keysElem) {
        // NOOP
    }
}
