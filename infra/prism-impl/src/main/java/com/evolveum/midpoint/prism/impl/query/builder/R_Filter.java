/*
 * Copyright (C) 2010-2023 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.prism.impl.query.builder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import javax.xml.namespace.QName;

import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.impl.PrismReferenceValueImpl;
import com.evolveum.midpoint.prism.impl.query.*;
import com.evolveum.midpoint.prism.impl.query.lang.QueryWriter;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.prism.query.*;
import com.evolveum.midpoint.prism.query.builder.*;

/**
 * Implementation of the top-level of the Query fluent API grammar (see {@link QueryBuilder}).
 * See {@link R_AtomicFilter} for low-level filters.
 */
public class R_Filter implements S_FilterEntryOrEmpty {

    private final QueryBuilder queryBuilder;
    private final Class<?> currentClass; // object we are working on (changes on Exists filter)
    private final PrismReferenceDefinition referenceSearchDefinition; // only for reference searches
    private final OrFilter currentFilter;
    private final LogicalSymbol lastLogicalSymbol;
    private final boolean isNegated;
    final R_Filter parentFilter;
    private final QName typeRestriction;
    private final ItemPath existsRestriction;
    private final List<ObjectOrdering> orderingList;
    private final Integer offset;
    private final Integer maxSize;

    private R_Filter(
            QueryBuilder queryBuilder,
            Class<?> currentClass,
            PrismReferenceDefinition referenceSearchDefinition,
            OrFilter currentFilter,
            LogicalSymbol lastLogicalSymbol,
            boolean isNegated,
            R_Filter parentFilter,
            QName typeRestriction,
            ItemPath existsRestriction,
            List<ObjectOrdering> orderingList,
            Integer offset,
            Integer maxSize) {
        this.queryBuilder = queryBuilder;
        this.currentClass = currentClass;
        this.referenceSearchDefinition = referenceSearchDefinition;
        this.currentFilter = currentFilter;
        this.lastLogicalSymbol = lastLogicalSymbol;
        this.isNegated = isNegated;
        this.parentFilter = parentFilter;
        this.typeRestriction = typeRestriction;
        this.existsRestriction = existsRestriction;
        this.orderingList = Objects.requireNonNullElseGet(orderingList, ArrayList::new);
        this.offset = offset;
        this.maxSize = maxSize;
    }

    public static S_FilterEntryOrEmpty create(QueryBuilder builder) {
        return new R_Filter(builder, builder.getQueryClass(), null,
                OrFilterImpl.createOr(), null, false, null, null, null,
                new ArrayList<>(), null, null);
    }

    public static S_FilterEntryOrEmpty create(QueryBuilder builder, PrismReferenceDefinition refDefinition) {
        return new R_Filter(builder, builder.getQueryClass(), refDefinition,
                OrFilterImpl.createOr(), null, false, null, null, null,
                new ArrayList<>(), null, null);
    }

    // subfilter might be null
    R_Filter addSubfilter(ObjectFilter subfilter) {
        if (!currentFilter.isEmpty() && lastLogicalSymbol == null) {
            throw new IllegalStateException(
                    "lastLogicalSymbol is empty but there is already some filter present: " + currentFilter);
        }
        if (typeRestriction != null && existsRestriction != null) {
            throw new IllegalStateException("Both type and exists restrictions present");
        }
        if (typeRestriction != null) {
            if (!currentFilter.isEmpty()) {
                throw new IllegalStateException("Type restriction with 2 filters?");
            }
            if (isNegated) {
                subfilter = NotFilterImpl.createNot(subfilter);
            }
            return parentFilter.addSubfilter(TypeFilterImpl.createType(typeRestriction, subfilter));
        } else if (existsRestriction != null) {
            if (!currentFilter.isEmpty()) {
                throw new IllegalStateException("Exists restriction with 2 filters?");
            }
            if (isNegated) {
                subfilter = NotFilterImpl.createNot(subfilter);
            }
            return parentFilter.addSubfilter(
                    ExistsFilterImpl.createExists(
                            existsRestriction,
                            parentFilter.getCurrentClass(),
                            queryBuilder.getPrismContext(),
                            subfilter));
        } else {
            OrFilter newFilter = appendAtomicFilter(subfilter, isNegated, lastLogicalSymbol);
            return new R_Filter(queryBuilder, currentClass, referenceSearchDefinition,
                    newFilter, null, false, parentFilter, null, null, orderingList, offset, maxSize);
        }
    }

