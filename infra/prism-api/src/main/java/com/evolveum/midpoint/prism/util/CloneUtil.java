/*
 * Copyright (c) 2010-2015 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.prism.util;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.xml.XmlTypeConverter;
import com.evolveum.midpoint.util.LocalizableMessage;
import com.evolveum.midpoint.util.MiscUtil;
import com.evolveum.midpoint.util.annotation.Experimental;
import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;
import org.apache.commons.lang3.SerializationUtils;

import com.evolveum.midpoint.prism.delta.ItemDelta;
import com.evolveum.midpoint.prism.delta.ObjectDelta;
import com.evolveum.midpoint.prism.polystring.PolyString;
import com.evolveum.prism.xml.ns._public.types_3.ObjectDeltaType;
import com.evolveum.prism.xml.ns._public.types_3.RawType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.springframework.util.ClassUtils;

import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;

/**
 * @author semancik
 *
 */
public class CloneUtil {

    private static final Trace PERFORMANCE_ADVISOR = TraceManager.getPerformanceAdvisorTrace();

    @Contract("null -> null; !null -> !null")
    public static <T> T clone(T orig) {
        if (orig == null) {
            return null;
        }
        Class<?> origClass = orig.getClass();
        if (ClassUtils.isPrimitiveOrWrapper(origClass)) {
            return orig;
        } else if (origClass.isArray()) {
            return cloneArray(orig);
        } else if (orig instanceof PolyString) {
            //noinspection unchecked
            return (T) clonePolyString((PolyString) orig);
        } else if (orig instanceof String) {
            // String is immutable
            return orig;
        } else if (orig instanceof QName) {
            // the same here
            return orig;
        } else if (origClass.isEnum()) {
            return orig;
        } else if (orig instanceof LocalizableMessage) {
            return orig;        // all fields are final
        } else if (orig instanceof RawType) {
            //noinspection unchecked
            return (T) ((RawType) orig).clone();
        } else if (orig instanceof Item<?,?>) {
            //noinspection unchecked
            return (T) ((Item<?,?>)orig).clone();
        } else if (orig instanceof PrismValue) {
            //noinspection unchecked
            return (T) ((PrismValue)orig).clone();
        } else if (orig instanceof ObjectDelta<?>) {
            //noinspection unchecked
            return (T) ((ObjectDelta<?>)orig).clone();
        } else if (orig instanceof ObjectDeltaType) {
            //noinspection unchecked
            return (T) ((ObjectDeltaType) orig).clone();
        } else if (orig instanceof ItemDelta<?,?>) {
            //noinspection unchecked
            return (T) ((ItemDelta<?,?>)orig).clone();
        } else if (orig instanceof Definition) {
            //noinspection unchecked
            return (T) ((Definition)orig).clone();
        } else if (orig instanceof XMLGregorianCalendar) {
            /*
             * In some environments we cannot clone XMLGregorianCalendar because of this:
             * Error when cloning class org.apache.xerces.jaxp.datatype.XMLGregorianCalendarImpl, will try serialization instead.
             * java.lang.IllegalAccessException: Class com.evolveum.midpoint.prism.util.CloneUtil can not access a member of
             * class org.apache.xerces.jaxp.datatype.XMLGregorianCalendarImpl with modifiers "public"
             */
            //noinspection unchecked
            return (T) XmlTypeConverter.createXMLGregorianCalendar((XMLGregorianCalendar) orig);
        } else if (orig instanceof Duration) {
            /*
             * The following is because of: "Cloning a Serializable (class com.sun.org.apache.xerces.internal.jaxp.datatype.DurationImpl). It could harm performance."
             */
            //noinspection unchecked
            return (T) XmlTypeConverter.createDuration(((Duration) orig));
        } else if (orig instanceof BigInteger || orig instanceof BigDecimal) {
            // todo could we use "instanceof Number" here instead?
            //noinspection RedundantCast
            return (T) orig;
        }

        if (orig instanceof Cloneable) {
            T clone = javaLangClone(orig);
            if (clone != null) {
                return clone;
            }
        }

        if (orig instanceof PrismList) {
            // The result is different from shallow cloning. But we probably can live with this.
            //noinspection unchecked
            return (T) CloneUtil.cloneCollectionMembers((Collection) orig);
        } else if (orig instanceof Serializable) {
            // Brute force
            PERFORMANCE_ADVISOR.info("Cloning a Serializable ({}). It could harm performance.", orig.getClass());
            //noinspection unchecked
            return (T)SerializationUtils.clone((Serializable)orig);
        } else {
            throw new IllegalArgumentException("Cannot clone " + orig + " (" + origClass + ")");
        }
    }

    @Contract("null -> null; !null -> !null")
    public static <T extends Cloneable> T cloneCloneable(T cloneable) {
        if (cloneable != null) {
            return MiscUtil.stateNonNull(
                    javaLangClone(cloneable),
                    "Cloneable is not really cloneable? %s",
                    cloneable);
        } else {
            return null;
        }
    }

