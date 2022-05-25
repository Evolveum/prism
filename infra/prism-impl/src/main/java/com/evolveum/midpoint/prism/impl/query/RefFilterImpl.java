/*
 * Copyright (C) 2010-2021 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.prism.impl.query;

import static com.evolveum.midpoint.util.MiscUtil.emptyIfNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import javax.xml.namespace.QName;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.match.MatchingRuleRegistry;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.prism.polystring.PolyString;
import com.evolveum.midpoint.prism.query.ObjectFilter;
import com.evolveum.midpoint.prism.query.RefFilter;
import com.evolveum.midpoint.util.DebugUtil;
import com.evolveum.midpoint.util.QNameUtil;
import com.evolveum.midpoint.util.exception.SchemaException;

public class RefFilterImpl extends ValueFilterImpl<PrismReferenceValue, PrismReferenceDefinition> implements RefFilter {
    private static final long serialVersionUID = 1L;

    /**
     * By default null OID means to match any value (no additional condition).
     * Value {@code false} means to match only refs where OID is {@code null}.
     * False is ignored by legacy SQL repo, but supported by Sqale repo.
     */
    private boolean oidNullAsAny = true;

    /**
     * By default null target type means to match any value (no additional condition).
     * Value {@code false} means to match only refs where type is {@code null}.
     * False is ignored by legacy SQL repo, but supported by Sqale repo.
     */
    private boolean targetTypeNullAsAny = true;

    /**
     * Target filter
     */
    private ObjectFilter filter;

    private RefFilterImpl(@NotNull ItemPath fullPath, @Nullable PrismReferenceDefinition definition,
            @Nullable List<PrismReferenceValue> values, @Nullable ExpressionWrapper expression, @Nullable ObjectFilter filter) {
        super(fullPath, definition, null, values, expression, null, null);
        this.filter = filter;
    }


    public static RefFilter createReferenceEqual(ItemPath path, PrismReferenceDefinition definition, Collection<PrismReferenceValue> values,
            ObjectFilter targetFilter) {
        return new RefFilterImpl(path, definition, values != null ? new ArrayList<>(values) : null, null, targetFilter);
    }

    public static RefFilter createReferenceEqual(ItemPath path, PrismReferenceDefinition definition, ExpressionWrapper expression,
            ObjectFilter targetFilter) {
        return new RefFilterImpl(path, definition, null, expression, targetFilter);
    }

    public static RefFilter createReferenceEqual(ItemPath path, PrismReferenceDefinition definition, Collection<PrismReferenceValue> values) {
        return createReferenceEqual(path, definition, values, null);
    }

    public static RefFilter createReferenceEqual(ItemPath path, PrismReferenceDefinition definition, ExpressionWrapper expression) {
        return createReferenceEqual(path, definition, expression, null);
    }

    @Override
    public RefFilterImpl clone() {
        ObjectFilter targetFilter = filter != null ? filter.clone() : null;
        RefFilterImpl ret = new RefFilterImpl(getFullPath(), getDefinition(), getClonedValues(), getExpression(), targetFilter);
        ret.setOidNullAsAny(oidNullAsAny);
        ret.setTargetTypeNullAsAny(targetTypeNullAsAny);
        return ret;
    }

    @Override
    public @Nullable QName getMatchingRule() {
        return getDeclaredMatchingRule();
    }
    
    @Override
    protected String getFilterName() {
        return "REF";
    }

    @Override
    public boolean match(PrismContainerValue objectValue, MatchingRuleRegistry matchingRuleRegistry) throws SchemaException {
        Collection<PrismValue> objectItemValues = getObjectItemValues(objectValue);
        Collection<? extends PrismValue> filterValues = emptyIfNull(getValues());
        if (objectItemValues.isEmpty()) {
            return filterValues.isEmpty();
        }
        for (PrismValue filterValue : filterValues) {
            checkPrismReferenceValue(filterValue);
            for (PrismValue objectItemValue : objectItemValues) {
                checkPrismReferenceValue(objectItemValue);
                if (valuesMatch(((PrismReferenceValue) filterValue), (PrismReferenceValue) objectItemValue)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void checkPrismReferenceValue(Object value) {
        if (!(value instanceof PrismReferenceValue)) {
            throw new IllegalArgumentException("Not supported prism value for ref filter."
                    + " It must be an instance of PrismReferenceValue but it is " + value.getClass());
        }
    }

    private boolean valuesMatch(PrismReferenceValue filterValue, PrismReferenceValue objectValue) {
        if (filter != null) {
            throw new UnsupportedOperationException("Target filter not supported in in-memory processing");
        }
        if (!matchOid(filterValue.getOid(), objectValue.getOid())) {
            return false;
        }
        if (!QNameUtil.match(PrismConstants.Q_ANY, filterValue.getRelation())) {
            // similar to relation-matching code in PrismReferenceValue (but awkward to unify, so keeping separate)
            PrismContext prismContext = getPrismContext();
            QName objectRelation = objectValue.getRelation();
            QName filterRelation = filterValue.getRelation();
            if (prismContext != null) {
                if (objectRelation == null) {
                    objectRelation = prismContext.getDefaultRelation();
                }
                if (filterRelation == null) {
                    filterRelation = prismContext.getDefaultRelation();
                }
            }
            if (!QNameUtil.match(filterRelation, objectRelation)) {
                return false;
            }
        }
        return matchTargetType(filterValue.getTargetType(), objectValue.getTargetType());
    }

    private boolean matchTargetName(PolyString filterName, PolyString valueName) {
        if (filterName == null) {
            return true;
        }
        // Norm Match
        return valueName != null && filterName.match(valueName);
    }


    private boolean matchOid(String filterOid, String objectOid) {
        return oidNullAsAny && filterOid == null || Objects.equals(objectOid, filterOid);
    }

    private boolean matchTargetType(QName filterType, QName objectType) {
        return targetTypeNullAsAny && filterType == null || QNameUtil.match(objectType, filterType);

    }

    @Override
    public boolean equals(Object obj, boolean exact) {
        if (obj instanceof RefFilter && super.equals(obj, exact)) {
            RefFilter other = (RefFilter) obj;
            if (filter != null) {
                return filter.equals(other.getFilter(), exact);
            } else if (other.getFilter() != null) {
                return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return super.toString() +", targetFilter=" + filter;
    }

    @Override
    public void setOidNullAsAny(boolean oidNullAsAny) {
        checkMutable();
        this.oidNullAsAny = oidNullAsAny;
    }

    @Override
    public void setTargetTypeNullAsAny(boolean targetTypeNullAsAny) {
        checkMutable();
        this.targetTypeNullAsAny = targetTypeNullAsAny;
    }

    @Override
    public boolean isOidNullAsAny() {
        return oidNullAsAny;
    }

    @Override
    public boolean isTargetTypeNullAsAny() {
        return targetTypeNullAsAny;
    }

    @Override
    public ObjectFilter getFilter() {
        return filter;
    }

    @Override
    protected void debugDump(int indent, StringBuilder sb) {
        super.debugDump(indent, sb);
        sb.append("\n");
        DebugUtil.debugDumpWithLabelLn(sb, "Null OID means any", oidNullAsAny, indent + 1);
        DebugUtil.debugDumpWithLabel(sb, "Null target type means any", targetTypeNullAsAny, indent + 1);
        // relationNullAsAny is currently ignored anyway
    }


    public void setFilter(ObjectFilter buildFilter) {
        checkMutable();
        filter = buildFilter;
    }


}
