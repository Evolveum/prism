/*
 * Copyright (C) 2020 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
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
