/*
 * Copyright (c) 2010-2018 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.prism.delta;

import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.util.QNameUtil;

import javax.xml.namespace.QName;
import java.util.Collection;

/**
 * TEMPORARY. Unify with ItemDeltaCollectionsUtil
 */
public class PropertyDeltaCollectionsUtil {

    public static <T> PropertyDelta<T> findPropertyDelta(Collection<? extends ItemDelta> modifications, ItemPath propertyPath) {
        for (var delta: modifications) {
            if (delta instanceof PropertyDelta propertyDelta && delta.getPath().equivalent(propertyPath)) {
                return propertyDelta;
            }
        }
        return null;
    }

    public static <T> PropertyDelta<T> findPropertyDelta(Collection<? extends ItemDelta> modifications, QName propertyName) {
        for (var delta: modifications) {
            if (delta instanceof PropertyDelta propertyDelta && delta.getParentPath().isEmpty() &&
                    QNameUtil.match(delta.getElementName(), propertyName)) {
                return propertyDelta;
            }
        }
        return null;
    }
}
