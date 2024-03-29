/*
 * Copyright (c) 2010-2018 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism;

import org.jetbrains.annotations.NotNull;

import com.evolveum.midpoint.util.annotation.Experimental;

import javax.xml.namespace.QName;

import java.util.List;

/**
 * An interface to mutate the definition of a complex type.
 *
 * TODO document the interface (e.g. what should {@link #add(ItemDefinition)} method do
 *   in the case of duplicate definitions, etc)
 */
public interface MutableComplexTypeDefinition extends ComplexTypeDefinition, MutableTypeDefinition {

    void add(ItemDefinition<?> definition);

    void delete(QName itemName);

    MutablePrismPropertyDefinition<?> createPropertyDefinition(QName name, QName typeName);

    MutablePrismPropertyDefinition<?> createPropertyDefinition(String name, QName typeName);

    @Override
    @NotNull
    ComplexTypeDefinition clone();

    void setExtensionForType(QName type);

    void setAbstract(boolean value);

    void setSuperType(QName superType);

    void setObjectMarker(boolean value);

    void setContainerMarker(boolean value);

    void setReferenceMarker(boolean value);

    void setDefaultItemTypeName(QName value);

    void setDefaultNamespace(String namespace);

    void setIgnoredNamespaces(@NotNull List<String> ignoredNamespaces);

    void setXsdAnyMarker(boolean value);

    void setListMarker(boolean value);

    void setCompileTimeClass(Class<?> compileTimeClass);

    /**
     * Replaces a definition for an item with given name.
     *
     * TODO specify the behavior more precisely
     */
    void replaceDefinition(@NotNull QName itemName, ItemDefinition<?> newDefinition);

    @Experimental
    void addSubstitution(ItemDefinition<?> itemDef, ItemDefinition<?> maybeSubst);

    @Experimental
    default void setAttributeDefinitions(List<PrismPropertyDefinition<?>> definitions) {
        // Intentional NOOP
    }

    default void setStrictAnyMarker(boolean marker) {
        // intentional NOOP
    }
}
