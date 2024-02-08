/*
 * Copyright (C) 2010-2024 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.util;

import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Represents the dependencies between items.
 *
 * Provides {@link #getSortedItems()} and {@link #getTopologicalSort()} methods to sort the items topologically.
 *
 * Can be created right from the dependency map ({@link #ofMap(Map)}) or from individual items ({@link #ofItems(Collection)}).
 * The dependencies themselves must reference only known items.
 */
public class DependencyGraph<X> {

    /** Client-supplied data. Should not be touched. */
    @NotNull private final Map<X, Collection<X>> dependencyMap;

    private DependencyGraph(@NotNull Map<X, Collection<X>> dependencyMap) {
        this.dependencyMap = dependencyMap;
    }

    /** Creates the dependency graph from the given dependency map. */
    public static <X> DependencyGraph<X> ofMap(Map<X, Collection<X>> dependencyMap) {
        return new DependencyGraph<>(dependencyMap);
    }

    /** Creates the dependency graph from items that can tell us about their dependencies. */
    public static <I extends Item<I>> DependencyGraph<I> ofItems(@NotNull Collection<I> items) {
        Map<I, Collection<I>> dependencyMap = new HashMap<>();
        for (I item : items) {
            dependencyMap.put(item, item.getDependencies());
        }
        return DependencyGraph.ofMap(dependencyMap);
    }

    /**
     * Returns the items sorted topologically: if A is before B, then A does not depend on B,
     * or throws an exception if no such ordering exists.
     */
    public List<X> getSortedItems() {
        TopologicalSort<X> sort = new TopologicalSort<>(dependencyMap);
        if (sort.isComplete()) {
            return sort.getSortedItems();
        } else {
            throw new IllegalStateException("Cyclic dependencies. Remaining items: " + sort.getRemainingItems());
        }
    }

    /** Returns items topologically sorted (as much as possible). */
    public @NotNull TopologicalSort<X> getTopologicalSort() {
        return new TopologicalSort<>(dependencyMap);
    }

    /** Represents a topological sort of items. The sorting itself is done at the construction time. */
    public static class TopologicalSort<X> {

        @NotNull private final Map<X, Set<X>> remainingDependencies;
        @NotNull private final List<X> sortedItems;

        private TopologicalSort(Map<X, Collection<X>> dependencyMap) {
            remainingDependencies = copyAndCheckMap(dependencyMap);
            sortedItems = new ArrayList<>(remainingDependencies.size());
            for (;;) {
                X item = findItemWithNoDependencies();
                if (item == null) {
                    break;
                }
                sortedItems.add(item);
                remove(item);
            }
        }

        private Map<X, Set<X>> copyAndCheckMap(Map<X, Collection<X>> origMap) {
            Map<X, Set<X>> targetMap = new HashMap<>();
            for (Map.Entry<X, Collection<X>> origEntry : origMap.entrySet()) {
                X origItem = origEntry.getKey();
                Collection<X> origItemDependencies = origEntry.getValue();
                Set<X> targetDependencySet = new HashSet<>();
                for (X dependency : origItemDependencies) {
                    if (!origMap.containsKey(dependency)) {
                        throw new IllegalStateException(
                                "Item " + origItem + " depends on " + dependency + " which is not in the graph");
                    }
                    targetDependencySet.add(dependency);
                }
                targetMap.put(origItem, targetDependencySet);
            }
            return targetMap;
        }

        private X findItemWithNoDependencies() {
            for (Map.Entry<X, Set<X>> entry : remainingDependencies.entrySet()) {
                if (entry.getValue().isEmpty()) {
                    return entry.getKey();
                }
            }
            return null;
        }

        private void remove(X item) {
            remainingDependencies.remove(item);
            for (Set<X> dependencies : remainingDependencies.values()) {
                dependencies.remove(item);
            }
        }

        private boolean isComplete() {
            return remainingDependencies.isEmpty();
        }

        @SuppressWarnings("WeakerAccess")
        public @NotNull List<X> getSortedItems() {
            return sortedItems;
        }

        @SuppressWarnings("WeakerAccess")
        public @NotNull Collection<X> getRemainingItems() {
            return remainingDependencies.keySet();
        }
    }

    /** An item that can tell us about its dependencies. */
    public interface Item<I extends Item<I>> {

        /** Returns the items that this item depends on. */
        @NotNull Collection<I> getDependencies();
    }
}