    private OrFilter appendAtomicFilter(ObjectFilter subfilter, boolean negated, LogicalSymbol logicalSymbol) {
        if (negated) {
            subfilter = NotFilterImpl.createNot(subfilter);
        }
        OrFilter updatedFilter = currentFilter.clone();
        if (logicalSymbol == null || logicalSymbol == LogicalSymbol.OR) {
            updatedFilter.addCondition(AndFilterImpl.createAnd(subfilter));
        } else if (logicalSymbol == LogicalSymbol.AND) {
            ((AndFilter) updatedFilter.getLastCondition()).addCondition(subfilter);
        } else {
            throw new IllegalStateException("Unknown logical symbol: " + logicalSymbol);
        }
        return updatedFilter;
    }

    private R_Filter setLastLogicalSymbol(LogicalSymbol newLogicalSymbol) {
        if (this.lastLogicalSymbol != null) {
            throw new IllegalStateException("Two logical symbols in a sequence");
        }
        return new R_Filter(queryBuilder, currentClass, referenceSearchDefinition,
                currentFilter, newLogicalSymbol, isNegated,
                parentFilter, typeRestriction, existsRestriction,
                orderingList, offset, maxSize);
    }

    private R_Filter setNegated() {
        if (isNegated) {
            throw new IllegalStateException("Double negation");
        }
        return new R_Filter(queryBuilder, currentClass, referenceSearchDefinition,
                currentFilter, lastLogicalSymbol, true,
                parentFilter, typeRestriction, existsRestriction,
                orderingList, offset, maxSize);
    }

    private R_Filter addOrdering(ObjectOrdering ordering) {
        Validate.notNull(ordering);
        List<ObjectOrdering> newList = new ArrayList<>(orderingList);
        newList.add(ordering);
        return new R_Filter(queryBuilder, currentClass, referenceSearchDefinition,
                currentFilter, lastLogicalSymbol, isNegated,
                parentFilter, typeRestriction, existsRestriction,
                newList, offset, maxSize);
    }

    private R_Filter setOffset(Integer n) {
        return new R_Filter(queryBuilder, currentClass, referenceSearchDefinition,
                currentFilter, lastLogicalSymbol, isNegated,
                parentFilter, typeRestriction, existsRestriction,
                orderingList, n, maxSize);
    }

    private R_Filter setMaxSize(Integer n) {
        return new R_Filter(queryBuilder, currentClass, referenceSearchDefinition,
                currentFilter, lastLogicalSymbol, isNegated,
                parentFilter, typeRestriction, existsRestriction,
                orderingList, offset, n);
    }

    @Override
    public S_FilterExit all() {
        return addSubfilter(AllFilterImpl.createAll());
    }

    @Override
    public S_FilterExit none() {
        return addSubfilter(NoneFilterImpl.createNone());
    }

    @Override
    public S_FilterExit undefined() {
        return addSubfilter(UndefinedFilterImpl.createUndefined());
    }

    @Override
    public S_FilterExit filter(ObjectFilter filter) {
        return addSubfilter(filter);
    }

    @Override
    public S_FilterExit id(String... identifiers) {
        return addSubfilter(InOidFilterImpl.createInOid(identifiers));
    }

    @Override
    public S_FilterExit id(long... identifiers) {
        List<String> ids = longsToStrings(identifiers);
        return addSubfilter(InOidFilterImpl.createInOid(ids));
    }

