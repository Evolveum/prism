/*
 * Copyright (c) 2020 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism.path;

import java.io.Serializable;
import java.util.*;
import javax.xml.namespace.QName;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.evolveum.midpoint.util.QNameUtil;

/**
 * Special case of a map that has {@link QName} or {@link ItemName} as a key.
 *
 * The main issue with path-keyed maps is that comparing item paths using equals/hashCode is
 * unreliable.
 *
 * This map does _not_ support null keys. Also, collections returned by keySet(), values(), entrySet()
 * are not modifiable.
 *
 * @see PathKeyedMap
 */
public class NameKeyedMap<K extends QName, T> implements Map<K, T>, Serializable {

    private final Map<K, T> internalMap = new HashMap<>();

    @Override
    public int size() {
        return internalMap.size();
    }

    @Override
    public boolean isEmpty() {
        return internalMap.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return key instanceof QName &&
                QNameUtil.contains(internalMap.keySet(), (QName) key);
    }

    @Override
    public boolean containsValue(Object value) {
        return internalMap.containsValue(value);
    }

    @Override
    public T get(Object key) {
        // TODO optimize if all is fully qualified
        if (key instanceof QName) {
            for (Entry<K, T> entry : internalMap.entrySet()) {
                if (QNameUtil.match(entry.getKey(), (QName) key)) {
                    return entry.getValue();
                }
            }
        }
        return null;
    }

    @Nullable
    @Override
    public T put(K key, T value) {
        Objects.requireNonNull(key);
        for (K existingKey : internalMap.keySet()) {
            if (QNameUtil.match(existingKey, key)) {
                return internalMap.put(existingKey, value);
            }
        }
        return internalMap.put(key, value);
    }

    @Override
    public T remove(Object key) {
        if (key instanceof QName) {
            for (K existingKey : internalMap.keySet()) {
                if (QNameUtil.match(existingKey, (QName) key)) {
                    return internalMap.remove(existingKey);
                }
            }
        }
        return null;
    }

    @Override
    public void putAll(@NotNull Map<? extends K, ? extends T> m) {
        for (Entry<? extends K, ? extends T> entry : m.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void clear() {
        internalMap.clear();
    }

    @NotNull
    @Override
    public Set<K> keySet() {
        return Collections.unmodifiableSet(internalMap.keySet());
    }

    @NotNull
    @Override
    public Collection<T> values() {
        return Collections.unmodifiableCollection(internalMap.values());
    }

    @NotNull
    @Override
    public Set<Entry<K, T>> entrySet() {
        return Collections.unmodifiableSet(internalMap.entrySet());
    }

    @Override
    public String toString() {
        return internalMap.toString();
    }
}
