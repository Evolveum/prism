/*
 * Copyright (C) 2020 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.axiom.api.stream;

import com.evolveum.axiom.api.AxiomName;
import com.evolveum.axiom.api.AxiomPrefixedName;
import com.evolveum.axiom.lang.spi.AxiomNameResolver;
import com.evolveum.concepts.SourceLocation;

public interface AxiomItemStream {

    interface Target extends AxiomStreamTarget<AxiomName, Object> {

    }

    interface TargetWithContext extends Target, AxiomStreamTarget.WithContext<AxiomName, Object> {

    }

}
