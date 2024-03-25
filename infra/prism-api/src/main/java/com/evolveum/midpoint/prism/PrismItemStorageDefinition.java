/*
 * Copyright (C) 2010-2024 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism;

import com.evolveum.midpoint.util.annotation.Experimental;

/** TODO */
public interface PrismItemStorageDefinition {

    /**
     * This is XSD annotation that specifies whether a property should
     * be indexed in the storage. It can only apply to properties. It
     * has following meaning:
     *
     * true: the property must be indexed. If the storage is not able to
     * index the value, it should indicate an error.
     *
     * false: the property should not be indexed.
     *
     * null: data store decides whether to index the property or
     * not.
     */
    Boolean isIndexed();

    /**
     * If true, this item is not stored in XML representation in repo.
     *
     * TODO better name
     */
    @Experimental
    boolean isIndexOnly();

    /**
     * Returns true if item definition is searchable.
     */
    @Experimental
    default boolean isSearchable() {
        return false;
    }

    interface Mutable {
        void setIndexed(Boolean indexed);
        void setIndexOnly(boolean indexOnly);
        void setSearchable(boolean searchable);
    }
}