    /**
     * @return List that can be freely used.
     */
    @Contract("!null -> !null; null -> null")
    public static <T> List<T> cloneCollectionMembers(Collection<T> collection) {
        if (collection == null) {
            return null;
        }
        List<T> clonedCollection = new ArrayList<>(collection.size());
        cloneMembersToCollection(clonedCollection, collection);
        return clonedCollection;
    }

    public static <T> void cloneMembersToCollection(Collection<T> target, Collection<T> source) {
        for (T element : source) {
            target.add(clone(element));
        }
    }

    /**
     * @return List that contains its members clones. For members that are Containerable the IDs are cleared.
     *
     * FIXME: BEWARE - UNFINISHED
     *  - does not clear IDs if members are PCVs; does it only for Containerables
     *  - should be named maybe "copyForReuse" and use complexClone with the strategy of REUSE for all prism data
     *
     * See {@link Containerable#cloneWithoutId()} and its TODOs.
     */
    @Experimental
    @Contract("!null -> !null; null -> null")
    public static <T> List<T> cloneCollectionMembersWithoutIds(Collection<T> collection) {
        if (collection == null) {
            return null;
        }
        List<T> clonedCollection = new ArrayList<>(collection.size());
        for (T element : collection) {
            T elementCopy;
            if (element instanceof Containerable) {
                //noinspection unchecked
                elementCopy = ((Containerable) element).cloneWithoutId();
            } else {
                elementCopy = clone(element);
            }
            clonedCollection.add(elementCopy);
        }
        return clonedCollection;
    }

    private static PolyString clonePolyString(PolyString value) {
        if (value != null) {
            return new PolyString(value.getOrig(), value.getNorm(),
                    value.getTranslation() != null ? value.getTranslation().clone() : null,
                    value.getLang() != null ? new HashMap<>(value.getLang()) : null);
        } else {
            return null;
        }
    }

    @SuppressWarnings("SuspiciousSystemArraycopy")
    private static <T> T cloneArray(T orig) {
        int length = Array.getLength(orig);
        //noinspection unchecked
        T clone = (T) Array.newInstance(orig.getClass().getComponentType(), length);
        System.arraycopy(orig, 0, clone, 0, length);
        return clone;
    }

    private static <T> T javaLangClone(T orig) {
        try {
            Method cloneMethod = orig.getClass().getMethod("clone");
            //noinspection unchecked
            return (T) cloneMethod.invoke(orig);
        } catch (NoSuchMethodException|IllegalAccessException|InvocationTargetException|RuntimeException e) {
            PERFORMANCE_ADVISOR.info("Error when cloning {}, will try serialization instead.", orig.getClass(), e);
            return null;
        }
    }

    @Contract("null -> null; !null -> !null")
    public static <C extends Containerable> @Nullable C cloneIfImmutable(@Nullable C value) {
        if (value == null) {
            return null;
        }
        //noinspection unchecked
        PrismContainerValue<C> pcv = value.asPrismContainerValue();
        if (!pcv.isImmutable()) {
            return value;
        }
        return pcv.clone().asContainerable();
    }

    /**
     * Provides an immutable version of the input:
     *
     * - If the input is immutable, returns it as is.
     * - If the input is mutable, creates an immutable clone.
     */
    @Contract("null -> null; !null -> !null")
    public static <CF extends Cloneable & Freezable> CF toImmutable(@Nullable CF input) {
        if (input == null) {
            return null;
        }
        if (input.isImmutable()) {
            return input;
        } else {
            CF clone = CloneUtil.clone(input);
            clone.freeze();
            return clone;
        }
    }

    /**
     * Provides an immutable version of the input:
     *
     * - If the input is immutable, returns it as is.
     * - If the input is mutable, creates an immutable clone.
     *
     * This method is needed (in addition to {@link #toImmutable(Cloneable)}) because {@link Containerable}
     * is not {@link Freezable}.
     */
    @Contract("null -> null; !null -> !null")
    public static <C extends Containerable> C toImmutableContainerable(@Nullable C input) {
        if (input == null) {
            return null;
        }
        //noinspection unchecked
        PrismContainerValue<C> pcv = input.asPrismContainerValue();
        if (pcv.isImmutable()) {
            return input;
        } else {
            PrismContainerValue<C> pcvClone = pcv.clone();
            pcvClone.freeze();
            return pcvClone.asContainerable();
        }
    }

    /**
     * Returns a (mutable) list of input items after being processed by {@link #toImmutableContainerable(Containerable)} method.
     */
    @Contract("null -> null; !null -> !null")
    public static <C extends Containerable> List<C> toImmutableContainerablesList(@Nullable Collection<C> inputCollection) {
        if (inputCollection == null) {
            return null;
        }
        List<C> clonedCollection = new ArrayList<>(inputCollection.size());
        for (C element : inputCollection) {
            clonedCollection.add(toImmutableContainerable(element));
        }
        return clonedCollection;
    }
}
