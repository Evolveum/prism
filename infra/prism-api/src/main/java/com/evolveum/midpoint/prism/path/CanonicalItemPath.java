/*
 * Copyright (c) 2010-2018 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
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
