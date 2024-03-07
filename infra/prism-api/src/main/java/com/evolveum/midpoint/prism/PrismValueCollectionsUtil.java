/*
 * Copyright (c) 2010-2018 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism;

import com.evolveum.midpoint.prism.equivalence.EquivalenceStrategy;
import com.evolveum.midpoint.prism.equivalence.ParameterizedEquivalenceStrategy;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.prism.util.PrismUtil;
import com.evolveum.midpoint.util.EqualsChecker;
import com.evolveum.midpoint.util.MiscUtil;
import com.evolveum.midpoint.util.exception.SchemaException;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 *
 */
public class PrismValueCollectionsUtil {

    public static <T> Collection<T> getValues(Collection<PrismPropertyValue<T>> pvals) {
        Collection<T> realValues = new ArrayList<>(pvals.size());
        for (PrismPropertyValue<T> pval: pvals) {
            realValues.add(pval.getValue());
        }
        return realValues;
    }

    public static boolean containsRealValue(Collection<PrismPropertyValue<?>> collection, PrismPropertyValue<?> value) {
        for (PrismPropertyValue<?> colVal: collection) {
            if (value.equals(colVal, EquivalenceStrategy.REAL_VALUE)) {
                return true;
            }
        }
        return false;
    }

    public static <T> boolean containsValue(
            Collection<PrismPropertyValue<T>> collection,
            PrismPropertyValue<T> value,
            EqualsChecker<PrismPropertyValue<T>> checker) {
        for (PrismPropertyValue<T> colVal: collection) {
            if (checker.test(colVal, value)) {
                return true;
            }
        }
        return false;
    }

    public static <T> Collection<PrismPropertyValue<T>> createCollection(Collection<T> realValueCollection) {
        Collection<PrismPropertyValue<T>> pvalCol = new ArrayList<>(realValueCollection.size());
        for (T realValue: realValueCollection) {
            pvalCol.add(PrismContext.get().itemFactory().createPropertyValue(realValue));
        }
        return pvalCol;
    }

    public static <T> Collection<PrismPropertyValue<T>> createCollection(PrismContext prismContext, T[] realValueArray) {
        Collection<PrismPropertyValue<T>> pvalCol = new ArrayList<>(realValueArray.length);
        for (T realValue: realValueArray) {
            pvalCol.add(prismContext.itemFactory().createPropertyValue(realValue));
        }
        return pvalCol;
    }

    public static <T> Collection<PrismPropertyValue<T>> wrap(PrismContext prismContext, @NotNull Collection<T> realValues) {
        return realValues.stream()
                .map(val -> prismContext.itemFactory().createPropertyValue(val))
                .collect(Collectors.toList());
    }

    @SafeVarargs
    public static <T> PrismPropertyValue<T>[] wrap(T... realValues) {
        //noinspection unchecked
        return Arrays.stream(realValues)
                .map(val -> PrismContext.get().itemFactory().createPropertyValue(val))
                .toArray(PrismPropertyValue[]::new);
    }

    public static <T> @NotNull List<PrismPropertyValue<T>> wrap(@NotNull Collection<T> realValues) {
        return realValues.stream()
                .map(val -> PrismContext.get().itemFactory().createPropertyValue(val))
                .toList();
    }

    public static <T> @NotNull List<T> unwrap(@NotNull Collection<PrismPropertyValue<T>> values) {
        return values.stream()
                .map(val -> val.getRealValue())
                .toList();
    }

    @NotNull
    public static List<Referencable> asReferencables(@NotNull Collection<PrismReferenceValue> values) {
        return values.stream().map(prv -> prv.asReferencable()).collect(Collectors.toList());
    }

    @NotNull
    public static List<PrismReferenceValue> asReferenceValues(@NotNull Collection<? extends Referencable> referencables) {
        return referencables.stream().map(ref -> ref.asReferenceValue()).collect(Collectors.toList());
    }

