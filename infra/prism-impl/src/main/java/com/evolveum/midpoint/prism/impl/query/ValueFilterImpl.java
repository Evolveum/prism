/*
 * Copyright (C) 2010-2023 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.prism.impl.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import javax.xml.namespace.QName;

import com.evolveum.midpoint.prism.path.TypedItemPath;

import com.evolveum.midpoint.prism.query.FilterItemPathTransformer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.match.MatchingRule;
import com.evolveum.midpoint.prism.match.MatchingRuleRegistry;
import com.evolveum.midpoint.prism.path.ItemName;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.prism.query.ValueFilter;
import com.evolveum.midpoint.util.DebugUtil;
import com.evolveum.midpoint.util.MiscUtil;
import com.evolveum.midpoint.util.QNameUtil;
import com.evolveum.midpoint.util.exception.SchemaException;

public abstract class ValueFilterImpl<V extends PrismValue, D extends ItemDefinition<?>>
        extends AbstractItemFilter implements ValueFilter<V, D> {

    private static final long serialVersionUID = 1L;

    /**
     * This is a definition of the item pointed to by "fullPath".
     * Not marked as @NotNull, because it can be filled-in after creation of the filter - e.g. in provisioning.
     */
    @Nullable private D definition;
    @Nullable private QName matchingRule;
    @Nullable private List<V> values;
    @Nullable private ExpressionWrapper expression;

    /** Alternative to values/expression; can be provided later. */
    @Nullable private ItemPath rightHandSidePath;

    /** Optional (needed only if path points to dynamically defined item). */
    @Nullable private ItemDefinition<?> rightHandSideDefinition;

    // At most one of values, expression, rightHandSidePath can be non-null.
    // It is a responsibility of the client to ensure it.

    protected ValueFilterImpl(@NotNull ItemPath fullPath, @Nullable D definition, @Nullable QName matchingRule,
            @Nullable List<V> values, @Nullable ExpressionWrapper expression,
            @Nullable ItemPath rightHandSidePath, @Nullable ItemDefinition<?> rightHandSideDefinition) {
        super(fullPath);
        this.definition = definition;
        this.matchingRule = matchingRule;
        this.expression = expression;
        this.values = values;
        this.rightHandSidePath = rightHandSidePath;
        this.rightHandSideDefinition = rightHandSideDefinition;
        if (values != null) {
            for (V value : values) {
                value.setParent(this);
                // Apply the definition in order to convert compatible types (e.g. String to PolyString).
                applyDefinition(value);
            }
        }
        checkConsistence(false);
    }

    @Override
    @NotNull
    public ItemPath getParentPath() {
        return fullPath.allExceptLast();
    }

    @Override
    @NotNull
    public ItemName getElementName() {
        if (definition != null) {
            // this is more precise, as the name in path can be unqualified
            return definition.getItemName();
        }
        if (fullPath.isEmpty()) {
            throw new IllegalStateException("Empty full path in filter " + this);
        }
        Object last = fullPath.last();
        if (ItemPath.isName(last)) {
            return ItemPath.toName(last);
        } else {
            throw new IllegalStateException("Got " + last + " as a last path segment in value filter " + this);
        }
    }

    @Override
    @Nullable
    public D getDefinition() {
        return definition;
    }

    @Override
    public void setDefinition(@Nullable D definition) {
        this.definition = definition;
        if (this.values != null) {
            // Apply the definition in order to convert compatible types (e.g. String to PolyString).
            for (V value : this.values) {
                applyDefinition(value);
            }
        }
        checkConsistence(false);
    }

    @Override
    public @Nullable QName getDeclaredMatchingRule() {
        return matchingRule;
    }

    @Override
    public void setMatchingRule(@Nullable QName matchingRule) {
        this.matchingRule = matchingRule;
    }

    @NotNull
    MatchingRule<Object> getMatchingRuleFromRegistry(MatchingRuleRegistry matchingRuleRegistry) {
        try {
            QName typeName = definition != null ? definition.getTypeName() : null;
            return matchingRuleRegistry.getMatchingRule(matchingRule, typeName);
        } catch (SchemaException ex) {
            throw new IllegalArgumentException(ex.getMessage(), ex);
        }
    }

    @Override
    @Nullable
    public List<V> getValues() {
        return values;
    }

    @Nullable
    List<V> getClonedValues() {
        if (values == null) {
            return null;
        } else {
            List<V> clonedValues = new ArrayList<>(values.size());
            for (V value : values) {
                @SuppressWarnings("unchecked")
                V cloned = (V) value.clone();
                clonedValues.add(cloned);
            }
            return clonedValues;
        }
    }

    @Nullable
    V getClonedValue() {
        V value = getSingleValue();
        if (value == null) {
            return null;
        } else {
            @SuppressWarnings("unchecked")
            V cloned = (V) value.clone();
            return cloned;
        }
    }

    @Override
    @Nullable
    public V getSingleValue() {
        if (values == null || values.isEmpty()) {
            return null;
        } else if (values.size() > 1) {
            throw new IllegalArgumentException("Filter '" + this + "' should contain at most one value, but it has " + values.size() + " of them.");
        } else {
            return values.iterator().next();
        }
    }

    /**
     * @param value value, has to be parent-less
     */
    @Override
    public void setValue(V value) {
        checkMutable();
        this.values = new ArrayList<>();
        if (value != null) {
            value.setParent(this);
            // Apply the definition in order to convert compatible types (e.g. String to PolyString).
            applyDefinition(value);
            values.add(value);
        }
    }

    @Override
    public void setValues(@NotNull Collection<V> values) {
        checkMutable();
        this.values = new ArrayList<>();
        for (V value : values) {
            value.setParent(this);
            // Apply the definition in order to convert compatible types (e.g. String to PolyString).
            applyDefinition(value);
            this.values.add(value);
        }
    }

    @Override
    @Nullable
    public ExpressionWrapper getExpression() {
        return expression;
    }

    @Override
    public void setExpression(@Nullable ExpressionWrapper expression) {
        checkMutable();
        this.expression = expression;
    }

    @Override
    @Nullable
    public ItemPath getRightHandSidePath() {
        return rightHandSidePath;
    }

    @Override
    public void setRightHandSidePath(@Nullable ItemPath rightHandSidePath) {
        checkMutable();
        this.rightHandSidePath = rightHandSidePath;
    }

    @Override
    @Nullable
    public ItemDefinition<?> getRightHandSideDefinition() {
        return rightHandSideDefinition;
    }

    @Override
    public void setRightHandSideDefinition(@Nullable ItemDefinition<?> rightHandSideDefinition) {
        this.rightHandSideDefinition = rightHandSideDefinition;
    }

    @Override
    public ItemPath getPath() {
        return getFullPath();
    }

    @Override
    public boolean isRaw() {
        if (values != null) {
            for (V value : values) {
                if (value.isRaw()) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public abstract boolean match(PrismContainerValue<?> cvalue, MatchingRuleRegistry matchingRuleRegistry) throws SchemaException;

    @NotNull
    Collection<PrismValue> getObjectItemValues(PrismContainerValue<?> value) {
        return value.getAllValues(fullPath);
    }

    @Override
    public abstract ValueFilterImpl<V, D> clone();

    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    @Override
    public boolean equals(Object o) {
        return equals(o, true);
    }

    @Override
    public boolean equals(Object o, boolean exact) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ValueFilterImpl<?, ?> that = (ValueFilterImpl<?, ?>) o;
        return fullPath.equals(that.fullPath, exact) &&
                (!exact || Objects.equals(definition, that.definition)) &&
                Objects.equals(matchingRule, that.matchingRule) &&
                MiscUtil.nullableCollectionsEqual(values, that.values) &&
                Objects.equals(expression, that.expression) &&
                (rightHandSidePath == null && that.rightHandSidePath == null ||
                        rightHandSidePath != null && rightHandSidePath.equals(that.rightHandSidePath, exact)) &&
                (!exact || Objects.equals(rightHandSideDefinition, that.rightHandSideDefinition));
    }

    @Override
    public int hashCode() {
        return Objects.hash(fullPath, matchingRule, values, expression, rightHandSidePath);
    }

    @Override
    public String debugDump(int indent) {
        StringBuilder sb = new StringBuilder();
        DebugUtil.debugDumpLabel(sb, getFilterName(), indent);
        debugDump(indent, sb);
        return sb.toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getFilterName()).append(": ");
        return toString(sb);
    }

    protected abstract String getFilterName();

    protected void debugDump(int indent, StringBuilder sb) {
        sb.append("\n");
        DebugUtil.indentDebugDump(sb, indent + 1);
        sb.append("PATH: ");
        sb.append(getFullPath());

        sb.append("\n");
        DebugUtil.indentDebugDump(sb, indent + 1);
        sb.append("DEF: ");
        if (getDefinition() != null) {
            sb.append(getDefinition().toString());
        } else {
            sb.append("null");
        }

        List<V> values = getValues();
        if (values != null) {
            sb.append("\n");
            DebugUtil.indentDebugDump(sb, indent + 1);
            sb.append("VALUE:");
            for (PrismValue val : getValues()) {
                sb.append("\n");
                sb.append(DebugUtil.debugDump(val, indent + 2));
            }
        }

        ExpressionWrapper expression = getExpression();
        if (expression != null && expression.getExpression() != null) {
            sb.append("\n");
            DebugUtil.indentDebugDump(sb, indent + 1);
            sb.append("EXPRESSION:");
            sb.append("\n");
            sb.append(DebugUtil.debugDump(expression.getExpression(), indent + 2));
        }

        if (getRightHandSidePath() != null) {
            sb.append("\n");
            DebugUtil.indentDebugDump(sb, indent + 1);
            sb.append("RIGHT SIDE PATH: ");
            sb.append(getFullPath());
            sb.append("\n");
            DebugUtil.indentDebugDump(sb, indent + 1);
            sb.append("RIGHT SIDE DEF: ");
            if (getRightHandSideDefinition() != null) {
                sb.append(getRightHandSideDefinition().toString());
            } else {
                sb.append("null");
            }
        }

        QName matchingRule = getMatchingRule();
        if (matchingRule != null) {
            sb.append("\n");
            DebugUtil.indentDebugDump(sb, indent + 1);
            sb.append("MATCHING: ");
            sb.append(matchingRule);
        }
    }

    protected String toString(StringBuilder sb) {
        sb.append(getFullPath());
        sb.append(", ");
        if (getValues() != null) {
            for (int i = 0; i < getValues().size(); i++) {
                PrismValue value = getValues().get(i);
                if (value == null) {
                    sb.append("null");
                } else {
                    sb.append(value);
                }
                if (i != getValues().size() - 1) {
                    sb.append(",");
                }
            }
        }
        if (getRightHandSidePath() != null) {
            sb.append(getRightHandSidePath());
        }
        return sb.toString();
    }

    @Override
    protected void performFreeze() {
        values = freezeNullableList(values);
        // Values can be null if there is an expression that was not executed yet.
        if (values != null) {
            freezeAll(values);
        }
        freeze(definition);
        freeze(rightHandSideDefinition);
        freeze(expression);
    }

    @Override
    public void checkConsistence(boolean requireDefinitions) {
        if (requireDefinitions && definition == null) {
            throw new IllegalArgumentException("Null definition in " + this);
        }
        Object last = fullPath.last();
        // Empty path indicates self (.) path, we want to skip the next tests for that.
        if (!fullPath.isEmpty()) {
            if (!ItemPath.isName(last) && !ItemPath.isIdentifier(last)) {
                throw new IllegalArgumentException("Last segment of item path is not a name or identifier segment: " + fullPath + " (it is " + last + ")");
            }
            if (rightHandSidePath != null && rightHandSidePath.isEmpty()) {
                throw new IllegalArgumentException("Not-null but empty right side path in " + this);
            }
        }
        int count = 0;
        if (values != null) {
            count++;
        }
        if (expression != null) {
            count++;
        }
        if (rightHandSidePath != null) {
            count++;
        }
        if (count > 1) {
            throw new IllegalStateException("Two or more of the following are non-null: values (" + values
                    + "), expression (" + expression + "), rightHandSidePath (" + rightHandSidePath + ") in " + this);
        }
        if (values != null) {
            for (V value : values) {
                if (value == null) {
                    throw new IllegalArgumentException("Null value in " + this);
                }
                if (value.getParent() != this) {
                    throw new IllegalArgumentException("Value " + value + " in " + this + " has a bad parent " + value.getParent());
                }
                if (value.isEmpty() && !value.isRaw()) {
                    throw new IllegalArgumentException("Empty value in " + this);
                }
            }
        }
        if (definition != null && ItemPath.isName(last)) {
            ItemName itemName = ItemPath.toName(last);
            if (!QNameUtil.match(definition.getItemName(), itemName)) {
                throw new IllegalArgumentException("Last segment of item path (" + fullPath.lastName() + ") "
                        + "does not match item name from the definition: " + definition);
            }
            // todo check consistence for ID-based filters
        }
    }

    @Override
    public boolean canNestInsideExists(ItemPath existsPath) {
        if (!fullPath.startsWith(existsPath)) {
            return false;
        }
        if (getRightHandSidePath() != null) {
            return false;
        }
        if (getExpression() != null) {
            return false;
        }
        return true;
    }

    @Override
    public void collectUsedPaths(TypedItemPath base, Consumer<TypedItemPath> pathConsumer, boolean expandReferences) {
        base.append(fullPath).emitTo(pathConsumer, expandReferences);
        // UserType/archetypeRef a ArchetypeType/name
        if (getRightHandSidePath() != null) {
            base.append(getRightHandSidePath()).emitTo(pathConsumer, expandReferences);
        }
    }

    private void applyDefinition(V value) {
        if (this.definition != null) {
            try {
                value.applyDefinition(this.definition);
            } catch (SchemaException e) {
                // We don't want to wrap it into some runtime exception, because of scripts which may use this filter
                // in various unknown ways
            }
        }
    }
}
