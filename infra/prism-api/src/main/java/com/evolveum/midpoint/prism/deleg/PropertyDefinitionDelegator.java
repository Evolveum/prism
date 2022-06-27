/*
 * Copyright (c) 2021 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism.deleg;

import java.util.Collection;
import javax.xml.namespace.QName;

import org.jetbrains.annotations.NotNull;

import com.evolveum.midpoint.prism.PrismProperty;
import com.evolveum.midpoint.prism.PrismPropertyDefinition;
import com.evolveum.midpoint.prism.delta.PropertyDelta;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.util.DisplayableValue;

import org.jetbrains.annotations.Nullable;

public interface PropertyDefinitionDelegator<T> extends ItemDefinitionDelegator<PrismProperty<T>>, PrismPropertyDefinition<T> {

    @Override
    PrismPropertyDefinition<T> delegate();

    @Override
    @Nullable
    default Collection<? extends DisplayableValue<T>> getAllowedValues() {
        return delegate().getAllowedValues();
    }

    @Override
    @Nullable
    default Collection<? extends DisplayableValue<T>> getSuggestedValues() {
        return delegate().getSuggestedValues();
    }
    @Override
    default T defaultValue() {
        return delegate().defaultValue();
    }

    @Override
    default Boolean isIndexed() {
        return delegate().isIndexed();
    }

    @Override
    default QName getMatchingRuleQName() {
        return delegate().getMatchingRuleQName();
    }

    @Override
    default @NotNull PropertyDelta<T> createEmptyDelta(ItemPath path) {
        return delegate().createEmptyDelta(path);
    }

    @Override
    default @NotNull PrismProperty<T> instantiate() {
        return delegate().instantiate();
    }

    @Override
    default @NotNull PrismProperty<T> instantiate(QName name) {
        return delegate().instantiate(name);
    }

    @Override
    default Class<T> getTypeClass() {
        return delegate().getTypeClass();
    }
}
