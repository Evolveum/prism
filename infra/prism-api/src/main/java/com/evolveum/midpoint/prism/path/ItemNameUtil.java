/*
 * Copyright (C) 2024 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.prism.path;

import com.evolveum.midpoint.util.QNameUtil;

import org.jetbrains.annotations.NotNull;

import javax.xml.namespace.QName;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ItemNameUtil {

    /**
     * Finds value in the map by QName key using {@link QNameUtil#match(QName, QName)}.
     * If normal and infra item is found, prefers normal item instead of infra item.
     * Fails if multiple matches are found.
     * Returns {@code null} if no match is found.
     *
     * !!! EXPECTS THAT THE MAP CONTAINS QUALIFIED NAMES (if querying by qualified key) !!!
     */
    public static <K extends QName, V> V getByQName(@NotNull Map<K, V> map, @NotNull K key) {
        if (QNameUtil.hasNamespace(key)) {
            return map.get(key);
        }
        List<Map.Entry<K, V>> matching = map.entrySet().stream()
                .filter(e -> QNameUtil.match(e.getKey(), key))
                .collect(Collectors.toList());

        if (matching.size() > 1) {
            // throw out all InfraItemNames if size is larger then one
            matching  = matching.stream().filter(e -> !(e.getKey() instanceof InfraItemName)).toList();
        }

        if (matching.isEmpty()) {
            return null;
        } else if (matching.size() == 1) {
            return matching.get(0).getValue();
        }
        throw new IllegalStateException("More than one matching value for key " + key + ": " + matching);
    }

}
