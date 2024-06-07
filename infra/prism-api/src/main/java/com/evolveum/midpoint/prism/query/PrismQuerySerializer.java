/*
 * Copyright (C) 2020-2021 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.prism.query;

import java.util.Optional;

import com.evolveum.midpoint.prism.PrismNamespaceContext;

public interface PrismQuerySerializer {

    default PrismQuerySerialization serialize(ObjectFilter filter, PrismNamespaceContext context) throws PrismQuerySerialization.NotSupportedException {
        return serialize(filter, context, false);
    }

    PrismQuerySerialization serialize(ObjectFilter filter, PrismNamespaceContext context, boolean forceDefaultPrefix) throws PrismQuerySerialization.NotSupportedException;


    default PrismQuerySerialization serialize(ObjectFilter filter) throws PrismQuerySerialization.NotSupportedException {
        return serialize(filter, PrismNamespaceContext.EMPTY);
    }

    default Optional<PrismQuerySerialization> trySerialize(ObjectFilter filter) {
        return trySerialize(filter, PrismNamespaceContext.EMPTY);
    }

    default Optional<PrismQuerySerialization> trySerialize(ObjectFilter filter, PrismNamespaceContext namespaceContext) {
        return trySerialize(filter, namespaceContext, false);
    }
    default Optional<PrismQuerySerialization> trySerialize(ObjectFilter filter, PrismNamespaceContext namespaceContext, boolean forceDefaultPrefix) {
        try {
            return Optional.of(serialize(filter, namespaceContext, forceDefaultPrefix));
        } catch (PrismQuerySerialization.NotSupportedException e) {
            return Optional.empty();
        }
    }


}
