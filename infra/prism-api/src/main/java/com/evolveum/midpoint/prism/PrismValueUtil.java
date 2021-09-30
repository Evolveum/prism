/*
 * Copyright (C) 2010-2021 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.prism;

import javax.xml.namespace.QName;

import org.apache.commons.lang.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.evolveum.midpoint.prism.xnode.ListXNode;
import com.evolveum.midpoint.prism.xnode.RootXNode;
import com.evolveum.midpoint.prism.xnode.XNode;
import com.evolveum.midpoint.util.exception.SchemaException;

public class PrismValueUtil {

    @Nullable
    public static PrismContainerValue<?> getParentContainerValue(PrismValue value) {
        Itemable parent = value.getParent();
        if (parent instanceof Item) {
            return ((Item<?, ?>) parent).getParent();
        } else {
            return null;
        }
    }

    /**
     * Returns parent object, potentially traversing multiple parent links to get there.
     */
    @Nullable
    public static PrismObject<?> getParentObject(@Nullable PrismValue value) {
        while (value != null) {
            if (value instanceof PrismObjectValue) {
                return ((PrismObjectValue<?>) value).asPrismObject();
            }
            value = getParentContainerValue(value);
        }
        return null;
    }

    public static <T> PrismProperty<T> createRaw(@NotNull XNode node, @NotNull QName itemName, PrismContext prismContext)
            throws SchemaException {
        Validate.isTrue(!(node instanceof RootXNode));
        PrismProperty<T> property = prismContext.itemFactory().createProperty(itemName);
        if (node instanceof ListXNode) {
            for (XNode subnode : ((ListXNode) node).asList()) {
                property.add(createRaw(subnode, prismContext));
            }
        } else {
            property.add(createRaw(node, prismContext));
        }
        return property;
    }

    private static <T> PrismPropertyValue<T> createRaw(XNode rawElement, PrismContext prismContext) {
        return prismContext.itemFactory().createPropertyValue(rawElement);
    }

    public static boolean differentIds(PrismValue v1, PrismValue v2) {
        Long id1 = v1 instanceof PrismContainerValue ? ((PrismContainerValue<?>) v1).getId() : null;
        Long id2 = v2 instanceof PrismContainerValue ? ((PrismContainerValue<?>) v2).getId() : null;
        return id1 != null && id2 != null && id1.longValue() != id2.longValue();
    }
}
