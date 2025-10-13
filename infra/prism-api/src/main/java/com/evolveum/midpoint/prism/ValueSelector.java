/*
 * Copyright (C) 2020-2023 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.prism;

import java.util.Objects;
import java.util.function.Predicate;
import javax.xml.namespace.QName;

import org.jetbrains.annotations.NotNull;

import com.evolveum.midpoint.prism.path.ItemName;
import com.evolveum.midpoint.prism.polystring.PolyString;
import com.evolveum.midpoint.util.QNameUtil;

/**
 * Selects a value from multivalued item (property, container, reference). A typical use is to select
 * among PrismContainerValues by checking some sub-item ("key") value.
 *
 * TODO Find a better name. "ValueMatcher" is already used in a different context.
 */
@FunctionalInterface
public interface ValueSelector<V extends PrismValue> extends Predicate<V> {

    /**
     * Matches PrismContainerValue if it has single-valued sub-item named "itemName" with the value of "expectedValue"
     * (or if the sub-item is not present and expectedValue is null).
     */
    static <C extends Containerable> ValueSelector<PrismContainerValue<C>> itemEquals(ItemName itemName, Object expectedValue) {
        return containerValue -> {
            Item<?, ?> item = containerValue.findItem(itemName);
            Object itemValue = item != null ? item.getRealValue() : null;
            return Objects.equals(itemValue, expectedValue);
        };
    }

    static <T> ValueSelector<PrismPropertyValue<T>> valueEquals(@NotNull T expectedValue) {
        return ppv -> ppv != null && expectedValue.equals(ppv.getRealValue());
    }

    static <T> ValueSelector<PrismPropertyValue<T>> origEquals(@NotNull T expectedValue) {
        return ppv -> ppv != null && expectedValue.equals(PolyString.getOrig((PolyString) ppv.getRealValue()));
    }

    static ValueSelector<PrismReferenceValue> refEquals(@NotNull String expectedTargetOid) {
        return ppv -> ppv != null && expectedTargetOid.equals(ppv.getOid());
    }

    static ValueSelector<PrismReferenceValue> refEquals(@NotNull String expectedTargetOid, QName expectedRelation) {
        return ppv -> ppv != null
                && expectedTargetOid.equals(ppv.getOid())
                && QNameUtil.match(expectedRelation, ppv.getRelation());
    }
}
