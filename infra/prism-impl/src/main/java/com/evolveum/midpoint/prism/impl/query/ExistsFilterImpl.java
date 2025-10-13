/*
 * Copyright (C) 2010-2023 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.prism.impl.query;

import java.util.Objects;
import java.util.function.Consumer;

import com.evolveum.midpoint.prism.path.TypedItemPath;

import com.evolveum.midpoint.prism.query.FilterItemPathTransformer;

import org.jetbrains.annotations.NotNull;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.match.MatchingRuleRegistry;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.prism.query.ExistsFilter;
import com.evolveum.midpoint.prism.query.ObjectFilter;
import com.evolveum.midpoint.prism.query.Visitor;
import com.evolveum.midpoint.util.DebugUtil;
import com.evolveum.midpoint.util.PrettyPrinter;
import com.evolveum.midpoint.util.exception.SchemaException;

public final class ExistsFilterImpl extends AbstractItemFilter implements ExistsFilter {

    private final ItemDefinition<?> definition;
    private ObjectFilter filter;

    private ExistsFilterImpl(@NotNull ItemPath fullPath, ItemDefinition<?> definition, ObjectFilter filter) {
        super(fullPath);
        this.definition = definition;
        this.filter = filter;
        checkConsistence(true);
    }

    @Override
    public ItemDefinition<?> getDefinition() {
        return definition;
    }

    @Override
    public ObjectFilter getFilter() {
        return filter;
    }

    @Override
    public void setFilter(ObjectFilter filter) {
        checkMutable();
        this.filter = filter;
    }

    @Override
    protected void performFreeze() {
        freeze(filter);
    }

    public static ExistsFilter createExists(
            ItemPath itemPath, ItemDefinition<?> containerDef, ObjectFilter filter) {
        if (containerDef instanceof PrismContainerDefinition) {
            ItemDefinition<?> itemDefinition =
                    FilterImplUtil.findItemDefinition(
                            itemPath, (PrismContainerDefinition<? extends Containerable>) containerDef);
            return new ExistsFilterImpl(itemPath, itemDefinition, filter);
        }
        throw new UnsupportedOperationException(
                "Not supported for non-container definitions, itemPath=" + itemPath + ", "
                        + " containerDef=" + containerDef + ", filter: " + filter);
    }

    public static <C extends Containerable> ExistsFilter createExists(
            ItemPath itemPath, Class<C> clazz, ObjectFilter filter) {
        ItemDefinition<?> itemDefinition = FilterImplUtil.findItemDefinition(itemPath, clazz);
        return new ExistsFilterImpl(itemPath, itemDefinition, filter);
    }

    @Override
    public ExistsFilterImpl clone() {
        ObjectFilter f = filter != null ? filter.clone() : null;
        return new ExistsFilterImpl(fullPath, definition, f);
    }

    @Override
    public ExistsFilter cloneEmpty() {
        return new ExistsFilterImpl(fullPath, definition, null);
    }

    @Override
    public boolean match(PrismContainerValue<?> value, MatchingRuleRegistry matchingRuleRegistry) throws SchemaException {
        Item<?, ?> itemToFind = value.findItem(fullPath);
        if (itemToFind == null || itemToFind.getValues().isEmpty()) {
            return false;
        }
        if (!(itemToFind instanceof PrismContainer)) {
            throw new UnsupportedOperationException(
                    "Using exists query to search for items other than containers is not supported in-memory: " + itemToFind);
        }
        if (filter == null) {
            return true;
        }
        for (PrismContainerValue<?> pcv : ((PrismContainer<?>) itemToFind).getValues()) {
            if (filter.match(pcv, matchingRuleRegistry)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void checkConsistence(boolean requireDefinitions) {
        if (fullPath.isEmpty()) {
            throw new IllegalArgumentException("Null or empty path in " + this);
        }
        if (requireDefinitions && definition == null) {
            throw new IllegalArgumentException("Null definition in " + this);
        }
        // null subfilter is legal. It means "ALL".
        if (filter != null) {
            filter.checkConsistence(requireDefinitions);
        }
    }

    @Override
    public String debugDump(int indent) {
        StringBuilder sb = new StringBuilder();
        DebugUtil.indentDebugDump(sb, indent);
        sb.append("EXISTS: ");
        sb.append(fullPath);
        sb.append('\n');
        DebugUtil.indentDebugDump(sb, indent + 1);
        sb.append("DEF: ");
        if (getDefinition() != null) {
            sb.append(getDefinition());
        } else {
            sb.append("null");
        }
        sb.append("\n");
        if (filter != null) {
            sb.append(filter.debugDump(indent + 1));
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return "EXISTS("
                + PrettyPrinter.prettyPrint(fullPath)
                + ", "
                + filter
                + ")";
    }

    @Override
    public void accept(Visitor visitor) {
        super.accept(visitor);
        if (filter != null) {
            visitor.visit(filter);
        }
    }

    @Override
    public boolean equals(Object o, boolean exact) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ExistsFilterImpl that = (ExistsFilterImpl) o;

        if (!fullPath.equals(that.fullPath, exact)) {
            return false;
        }
        if (exact && !Objects.equals(definition, that.definition)) {
            return false;
        }
        return filter != null ? filter.equals(that.filter, exact) : that.filter == null;
    }

    // Just to make checkstyle happy
    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + (definition != null ? definition.hashCode() : 0);
        result = 31 * result + (filter != null ? filter.hashCode() : 0);
        return result;
    }

    @Override
    public void collectUsedPaths(TypedItemPath base, Consumer<TypedItemPath> pathConsumer, boolean expandReferences) {
        var newBase = base.append(getFullPath()).emitTo(pathConsumer, expandReferences);
        if (getFilter() != null) {
            getFilter().collectUsedPaths(newBase, pathConsumer, expandReferences);
        }
    }

    @Override
    public void transformItemPaths(ItemPath parentPath, ItemDefinition<?> parentDef, FilterItemPathTransformer transformer) {
        super.transformItemPaths(parentPath, parentDef, transformer);
        // If this is dereference, we should break it somehow.
        if (filter != null) {
            filter.transformItemPaths(ItemPath.create(parentPath, fullPath), definition, transformer);
        }
    }
}
