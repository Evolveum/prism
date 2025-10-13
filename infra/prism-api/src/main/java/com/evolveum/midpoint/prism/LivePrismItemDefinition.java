/*
 * Copyright (C) 2010-2024 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.prism;

import com.evolveum.midpoint.prism.path.ItemPath;

import org.jetbrains.annotations.NotNull;

import javax.xml.namespace.QName;

/**
 * TODO ... describes operations executable when the definition is "live and well" in its place,
 *  like the complex type definition in prism container
 *
 * Work in progress.
 */
public interface LivePrismItemDefinition {

    /**
     * Returns true if this definition is valid for given element name and definition class,
     * in either case-sensitive (the default) or case-insensitive way.
     *
     * Used e.g. for "slow" path lookup where we iterate over all definitions in a complex type.
     */
    boolean isValidFor(@NotNull QName elementQName, @NotNull Class<? extends ItemDefinition<?>> clazz, boolean caseInsensitive);

    /**
     * Used to find a matching item definition _within_ this definition.
     * Treats e.g. de-referencing in prism references.
     */
    <T extends ItemDefinition<?>> T findItemDefinition(@NotNull ItemPath path, @NotNull Class<T> clazz);

    /** Helper method to implement {@link #findItemDefinition(ItemPath, Class)} for unstructured definitions. */
    static boolean matchesThisDefinition(@NotNull ItemPath path, @NotNull Class<?> clazz, @NotNull Object _this) {
        if (path.isEmpty()) {
            if (clazz.isAssignableFrom(_this.getClass())) {
                return true;
            } else {
                throw new IllegalArgumentException("Looking for definition of class " + clazz + " but found " + _this);
            }
        } else {
            return false;
        }
    }
}
