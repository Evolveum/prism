/*
 * Copyright (c) 2010-2013 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.prism;

import com.evolveum.midpoint.prism.path.ItemPath;

/**
 * Visits only objects that are on the specified path or below.
 *
 * @author Radovan Semancik
 *
 */
@FunctionalInterface
public interface PathVisitable {

    void accept(Visitor visitor, ItemPath path, boolean recursive);

}
