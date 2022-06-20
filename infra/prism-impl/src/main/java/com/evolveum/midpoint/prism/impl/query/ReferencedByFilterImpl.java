/*
 * Copyright (C) 2022 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.prism.impl.query;

import java.util.Objects;
import javax.xml.namespace.QName;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.evolveum.midpoint.prism.ComplexTypeDefinition;
import com.evolveum.midpoint.prism.PrismContainerValue;
import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.match.MatchingRuleRegistry;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.prism.query.ObjectFilter;
import com.evolveum.midpoint.prism.query.ReferencedByFilter;
import com.evolveum.midpoint.util.DebugUtil;
import com.evolveum.midpoint.util.PrettyPrinter;
import com.evolveum.midpoint.util.QNameUtil;
import com.evolveum.midpoint.util.exception.SchemaException;

public class ReferencedByFilterImpl extends ObjectFilterImpl implements ReferencedByFilter {

    private static final long serialVersionUID = 1L;

    private final @NotNull ComplexTypeDefinition type;
    private final @NotNull ItemPath path;
    private final @Nullable ObjectFilter filter;
    private final @Nullable QName relation;

    private ReferencedByFilterImpl(@NotNull ComplexTypeDefinition type, @NotNull ItemPath path,
            @Nullable ObjectFilter filter, @Nullable QName relation) {
        this.type = Objects.requireNonNull(type);
        this.path = Objects.requireNonNull(path);
        this.filter = filter;
        this.relation = relation;
    }

    public static ReferencedByFilter create(@NotNull QName typeName, @NotNull ItemPath path, ObjectFilter filter,
            @Nullable QName relation) {
        var type = PrismContext.get().getSchemaRegistry().findComplexTypeDefinitionByType(typeName);
        Preconditions.checkArgument(type != null, "Type %s does not have complex type definition", typeName);
        return new ReferencedByFilterImpl(type, path, filter, relation);
    }

    public static ReferencedByFilter create(@NotNull ComplexTypeDefinition type, @NotNull ItemPath path, ObjectFilter filter,
            @Nullable QName relation) {
        return new ReferencedByFilterImpl(type, path, filter, relation);
    }

    @Override
    public @NotNull ComplexTypeDefinition getType() {
        return type;
    }

    @Override
    public @Nullable ObjectFilter getFilter() {
        return filter;
    }

    @Override
    public @NotNull ItemPath getPath() {
        return path;
    }

    @Override
    public @Nullable QName getRelation() {
        return relation;
    }

    @Override
    public boolean match(PrismContainerValue<?> value, MatchingRuleRegistry matchingRuleRegistry) throws SchemaException {
        throw new UnsupportedOperationException("ReferencedBy is not supported for in-memory");
    }

    @Override
    public void checkConsistence(boolean requireDefinitions) {
        if (filter != null) {
            filter.checkConsistence(requireDefinitions);
        }
    }

    @Override
    public boolean equals(Object o, boolean exact) {
        if (!(o instanceof ReferencedByFilter)) {
            return false;
        }

        var other = (ReferencedByFilter) o;
        if (!QNameUtil.match(getType().getTypeName(), other.getType().getTypeName())) {
            return false;
        }
        if (!QNameUtil.match(getRelation(), other.getRelation())) {
            return false;
        }
        if (!path.equals(other.getPath(), exact)) {
            return false;
        }
        return ObjectFilter.equals(filter, other.getFilter(), exact);
    }

    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    @Override
    public boolean equals(Object o) {
        return equals(o, false);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, path, filter);
    }

    @Override
    public String toString() {
        return "REFERENCED-BY("
                + PrettyPrinter.prettyPrint(type)
                + ","
                + path
                + ","
                + filter
                + ")";
    }

    @Override
    public String debugDump(int indent) {
        StringBuilder sb = new StringBuilder();
        DebugUtil.indentDebugDump(sb, indent);
        sb.append("ReferencedBy: type: ");
        sb.append(type.getTypeName().getLocalPart());
        sb.append(" path: ");
        sb.append(path);
        sb.append('\n');
        if (filter != null) {
            sb.append(filter.debugDump(indent + 1));
        }
        return sb.toString();
    }

    @Override
    protected void performFreeze() {
        if (filter != null) {
            filter.freeze();
        }
    }

    @Override
    public ReferencedByFilterImpl clone() {
        return new ReferencedByFilterImpl(type, path,
                filter != null ? filter.clone() : null,
                relation);
    }
}
