/*
 * Copyright (C) 2020-2021 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.prism.impl.query.lang;

import com.evolveum.midpoint.prism.query.PrismQuerySerialization;
import com.evolveum.midpoint.prism.query.ObjectFilter;

interface FilterSerializer<T extends ObjectFilter> {

    @SuppressWarnings("unchecked")
    default void castAndWrite(ObjectFilter source, QueryWriter target) throws PrismQuerySerialization.NotSupportedException {
        write((T) source, target);
    }

    void write(T source, QueryWriter target) throws PrismQuerySerialization.NotSupportedException;

}
