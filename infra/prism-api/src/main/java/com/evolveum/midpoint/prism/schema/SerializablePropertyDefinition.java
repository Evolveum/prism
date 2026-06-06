/*
 * Copyright (C) 2010-2024 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.prism.schema;

import com.evolveum.midpoint.prism.PrismReferenceValue;
import com.evolveum.midpoint.util.DisplayableValue;

import javax.xml.namespace.QName;
import java.util.Collection;

/** Any property definition that can be serialized. */
public interface SerializablePropertyDefinition<T> extends SerializableItemDefinition {

    QName getMatchingRuleQName();
    PrismReferenceValue getValueEnumerationRef();
    Collection<? extends DisplayableValue<T>> getAllowedValues();
    Collection<? extends DisplayableValue<T>> getSuggestedValues();
}
