/*
 * Copyright (c) 2020 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism.path;

import java.io.Serializable;
import java.util.*;

import com.evolveum.midpoint.prism.AbstractFreezable;

import org.jetbrains.annotations.NotNull;

import com.evolveum.midpoint.util.annotation.Experimental;

/**
 * A "safe" set of {@link ItemPath} - i.e. the one where (e.g.) presence is checked using {@link ItemPath#equivalent(ItemPath)},
 * not {@link Object#equals(Object)} method.
 *
 * Slower than standard set! Operations are evaluated in `O(n)` time.
 */
@Experimental
public class PathSet extends AbstractFreezable implements Set<ItemPath>, Serializable {

    private static final PathSet EMPTY = new PathSet(List.of(), false);

    /** Can be mutable or immutable. */
    @NotNull private List<ItemPath> content;

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

    /** Returns immutable empty path set. */
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

    public boolean containsSubpathOrEquivalent(@NotNull ItemPath path) {
        return ItemPathCollectionsUtil.containsSubpathOrEquivalent(this, path);
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

    /**
     * Factors the path set on the first segment of the paths.
     *
     * Assumes that each of the paths begins with a name.
     */
    public @NotNull NameKeyedMap<ItemName, PathSet> factor() {
        NameKeyedMap<ItemName, PathSet> map = new NameKeyedMap<>();
        for (ItemPath path : this) {
            map.computeIfAbsent(path.firstNameOrFail(), k -> new PathSet())
                    .add(path.rest());
        }
        return map;
    }

    public @NotNull PathSet remainder(@NotNull ItemPath prefix) {
        return new PathSet(
                ItemPathCollectionsUtil.remainder(this, prefix, true),
                false); // the list returned by the callee is not shared
    }

    @Override
    public String toString() {
        return content.toString();
    }

    @Override
    protected void performFreeze() {
        content = Collections.unmodifiableList(content);
    }

    /** Returns `true` it the set (at least partially) covers given item. */
    public boolean containsRelated(@NotNull ItemPath path) {
        return ItemPathCollectionsUtil.containsRelated(this, path);
    }
}
