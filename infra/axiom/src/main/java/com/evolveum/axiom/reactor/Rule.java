/*
 * Copyright (C) 2020 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.axiom.reactor;

import java.util.Collection;

public interface Rule<C, A extends Action<?>> {

    boolean applicableTo(C context);

    Collection<A> applyTo(C context);

}
