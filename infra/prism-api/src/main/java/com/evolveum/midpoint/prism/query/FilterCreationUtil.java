/*
 * Copyright (c) 2010-2018 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.prism.query;

import com.evolveum.midpoint.prism.PrismContext;

/**
 * TODO decide what to do with this
 */
public class FilterCreationUtil {

    private static QueryFactory queryFactory(PrismContext prismContext) {
        return prismContext.queryFactory();
    }

    private static QueryFactory queryFactory() {
        return PrismContext.get().queryFactory();
    }

    @Deprecated
    public static AllFilter createAll(PrismContext prismContext) {
        return queryFactory(prismContext).createAll();
    }

    public static AllFilter createAll() {
        return queryFactory().createAll();
    }

    @Deprecated
    public static NoneFilter createNone(PrismContext prismContext) {
        return queryFactory(prismContext).createNone();
    }

    public static NoneFilter createNone() {
        return queryFactory().createNone();
    }

    @Deprecated
    public static ObjectFilter createUndefined(PrismContext prismContext) {
        return queryFactory(prismContext).createUndefined();
    }

    public static ObjectFilter createUndefined() {
        return queryFactory().createUndefined();
    }
}
