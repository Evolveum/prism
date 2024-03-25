/*
 * Copyright (C) 2010-2024 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism.impl.schema.annotation;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import javax.xml.namespace.QName;

import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Element;

import com.evolveum.midpoint.util.DOMUtil;

/** Holds the value corresponding to the "alwaysUseForEquals" annotation. */
public class AlwaysUseForEquals implements Serializable {

    /** Applies to the whole item. */
    private final boolean universal;

    /** Applies only to selected sub-items of the item. */
    @NotNull private final Collection<QName> itemNames;

    private AlwaysUseForEquals(boolean universal, @NotNull Collection<QName> itemNames) {
        this.universal = universal;
        this.itemNames = itemNames;
    }

    /** Converts the XSD annotation into the (parsed) value. */
    public static AlwaysUseForEquals parse(List<Element> elements) {
        if (elements == null || elements.isEmpty()) {
            return null;
        }

        if (elements.size() == 1) {
            var text = elements.get(0).getTextContent();
            if (text.isBlank() || "true".equals(text)) { // we hope there won't be QName of "true"
                return new AlwaysUseForEquals(true, List.of());
            }
        }

        return new AlwaysUseForEquals(
                false,
                elements.stream()
                        .map(DOMUtil::getQNameValue)
                        .filter(qname -> qname != null)
                        .toList());
    }

    public boolean isUniversal() {
        return universal;
    }

    public @NotNull Collection<QName> getItemNames() {
        return itemNames;
    }
}
