/*
 * Copyright (C) 2010-2022 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism.impl.query;

import javax.xml.namespace.QName;

import com.evolveum.midpoint.prism.path.TypedItemPath;

import org.jetbrains.annotations.NotNull;

import com.evolveum.midpoint.prism.ComplexTypeDefinition;
import com.evolveum.midpoint.prism.PrismContainer;
import com.evolveum.midpoint.prism.PrismContainerDefinition;
import com.evolveum.midpoint.prism.PrismContainerValue;
import com.evolveum.midpoint.prism.match.MatchingRuleRegistry;
import com.evolveum.midpoint.prism.query.ObjectFilter;
import com.evolveum.midpoint.prism.query.TypeFilter;
import com.evolveum.midpoint.prism.query.Visitor;
import com.evolveum.midpoint.util.DebugUtil;
import com.evolveum.midpoint.util.PrettyPrinter;
import com.evolveum.midpoint.util.QNameUtil;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;

import java.util.Collection;
import java.util.function.Consumer;

/**
 * @author lazyman
 */
public class TypeFilterImpl extends ObjectFilterImpl implements TypeFilter {

    private static final Trace LOGGER = TraceManager.getTrace(TypeFilter.class);

    @NotNull private final QName type;
    private ObjectFilter filter;

    public TypeFilterImpl(@NotNull QName type, ObjectFilter filter) {
        this.type = type;
        this.filter = filter;
    }

    @Override
    @NotNull
    public QName getType() {
        return type;
    }

    @Override
    public ObjectFilter getFilter() {
        return filter;
    }

    @Override
    public void setFilter(ObjectFilter filter) {
        checkMutable();
        if (filter == this) {
            throw new IllegalArgumentException("Type filte has itself as a subfilter");
        }
        this.filter = filter;
    }

    public static TypeFilter createType(QName type, ObjectFilter filter) {
        return new TypeFilterImpl(type, filter);
    }

    @SuppressWarnings("CloneDoesntCallSuperClone")
    @Override
    public TypeFilterImpl clone() {
        ObjectFilter f = filter != null ? filter.clone() : null;
        return new TypeFilterImpl(type, f);
    }

    @Override
    public TypeFilter cloneEmpty() {
        return new TypeFilterImpl(type, null);
    }

    // untested; TODO test this method
    @Override
    public boolean match(PrismContainerValue<?> value, MatchingRuleRegistry matchingRuleRegistry) throws SchemaException {
        if (value == null) {
            return false;           // just for safety
        }
        ComplexTypeDefinition definition = value.getComplexTypeDefinition();
        if (definition == null) {
            if (!(value.getParent() instanceof PrismContainer)) {
                LOGGER.trace("Parent of {} is not a PrismContainer, returning false; it is {}", value, value.getParent());
                return false;
            }
            PrismContainer<?> container = (PrismContainer<?>) value.getParent();
            PrismContainerDefinition pcd = container.getDefinition();
            if (pcd == null) {
                LOGGER.trace("Parent of {} has no definition, returning false", value);
                return false;
            }
            definition = pcd.getComplexTypeDefinition();
        }
        // TODO TODO TODO subtypes!!!!!!!!
        if (!QNameUtil.match(definition.getTypeName(), type)) {
            return false;
        }
        if (filter == null) {
            return true;
        } else {
            return filter.match(value, matchingRuleRegistry);
        }
    }

    @Override
    protected void performFreeze() {
        freeze(filter);
    }

    @Override
    public void checkConsistence(boolean requireDefinitions) {
        if (type == null) {
            throw new IllegalArgumentException("Null type in " + this);
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
        sb.append("TYPE: ");
        sb.append(type.getLocalPart());
        sb.append('\n');
        if (filter != null) {
            sb.append(filter.debugDump(indent + 1));
        }

        return sb.toString();
    }

    @Override
    public boolean equals(Object o, boolean exact) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TypeFilterImpl that = (TypeFilterImpl) o;
        if (!type.equals(that.type)) {
            return false;
        }
        return ObjectFilter.equals(filter, that.filter, exact);
    }

    // Just to make checkstyle happy
    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return type.hashCode();
    }

    @Override
    public String toString() {
        return "TYPE("
                + PrettyPrinter.prettyPrint(type)
                + ","
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
    public void collectUsedPaths(TypedItemPath base, Consumer<TypedItemPath> pathConsumer, boolean expandReferences) {
        var retyped = TypedItemPath.of(getType(),base.getPath()).emitTo(pathConsumer, expandReferences);
        if (getFilter() != null) {
            getFilter().collectUsedPaths(retyped, pathConsumer, expandReferences);
        }
    }
}
