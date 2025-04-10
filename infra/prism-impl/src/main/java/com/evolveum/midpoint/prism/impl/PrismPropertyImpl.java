/*
 * Copyright (C) 2010-2022 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.prism.impl;

import java.util.*;
import javax.xml.namespace.QName;

import com.evolveum.midpoint.prism.lazy.FlyweightClonedItem;

import org.jetbrains.annotations.NotNull;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.delta.ItemDelta;
import com.evolveum.midpoint.prism.delta.PropertyDelta;
import com.evolveum.midpoint.prism.equivalence.EquivalenceStrategy;
import com.evolveum.midpoint.prism.equivalence.ParameterizedEquivalenceStrategy;
import com.evolveum.midpoint.prism.impl.delta.PropertyDeltaImpl;
import com.evolveum.midpoint.prism.impl.xnode.PrimitiveXNodeImpl;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.prism.util.PrismPrettyPrinter;
import com.evolveum.midpoint.util.DebugDumpable;
import com.evolveum.midpoint.util.DebugUtil;
import com.evolveum.midpoint.util.PrettyPrinter;
import com.evolveum.midpoint.util.ShortDumpable;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;

/**
 * Property is a specific characteristic of an object. It may be considered
 * object "attribute" or "field". For example User has fullName property that
 * contains string value of user's full name.
 * <p>
 * Properties may be single-valued or multi-valued
 * <p>
 * Properties may contain primitive types or complex types (defined by XSD
 * schema)
 * <p>
 * Property values are unordered, implementation may change the order of values
 * <p>
 * Duplicate values of properties should be silently removed by implementations,
 * but clients must be able tolerate presence of duplicate values.
 * <p>
 * Operations that modify the objects work with the granularity of properties.
 * They add/remove/replace the values of properties, but do not "see" inside the
 * property.
 * <p>
 * Property is mutable.
 *
 * @author Radovan Semancik
 */
