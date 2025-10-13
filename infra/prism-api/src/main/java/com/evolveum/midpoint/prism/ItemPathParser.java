/*
 * Copyright (c) 2010-2018 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.prism;

import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.prism.path.UniformItemPath;
import com.evolveum.prism.xml.ns._public.types_3.ItemPathType;

/**
 * Parses string representation of {@link ItemPath} and {@link ItemPathType} objects.
 */
public interface ItemPathParser {

    ItemPathType asItemPathType(String value);

    UniformItemPath asItemPath(String value);
}
