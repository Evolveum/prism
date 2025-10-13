/*
 * Copyright (c) 2010-2025 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.axiom.concepts;

public interface CheckedSupplier<O,E extends Exception> {

    O get() throws E;
}