    public static boolean containsOid(Collection<PrismReferenceValue> values, @NotNull String oid) {
        return values.stream().anyMatch(v -> oid.equals(v.getOid()));
    }

    public static <T> void clearParent(List<PrismPropertyValue<T>> values) {
        if (values == null) {
            return;
        }
        for (PrismPropertyValue<T> val: values) {
            val.clearParent();
        }
    }

    public static <V extends PrismValue> boolean containsRealValue(Collection<V> collection, V value) {
        return containsRealValue(collection, value, Function.identity());
    }


    public static <V extends PrismValue> boolean equalsRealValues(Collection<V> collection1, Collection<V> collection2) {
        return MiscUtil.unorderedCollectionEquals(collection1, collection2, (v1, v2) -> v1.equals(v2, EquivalenceStrategy.REAL_VALUE));
    }

    public static <V extends PrismValue> boolean containsAll(Collection<V> thisSet, Collection<V> otherSet, EquivalenceStrategy strategy) {
        if (thisSet == null && otherSet == null) {
            return true;
        }
        if (otherSet == null) {
            return true;
        }
        if (thisSet == null) {
            return false;
        }
        for (V otherValue: otherSet) {
            if (!contains(thisSet, otherValue, strategy)) {
                return false;
            }
        }
        return true;
    }

    @NotNull
    public static <T extends PrismValue> Collection<T> cloneCollection(Collection<T> values) {
        return cloneCollectionComplex(CloneStrategy.LITERAL, values);
    }

    /**
     * Sets all parents to null. This is good if the items are to be "transplanted" into a
     * different Containerable.
     */
    public static <T extends PrismValue> Collection<T> resetParentCollection(Collection<T> values) {
        for (T value: values) {
            value.setParent(null);
        }
        return values;
    }

    public static <T> Set<T> getRealValuesOfCollectionPreservingNull(Collection<? extends PrismValue> collection) {
        return collection != null ? getRealValuesOfCollection(collection) : null;
    }

    public static <T> Set<T> getRealValuesOfCollection(Collection<? extends PrismValue> collection) {
        if (collection != null) {
            Set<T> retval = new HashSet<>(collection.size());
            for (PrismValue value : collection) {
                retval.add(value.getRealValue());
            }
            return retval;
        } else {
            return Collections.emptySet();
        }
    }

    public static <X, V extends PrismValue> boolean containsRealValue(Collection<X> collection, V value,
            Function<X, V> valueExtractor) {
        if (collection == null) {
            return false;
        }

        for (X colVal: collection) {
            if (colVal == null) {
                return value == null;
            }

            if (valueExtractor.apply(colVal).equals(value, EquivalenceStrategy.REAL_VALUE)) {

                return true;
            }
        }
        return false;
    }

    public static <V extends PrismValue> boolean contains(Collection<V> thisSet, V otherValue, EquivalenceStrategy strategy) {
        for (V thisValue: thisSet) {
            if (thisValue.equals(otherValue, strategy)) {
                return true;
            }
        }
        return false;
    }

    public static <X extends PrismValue> Collection<X> cloneValues(Collection<X> values) {
        Collection<X> clonedCollection = new ArrayList<>(values.size());
        for (X val: values) {
            clonedCollection.add((X) val.clone());
        }
        return clonedCollection;
    }

    @NotNull
    public static <T extends PrismValue> Collection<T> cloneCollectionComplex(CloneStrategy strategy, Collection<T> values) {
        Collection<T> clones = new ArrayList<>();
        if (values != null) {
            for (T value : values) {
                clones.add((T) value.cloneComplex(strategy));
            }
        }
        return clones;
    }

    public static <V extends PrismValue> boolean collectionContainsEquivalentValue(Collection<V> collection, V value, ParameterizedEquivalenceStrategy equivalenceStrategy) {
        if (collection == null) {
            return false;
        }
        for (V collectionVal: collection) {
            if (collectionVal.equals(value, equivalenceStrategy)) {
                return true;
            }
        }
        return false;
    }