    private List<String> longsToStrings(long[] identifiers) {
        List<String> ids = new ArrayList<>(identifiers.length);
        for (long id : identifiers) {
            ids.add(String.valueOf(id));
        }
        return ids;
    }

    @Override
    public S_FilterExit ownerId(String... identifiers) {
        return addSubfilter(InOidFilterImpl.createOwnerHasOidIn(identifiers));
    }

    @Override
    public S_FilterExit ownerId(long... identifiers) {
        return addSubfilter(InOidFilterImpl.createOwnerHasOidIn(longsToStrings(identifiers)));
    }

    @Override
    public S_FilterExit isDirectChildOf(PrismReferenceValue value) {
        OrgFilter orgFilter = OrgFilterImpl.createOrg(value, OrgFilter.Scope.ONE_LEVEL);
        return addSubfilter(orgFilter);
    }

    @Override
    public S_FilterExit isChildOf(PrismReferenceValue value) {
        OrgFilter orgFilter = OrgFilterImpl.createOrg(value, OrgFilter.Scope.SUBTREE);
        return addSubfilter(orgFilter);
    }

    @Override
    public S_FilterExit isParentOf(PrismReferenceValue value) {
        OrgFilter orgFilter = OrgFilterImpl.createOrg(value, OrgFilter.Scope.ANCESTORS);
        return addSubfilter(orgFilter);
    }

    @Override
    public S_FilterExit isDirectChildOf(String oid) {
        OrgFilter orgFilter = OrgFilterImpl.createOrg(oid, OrgFilter.Scope.ONE_LEVEL);
        return addSubfilter(orgFilter);
    }

    @Override
    public S_FilterExit isChildOf(String oid) {
        OrgFilter orgFilter = OrgFilterImpl.createOrg(oid, OrgFilter.Scope.SUBTREE);
        return addSubfilter(orgFilter);
    }

    @Override
    public S_FilterExit isInScopeOf(String oid, OrgFilter.Scope scope) {
        return addSubfilter(OrgFilterImpl.createOrg(oid, scope));
    }

    @Override
    public S_FilterExit isInScopeOf(PrismReferenceValue value, OrgFilter.Scope scope) {
        return addSubfilter(OrgFilterImpl.createOrg(value, scope));
    }

    @Override
    public S_FilterExit isParentOf(String oid) {
        OrgFilter orgFilter = OrgFilterImpl.createOrg(oid, OrgFilter.Scope.ANCESTORS);
        return addSubfilter(orgFilter);
    }

    @Override
    public S_FilterExit isRoot() {
        OrgFilter orgFilter = OrgFilterImpl.createRootOrg();
        return addSubfilter(orgFilter);
    }

    @Override
    public S_FilterExit fullText(String... words) {
        FullTextFilter fullTextFilter = FullTextFilterImpl.createFullText(words);
        return addSubfilter(fullTextFilter);
    }

    @Override
    public S_FilterEntryOrEmpty block() {
        return new R_Filter(queryBuilder, currentClass, referenceSearchDefinition, OrFilterImpl.createOr(),
                null, false, this, null, null, null, null, null);
    }

    @Override
    public S_FilterEntryOrEmpty type(Class<? extends Containerable> type) {
        ComplexTypeDefinition ctd = queryBuilder.getPrismContext()
                .getSchemaRegistry().findComplexTypeDefinitionByCompileTimeClass(type);
        if (ctd == null) {
            throw new IllegalArgumentException("Unknown type: " + type);
        }
        QName typeName = ctd.getTypeName();
        if (typeName == null) {
            throw new IllegalStateException("No type name for " + ctd);
        }
        return new R_Filter(queryBuilder, type, referenceSearchDefinition,
                OrFilterImpl.createOr(), null, false,
                this, typeName, null, null, null, null);
    }

    @Override
    public S_FilterEntryOrEmpty referencedBy(
            @NotNull Class<? extends Containerable> type,
            @NotNull ItemPath path,
            QName relation) {
        ComplexTypeDefinition ctd = queryBuilder.getPrismContext()
                .getSchemaRegistry().findComplexTypeDefinitionByCompileTimeClass(type);
        if (ctd == null) {
            throw new IllegalArgumentException("Unknown type: " + type);
        }

        return new RefByEntry(queryBuilder, this, ctd, type, path, relation);
    }

