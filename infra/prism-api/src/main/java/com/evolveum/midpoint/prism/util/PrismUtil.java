/*
 * Copyright (C) 2010-2021 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.prism.util;

import static java.util.Collections.unmodifiableCollection;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.xml.namespace.QName;

import com.evolveum.midpoint.prism.equivalence.EquivalenceStrategy;

import org.apache.commons.lang3.StringUtils;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.delta.ItemDelta;
import com.evolveum.midpoint.prism.match.MatchingRule;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.prism.polystring.PolyString;
import com.evolveum.midpoint.prism.xml.XsdTypeMapper;
import com.evolveum.midpoint.util.DebugDumpable;
import com.evolveum.midpoint.util.DebugUtil;
import com.evolveum.midpoint.util.QNameUtil;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.prism.xml.ns._public.types_3.PolyStringType;

/**
 * TODO clean this up as it is part of prism-api!
 *
 * @author semancik
 */
public class PrismUtil {

    public static <T> void recomputeRealValue(T realValue, PrismContext prismContext) {
        recomputeRealValue(realValue);
    }

    public static <T> void recomputeRealValue(T realValue) {
        if (realValue == null) {
            return;
        }
        // TODO: switch to Recomputable interface instead of PolyString
        if (realValue instanceof PolyString polyStringVal && polyStringVal.getNorm() == null) {
            // Compute only if norm is missing. Otherwise, this could destroy items with non-standard normalizations
            // (like resource attributes)
            polyStringVal.recompute(
                    PrismContext.get().getDefaultPolyStringNormalizer());
        }
    }

    public static <T> void recomputePrismPropertyValue(PrismPropertyValue<T> pValue) {
        if (pValue != null) {
            recomputeRealValue(pValue.getValue());
        }
    }

    public static boolean isEmpty(PolyStringType value) {
        return value == null || StringUtils.isEmpty(value.getOrig()) && StringUtils.isEmpty(value.getNorm());
    }

    public static <T, X> PrismProperty<X> convertProperty(PrismProperty<T> srcProp, PrismPropertyDefinition<X> targetDef)
            throws SchemaException {
        if (targetDef.getTypeName().equals(srcProp.getDefinition().getTypeName())) {
            //noinspection unchecked
            return (PrismProperty<X>) srcProp;
        } else {
            PrismProperty<X> targetProp = targetDef.instantiate();
            Class<X> expectedJavaType = XsdTypeMapper.toJavaType(targetDef.getTypeName());
            for (PrismPropertyValue<T> srcPVal : srcProp.getValues()) {
                X convertedRealValue = JavaTypeConverter.convert(expectedJavaType, srcPVal.getValue());
                targetProp.add(PrismContext.get().itemFactory().createPropertyValue(convertedRealValue));
            }
            return targetProp;
        }
    }

    public static <O extends Objectable> void setDeltaOldValue(PrismObject<O> oldObject, ItemDelta<?, ?> itemDelta) {
        if (oldObject == null) {
            return;
        }
        Item<PrismValue, ItemDefinition<?>> itemOld = oldObject.findItem(itemDelta.getPath());
        if (itemOld != null) {
            itemDelta.setEstimatedOldValues((Collection) PrismValueCollectionsUtil.cloneCollection(itemOld.getValues()));
        }
    }

    public static <O extends Objectable> void setDeltaOldValue(PrismObject<O> oldObject, Collection<? extends ItemDelta> itemDeltas) {
        for (ItemDelta itemDelta : itemDeltas) {
            setDeltaOldValue(oldObject, itemDelta);
        }
    }

    public static <T> boolean equals(T a, T b, MatchingRule<T> matchingRule) throws SchemaException {
        if (a == null && b == null) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }
        if (matchingRule == null) {
            if (a instanceof byte[]) {
                if (b instanceof byte[]) {
                    return Arrays.equals((byte[]) a, (byte[]) b);
                } else {
                    return false;
                }
            } else {
                return a.equals(b);
            }
        } else {
            return matchingRule.match(a, b);
        }
    }

    // for diagnostic purposes
    public static String serializeQuietly(PrismContext prismContext, Object object) {
        if (object == null) {
            return null;
        }
        if (object instanceof Collection) {
            return ((Collection<?>) object).stream()
                    .map(o -> serializeQuietly(prismContext, o))
                    .collect(Collectors.joining("; "));
        }
        try {
            PrismSerializer<String> serializer = prismContext.xmlSerializer();
            if (object instanceof Item) {
                return serializer.serialize((Item) object);
            } else {
                return serializer.serializeRealValue(object, new QName("value"));
            }
        } catch (Throwable t) {
            return "Couldn't serialize (" + t.getMessage() + "): " + object;
        }
    }

    // for diagnostic purposes
    public static Object serializeQuietlyLazily(PrismContext prismContext, Object object) {
        if (object == null) {
            return null;
        }
        return new Object() {
            @Override
            public String toString() {
                return serializeQuietly(prismContext, object);
            }
        };
    }

    public static void debugDumpWithLabel(StringBuilder sb, String label, Containerable cc, int indent) {
        if (cc == null) {
            DebugUtil.debugDumpWithLabel(sb, label, (DebugDumpable) null, indent);
        } else {
            DebugUtil.debugDumpWithLabel(sb, label, cc.asPrismContainerValue(), indent);
        }
    }

    public static void debugDumpWithLabelLn(StringBuilder sb, String label, Containerable cc, int indent) {
        debugDumpWithLabel(sb, label, cc, indent);
        sb.append("\n");
    }

    public static boolean isStructuredType(QName typeName) {
        return QNameUtil.match(PolyStringType.COMPLEX_TYPE, typeName);
    }

    // Does NOT clone individual freezables.
    public static <T extends Freezable> Collection<T> freezeCollectionDeeply(Collection<T> freezables) {
        freezables.forEach(Freezable::freeze);
        return unmodifiableCollection(freezables);
    }

    /** Sets the property with the provided path on the provided prism object to null and complete, if found. */
    public static <T extends Objectable> void setPropertyNullAndComplete(
            PrismObject<T> prismObject, ItemPath path) {
        PrismProperty<?> property = prismObject.findProperty(path);
        if (property != null) {
            property.setRealValue(null);
            property.setIncomplete(false);
        }
    }

    /**
     * As {@link Objects#equals(Object, Object)} but comparing with {@link EquivalenceStrategy#REAL_VALUE}.
     *
     * Null values are considered equal to empty PCVs.
     */
    public static boolean realValueEquals(Containerable first, Containerable second) {
        if (first != null && second != null) {
            return first.asPrismContainerValue()
                    .equals(second.asPrismContainerValue(), EquivalenceStrategy.REAL_VALUE);
        } else if (first == null) {
            return second == null || second.asPrismContainerValue().isEmpty();
        } else {
            assert second == null;
            assert first != null;
            return first.asPrismContainerValue().isEmpty();
        }
    }
}
