package com.evolveum.midpoint.prism;

import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.util.exception.SchemaException;

/**
 * Allow to walk conditionally through an object hierarchy
 */
public interface Walkable {

    /**
     * Walk through hierarchy of containing items based on provided conditions.
     *
     * Caller can provide two types of conditions. One ({@code consumePredicate}) is used to decide if currently
     * iterated item should be consumed (by provided {@code itemConsumer}). Second ({@code descendPredicate}) tells, if
     * walk should descend into currently iterated item.
     *
     * Descending condition is a BiPredicate in order to allow caller decide not just based on item path, but also
     * based on the fact if the item was also consumed (depending on implementation, the results of
     * {@code consumePredicate} could be directly passed to the descending condition).
     *
     * @param descendPredicate the {@code BiPredicate} which tells whether to descend into current item. Boolean
     * parameter tells whether the item was also consumed or not.
     * @param consumePredicate the {@code Predicate} which tells whether to consume current item.
     * @param itemConsumer the consumer to consume item with if it passes the {@code consumePredicate} test.
     * @throws SchemaException when something wrong happen during the walk.
     */
    void walk(BiPredicate<? super ItemPath, Boolean> descendPredicate, Predicate<? super ItemPath> consumePredicate,
            Consumer<? super Item<?, ?>> itemConsumer) throws SchemaException;
}
