/*
 * Copyright (c) 2010-2017 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism;

import com.evolveum.midpoint.prism.delta.PropertyDelta;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.util.DOMUtil;
import com.evolveum.midpoint.util.DisplayableValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.xml.namespace.QName;
import java.util.Collection;

/**
 * Definition of a prism property.
 */
public interface PrismPropertyDefinition<T> extends ItemDefinition<PrismProperty<T>> {

    /**
     * Returns allowed values for this property.
     */
    @Nullable Collection<? extends DisplayableValue<T>> getAllowedValues();

    /**
     * Returns suggested values for this property.
     */
    @Nullable Collection<? extends DisplayableValue<T>> getSuggestedValues();

    /**
     * TODO is this ever used?
     */
    @Nullable T defaultValue();

    /**
     * This is XSD annotation that specifies whether a property should
     * be indexed in the storage. It can only apply to properties. It
     * has following meaning:
     *
     * true: the property must be indexed. If the storage is not able to
     * index the value, it should indicate an error.
     *
     * false: the property should not be indexed.
     *
     * null: data store decides whether to index the property or
     * not.
     */
    Boolean isIndexed();

    default boolean isAnyType() {
        return DOMUtil.XSD_ANYTYPE.equals(getTypeName());
    }

    /**
     * Returns matching rule name. Matching rules are algorithms that specify
     * how to compare, normalize and/or order the values. E.g. there are matching
     * rules for case insensitive string comparison, for LDAP DNs, etc.
     *
     * @return matching rule name
     */
    QName getMatchingRuleQName();

    @Override
    @NotNull
    PropertyDelta<T> createEmptyDelta(ItemPath path);

    @NotNull
    @Override
    PrismProperty<T> instantiate();

    @NotNull
    @Override
    PrismProperty<T> instantiate(QName name);

    @NotNull
    @Override
    PrismPropertyDefinition<T> clone();

    @Override
    Class<T> getTypeClass();

    @Override
    MutablePrismPropertyDefinition<T> toMutable();
}