    @Override
    public S_FilterEntryOrEmpty ref(ItemPath path, QName targetType, QName relation, String... oids) {
        PrismReferenceDefinition refDef = determineReferenceDefinition(path);
        if (referenceSearchDefinition != null) {
            path = ItemPath.SELF_PATH;
        }

        List<PrismReferenceValue> prismRefValues = new ArrayList<>();
        if (oids != null && oids.length > 0) {
            for (String oid : oids) {
                PrismReferenceValueImpl ref = new PrismReferenceValueImpl(oid);
                ref.setTargetType(targetType);
                ref.setRelation(relation);
                prismRefValues.add(ref);
            }
        } else if (targetType != null || relation != null) {
            PrismReferenceValueImpl ref = new PrismReferenceValueImpl();
            ref.setTargetType(targetType);
            ref.setRelation(relation);
            prismRefValues.add(ref);
        }
        targetType = targetType != null ? targetType : refDef.getTargetTypeName();
        var targetClass = getPrismContext().getSchemaRegistry().getCompileTimeClassForObjectType(targetType);
        return new RefFilterEntry(queryBuilder, this, refDef, targetClass, path, prismRefValues);
    }

    private PrismReferenceDefinition determineReferenceDefinition(ItemPath path) {
        PrismReferenceDefinition refDef;
        if (referenceSearchDefinition != null) {
            refDef = checkSelfPathAndGetReferenceSearchDefinition(path);
        } else {
            refDef = queryBuilder.findItemDefinition(getCurrentClass(), path, PrismReferenceDefinition.class);
        }
        return refDef;
    }

    private PrismReferenceDefinition checkSelfPathAndGetReferenceSearchDefinition(ItemPath path) {
        PrismReferenceDefinition refDef;
        if (ItemPath.isEmpty(path)) {
            refDef = referenceSearchDefinition;
        } else {
            throw new IllegalArgumentException(
                    "Reference search only supports REF filter with SELF path (.) on the top level."
                            + " You probably need to use target filter nested inside REF filter."
                            + " Used item path: " + path);
        }
        return refDef;
    }

    @Override
    public OwnedByEntry ownedBy(Class<? extends Containerable> type, ItemPath path) {
        ComplexTypeDefinition ctd = queryBuilder.getPrismContext()
                .getSchemaRegistry().findComplexTypeDefinitionByCompileTimeClass(type);
        if (ctd == null) {
            throw new IllegalArgumentException("Unknown type: " + type);
        }
        return new OwnedByEntry(queryBuilder, this, ctd, type, path);
    }

    @Override
    public S_FilterEntryOrEmpty type(@NotNull QName typeName) {
        ComplexTypeDefinition ctd = queryBuilder.getPrismContext()
                .getSchemaRegistry().findComplexTypeDefinitionByType(typeName);
        if (ctd == null) {
            throw new IllegalArgumentException("Unknown type: " + typeName);
        }
        //noinspection unchecked
        Class<? extends Containerable> type = (Class<? extends Containerable>) ctd.getCompileTimeClass();
        if (type == null) {
            throw new IllegalStateException("No compile time class for " + ctd);
        }
        return new R_Filter(queryBuilder, type, referenceSearchDefinition,
                OrFilterImpl.createOr(), null, false,
                this, typeName, null, null, null, null);
    }

