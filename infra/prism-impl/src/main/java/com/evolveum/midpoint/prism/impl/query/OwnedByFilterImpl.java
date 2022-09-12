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
import com.evolveum.midpoint.prism.query.OwnedByFilter;
import com.evolveum.midpoint.util.DebugUtil;
import com.evolveum.midpoint.util.PrettyPrinter;
import com.evolveum.midpoint.util.QNameUtil;
import com.evolveum.midpoint.util.exception.SchemaException;

public class OwnedByFilterImpl extends ObjectFilterImpl implements OwnedByFilter {

    private static final long serialVersionUID = 1L;

    private final @NotNull ComplexTypeDefinition type;
    private final @Nullable ItemPath path;
    private final @Nullable ObjectFilter filter;

    private OwnedByFilterImpl(@NotNull ComplexTypeDefinition type,
            @Nullable ItemPath path, @Nullable ObjectFilter filter) {
        this.type = Objects.requireNonNull(type);
        this.path = path;
        this.filter = filter;
    }

    public static OwnedByFilter create(@NotNull QName typeName,
            @Nullable ItemPath path, @Nullable ObjectFilter filter) {
        var type = PrismContext.get().getSchemaRegistry().findComplexTypeDefinitionByType(typeName);
        Preconditions.checkArgument(type != null, "Type %s does not have complex type definition", typeName);
        return new OwnedByFilterImpl(type, path, filter);
    }

    public static OwnedByFilter create(@NotNull ComplexTypeDefinition type,
            @Nullable ItemPath path, @Nullable ObjectFilter filter) {
        return new OwnedByFilterImpl(type, path, filter);
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
    public @Nullable ItemPath getPath() {
        return path;
    }

    @Override
    public boolean match(PrismContainerValue<?> value, MatchingRuleRegistry matchingRuleRegistry) throws SchemaException {
        throw new UnsupportedOperationException("OwnedBy is not supported for in-memory");
    }

    @Override
    public void checkConsistence(boolean requireDefinitions) {
        if (filter != null) {
            filter.checkConsistence(requireDefinitions);
        }
    }

    @Override
    public boolean equals(Object o, boolean exact) {
        if (!(o instanceof OwnedByFilterImpl)) {
            return false;
        }

        var other = (OwnedByFilterImpl) o;
        return QNameUtil.match(type.getTypeName(), other.type.getTypeName())
                && ItemPath.equals(path, other.getPath(), exact)
                && ObjectFilter.equals(filter, other.getFilter(), exact);
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
        return "OWNED-BY("
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
        sb.append("OwnedBy: type: ");
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
    public OwnedByFilterImpl clone() {
        return new OwnedByFilterImpl(type, path, filter != null ? filter.clone() : null);
    }
}
