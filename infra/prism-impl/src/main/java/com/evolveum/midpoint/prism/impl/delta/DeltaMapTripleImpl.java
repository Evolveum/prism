/*
 * Copyright (c) 2010-2018 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.prism.impl.delta;

import com.evolveum.midpoint.prism.SimpleVisitor;
import com.evolveum.midpoint.prism.delta.DeltaMapTriple;
import com.evolveum.midpoint.prism.delta.PlusMinusZero;
import com.evolveum.midpoint.util.Cloner;
import com.evolveum.midpoint.util.DebugUtil;
import com.evolveum.midpoint.util.MiscUtil;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 *
 * @author Radovan Semancik
 */
public class DeltaMapTripleImpl<K,V> implements DeltaMapTriple<K,V> {

    /** Collection of values that were not changed. */
    @NotNull private final Map<K,V> zeroMap = new HashMap<>();

    /** Collection of values that were added. */
    @NotNull private final Map<K,V> plusMap = new HashMap<>();

    /** Collection of values that were deleted. */
    @NotNull private final Map<K,V> minusMap = new HashMap<>();

    private Map<K,V> createMap() {
        return new HashMap<>();
    }

    public @NotNull Map<K,V> getZeroMap() {
        return zeroMap;
    }

    public @NotNull Map<K,V> getPlusMap() {
        return plusMap;
    }

    public @NotNull Map<K,V> getMinusMap() {
        return minusMap;
    }

    public Map<K,V> getMap(PlusMinusZero plusMinusZero) {
        if (plusMinusZero == null) {
            return null;
        }
        switch (plusMinusZero) {
            case PLUS: return plusMap;
            case MINUS: return minusMap;
            case ZERO: return zeroMap;
        }
        throw new IllegalStateException("not reached");
    }

    public boolean hasPlusMap() {
        return !plusMap.isEmpty();
    }

    public boolean hasZeroMap() {
        return !zeroMap.isEmpty();
    }

    public boolean hasMinusMap() {
        return !minusMap.isEmpty();
    }

    public boolean isZeroOnly() {
        return hasZeroMap() && !hasPlusMap() && !hasMinusMap();
    }

    public void addToPlusMap(K key, V value) {
        addToMap(plusMap, key, value);
    }

    public void addToMinusMap(K key, V value) {
        addToMap(minusMap, key, value);
    }

    public void addToZeroMap(K key, V value) {
        addToMap(zeroMap, key, value);
    }

    public void addAllToPlusMap(Map<K,V> map) {
        addAllToMap(plusMap, map);
    }

    public void addAllToMinusMap(Map<K,V> map) {
        addAllToMap(minusMap, map);
    }

    public void addAllToZeroMap(Map<K,V> map) {
        addAllToMap(zeroMap, map);

    }

    public void addAllToMap(PlusMinusZero destination, Map<K,V> map) {
        if (destination == null) {
            // no-op
        } else if (destination == PlusMinusZero.PLUS) {
            addAllToMap(plusMap, map);
        } else if (destination == PlusMinusZero.MINUS) {
            addAllToMap(minusMap, map);
        } else if (destination == PlusMinusZero.ZERO) {
            addAllToMap(zeroMap, map);
        }
    }

    private void addAllToMap(Map<K,V> set, Map<K,V> items) {
        if (items == null) {
            return;
        }
        for (Entry<K, V> item: items.entrySet()) {
            addToMap(set, item.getKey(), item.getValue());
        }
    }

    private void addToMap(Map<K,V> set, K key, V value) {
        if (set == null) {
            set = createMap();
        }
        set.put(key, value);
    }

    public void clearPlusMap() {
        clearMap(plusMap);
    }

    public void clearMinusMap() {
        clearMap(minusMap);
    }

    public void clearZeroMap() {
        clearMap(zeroMap);
    }

    private void clearMap(Map<K,V> set) {
        if (set != null) {
            set.clear();
        }
    }

    public int size() {
        return sizeMap(zeroMap) + sizeMap(plusMap) + sizeMap(minusMap);
    }

    private int sizeMap(Map<K,V> set) {
        if (set == null) {
            return 0;
        }
        return set.size();
    }

    public void merge(DeltaMapTriple<K,V> triple) {
        addAllToZeroMap(triple.getZeroMap());
        addAllToPlusMap(triple.getPlusMap());
        addAllToMinusMap(triple.getMinusMap());
    }

    /**
     * Returns all values, regardless of the internal sets.
     */
    public Collection<K> unionKeySets() {
        return MiscUtil.union(zeroMap.keySet(), plusMap.keySet(), minusMap.keySet());
    }

    public DeltaMapTriple<K,V> clone(Cloner<Entry<K, V>> cloner) {
        DeltaMapTripleImpl<K,V> clone = new DeltaMapTripleImpl<>();
        copyValues(clone, cloner);
        return clone;
    }

    protected void copyValues(DeltaMapTripleImpl<K,V> clone, Cloner<Entry<K, V>> cloner) {
        cloneSet(clone.zeroMap, this.zeroMap, cloner);
        cloneSet(clone.plusMap, this.plusMap, cloner);
        cloneSet(clone.minusMap, this.minusMap, cloner);
    }

    private void cloneSet(
            @NotNull Map<K,V> cloneSet,
            @NotNull Map<K,V> origSet,
            @NotNull Cloner<Entry<K, V>> cloner) {
        for (Entry<K, V> origVal: origSet.entrySet()) {
            Entry<K, V> clonedVal = cloner.clone(origVal);
            cloneSet.put(clonedVal.getKey(), clonedVal.getValue());
        }
    }

    public boolean isEmpty() {
        return isEmpty(minusMap) && isEmpty(plusMap) && isEmpty(zeroMap);
    }

    private boolean isEmpty(Map<K,V> set) {
        if (set == null) {
            return true;
        }
        return set.isEmpty();
    }

    @Override
    public void simpleAccept(SimpleVisitor<Entry<K, V>> visitor) {
        acceptMap(visitor, zeroMap);
        acceptMap(visitor, plusMap);
        acceptMap(visitor, minusMap);
    }

    private void acceptMap(SimpleVisitor<Entry<K, V>> visitor, Map<K,V> set) {
        if (set == null) {
            return;
        }
        for (Entry<K, V> element: set.entrySet()) {
            visitor.visit(element);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(debugName()).append("(");
        dumpMap(sb, "zero", zeroMap);
        dumpMap(sb, "plus", plusMap);
        dumpMap(sb, "minus", minusMap);
        sb.append(")");
        return sb.toString();
    }

    private String debugName() {
        return "DeltaMapTriple";
    }

    private void dumpMap(StringBuilder sb, String label, Map<K,V> set) {
        sb.append(label).append(": ").append(set).append("; ");
    }

    @Override
    public String debugDump(int indent) {
        StringBuilder sb = new StringBuilder();
        DebugUtil.indentDebugDump(sb, indent);
        sb.append("DeltaSetTriple:\n");
        debugDumpMap(sb, "zero", zeroMap, indent + 1);
        sb.append("\n");
        debugDumpMap(sb, "plus", plusMap, indent + 1);
        sb.append("\n");
        debugDumpMap(sb, "minus", minusMap, indent + 1);
        return sb.toString();
    }

    private void debugDumpMap(StringBuilder sb, String label, Map<K,V> set, int indent) {
        DebugUtil.debugDumpLabel(sb, label, indent);
        sb.append("\n");
        DebugUtil.debugDumpMapMultiLine(sb, set, indent + 1);
    }
}
