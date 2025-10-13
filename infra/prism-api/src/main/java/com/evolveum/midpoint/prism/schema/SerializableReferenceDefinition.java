/*
 * Copyright (C) 2010-2024 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.prism.schema;

import javax.xml.namespace.QName;

/** Any reference definition that can be serialized. */
public interface SerializableReferenceDefinition extends SerializableItemDefinition {
    QName getTargetTypeName();
    boolean isComposite();
}
