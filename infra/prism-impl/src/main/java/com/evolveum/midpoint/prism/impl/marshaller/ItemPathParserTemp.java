/*
 * Copyright (c) 2010-2018 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.prism.impl.marshaller;

import com.evolveum.midpoint.prism.path.UniformItemPath;
import org.w3c.dom.Element;

/**
 *
 */
public class ItemPathParserTemp {
    public static UniformItemPath parseFromString(String string) {
        return ItemPathHolder.parseFromString(string);
    }

    public static UniformItemPath parseFromElement(Element element) {
        return ItemPathHolder.parseFromElement(element);
    }
}
