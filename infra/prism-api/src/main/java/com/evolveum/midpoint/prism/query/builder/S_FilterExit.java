/*
 * Copyright (C) 2010-2022 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.prism.query.builder;

/**
 * See the grammar in Javadoc for {@code QueryBuilder}.
 */
public interface S_FilterExit extends S_QueryExit {

    S_FilterEntry or();
    S_FilterEntry and();
}