public class PrismPropertyImpl<T> extends ItemImpl<PrismPropertyValue<T>, PrismPropertyDefinition<T>>
        implements PrismProperty<T> {

    private static final long serialVersionUID = 6843901365945935660L;

    private static final Trace LOGGER = TraceManager.getTrace(PrismPropertyImpl.class);

    private static final int MAX_SINGLELINE_LEN = 40;

    // Start with indexing enabled
    private boolean indexAvailable = true;

    private transient HashSet<PrismPropertyValue<T>> valueIndex = null;

    public PrismPropertyImpl(QName name) {
        super(name);
    }

    public PrismPropertyImpl(QName name, PrismPropertyDefinition<T> definition) {
        super(name, definition);
    }

    /**
     * Returns applicable property definition.
     * <p>
     * May return null if no definition is applicable or the definition is not
     * know.
     *
     * @return applicable property definition
     */
    @Override
    public PrismPropertyDefinition<T> getDefinition() {
        return definition;
    }

    /**
     * Type override, also for compatibility.
     */
    @Override
    public <X> List<PrismPropertyValue<X>> getValues(Class<X> type) {
        return (List) getValues();
    }

    @Override
    public @NotNull Collection<T> getRealValues() {
        Collection<T> realValues = new ArrayList<>(getValues().size());
        for (PrismPropertyValue<T> pValue : getValues()) {
            realValues.add(pValue.getValue());
        }
        return realValues;
    }

    /**
     * Type override, also for compatibility.
     */

    @Override
    public <X> @NotNull Collection<X> getRealValues(Class<X> type) {
        Collection<X> realValues = new ArrayList<>(getValues().size());
        for (PrismPropertyValue<T> pValue : getValues()) {
            realValues.add((X) pValue.getValue());
        }
        return realValues;
    }

    @Override
    public T getAnyRealValue() {
        Collection<T> values = getRealValues();
        if (values.isEmpty()) {
            return null;
        }
        return values.iterator().next();
    }

    @Override
    public T getRealValue() {
        PrismPropertyValue<T> value = getValue();
        return value != null ? value.getRealValue() : null;
    }

    /**
     * Type override, also for compatibility.
     */
    @Override
    public <X> PrismPropertyValue<X> getValue(Class<X> type) {
        if (getDefinition() != null) {
            if (getDefinition().isMultiValue()) {
                throw new IllegalStateException("Attempt to get single value from property " + elementName
                        + " with multiple values");
            }
        }
        if (getValues().size() > 1) {
            throw new IllegalStateException("Attempt to get single value from property " + elementName
                    + " with multiple values");
        }
        if (getValues().isEmpty()) {
            return null;
        }
        PrismPropertyValue<X> o = (PrismPropertyValue<X>) getValues().iterator().next();
        return o;
    }

    /**
     * Means as a short-hand for setting just a value for single-valued attributes.
     * Will remove all existing values.
     */
    @Override
    public void setValue(PrismPropertyValue<T> value) {
        clear();
        addValue(value);
    }

    @Override
    public void setRealValue(T realValue) {
        if (realValue == null) {
            // Just make sure there are no values
            clear();
        } else {
            setValue(new PrismPropertyValueImpl<>(realValue));
        }
    }

    @Override
    public void setRealValues(T... realValues) {
        clear();
        if (realValues == null || realValues.length == 0) {
            // nothing to do, already cleared
        } else {
            for (T realValue : realValues) {
                addValue(new PrismPropertyValueImpl<>(realValue));
            }
        }
    }

    @Override
    public void addValues(Collection<PrismPropertyValue<T>> pValuesToAdd) {
        if (pValuesToAdd == null) {
            return;
        }
        for (PrismPropertyValue<T> pValue : pValuesToAdd) {
            addValue(pValue);
        }
    }

    @Override
    public boolean add(@NotNull PrismPropertyValue<T> newValue) throws SchemaException {
        return super.add(newValue);
    }

    @Override
    public void addValue(PrismPropertyValue<T> pValueToAdd) {
        addValue(pValueToAdd, true);
    }

    public void addValue(@NotNull PrismPropertyValue<T> pValueToAdd, boolean checkUniqueness) {
        checkMutable();
        resetIncompleteFlagOnAddIfPossible();
        ((PrismPropertyValueImpl<T>) pValueToAdd).checkValue();
        enableOrDisableIndex(pValueToAdd, true);
        if (checkUniqueness && shouldIterateList(pValueToAdd)) {
            Iterator<PrismPropertyValue<T>> iterator = getValues().iterator();
            while (iterator.hasNext()) {
                PrismPropertyValue<T> pValue = iterator.next();
                if (pValue.equals(pValueToAdd, EquivalenceStrategy.REAL_VALUE)) {
                    LOGGER.warn("Adding value to property {} that already exists (overwriting), value: {}",
                            getElementName(), pValueToAdd);
                    iterator.remove();
                }
            }
        }
        pValueToAdd.setParent(this);
        pValueToAdd.recompute();
        addInternalExecution(pValueToAdd);
    }

    @Override
    protected boolean addInternalExecution(@NotNull PrismPropertyValue<T> newValue) {
        addToIndex(newValue, true);
        return super.addInternalExecution(newValue);
    }

    @Override
    public void addRealValue(T valueToAdd) {
        addValue(new PrismPropertyValueImpl<>(valueToAdd));
    }

    @Override
    public void addRealValueSkipUniquenessCheck(T valueToAdd) {
        addValue(new PrismPropertyValueImpl<>(valueToAdd), false);
    }

    @Override
    public boolean deleteValues(Collection<PrismPropertyValue<T>> pValuesToDelete) {
        checkMutable();
        boolean changed = false;
        for (PrismPropertyValue<T> pValue : pValuesToDelete) {
            if (!changed) {
                changed = deleteValue(pValue);
            } else {
                deleteValue(pValue);
            }
        }
        return changed;
    }

    @Override
    public boolean deleteValue(PrismPropertyValue<T> pValueToDelete) {
        checkMutable();
        Iterator<PrismPropertyValue<T>> iterator = getValues().iterator();
        boolean found = false;
        if (shouldIterateList(pValueToDelete)) {
            removeFromIndex(pValueToDelete);
            while (iterator.hasNext()) {
                PrismPropertyValue<T> pValue = iterator.next();
                if (pValue.equals(pValueToDelete, EquivalenceStrategy.REAL_VALUE)) {
                    iterator.remove();
                    pValue.setParent(null);

                    found = true;
                }
            }
        }
        if (!found) {
            LOGGER.warn("Deleting value of property " + getElementName()
                    + " that does not exist (skipping), value: " + pValueToDelete);
        }

        return found;
    }

    @Override
    public void replaceValues(Collection<PrismPropertyValue<T>> valuesToReplace) {
        clear();
        addValues(valuesToReplace);
    }

    @Override
    public boolean hasRealValue(PrismPropertyValue<T> value) {
        for (PrismPropertyValue<T> propVal : getValues()) {
            if (propVal.equals(value, EquivalenceStrategy.REAL_VALUE)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public Class<T> getValueClass() {
        if (getDefinition() != null) {
            return getDefinition().getTypeClass();
        }
        if (!getValues().isEmpty()) {
            PrismPropertyValue<T> firstPVal = getValues().get(0);
            if (firstPVal != null) {
                T firstVal = firstPVal.getValue();
                if (firstVal != null) {
                    return (Class<T>) firstVal.getClass();
                }
            }
        }
        // TODO: How to determine value class?????
        return PrismConstants.DEFAULT_VALUE_CLASS;
    }

    @Override
    public PropertyDelta<T> createDelta() {
        return new PropertyDeltaImpl<>(getPath(), getDefinition());
    }

    @Override
    public PropertyDelta<T> createDelta(ItemPath path) {
        return new PropertyDeltaImpl<>(path, getDefinition());
    }

    @Override
    public Object find(ItemPath path) {
        if (path == null || path.isEmpty()) {
            return this;
        }
        if (!isSingleValue()) {
            throw new IllegalStateException("Attempt to resolve sub-path '"
                    + path + "' on multi-value property " + getElementName());
        }
        PrismPropertyValue<T> value = getValue();
        return value.find(path);
    }

    @Override
    public <IV extends PrismValue, ID extends ItemDefinition<?>> PartiallyResolvedItem<IV, ID> findPartial(ItemPath path) {
        if (path == null || path.isEmpty()) {
            return new PartiallyResolvedItem<>((Item<IV, ID>) this, null);
        }
        for (PrismPropertyValue<T> pvalue : getValues()) {
            T value = pvalue.getValue();
            if (!(value instanceof Structured)) {
                throw new IllegalArgumentException("Attempt to resolve sub-path '"
                        + path + "' on non-structured property value " + pvalue);
            }
        }
        return new PartiallyResolvedItem<>((Item<IV, ID>) this, path);
    }

    @Override
    public PropertyDelta<T> diff(PrismProperty<T> other) {
        return (PropertyDelta<T>) super.diff(other);
    }

    @Override
    public PropertyDelta<T> diff(PrismProperty<T> other, ParameterizedEquivalenceStrategy strategy) {
        return (PropertyDelta<T>) super.diff(other, strategy);
    }

    @Override
    public PrismProperty<T> clone() {
        return cloneComplex(CloneStrategy.LITERAL_MUTABLE);
    }

    @Override
    public PrismProperty<T> createImmutableClone() {
        return (PrismProperty<T>) super.createImmutableClone();
    }

    @Override
    public @NotNull PrismProperty<T> cloneComplex(@NotNull CloneStrategy strategy) {
        if (isImmutable() && !strategy.mutableCopy()) {
            return FlyweightClonedItem.from(this);
        }

        PrismPropertyImpl<T> clone = new PrismPropertyImpl<>(getElementName(), getDefinition());
        copyValues(strategy, clone);
        return clone;
    }

    protected void copyValues(@NotNull CloneStrategy strategy, PrismPropertyImpl<T> clone) {
        super.copyValues(strategy, clone);
        for (PrismPropertyValue<T> value : getValues()) {
            clone.addValue(value.cloneComplex(strategy), false);
        }
    }

    @Override
    protected ItemDelta<PrismPropertyValue<T>, PrismPropertyDefinition<T>> fixupDelta(
            ItemDelta<PrismPropertyValue<T>, PrismPropertyDefinition<T>> delta,
            Item<PrismPropertyValue<T>, PrismPropertyDefinition<T>> otherItem) {
        PrismPropertyDefinition<?> def = getDefinition();
        if (def != null && def.isSingleValue() && !delta.isEmpty()) {
            // Drop the current delta (it was used only to detect that something has changed
            // Generate replace delta instead of add/delete delta
            PrismProperty<T> other = (PrismProperty<T>) otherItem;
            PropertyDelta<T> propertyDelta = (PropertyDelta<T>) delta;
            delta.clear();
            Collection<PrismPropertyValue<T>> replaceValues = new ArrayList<>(other.getValues().size());
            for (PrismPropertyValue<T> value : other.getValues()) {
                replaceValues.add(value.clone());
            }
            propertyDelta.setValuesToReplace(replaceValues);
            return propertyDelta;
        } else {
            return super.fixupDelta(delta, otherItem);
        }
    }

    @Override
    public String toString() {
        return getDebugDumpClassName() + "(" + PrettyPrinter.prettyPrint(getElementName()) + "):" + getValues();
    }

    @Override
    public String debugDump(int indent) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < indent; i++) {
            sb.append(INDENT_STRING);
        }
        if (DebugUtil.isDetailedDebugDump()) {
            sb.append(getDebugDumpClassName()).append(": ");
        }
        sb.append(DebugUtil.formatElementName(getElementName())).append(": ");

        boolean isMultivalue = true;
        PrismPropertyDefinition<T> def = getDefinition();
        if (def != null) {
            isMultivalue = def.isMultiValue();
        }

        List<PrismPropertyValue<T>> values = getValues();
        if (values.isEmpty()) {
            sb.append("[]");

        } else {
            String dump = null;
            boolean multiline = false;
            PrismPropertyValue<T> firstVal = values.iterator().next();

            if (firstVal != null && !firstVal.isRaw() && firstVal.getValue() != null) {
                if (DebugUtil.isDetailedDebugDump() && firstVal.getValue() instanceof DebugDumpable) {
                    multiline = true;
                } else if ((firstVal.getValue() instanceof ShortDumpable)) {
                    multiline = false;
                } else if (firstVal.getValue() instanceof DebugDumpable) {
                    dump = PrettyPrinter.debugDump(firstVal.getValue(), indent + 1);
                    if (dump.length() > MAX_SINGLELINE_LEN || dump.contains("\n")) {
                        multiline = true;
                    }
                }
            }

            if (multiline) {
                for (PrismPropertyValue<T> value : getValues()) {
                    sb.append("\n");
                    appendMetadata(sb, value, "", " ");
                    if (value.isRaw()) {
                        sb.append(formatRawValueForDump(value.getRawElement()));
                        sb.append(" (raw)");
                    } else if (value.getExpression() != null) {
                        sb.append(" (expression)");
                    } else {
                        if (dump != null) {
                            sb.append(dump);
                            dump = null;
                        } else {
                            T realValue = value.getValue();

                            if (DebugUtil.isDetailedDebugDump() && realValue instanceof DebugDumpable) {
                                // Override in case that the value is both DebugDumpable and ShortDumpable
                                // In that case we want to force debugDump as we are in detailedDebugMode here.
                                // This is important e.g. for PolyString, in detailedDebugMode we want to see
                                // all the PolyString details.
                                sb.append(((DebugDumpable) realValue).debugDump(indent + 1));
                            } else {
                                if (realValue instanceof ShortDumpable) {
                                    DebugUtil.indentDebugDump(sb, indent + 1);
                                    ((ShortDumpable) realValue).shortDump(sb);
                                } else if (realValue instanceof DebugDumpable) {
                                    sb.append(((DebugDumpable) realValue).debugDump(indent + 1));
                                } else {
                                    if (DebugUtil.isDetailedDebugDump()) {
                                        PrismPrettyPrinter.debugDumpValue(sb, indent + 1, realValue, getElementName(), null);
                                    } else {
                                        sb.append("SS{").append(realValue).append("}");
                                        PrettyPrinter.shortDump(sb, realValue);
                                    }
                                }
                            }
                        }
                    }
                }

            } else {
                if (isMultivalue) {
                    sb.append("[ ");
                }
                Iterator<PrismPropertyValue<T>> iterator = getValues().iterator();
                while (iterator.hasNext()) {
                    PrismPropertyValue<T> value = iterator.next();
                    if (value.isRaw()) {
                        sb.append(formatRawValueForDump(value.getRawElement()));
                        sb.append(" (raw)");
                    } else if (value.getExpression() != null) {
                        sb.append(" (expression)");
                    } else {
                        T realValue = value.getValue();
                        if (DebugUtil.isDetailedDebugDump() ||
                                !(realValue instanceof ShortDumpable) && DebugUtil.getPrettyPrintBeansAs() != null) {
                            PrismPrettyPrinter.debugDumpValue(sb, indent + 1, realValue, getElementName(), null);
                        } else {
                            PrettyPrinter.shortDump(sb, realValue);
                        }
                        appendMetadata(sb, value, " ", "");
                    }
                    if (iterator.hasNext()) {
                        sb.append(", ");
                    }
                }
                if (isMultivalue) {
                    sb.append(" ]");
                }
            }
        }

        appendDebugDumpSuffix(sb);

        if (def != null && DebugUtil.isDetailedDebugDump()) {
            sb.append(" def(");
            def.debugDumpShortToString(sb);
//            if (def.isIndexed() != null) {
//                sb.append(def.isIndexed() ? ",i+" : ",i-");
//            }
            sb.append(")");
        }
        return sb.toString();
    }

    private void appendMetadata(StringBuilder sb, PrismPropertyValue<T> value, String before, String after) {
        ValueMetadata metadata = value.getValueMetadata();
        if (!metadata.isEmpty()) {
            sb.append(before).append("[meta: ")
                    .append(metadata.shortDump())
                    .append("]").append(after);
        }
    }

    private String formatRawValueForDump(Object rawElement) {
        if (rawElement == null) {
            return null;
        }
        if (rawElement instanceof PrimitiveXNodeImpl<?>) {
            return ((PrimitiveXNodeImpl<?>) rawElement).getStringValue();
        } else {
            return "<class " + rawElement.getClass().getSimpleName() + ">";
        }
    }

    @Override
    public String toHumanReadableString() {
        StringBuilder sb = new StringBuilder();
        sb.append(PrettyPrinter.prettyPrint(getElementName())).append(" = ");
        if (getValues() == null) {
            sb.append("null");
        } else {
            sb.append("[ ");
            Iterator<PrismPropertyValue<T>> iterator = getValues().iterator();
            while (iterator.hasNext()) {
                PrismPropertyValue<T> value = iterator.next();
                sb.append(value.toHumanReadableString());
                if (iterator.hasNext()) {
                    sb.append(", ");
                }
            }
            sb.append(" ]");
        }
        return sb.toString();
    }

    /**
     * Return a human-readable name of this class suitable for logs.
     */
    @Override
    protected String getDebugDumpClassName() {
        return "PP";
    }

    // Value Hash index

    /**
     * If added value is raw or expression, value hash code index will be disabled.
     *
     * Index is only enabled when:
     * - property never contained any raw value or expression
     * - property has more than {@link PrismStaticConfiguration#indexEnableThreshold()} items
     *
     * Index is disabled when:
     * - raw value is inserted
     */
    private void enableOrDisableIndex(PrismPropertyValue<T> valueToAdd, boolean createIndexIfNecessary) {
        if (valueToAdd.isRaw() || valueToAdd.getExpression() != null) {
            // Index is not usable anymore, since value may change its hashCode during lifetime
            this.indexAvailable = false;
            this.valueIndex = null;
            return;
        }
        if (isSingleValueByDefinition()) {
            return;
        }
        if (valueIndex == null && indexAvailable && createIndexIfNecessary
                && PrismStaticConfiguration.indexEnableThreshold() <= size()) {
            // Lazily initialize index on N-th value
            this.valueIndex = new HashSet<>();
            for (PrismPropertyValue<T> value : getValues()) {
                addToIndex(value, false);
            }
        }
    }

    private boolean shouldIterateList(PrismPropertyValue<T> valueToAdd) {
        if (this.valueIndex != null) {
            return this.valueIndex.contains(valueToAdd);
        }
        return true;
    }

    /**
     * Notifies that value is going to be changed, this removes
     * value from index, so later it can be reinserted with new hashCode.
     *
     * This call needs to be followed with {@link #valueChangeEnd(PrismPropertyValue)} in success case
     * or {@link #valueChangeFailed(PrismPropertyValue)} in case of any error.
     */
    void valueChangeStart(PrismPropertyValue<T> propertyValue) {
        removeFromIndex(propertyValue);
    }

    private void addToIndex(PrismPropertyValue<T> value, boolean createIndex) {
        enableOrDisableIndex(value, createIndex);
        if (this.valueIndex != null) {
            valueIndex.add(value);
        }
    }

    private void removeFromIndex(PrismPropertyValue<T> value) {
        if (this.valueIndex != null) {
            valueIndex.remove(value);
        }
    }

    void valueChangeEnd(PrismPropertyValue<T> value) {
        addToIndex(value, true);
    }

    void valueChangeFailed(PrismPropertyValue<T> value) {
        // PPV.setValue failed (value is set to something incorrect
        // index is unusable from now
        this.indexAvailable = false;
        this.valueIndex = null;
    }

    @Override
    public void addForced(@NotNull PrismPropertyValue<T> newValue) {
        enableOrDisableIndex(newValue, true);
        super.addForced(newValue);
        addToIndex(newValue, false);
    }

    @Override
    protected void valueRemoved(PrismPropertyValue<T> currentValue) {
        super.valueRemoved(currentValue);
        removeFromIndex(currentValue);
    }

    @Override
    public void clear() {
        super.clear();
        if (this.valueIndex != null) {
            this.valueIndex.clear();
        }
    }
}