    @Override
    public S_FilterEntryOrEmpty exists(Object... components) {
        if (existsRestriction != null) {
            throw new IllegalStateException("Exists within exists");
        }
        if (components.length == 0) {
            throw new IllegalArgumentException("Empty path in exists() filter is not allowed.");
        }
        ItemPath existsPath = ItemPath.create(components);
        PrismContainerDefinition<?> pcd =
                queryBuilder.findItemDefinition(getCurrentClass(), existsPath, PrismContainerDefinition.class);
        Class<? extends Containerable> clazz = pcd.getCompileTimeClass();
        if (clazz == null) {
            throw new IllegalArgumentException("Item path of '" + existsPath
                    + "' in " + currentClass + " does not point to a valid prism container.");
        }
        return new R_Filter(queryBuilder, clazz, referenceSearchDefinition,
                OrFilterImpl.createOr(), null, false,
                this, null, existsPath, null, null, null);
    }

    @Override
    public S_FilterEntry and() {
        return setLastLogicalSymbol(LogicalSymbol.AND);
    }

    @Override
    public S_FilterEntry or() {
        return setLastLogicalSymbol(LogicalSymbol.OR);
    }

    @Override
    public S_FilterEntry not() {
        return setNegated();
    }

    @Override
    public S_ConditionEntry item(QName... names) {
        return item(
                // Convenience conversion for various ways how self path can be written:
                names.length == 1 && names[0].equals(PrismConstants.T_SELF)
                        ? ItemPath.SELF_PATH
                        : ItemPath.create((Object[]) names));
    }

    @Override
    public S_ConditionEntry item(String... names) {
        return item(
                // Convenience conversion for various ways how self path can be written:
                names.length == 1 && (names[0].equals("") || names[0].equals(QueryWriter.SELF_PATH_SYMBOL))
                        ? ItemPath.SELF_PATH
                        : ItemPath.create((Object[]) names));
    }

    @Override
    public S_ConditionEntry item(ItemPath itemPath) {
        ItemDefinition<?> itemDefinition;
        if (referenceSearchDefinition != null) {
            itemDefinition = checkSelfPathAndGetReferenceSearchDefinition(itemPath);
            itemPath = ItemPath.SELF_PATH;
        } else {
            itemDefinition = queryBuilder.findItemDefinition(
                    getCurrentClass(), itemPath, ItemDefinition.class);
        }
        return item(itemPath, itemDefinition);
    }

    @Override
    public S_ConditionEntry itemWithDef(ItemDefinition<?> itemDefinition, QName... names) {
        ItemPath itemPath = ItemPath.create((Object[]) names);
        return item(itemPath, itemDefinition);
    }

    @Override
    public S_ConditionEntry item(ItemPath itemPath, ItemDefinition<?> itemDefinition) {
        if (itemDefinition != null) {
            return R_AtomicFilter.create(itemPath, itemDefinition, this);
        } else {
            return item(itemPath);
        }
    }

    @Override
    public S_ConditionEntry item(PrismContainerDefinition<?> containerDefinition, QName... names) {
        return item(containerDefinition, ItemPath.create((Object[]) names));
    }

    @Override
    public S_ConditionEntry item(PrismContainerDefinition<?> containerDefinition, ItemPath itemPath) {
        ItemDefinition<?> itemDefinition = containerDefinition.findItemDefinition(itemPath);
        if (itemDefinition == null) {
            throw new IllegalArgumentException("No definition of " + itemPath + " in " + containerDefinition);
        }
        return item(itemPath, itemDefinition);
    }

    @Override
    public S_MatchingRuleEntry itemAs(PrismProperty<?> property) {
        return item(property.getPath(), property.getDefinition()).eq(property);
    }

    @Override
    public S_FilterExit endBlock() {
        if (parentFilter == null) {
            throw new IllegalStateException("endBlock() call without preceding block() one");
        }
        if (hasRestriction()) {
            return addSubfilter(null).endBlock(); // finish if this is open 'type' or 'exists' filter
        }
        if (currentFilter != null || parentFilter.hasRestriction()) {
            ObjectFilter simplified = simplify(currentFilter);
            if (simplified != null || parentFilter.hasRestriction()) {
                return parentFilter.addSubfilter(simplified);
            }
        }
        return parentFilter;
    }

    protected boolean hasRestriction() {
        return existsRestriction != null || typeRestriction != null;
    }

