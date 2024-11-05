/*
 * Copyright (C) 2024 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.prism.impl.lazy;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.deleg.PrismContainerValueDelegator;
import com.evolveum.midpoint.prism.impl.PrismContextImpl;
import com.evolveum.midpoint.prism.impl.marshaller.PrismUnmarshaller;
import com.evolveum.midpoint.prism.impl.xnode.MapXNodeImpl;

import com.evolveum.midpoint.util.exception.SchemaException;

import com.evolveum.midpoint.util.exception.SystemException;

import org.jetbrains.annotations.NotNull;

import javax.xml.namespace.QName;

public class LazyPrismContainerValue<C extends Containerable>
        extends LazyXNodeBasedValue<MapXNodeImpl, PrismContainerValue<C>>
        implements PrismContainerValueDelegator<C> {

    protected ComplexTypeDefinition complexTypeDefinition = null;

    private Itemable parent;

    public LazyPrismContainerValue(ComplexTypeDefinition ctd, ParsingContext parsingContext, @NotNull MapXNodeImpl xnode) {
        super(parsingContext, xnode);
    }

    @Override
    public Long getId() {
        if (isMaterialized()) {
            return delegate().getId();
        }
        try {
            return PrismUnmarshaller.getContainerId(xnode(), containerDef());
        } catch (SchemaException e) {
            return delegate().getId();
        }
    }

    @Override
    public boolean isIdOnly() {
        // FIXME: Maybe we can have more effective method without need to parse fully
        return delegate().isIdOnly();
    }

    @Override
    public PrismContainerValue<C> clone() {
        return delegate().clone();
    }

    @Override
    public QName getTypeName() {
        return complexTypeDefinition.getTypeName();
    }

    @Override
    public PrismContainerValue<C> delegate() {
        return super.materialized();
    }

    @Override
    protected PrismContainerValue<C> materialize(Source<MapXNodeImpl> source) {
        try {
            var ret = ((PrismContextImpl) PrismContext.get()).getPrismUnmarshaller()
                    .parseRealContainerValueFromMap(source.value(), containerDef(), source.parsingContext());
            ret.setParent(parent);
            return ret;
        } catch (SchemaException e) {
            throw new SystemException("Error during lazy parse of PCV", e);
        }
    }

    @Override
    public void setParent(Itemable parent) {
        this.parent = parent;
        if (isMaterialized()) {
            delegate().setParent(parent);
        }
    }



    @Override
    public PrismContainerable<C> getParent() {
        return (PrismContainerable<C>) parent;
    }

    @Override
    public PrismContainerValue<C> applyDefinition(@NotNull ItemDefinition<?> itemDefinition, boolean force) throws SchemaException {
        if (!force && !isMaterialized() && containerDef() == itemDefinition) {
            return this;
        }
        return PrismContainerValueDelegator.super.applyDefinition(itemDefinition);
    }

    @Override
    public String debugDump(int indent) {
        return "";
    }

    private PrismContainerDefinition<C> containerDef() {
        if (parent != null) {
            return (PrismContainerDefinition<C>) parent.getDefinition();
        }
        return null;
    }
}
