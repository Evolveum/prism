/*
 * Copyright (C) 2010-2022 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.prism.query;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.function.Consumer;

import com.evolveum.midpoint.prism.path.TypedItemPath;

import org.jetbrains.annotations.Nullable;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.match.MatchingRuleRegistry;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.util.DebugDumpable;
import com.evolveum.midpoint.util.annotation.Experimental;
import com.evolveum.midpoint.util.exception.SchemaException;

public interface ObjectFilter extends DebugDumpable, Serializable, Revivable, Freezable {

    /**
     * Does a SHALLOW clone.
     */
    ObjectFilter clone();

    boolean match(PrismContainerValue<?> value, MatchingRuleRegistry matchingRuleRegistry) throws SchemaException;

    void accept(Visitor visitor);

    @Override
    void revive(PrismContext prismContext);

    void checkConsistence(boolean requireDefinitions);

    boolean equals(Object o, boolean exact);

    /** Utility method performing {@link #equals(Object, boolean)} on two nullable objects. */
    static boolean equals(@Nullable ObjectFilter filter, @Nullable Object other, boolean exact) {
        return filter != null
                ? filter.equals(other, exact)
                : other == null;
    }

    /**
     * Returns true, if filter processes only enumerated item paths and does not use referencedBy,
     * ownedBy where path context changes drastically.
     *
     * Useful to find if filter could be executed with limited set of information (e.g. only access
     * to target name in {@link RefFilter}
     */
    @Experimental
    default boolean matchesOnly(ItemPath... paths) {
        return false;
    }

    default Collection<TypedItemPath> usedPaths(TypedItemPath base, boolean expandReferences) {
        var paths = new HashSet<TypedItemPath>();
        collectUsedPaths(base, (p) -> paths.add(p), expandReferences);
        return paths;
    }

    void collectUsedPaths(TypedItemPath base, Consumer<TypedItemPath> pathConsumer, boolean expandReferences);

    void transformItemPaths(ItemPath parentPath, ItemDefinition<?> parentDef, FilterItemPathTransformer transformer);

}