    @Override
    public S_QueryExit asc(QName... names) {
        if (names.length == 0) {
            throw new IllegalArgumentException("There must be at least one name for asc(...) ordering");
        }
        return addOrdering(ObjectOrderingImpl.createOrdering(ItemPath.create((Object[]) names), OrderDirection.ASCENDING));
    }

    @Override
    public S_QueryExit asc(ItemPath path) {
        if (ItemPath.isEmpty(path)) {
            throw new IllegalArgumentException("There must be non-empty path for asc(...) ordering");
        }
        return addOrdering(ObjectOrderingImpl.createOrdering(path, OrderDirection.ASCENDING));
    }

    @Override
    public S_QueryExit desc(QName... names) {
        if (names.length == 0) {
            throw new IllegalArgumentException("There must be at least one name for asc(...) ordering");
        }
        return addOrdering(ObjectOrderingImpl.createOrdering(ItemPath.create((Object[]) names), OrderDirection.DESCENDING));
    }

    @Override
    public S_QueryExit desc(ItemPath path) {
        if (ItemPath.isEmpty(path)) {
            throw new IllegalArgumentException("There must be non-empty path for desc(...) ordering");
        }
        return addOrdering(ObjectOrderingImpl.createOrdering(path, OrderDirection.DESCENDING));
    }

    @Override
    public S_QueryExit offset(Integer n) {
        return setOffset(n);
    }

    @Override
    public S_QueryExit maxSize(Integer n) {
        return setMaxSize(n);
    }

    @Override
    public ObjectQuery build() {
        if (typeRestriction != null || existsRestriction != null) {
            // unfinished empty type restriction or exists restriction
            return addSubfilter(null).build();
        }
        if (parentFilter != null) {
            throw new IllegalStateException("A block in filter definition was probably not closed.");
        }
        ObjectPaging paging = null;
        if (!orderingList.isEmpty()) {
            paging = createIfNeeded(null);
            paging.setOrdering(orderingList);
        }
        if (offset != null) {
            paging = createIfNeeded(paging);
            paging.setOffset(offset);
        }
        if (maxSize != null) {
            paging = createIfNeeded(paging);
            paging.setMaxSize(maxSize);
        }
        return ObjectQueryImpl.createObjectQuery(simplify(currentFilter), paging);
    }

    private ObjectPaging createIfNeeded(ObjectPaging paging) {
        return paging != null ? paging : ObjectPagingImpl.createEmptyPaging();
    }

    @Override
    public ObjectFilter buildFilter() {
        return build().getFilter();
    }

    private ObjectFilter simplify(OrFilter filter) {

        if (filter == null) {
            return null;
        }

        OrFilter simplified = OrFilterImpl.createOr();

        // step 1 - simplify conjunctions
        for (ObjectFilter condition : filter.getConditions()) {
            AndFilter conjunction = (AndFilter) condition;
            if (conjunction.getConditions().size() == 1) {
                simplified.addCondition(conjunction.getLastCondition());
            } else {
                simplified.addCondition(conjunction);
            }
        }

        // step 2 - simplify disjunction
        if (simplified.getConditions().size() == 0) {
            return null;
        } else if (simplified.getConditions().size() == 1) {
            return simplified.getLastCondition();
        } else {
            return simplified;
        }
    }

    public PrismContext getPrismContext() {
        return queryBuilder.getPrismContext();
    }

    /**
     * Client-friendly getter that assumes the expected type.
     * This was created to make working with both containers and references easier.
     */
    @SuppressWarnings("unchecked")
    public <C> Class<C> getCurrentClass() {
        return (Class<C>) currentClass;
    }

    /**
     * This helper {@link R_Filter} subclass makes filters containing optional inner filters more convenient.
     * For example, {@link S_FilterEntry#ref(ItemPath)}, {@link S_FilterEntry#ownedBy(Class)}
     * and {@link S_FilterEntry#referencedBy(Class, ItemPath)} now can be followed by {@link #and()}, {@link #or()},
     * {@link #build()} and similar methods immediately without the need to use chain of {@link #block()} and {@link #endBlock()}.
     *
     * @since 4.7
     */
    private static class R_FilterBlockAutoClose extends R_Filter {

