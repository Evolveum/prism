/*
 * Copyright (c) 2010-2013 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import org.jetbrains.annotations.NotNull;
import org.testng.annotations.Test;

import com.evolveum.midpoint.tools.testng.AbstractUnitTest;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DependencyGraphTest extends AbstractUnitTest {

    @Test
    public void testEmptyGraph() {
        var emptyGraph = DependencyGraph.ofMap(Map.of());
        var sortedItems = emptyGraph.getSortedItems();

        displayValue("sortedItems", sortedItems);
        assertThat(sortedItems).isEmpty();
    }

    @Test
    public void testNoDependencies() {
        var noDepGraph = DependencyGraph.ofMap(Map.of("a", Set.of(), "b", Set.of(), "c", Set.of()));
        var sortedItems = noDepGraph.getSortedItems();

        displayValue("sortedItems", sortedItems);
        assertThat(sortedItems).containsExactlyInAnyOrder("a", "b", "c");
    }

    @Test
    public void testInvalidDependencies() {
        var invalidGraph = DependencyGraph.ofMap(Map.of("a", Set.of("b")));
        try {
            invalidGraph.getSortedItems();
            fail("unexpected success");
        } catch (IllegalStateException e) {
            displayExpectedException(e);
            assertThat(e).hasMessage("Item a depends on b which is not in the graph");
        }
    }

    @Test
    public void testSimpleCyclicDependencies() {
        var cycle = DependencyGraph.ofMap(Map.of("a", Set.of("a"), "b", Set.of()));
        try {
            cycle.getSortedItems();
            fail("unexpected success");
        } catch (IllegalStateException e) {
            displayExpectedException(e);
            assertThat(e).hasMessage("Cyclic dependencies. Remaining items: [a]");
        }

        var sort = cycle.getTopologicalSort();
        assertThat(sort.getSortedItems()).containsExactly("b");
        assertThat(sort.getRemainingItems()).containsExactly("a");
    }

    @Test
    public void testComplexCyclicDependencies() {
        var cycle = DependencyGraph.ofMap(Map.of("a", Set.of("b"), "b", Set.of("c"), "d", Set.of(), "c", Set.of("a")));
        try {
            cycle.getSortedItems();
            fail("unexpected success");
        } catch (IllegalStateException e) {
            displayExpectedException(e);
            assertThat(e).hasMessageContaining("Cyclic dependencies. Remaining items:");
        }

        var sort = cycle.getTopologicalSort();
        assertThat(sort.getSortedItems()).containsExactly("d");
        assertThat(sort.getRemainingItems()).containsExactlyInAnyOrder("a", "b", "c");
    }

    @Test
    public void testConstruction() {
        var a = new TestItem("a", Set.of());
        var b = new TestItem("b", Set.of(a));
        var c = new TestItem("c", Set.of(a, b));
        var d = new TestItem("d", Set.of());

        var graph = DependencyGraph.ofItems(List.of(a, b, c, d));
        var sortedItems = graph.getSortedItems();
        displayValue("sortedItems", sortedItems);

        assertThat(sortedItems).containsExactlyInAnyOrder(a, b, c, d);
        int indexOfA = sortedItems.indexOf(a);
        int indexOfB = sortedItems.indexOf(b);
        int indexOfC = sortedItems.indexOf(c);
        assertThat(indexOfA).isLessThan(indexOfB);
        assertThat(indexOfA).isLessThan(indexOfC);
        assertThat(indexOfB).isLessThan(indexOfC);
    }

    static class TestItem implements DependencyGraph.Item<TestItem> {

        @NotNull private final String id;
        private final Collection<TestItem> dependencies;

        TestItem(@NotNull String id, Collection<TestItem> dependencies) {
            this.id = id;
            this.dependencies = dependencies;
        }

        @Override
        public @NotNull Collection<TestItem> getDependencies() {
            return dependencies;
        }

        @Override
        public String toString() {
            return id;
        }
    }
}
