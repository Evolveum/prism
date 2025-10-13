/*
 * Copyright (C) 2010-2024 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.prism;

import java.io.Serializable;

import org.jetbrains.annotations.NotNull;

import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.util.annotation.Experimental;

/**
 * Resolution of a path in a prism container definition, as far as possible.
 * See {@link #resolvedPath}, {@link #remainderPath} and {@link #lastDefinition} for details.
 */
@Experimental
public class ResolvedItemPath<ID extends ItemDefinition<?>> implements Serializable {

    /** The path as in the definitions, i.e. usually with qualified names. */
    @NotNull private final ItemPath resolvedPath;

    /** Path that cannot be resolved. Hopefully empty. */
    @NotNull private final ItemPath remainderPath;

    /** Definition of the last item in {@link #resolvedPath}. */
    @NotNull private final ID lastDefinition;

    private ResolvedItemPath(@NotNull ItemPath resolvedPath, @NotNull ItemPath remainderPath, @NotNull ID lastDefinition) {
        this.lastDefinition = lastDefinition;
        this.resolvedPath = resolvedPath;
        this.remainderPath = remainderPath;
    }

    /** Resolves the path against a container definition. The path should be "names-only". */
    public static <ID extends ItemDefinition<?>> ResolvedItemPath<ID> create(
            @NotNull ComplexTypeDefinition rootDefinition, @NotNull ItemPath rawItemPath) {
        ItemPath resolvedPath = ItemPath.EMPTY_PATH;
        ItemPath remainderPath = rawItemPath.namedSegmentsOnly();
        ComplexTypeDefinition currentDefinition = rootDefinition;
        for (;;) {
            if (remainderPath.isEmpty()) {
                //noinspection unchecked
                return new ResolvedItemPath<>(resolvedPath, remainderPath, (ID) currentDefinition);
            }
            ItemDefinition<?> childDefinition = currentDefinition.findItemDefinition(remainderPath.firstToName());
            if (childDefinition == null) {
                //noinspection unchecked
                return new ResolvedItemPath<>(resolvedPath, remainderPath, (ID) currentDefinition);
            }
            resolvedPath = resolvedPath.append(childDefinition.getItemName());
            remainderPath = remainderPath.rest();
            var childCtd = childDefinition instanceof PrismContainerDefinition<?> childPcd ?
                    childPcd.getComplexTypeDefinition() : null;
            if (childCtd == null) {
                //noinspection unchecked
                return new ResolvedItemPath<>(resolvedPath, remainderPath, (ID) childDefinition);
            }
            currentDefinition = childCtd;
        }
    }

    public @NotNull ItemPath getResolvedPath() {
        return resolvedPath;
    }

    public @NotNull ID getLastDefinition() {
        return lastDefinition;
    }

    public boolean isComplete() {
        return remainderPath.isEmpty();
    }
}