        // TODO do we want to rename it and also add definition+path here?
        // TODO this currently does not help with EXISTS filter

        private R_FilterBlockAutoClose(QueryBuilder queryBuilder, Class<? extends Containerable> type, R_Filter parent) {
            super(queryBuilder, type, null, // TODO do we need reference definition here?
                    OrFilterImpl.createOr(), null, false, parent, null, null, null, null, null);
        }

        @Override
        public S_FilterEntry and() {
            return block().endBlock().and();
        }

        @Override
        public S_FilterEntry or() {
            return block().endBlock().or();
        }

        @Override
        public S_QueryExit asc(QName... names) {
            return block().endBlock().asc(names);
        }

        @Override
        public S_QueryExit asc(ItemPath path) {
            return block().endBlock().asc(path);
        }

        @Override
        public S_QueryExit desc(QName... names) {
            return block().endBlock().desc(names);
        }

        @Override
        public S_QueryExit desc(ItemPath path) {
            return block().endBlock().desc(path);
        }

        @Override
        public S_QueryExit offset(Integer n) {
            return block().endBlock().offset(n);
        }

        @Override
        public S_QueryExit maxSize(Integer n) {
            return block().endBlock().maxSize(n);
        }

        @Override
        public ObjectQuery build() {
            return block().endBlock().build();
        }

        @Override
        public ObjectFilter buildFilter() {
            return block().endBlock().buildFilter();
        }
    }

    private static class RefByEntry extends R_FilterBlockAutoClose {

        private final ComplexTypeDefinition definition;
        private final ItemPath path;
        private final QName relation;

        RefByEntry(QueryBuilder queryBuilder, R_Filter parent, ComplexTypeDefinition ctd,
                Class<? extends Containerable> type, ItemPath path, QName relation) {
            super(queryBuilder, type, parent);
            this.definition = ctd;
            this.path = path;
            this.relation = relation;
        }

        @Override
        R_Filter addSubfilter(ObjectFilter subfilter) {
            var filter = ReferencedByFilterImpl.create(definition, path, subfilter, relation);
            return parentFilter.addSubfilter(filter);
        }

        @Override
        protected boolean hasRestriction() {
            return true;
        }
    }

    private static class OwnedByEntry extends R_FilterBlockAutoClose {

        private final ComplexTypeDefinition definition;
        private final ItemPath path;

        OwnedByEntry(QueryBuilder queryBuilder, R_Filter parent,
                ComplexTypeDefinition ctd, Class<? extends Containerable> type, ItemPath path) {
            super(queryBuilder, type, parent);
            this.definition = ctd;
            this.path = path;
        }

        @Override
        R_Filter addSubfilter(ObjectFilter subfilter) {
            var filter = OwnedByFilterImpl.create(definition, path, subfilter);
            return parentFilter.addSubfilter(filter);
        }

        @Override
        protected boolean hasRestriction() {
            return true;
        }
    }

    private static class RefFilterEntry extends R_FilterBlockAutoClose {

        private final PrismReferenceDefinition definition;
        private final ItemPath path;
        private final Collection<PrismReferenceValue> values;

        RefFilterEntry(QueryBuilder queryBuilder, R_Filter parent, PrismReferenceDefinition def,
                Class<? extends Containerable> type, ItemPath path, Collection<PrismReferenceValue> values) {
            super(queryBuilder, type, parent);
            this.definition = def;
            this.path = path;
            this.values = values;
        }

        @Override
        R_Filter addSubfilter(ObjectFilter subfilter) {
            var filter = (RefFilterImpl) RefFilterImpl.createReferenceEqual(path, definition, values);
            filter.setFilter(subfilter);
            return parentFilter.addSubfilter(filter);
        }

        @Override
        protected boolean hasRestriction() {
            return true;
        }
    }
}
