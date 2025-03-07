/*
 * Copyright (C) 2024 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.prism.impl.lazy;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.deleg.PrismContainerValueDelegator;
import com.evolveum.midpoint.prism.equivalence.EquivalenceStrategy;
import com.evolveum.midpoint.prism.impl.PrismContextImpl;
import com.evolveum.midpoint.prism.impl.marshaller.PrismUnmarshaller;
import com.evolveum.midpoint.prism.impl.xnode.MapXNodeImpl;

import com.evolveum.midpoint.prism.lazy.LazyXNodeBasedPrismValue;
import com.evolveum.midpoint.util.exception.SchemaException;

import com.evolveum.midpoint.util.exception.SystemException;

import org.jetbrains.annotations.NotNull;

import javax.xml.namespace.QName;
import java.io.Serializable;
import java.util.Objects;

public class LazyPrismContainerValue<C extends Containerable>
        extends LazyXNodeBasedPrismValue<MapXNodeImpl, PrismContainerValue<C>>
        implements PrismContainerValueDelegator<C> {

    /**
     * Only for manual testing, will be removed later
     */
    @Deprecated
    private static final boolean TRACK_CALLERS = false;

    protected ComplexTypeDefinition complexTypeDefinition = null;

    private Itemable parent;

    public LazyPrismContainerValue(ComplexTypeDefinition ctd, ParsingContext parsingContext, @NotNull MapXNodeImpl xnode) {
        super(parsingContext, xnode);
        this.complexTypeDefinition = ctd;
    }

    public LazyPrismContainerValue(LazyPrismContainerValue<C> other) {
        super(other);
        this.complexTypeDefinition = other.complexTypeDefinition;
        this.parent = other.parent;

    }

    @Override
    public Long getId() {
        if (isMaterialized()) {
            return delegate().getId();
        }
        try {
            return PrismUnmarshaller.getContainerId(Objects.requireNonNull(xnode(), "invalid internal state"),
                    containerDef());
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
        trackCallers();
        try {
            var ret = ((PrismContextImpl) PrismContext.get()).getPrismUnmarshaller()
                    .parseRealContainerValueFromMap(source.value(), containerDef(), source.parsingContext());
            // We set parent, if it was provided before materialization
            ret.setParent(parent);
            if (isImmutable()) {
                // If we were frozen before materialization, we freeze result.
                ret.freeze();
            }

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
    public void accept(Visitor visitor) {
        PrismContainerValueDelegator.super.accept(visitor);
    }

    @Override
    public void acceptParentVisitor(Visitor visitor) {
        PrismContainerValueDelegator.super.acceptParentVisitor(visitor);
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
        // FIXME: maybe this should be illegalState? We should not have lazy without parent.
        return null;
    }

    @Override
    public PrismContainerDefinition<C> getDefinition() {
        return containerDef();
    }

    @Override
    public PrismContainerValue<C> cloneComplex(@NotNull CloneStrategy strategy) {
        if (strategy.isLiteral() && !isMaterialized()) {
            var ret = new LazyPrismContainerValue<>(this);
            if (!ret.isMaterialized()) {
                // During calling constructor, other thread may materialized value during copying of parent value
                // so if copy is already materialized it may share delegate
                // so it is safe only to return unmaterialized copy
                return ret;
            }
        }
        return PrismContainerValueDelegator.super.cloneComplex(strategy);
    }

    @Override
    protected void performFreeze() {
        if (isMaterialized()) {
            // We freeze delegate if it is already materialized
            delegate().freeze();
        }
    }

    @Override
    public boolean equals(PrismValue otherValue, @NotNull EquivalenceStrategy strategy) {
        if (otherValue instanceof LazyPrismContainerValue<?> other && hasSameSource(other)) {
            // If this value and other value are lazy prism containers with same source XNode,
            // they are equals without need to compare rest.
            return true;
        }
        return PrismContainerValueDelegator.super.equals(otherValue, strategy);
    }

    @Override
    public boolean isEmpty() {
        var raw = xnode();
        if (raw != null) {
            // If Value contains only XNode with container id, so it still considered nonEmpty (as per PrismContainerValueImpl.isEmpty)
            return raw.isEmpty(); // isEmpty checks may not need triggering of materialization (maybe container id and incomplete?)

        }
        return PrismContainerValueDelegator.super.isEmpty();
    }

    @Override
    public boolean hasNoItems() {
        var raw = xnode();
        if (raw != null) {
            if (raw.isEmpty()) {
                // Value is empty (no nested xnodes)
                return true;
            }
            // If Value contains only XNode with container id, it has no items,
            // otherwise it has items.
            return raw.size() == 1 && raw.containsKey(PrismConstants.T_ID);
        }
        return PrismContainerValueDelegator.super.isEmpty();
    }

    /**
     * Testing method, controlled by  #TRACK_CALLERS, should be removed soon
     *
     * Method invokes {@link #breakPoint()} method if caller of {@link #materialize(Source)} is not listed
     * as known or valid use case. This is used to find all materialization sources of lazy deserialized
     * containers (metadata)
     */
    @Deprecated
    private void trackCallers() {
        if (!TRACK_CALLERS) {
            return;
        }
        if (callerIsNoneOf(
                // "CryptoUtil", // Visitor, checking if protected values are encrypted
                //"ContainerValueIdGenerator", // Visitor, checking for container value IDs
                "AssignmentProcessor", // Assignment metadata
                "ItemDeltaImpl", // Application of the delta.
                "ShadowUpdater", // Shadow metadata
                //"AbstractMappingImpl", // Mapping provenance comparison
                "InboundProcessor",
                "OperationalDataManager", // Object metadata
                "PrismObjectAsserter" // testing, we should not care about materialization
        )) {
            breakPoint();
        }
    }

    private void breakPoint() {
        parent.getPath();
    }



    private static boolean callerIsNoneOf(String... callers) {
        for (var elem : new Throwable().fillInStackTrace().getStackTrace()) {
            for (var caller : callers) {
                if (elem.toString().contains(caller)) {
                    return false;
                }
            }
        }
        return true;
    }
}
