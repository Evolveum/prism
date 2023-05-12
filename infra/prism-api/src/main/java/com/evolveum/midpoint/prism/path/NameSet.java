/*
 * Copyright (c) 2020 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism.path;

import java.util.*;
import javax.xml.namespace.QName;

import org.jetbrains.annotations.NotNull;

import com.evolveum.midpoint.util.QNameUtil;

/**
 * A "safe" set of {@link QName} - i.e. the one where (e.g.) presence is checked using {@link QNameUtil#match(QName, QName)},
 * not {@link Object#equals(Object)} method.
 *
 * Slower than standard set! Operations are evaluated in `O(n)` time.
 *
 * @see PathSet
 */
public class NameSet<N extends QName> implements Set<N> {

    private static final NameSet<QName> EMPTY = new NameSet<>(List.of(), false);

    /** Can be mutable or immutable. */
    @NotNull private final List<N> content;

    private NameSet(@NotNull List<N> initialContent, boolean cloneOnCreation) {
        content = cloneOnCreation ?
                new ArrayList<>(initialContent) : initialContent;
    }

    public NameSet() {
        this(List.of(), true);
    }

    public NameSet(@NotNull Collection<N> initialContent) {
        this(new ArrayList<>(initialContent), false);
    }

    public static NameSet<QName> empty() {
        return EMPTY;
    }

    /**
     * TODO maybe we should return immutable {@link NameSet} here.
     */
    @SafeVarargs
    public static <N extends QName> @NotNull NameSet<N> of(N... names) {
        return new NameSet<>(List.of(names), true);
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
        return o instanceof ItemPath && QNameUtil.contains(content, (QName) o);
    }

    @NotNull
    @Override
    public Iterator<N> iterator() {
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
    public boolean add(@NotNull N name) {
        //noinspection SimplifiableIfStatement
        if (contains(name)) {
            return false;
        } else {
            return content.add(name);
        }
    }

    @Override
    public boolean remove(Object o) {
        return content.removeIf(name -> o instanceof QName && QNameUtil.match(name, (QName) o));
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
    public boolean addAll(@NotNull Collection<? extends N> c) {
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
