/*
 * Copyright (C) 2010-2021 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.prism;

import javax.xml.namespace.QName;

import org.apache.commons.lang3.Validate;
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
        if (parent instanceof Item<?, ?> item) {
            return item.getParent();
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
            if (value instanceof PrismObjectValue<?> objectValue) {
                return objectValue.asPrismObject();
            }
            value = getParentContainerValue(value);
        }
        return null;
    }

    /**
     * Returns the top-most object ({@link Objectable}).
     */
    @Deprecated // use value.getRootObjectable() directly
    public static @Nullable Objectable getRootObject(@NotNull PrismValue value) {
        return value.getRootObjectable();
    }

    /**
     * Returns the nearest (going upwards) real value of given `type`. Includes the provided `value` itself.
     */
    public static <T> T getNearestValueOfType(@Nullable PrismValue value, @NotNull Class<T> type) {
        for (;;) {
            if (value == null) {
                return null;
            }
            Class<?> realClass = value.getRealClass();
            if (realClass != null && type.isAssignableFrom(realClass)) {
                return value.getRealValue();
            }
            value = value.getParentContainerValue();
        }
    }

    public static <T> PrismProperty<T> createRaw(@NotNull XNode node, @NotNull QName itemName)
            throws SchemaException {
        Validate.isTrue(!(node instanceof RootXNode));
        PrismProperty<T> property = PrismContext.get().itemFactory().createProperty(itemName);
        if (node instanceof ListXNode list) {
            for (XNode subnode : list.asList()) {
                property.add(createRaw(subnode));
            }
        } else {
            property.add(createRaw(node));
        }
        return property;
    }

    private static <T> PrismPropertyValue<T> createRaw(XNode rawElement) {
        return PrismContext.get().itemFactory().createPropertyValue(rawElement);
    }

    public static boolean differentIds(PrismValue v1, PrismValue v2) {
        Long id1 = v1 instanceof PrismContainerValue<?> pcv ? pcv.getId() : null;
        Long id2 = v2 instanceof PrismContainerValue<?> pcv ? pcv.getId() : null;
        return id1 != null && id2 != null && id1.longValue() != id2.longValue();
    }
}
