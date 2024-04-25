/*
 * Copyright (c) 2010-2018 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism.path;

import java.io.Serializable;

/**
 * Fixme: Rename this to its actual use and meaning - item path representation normalized for textual search (used in indexing)
 *
 */
public interface CanonicalItemPath extends Serializable {

    int size();

    CanonicalItemPath allUpToIncluding(int n);

    String asString();

}
