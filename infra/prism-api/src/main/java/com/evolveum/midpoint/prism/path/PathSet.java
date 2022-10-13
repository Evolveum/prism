/*
 * Copyright (c) 2020 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism.path;

import com.evolveum.midpoint.util.annotation.Experimental;

import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * A "safe" set of {@link ItemPath} - i.e. the one where (e.g.) presence is checked using {@link ItemPath#equivalent(ItemPath)},
 * not {@link Object#equals(Object)} method.
 *
 * Slower than standard set! Operations are evaluated in `O(n)` time.
 */
@Experimental
public class PathSet implements Set<ItemPath> {

    private static final PathSet EMPTY = new PathSet(List.of(), false);

    /** Can be mutable or immutable. */
    @NotNull private final List<ItemPath> content;

    private PathSet(@NotNull List<ItemPath> initialContent, boolean cloneOnCreation) {
        content = cloneOnCreation ?
                new ArrayList<>(initialContent) : initialContent;
    }

    public PathSet() {
        this(List.of(), true);
    }

    public PathSet(@NotNull Collection<ItemPath> initialContent) {
        this(new ArrayList<>(initialContent), false);
    }

    public static PathSet empty() {
        return EMPTY;
    }

    /**
     * TODO maybe we should return immutable {@link PathSet} here.
     */
    public static @NotNull PathSet of(ItemPath... paths) {
        return new PathSet(List.of(paths), true);
    }

    @Override
    public int size() {
        return content.size();
    }

    @Override
    public boolean isEmpty() {
        return content.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return o instanceof ItemPath && ItemPathCollectionsUtil.containsEquivalent(content, (ItemPath) o);
    }

    @NotNull
    @Override
    public Iterator<ItemPath> iterator() {
        return content.iterator();
    }

    @SuppressWarnings("NullableProblems")
    @NotNull
    @Override
    public Object[] toArray() {
        return content.toArray();
    }

    @SuppressWarnings("NullableProblems")
    @NotNull
    @Override
    public <T> T[] toArray(@NotNull T[] a) {
        //noinspection SuspiciousToArrayCall
        return content.toArray(a);
    }

    @Override
    public boolean add(@NotNull ItemPath itemPath) {
        //noinspection SimplifiableIfStatement
        if (contains(itemPath)) {
            return false;
        } else {
            return content.add(itemPath);
        }
    }

    @Override
    public boolean remove(Object o) {
        return content.removeIf(path -> o instanceof ItemPath && path.equivalent((ItemPath) o));
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        for (Object o : c) {
            //noinspection SuspiciousMethodCalls
            if (!content.contains(o)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends ItemPath> c) {
        c.forEach(this::add);
        return true; // fixme
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        c.forEach(this::remove);
        return true; // fixme
    }

    @Override
    public void clear() {
        content.clear();
    }

    @Override
    public String toString() {
        return content.toString();
    }
}
