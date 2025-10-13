/*
 * Copyright (C) 2020 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.axiom.reactor;

import java.util.Collection;

public interface DependantAction<E extends Exception> extends Action<E> {

    Collection<Dependency<?>> dependencies();

    @Override
    default boolean canApply() {
        return Dependency.allSatisfied(dependencies());
    }


}
