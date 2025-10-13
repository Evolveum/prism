/*
 * Copyright (C) 2010-2022 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.prism.query.builder;

/**
 * See the grammar in Javadoc for {@code QueryBuilder}.
 */
public interface S_FilterExit extends S_QueryExit {

    S_FilterEntry or();
    S_FilterEntry and();
}
