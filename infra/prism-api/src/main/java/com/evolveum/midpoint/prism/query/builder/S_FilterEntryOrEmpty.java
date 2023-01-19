/*
 * Copyright (C) 2010-2023 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.prism.query.builder;

/**
 * See the grammar in Javadoc for {@code QueryBuilder}.
 *
 * Since 4.7 this helper interface includes {@link #and()} and {@link #or()} operations,
 * which is convenient for filters with nested filter without the need to explicitly
 * open and close empty block for the empty nested filter.
 * This is also in line with the aforementioned grammar.
 */
public interface S_FilterEntryOrEmpty extends S_FilterEntry, S_FilterExit {
}
