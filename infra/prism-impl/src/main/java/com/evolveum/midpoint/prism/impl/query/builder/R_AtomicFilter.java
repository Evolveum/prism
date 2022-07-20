/*
 * Copyright (C) 2010-2022 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.prism.impl.query.builder;

import java.util.*;
import java.util.stream.Collectors;
import javax.xml.namespace.QName;

import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.Nullable;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.impl.PrismPropertyValueImpl;
import com.evolveum.midpoint.prism.impl.PrismReferenceValueImpl;
import com.evolveum.midpoint.prism.impl.query.*;
import com.evolveum.midpoint.prism.impl.query.builder.R_AtomicFilter.FuzzyStringBuilderImpl;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.prism.polystring.PolyString;
import com.evolveum.midpoint.prism.query.FuzzyStringMatchFilter;
import com.evolveum.midpoint.prism.query.FuzzyStringMatchFilter.FuzzyMatchingMethod;
import com.evolveum.midpoint.prism.query.ObjectFilter;
import com.evolveum.midpoint.prism.query.ObjectQuery;
import com.evolveum.midpoint.prism.query.RefFilter;
import com.evolveum.midpoint.prism.query.ValueFilter;
import com.evolveum.midpoint.prism.query.builder.*;
import com.evolveum.midpoint.util.MiscUtil;

public class R_AtomicFilter implements S_ConditionEntry, S_MatchingRuleEntry, S_RightHandItemEntry {



    private final ItemPath itemPath;
    private final PrismPropertyDefinition<?> propertyDefinition;
    private final PrismReferenceDefinition referenceDefinition;
    private final ValueFilter<?, ?> filter;
    private final R_Filter owner;
    private final boolean expectingRightSide;

    private R_AtomicFilter(ItemPath itemPath, ItemDefinition<?> itemDefinition, R_Filter owner) {
        Validate.notNull(itemPath);
        Validate.notNull(itemDefinition);
        Validate.notNull(owner);
        this.itemPath = itemPath;
        if (itemDefinition instanceof PrismPropertyDefinition) {
            propertyDefinition = (PrismPropertyDefinition<?>) itemDefinition;
            referenceDefinition = null;
        } else if (itemDefinition instanceof PrismReferenceDefinition) {
            propertyDefinition = null;
            referenceDefinition = (PrismReferenceDefinition) itemDefinition;
        } else {
            throw new IllegalStateException("Unsupported item definition: " + itemDefinition);
        }
        this.filter = null;
        this.owner = owner;
        this.expectingRightSide = false;
    }

    private R_AtomicFilter(R_AtomicFilter original, ValueFilter<?, ?> filter, boolean expectingRightSide) {
        Validate.notNull(original);
        Validate.notNull(filter);
        this.itemPath = original.itemPath;
        this.propertyDefinition = original.propertyDefinition;
        this.referenceDefinition = original.referenceDefinition;
        this.filter = filter;
        this.owner = original.owner;
        this.expectingRightSide = expectingRightSide;
    }

    public R_AtomicFilter(R_AtomicFilter original, ValueFilter<?, ?> filter) {
        this(original, filter, false);
    }

    static R_AtomicFilter create(ItemPath itemPath, ItemDefinition<?> itemDefinition, R_Filter owner) {
        return new R_AtomicFilter(itemPath, itemDefinition, owner);
    }

    @Override
    public S_FilterExit item(QName... names) {
        return item(ItemPath.create((Object[]) names), null);
    }

    @Override
    public S_FilterExit item(ItemPath itemPath, ItemDefinition<?> itemDefinition) {
        if (!expectingRightSide) {
            throw new IllegalStateException("Unexpected item() call");
        }
        if (filter == null) {
            throw new IllegalStateException("item() call with no filter");
        }
        ValueFilter<?, ?> newFilter = filter.clone();
        newFilter.setRightHandSidePath(itemPath);
        newFilter.setRightHandSideDefinition(itemDefinition);
        return new R_AtomicFilter(this, newFilter);
    }

    @Override
    public <T> S_MatchingRuleEntry eq(PrismProperty<T> property) {
        List<PrismPropertyValue<T>> clonedValues = (List<PrismPropertyValue<T>>)
                PrismValueCollectionsUtil.cloneCollection(property.getValues());
        //noinspection unchecked
        PrismPropertyDefinition<T> definition = this.propertyDefinition != null
                ? (PrismPropertyDefinition<T>) this.propertyDefinition
                : property.getDefinition();
        return new R_AtomicFilter(this, EqualFilterImpl
                .createEqual(itemPath, definition, null, clonedValues));
    }

    @Override
    public S_MatchingRuleEntry eq(Object... values) {
        return new R_AtomicFilter(this, EqualFilterImpl.createEqual(
                itemPath, propertyDefinition, null, values));
    }

    @Override
    public S_RightHandItemEntry eq() {
        return new R_AtomicFilter(this,
                EqualFilterImpl.createEqual(itemPath, propertyDefinition, null),
                true);
    }

    @Override
    public S_MatchingRuleEntry eqPoly(String orig, String norm) {
        return eq(new PolyString(orig, norm));
    }

    @Override
    public S_MatchingRuleEntry eqPoly(String orig) {
        return eq(new PolyString(orig));
    }

    @Override
    public S_MatchingRuleEntry gt(Object value) {
        return new R_AtomicFilter(this, GreaterFilterImpl.createGreater(
                itemPath, propertyDefinition, null, value, false, owner.getPrismContext()));
    }

    @Override
    public S_RightHandItemEntry gt() {
        return new R_AtomicFilter(this,
                GreaterFilterImpl.createGreater(itemPath, propertyDefinition, false),
                true);
    }

    @Override
    public S_MatchingRuleEntry ge(Object value) {
        return new R_AtomicFilter(this, GreaterFilterImpl.createGreater(
                itemPath, propertyDefinition, null, value, true, owner.getPrismContext()));
    }

    @Override
    public S_RightHandItemEntry ge() {
        return new R_AtomicFilter(this,
                GreaterFilterImpl.createGreater(itemPath, propertyDefinition, true),
                true);
    }

    @Override
    public S_MatchingRuleEntry lt(Object value) {
        return new R_AtomicFilter(this, LessFilterImpl.createLess(
                itemPath, propertyDefinition, null, value, false, owner.getPrismContext()));
    }

    @Override
    public S_RightHandItemEntry lt() {
        return new R_AtomicFilter(this,
                LessFilterImpl.createLess(itemPath, propertyDefinition, false),
                true);
    }

    @Override
    public S_MatchingRuleEntry le(Object value) {
        return new R_AtomicFilter(this, LessFilterImpl.createLess(
                itemPath, propertyDefinition, null, value, true, owner.getPrismContext()));
    }

    @Override
    public S_RightHandItemEntry le() {
        return new R_AtomicFilter(this,
                LessFilterImpl.createLess(itemPath, propertyDefinition, true),
                true);
    }

    @Override
    public S_MatchingRuleEntry startsWith(Object value) {
        return new R_AtomicFilter(this,
                SubstringFilterImpl.createSubstring(
                        itemPath, propertyDefinition, null, value, true, false));
    }

    @Override
    public S_MatchingRuleEntry startsWithPoly(String orig, String norm) {
        return startsWith(new PolyString(orig, norm));
    }

    @Override
    public S_MatchingRuleEntry startsWithPoly(String orig) {
        return startsWith(new PolyString(orig));
    }

    @Override
    public S_MatchingRuleEntry endsWith(Object value) {
        return new R_AtomicFilter(this,
                SubstringFilterImpl.createSubstring(
                        itemPath, propertyDefinition, null, value, false, true));
    }

    @Override
    public S_MatchingRuleEntry endsWithPoly(String orig, String norm) {
        return endsWith(new PolyString(orig, norm));
    }

    @Override
    public S_MatchingRuleEntry endsWithPoly(String orig) {
        return endsWith(new PolyString(orig));
    }

    @Override
    public S_MatchingRuleEntry contains(Object value) {
        return new R_AtomicFilter(this,
                SubstringFilterImpl.createSubstring(
                        itemPath, propertyDefinition, null, value, false, false));
    }

    @Override
    public S_MatchingRuleEntry containsPoly(String orig, String norm) {
        return contains(new PolyString(orig, norm));
    }

    @Override
    public S_MatchingRuleEntry containsPoly(String orig) {
        return contains(new PolyString(orig));
    }

    @Override
    public S_FilterExit refRelation(QName... relations) {
        List<PrismReferenceValue> values = new ArrayList<>();
        for (QName relation : relations) {
            PrismReferenceValue ref = new PrismReferenceValueImpl();
            ref.setRelation(relation);
            values.add(ref);
        }
        return ref(values);
    }

    @Override
    public S_FilterExit refType(QName... targetTypeNames) {
        List<PrismReferenceValue> values = new ArrayList<>();
        for (QName targetTypeName : targetTypeNames) {
            PrismReferenceValue ref = new PrismReferenceValueImpl();
            ref.setTargetType(targetTypeName);
            values.add(ref);
        }
        return ref(values);
    }

    @Override
    public S_FilterExit ref(PrismReferenceValue... values) {
        return ref(MiscUtil.asListTreatingNull(values));
    }

    @Override
    public S_FilterExit ref(Collection<PrismReferenceValue> values) {
        return ref(values, true, true);
    }

    @Override
    public S_FilterExit ref(Collection<PrismReferenceValue> values, boolean nullTypeAsAny) {
        return ref(values, true, nullTypeAsAny);
    }

    @Override
    public S_FilterExit ref(
            Collection<PrismReferenceValue> values, boolean nullOidAsAny, boolean nullTypeAsAny) {
        RefFilter filter = RefFilterImpl.createReferenceEqual(itemPath, referenceDefinition, values);
        filter.setOidNullAsAny(nullOidAsAny);
        filter.setTargetTypeNullAsAny(nullTypeAsAny);
        return ref(filter);
    }

    @Override
    public S_FilterExit ref(RefFilter filter) {
        return new R_AtomicFilter(this, filter);
    }

    @Override
    public S_FilterExit ref(ExpressionWrapper expression) {
        RefFilter filter = RefFilterImpl.createReferenceEqual(itemPath, referenceDefinition, expression);
        return ref(filter);
    }

    @Override
    public S_FilterExit ref(String... oids) {
        if (oids.length == 0 || oids.length == 1 && oids[0] == null) {
            return isNull();
        } else {
            return ref(Arrays.stream(oids)
                    .map(oid -> new PrismReferenceValueImpl(oid)).collect(Collectors.toList()));
        }
    }

    @Override
    public S_FilterExit ref(@Nullable String oid, @Nullable QName targetTypeName, @Nullable QName relation) {
        if (oid == null && targetTypeName == null && relation == null) {
            return isNull();
        } else {
            PrismReferenceValueImpl value = new PrismReferenceValueImpl(oid, targetTypeName);
            value.setRelation(relation);
            return ref(value);
        }
    }

    @Override
    public FuzzyStringBuilder fuzzyString(String... values) {
        var builder = new FuzzyStringBuilderImpl();
        for (String value : values) {
            builder.value(value);
        }
        return builder;
    }

    @Override
    public S_FilterExit isNull() {
        if (propertyDefinition != null) {
            return new R_AtomicFilter(this,
                    EqualFilterImpl.createEqual(itemPath, propertyDefinition, null));
        } else if (referenceDefinition != null) {
            RefFilterImpl refFilter = (RefFilterImpl) RefFilterImpl.createReferenceEqual(
                    itemPath, referenceDefinition, Collections.emptyList());
            refFilter.setOidNullAsAny(false);
            return new R_AtomicFilter(this, refFilter);
        } else {
            throw new IllegalStateException("No definition");
        }
    }

    @Override
    public S_FilterExit matching(QName matchingRuleName) {
        ValueFilter<?, ?> clone = filter.clone();
        clone.setMatchingRule(matchingRuleName);
        return new R_AtomicFilter(this, clone);
    }

    @Override
    public S_FilterExit matchingOrig() {
        return matching(PrismConstants.POLY_STRING_ORIG_MATCHING_RULE_NAME);
    }

    @Override
    public S_FilterExit matchingNorm() {
        return matching(PrismConstants.POLY_STRING_NORM_MATCHING_RULE_NAME);
    }

    @Override
    public S_FilterExit matchingStrict() {
        return matching(PrismConstants.POLY_STRING_STRICT_MATCHING_RULE_NAME);
    }

    @Override
    public S_FilterExit matchingCaseIgnore() {
        return matching(PrismConstants.STRING_IGNORE_CASE_MATCHING_RULE_NAME);
    }

    // ==============================================================
    // Methods which implement actions common with R_Filter

    @Override
    public S_FilterEntry or() {
        return finish().or();
    }

    @Override
    public S_FilterEntry and() {
        return finish().and();
    }

    @Override
    public ObjectQuery build() {
        return finish().build();
    }

    @Override
    public ObjectFilter buildFilter() {
        return build().getFilter();
    }

    @Override
    public S_FilterExit asc(QName... names) {
        return finish().asc(names);
    }

    @Override
    public S_FilterExit asc(ItemPath path) {
        return finish().asc(path);
    }

    @Override
    public S_FilterExit desc(QName... names) {
        return finish().desc(names);
    }

    @Override
    public S_FilterExit desc(ItemPath path) {
        return finish().desc(path);
    }

    @Override
    public S_FilterExit offset(Integer n) {
        return finish().offset(n);
    }

    @Override
    public S_FilterExit maxSize(Integer n) {
        return finish().maxSize(n);
    }

    @Override
    public S_FilterExit endBlock() {
        return finish().endBlock();
    }

    private R_Filter finish() {
        if (filter == null) {
            throw new IllegalStateException("Filter is not yet created!");
        }
        return owner.addSubfilter(filter);
    }

    public class FuzzyStringBuilderImpl implements FuzzyStringBuilder {

        private List<PrismPropertyValue<String>> values = new ArrayList<>();

        @Override
        public FuzzyStringBuilder value(String value) {
            values.add(new PrismPropertyValueImpl<String>(value));
            return this;
        }

        @Override
        public S_FilterExit method(FuzzyMatchingMethod method) {
            var list = new ArrayList<PrismPropertyValue<String>>(values);
            ValueFilter<?, ?> fuzzyFilter = FuzzyStringMatchFilterImpl.<String>create(itemPath, (PrismPropertyDefinition<String>) propertyDefinition, method, list);
            return new R_AtomicFilter(R_AtomicFilter.this, fuzzyFilter);
        }

    }
}
