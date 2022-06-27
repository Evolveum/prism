/*
 * Copyright (c) 2010-2018 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism;

import com.evolveum.midpoint.util.DisplayableValue;

import org.jetbrains.annotations.NotNull;

import javax.xml.namespace.QName;
import java.util.Collection;

/**
 *
 */
public interface MutablePrismPropertyDefinition<T> extends PrismPropertyDefinition<T>, MutableItemDefinition<PrismProperty<T>> {

    void setIndexed(Boolean value);

    void setMatchingRuleQName(QName matchingRuleQName);

    @NotNull
    @Override
    PrismPropertyDefinition<T> clone();

    void setInherited(boolean value);

    void setAllowedValues(Collection<? extends DisplayableValue<T>> allowedValues);

    void setSuggestedValues(Collection<? extends DisplayableValue<T>> suggestedValues);
}
