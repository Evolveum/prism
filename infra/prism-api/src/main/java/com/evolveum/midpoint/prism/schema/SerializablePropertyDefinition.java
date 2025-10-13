/*
 * Copyright (C) 2010-2024 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.prism.schema;

import com.evolveum.midpoint.prism.PrismReferenceValue;

import javax.xml.namespace.QName;

/** Any property definition that can be serialized. */
public interface SerializablePropertyDefinition extends SerializableItemDefinition {

    QName getMatchingRuleQName();
    PrismReferenceValue getValueEnumerationRef();
}