    @SafeVarargs
    public static <X> Collection<PrismPropertyValue<X>> toPrismPropertyValues(X... realValues) {
        Collection<PrismPropertyValue<X>> pValues = new ArrayList<>(realValues.length);
        for (X val: realValues) {
            PrismUtil.recomputeRealValue(val);
            pValues.add(
                    PrismContext.get().itemFactory().createPropertyValue(val));
        }
        return pValues;
    }

    @SuppressWarnings("unchecked")
    public static <O extends Objectable, C extends Containerable> Collection<PrismContainerValue<C>> toPrismContainerValues(
            Class<O> type, ItemPath path, C... containerValues) throws SchemaException {
        Collection<PrismContainerValue<C>> prismValues = new ArrayList<>(containerValues.length);
        for (C val : containerValues) {
            PrismContext.get().adopt(val, type, path);
            //noinspection unchecked
            prismValues.add(
                    val.asPrismContainerValue());
        }
        return prismValues;
    }

    // Does no recomputation nor adoption
    public static Collection<PrismValue> toPrismValues(Object... realValues) {
        Collection<PrismValue> pValues = new ArrayList<>(realValues.length);
        for (Object realValue : realValues) {
            if (realValue instanceof Containerable) {
                pValues.add(((Containerable) realValue).asPrismContainerValue());
            } else if (realValue instanceof Referencable) {
                pValues.add(((Referencable) realValue).asReferenceValue());
            } else if (realValue != null) {
                pValues.add(PrismContext.get().itemFactory().createPropertyValue(realValue));
            }
        }
        return pValues;
    }

    /**
     * Returns values present in `collection1` but not in `collection2`.
     * Values matching by ID are treated as equal without looking at their content.
     */
    public static Collection<? extends PrismValue> differenceConsideringIds(
            @NotNull Collection<? extends PrismValue> collection1,
            @NotNull Collection<? extends PrismValue> collection2,
            @NotNull EquivalenceStrategy strategy) {
        Collection<PrismValue> result = new HashSet<>();
        main: for (PrismValue value1 : collection1) {
            for (PrismValue value2 : collection2) {
                if (matchById(value1, value2) || value1.equals(value2, strategy)) {
                    continue main;
                }
            }
            result.add(value1);
        }
        return result;
    }

    /**
     * Returns values that exist (by ID) in both collections but differ in content.
     */
    public static Collection<? extends PrismValue> sameIdDifferentContent(
            @NotNull Collection<? extends PrismValue> collection1,
            @NotNull Collection<? extends PrismValue> collection2,
            @NotNull EquivalenceStrategy strategy) {
        Collection<PrismValue> result = new HashSet<>();
        main: for (PrismValue value1 : collection1) {
            for (PrismValue value2 : collection2) {
                if (matchById(value1, value2) && !value1.equals(value2, strategy)) {
                    result.add(value1);
                    continue main;
                }
            }
        }
        return result;
    }

    /**
     * Returns values present in `collection1` as well as in `collection2`.
     * PCV IDs are NOT considered! We are interested here in really matching values.
     */
    public static Collection<? extends PrismValue> intersection(
            @NotNull Collection<? extends PrismValue> collection1,
            @NotNull Collection<? extends PrismValue> collection2,
            @NotNull EquivalenceStrategy strategy) {
        Collection<PrismValue> result = new HashSet<>();
        main: for (PrismValue value1 : collection1) {
            for (PrismValue value2 : collection2) {
                if (value1.equals(value2, strategy)) {
                    result.add(value1);
                    continue main;
                }
            }
        }
        return result;
    }

    private static boolean matchById(PrismValue value1, PrismValue value2) {
        Long id1 = getId(value1);
        Long id2 = getId(value2);
        return id1 != null && id1.equals(id2);
    }

    private static Long getId(PrismValue value) {
        return value instanceof PrismContainerValue<?> ? ((PrismContainerValue<?>) value).getId() : null;
    }
}
