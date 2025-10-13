/*
 * Copyright (C) 2010-2021 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.prism.impl.query;

import java.util.ArrayList;
import java.util.List;

import com.evolveum.midpoint.util.MiscUtil;

import com.google.common.collect.ImmutableList;

import com.evolveum.midpoint.prism.query.LogicalFilter;
import com.evolveum.midpoint.prism.query.ObjectFilter;
import com.evolveum.midpoint.prism.query.Visitor;
import com.evolveum.midpoint.util.DebugUtil;

import static com.evolveum.midpoint.util.MiscUtil.emptyIfNull;

public abstract class LogicalFilterImpl extends ObjectFilterImpl implements LogicalFilter {

    protected List<ObjectFilter> conditions;

    @Override
    public List<ObjectFilter> getConditions() {
        if (conditions == null) {
            conditions = new ArrayList<>();
        }
        return conditions;
    }

    @Override
    public void setConditions(List<ObjectFilter> condition) {
        checkMutable();
        this.conditions = condition;
    }

    @Override
    public void addCondition(ObjectFilter condition) {
        checkMutable();
        if (this.conditions == null) {
            conditions = new ArrayList<>();
        }
        this.conditions.add(condition);
    }

    @Override
    public boolean contains(ObjectFilter condition) {
        return this.conditions.contains(condition);
    }

    @Override
    protected void performFreeze() {
        conditions = ImmutableList.copyOf(getConditions());
        for (ObjectFilter objectFilter : conditions) {
            freeze(objectFilter);
        }
    }

    @Override
    abstract public LogicalFilter cloneEmpty();

    protected List<ObjectFilter> getClonedConditions() {
        if (conditions == null) {
            return null;
        }
        List<ObjectFilter> clonedConditions = new ArrayList<>(conditions.size());
        for (ObjectFilter condition : conditions) {
            clonedConditions.add(condition.clone());
        }
        return clonedConditions;
    }

    @Override
    public boolean isEmpty() {
        return conditions == null || conditions.isEmpty();
    }

    @Override
    public void checkConsistence(boolean requireDefinitions) {
        if (conditions == null) {
            throw new IllegalArgumentException("Null conditions in " + this);
        }
        if (conditions.isEmpty()) {
            throw new IllegalArgumentException("Empty conditions in " + this);
        }
        for (ObjectFilter condition : conditions) {
            if (condition == null) {
                throw new IllegalArgumentException("Null subfilter in " + this);
            }
            condition.checkConsistence(requireDefinitions);
        }
    }

    @Override
    public void accept(Visitor visitor) {
        super.accept(visitor);
        for (ObjectFilter condition : getConditions()) {
            condition.accept(visitor);
        }
    }

    // Just to make checkstyle happy
    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((conditions == null) ? 0 : conditions.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj, boolean exact) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        LogicalFilterImpl other = (LogicalFilterImpl) obj;
        var thisConditions = emptyIfNull(conditions);
        var otherConditions = emptyIfNull(other.conditions);
        if (thisConditions.size() != otherConditions.size()) {
            return false;
        }
        if (exact) {
            for (int i = 0; i < thisConditions.size(); i++) {
                ObjectFilter of1 = thisConditions.get(i);
                ObjectFilter of2 = otherConditions.get(i);
                if (!of1.equals(of2, true)) {
                    return false;
                }
            }
            return true;
        } else {
            return MiscUtil.unorderedCollectionEquals(
                    thisConditions, otherConditions,
                    (of1, of2) -> of1.equals(of2, false));
        }
    }

    @Override
    public String debugDump(int indent) {
        StringBuilder sb = new StringBuilder();
        DebugUtil.indentDebugDump(sb, indent);
        sb.append(getDebugDumpOperationName()).append(":");
        for (ObjectFilter filter : getConditions()) {
            sb.append("\n");
            sb.append(filter.debugDump(indent + 1));
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getDebugDumpOperationName());
        sb.append("(");
        for (int i = 0; i < getConditions().size(); i++) {
            sb.append(getConditions().get(i));
            if (i != getConditions().size() - 1) {
                sb.append("; "); // clearer separation than with ',' used in value filters
            }
        }
        sb.append(")");
        return sb.toString();
    }

    protected abstract String getDebugDumpOperationName();

    @Override
    public abstract LogicalFilterImpl clone();
}
