/*
 * Copyright (C) 2020 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.concepts;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

public interface Identifiable<I> {

    I identifier();

    static <K,V extends Identifiable<K>> Map<K,V> identifierMap(Iterable<? extends V> identifiables) {
        ImmutableMap.Builder<K,V> builder = ImmutableMap.builder();
        putAll(builder, identifiables);
        return builder.build();
    }

    static <K,V extends Identifiable<K>> void putAll(ImmutableMap.Builder<K,V> map, Iterable<? extends V> identifiables) {
        for (V v : identifiables) {
            map.put(v.identifier(), v);
        }
    }

    static <K,V extends Identifiable<K>> void putAll(Map<K,V> map, Iterable<? extends V> identifiables) {
        for (V v : identifiables) {
            map.put(v.identifier(), v);
        }
    }

}
