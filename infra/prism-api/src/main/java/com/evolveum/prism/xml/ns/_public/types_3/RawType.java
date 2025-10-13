/*
 * Copyright (C) 2010-2021 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.prism.xml.ns._public.types_3;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.function.Supplier;
import javax.xml.namespace.QName;

import com.evolveum.midpoint.util.MiscUtil;
import com.evolveum.midpoint.util.QNameUtil;

import jakarta.xml.bind.annotation.XmlType;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.binding.PlainStructured;
import com.evolveum.midpoint.prism.binding.StructuredEqualsStrategy;
import com.evolveum.midpoint.prism.binding.StructuredHashCodeStrategy;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.prism.polystring.PolyString;
import com.evolveum.midpoint.prism.xml.XsdTypeMapper;
import com.evolveum.midpoint.prism.xnode.PrimitiveXNode;
import com.evolveum.midpoint.prism.xnode.RootXNode;
import com.evolveum.midpoint.prism.xnode.XNode;
import com.evolveum.midpoint.util.ShortDumpable;
import com.evolveum.midpoint.util.annotation.Experimental;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.util.exception.SystemException;
import com.evolveum.midpoint.util.exception.TunnelException;

/**
 * A class used to hold raw XNodes until the definition for such an object is known.
 *
 * This class should be thread-safe because it is used in shared objects, like cached resources or roles. (See MID-6506.)
 * But by default it is not, as it contains internal state (xnode/parsed) that can lead to race conditions when parsing.
 *
 * In midPoint 4.2 it was made roughly thread-safe by including explicit synchronization at appropriate places.
 * See MID-6542.
 *
 * In midPoint 4.3 following changes were made to address following issues:
 *
 * 1. We need to support freezing the content (embedded xnode/prism value)
 * XNodes are freezable, and RawType requires frozen XNode.
 *
 * 2. We should consider avoiding explicit synchronization for performance reasons
 *
 * Internal structure is State class
 *
 * - State `Parsed` (noop for subsequent parsing calls)
 * - State `Raw` (parsing results in transition to state Parsed)
 * - TODO what about `Transient`?
 *
 * Implementation has stable Equals, but hashcode is unstable since it would require
 * significant effort to unify XNode and parsed items hashcode computation.
 */
@XmlType(name = "RawType")
public class RawType implements PlainStructured.WithoutStrategy, JaxbVisitable, Revivable, ShortDumpable {

    @Serial private static final long serialVersionUID = 4430291958902286779L;

    /**
     * State wrapper class captures if we have xnode or parsed value
     */
    private State state;

    public RawType() {
        this(new Parsed<>(PrismContext.get().itemFactory().createValue(null), null));
    }

    private RawType(State state) {
        Validate.notNull(state, "State is not set - perhaps a forgotten call to adopt() somewhere?");
        this.state = state;
    }

    public RawType(XNode node) {
        this(new Raw(node));
    }

    public RawType(PrismValue parsed, QName explicitTypeName) {
        this(new Parsed<>(parsed, explicitTypeName));
    }

    public static RawType fromPropertyRealValue(Object realValue, QName explicitTypeName) {
        return new RawType(PrismContext.get().itemFactory().createPropertyValue(realValue), explicitTypeName);
    }

    /**
     * Extracts a "real value" from a potential {@link RawType} object without expecting any specific type beforehand.
     * (Useful e.g. for determining value of xsd:anyType XML property.)
     */
    public static Object getValue(Object value) throws SchemaException {
        if (value instanceof RawType) {
            return ((RawType) value).getValue();
        } else {
            return value;
        }
    }

    public Object getValue() throws SchemaException {
        return getValue(false);
    }

    /**
     * Extracts a "real value" from {@link RawType} object without expecting any specific type beforehand.
     * If no explicit type is present, assumes `xsd:string` (and fails if the content is structured).
     */
    public synchronized Object getValue(boolean store) throws SchemaException {
        Parsed<?> parsed = current().parse();
        if (store) {
            transition(parsed);
        }
        try {
            return parsed.realValue();
        } catch (Throwable t) {
            // It may happen that the real value cannot be obtained, e.g. when the content is a PCV without
            // compile-time class. So we just return "this" as a real value.
            //
            // TODO implement this more seriously - for example, we may need something like Containerable
            //  for PCVs that are not statically (compile-time) representable.
            return this;
        }
    }

    /**
     * TEMPORARY. EXPERIMENTAL. DO NOT USE.
     */
    @Experimental
    public synchronized String extractString() {
        return current().extractString(() -> toString());
    }

    public synchronized String extractString(String defaultValue) {
        return current().extractString(() -> defaultValue);
    }

    @Override
    public void revive(PrismContext prismContext) {
        Validate.notNull(prismContext);
        current().revive(prismContext);
    }

    //region General getters/setters

    public XNode getXnode() {
        return current().xNodeNullable();
    }

    @NotNull
    public RootXNode getRootXNode(@NotNull QName itemName) {
        return PrismContext.get().xnodeFactory().root(itemName, getXnode());

    }

    // experimental
    public QName getExplicitTypeName() {
        return current().explicitTypeName();
    }
    //endregion

    //region Parsing and serialization
    // itemDefinition may be null; in that case we do the best what we can
    public <IV extends PrismValue> IV getParsedValue(@Nullable ItemDefinition<?> itemDefinition, @Nullable QName itemName) throws SchemaException {
        return this.<IV>parse(itemDefinition, itemName).value();
    }

    private <V extends PrismValue> Parsed<V> parse(@Nullable ItemDefinition<?> itemDefinition, @Nullable QName itemName) throws SchemaException {
        return transition(current().parse(itemDefinition, itemName));
    }

    private <V extends PrismValue> Parsed<V> transition(Parsed<V> newState) {
        if (!newState.isTransient()) {
            this.state = newState;
        }
        return newState;
    }

    State current() {
        return state;
    }

    public <V> V getParsedRealValue(ItemDefinition<?> itemDefinition, ItemPath itemPath) throws SchemaException {
        var current = current();
        var parsed = current.asParsed();
        if (current instanceof Parsed) {
            return parsed.realValue();
        }
        var raw = (Raw) current;
        if (itemDefinition == null) {
            // TODO what will be the result without definition?
            return PrismContext.get().parserFor(raw.xNodeNullable().toRootXNode()).parseRealValue();
        }
        return parse(itemDefinition, itemPath.lastName()).realValue();
    }

    public PrismValue getAlreadyParsedValue() {
        return current().parsedValueNullable();
    }

    public <T> T getParsedRealValue(@NotNull Class<T> clazz) throws SchemaException {
        return current().realValue(clazz);
    }

    public <IV extends PrismValue, ID extends ItemDefinition<?>> Item<IV, ID> getParsedItem(ID itemDefinition) throws SchemaException {
        Validate.notNull(itemDefinition);
        return getParsedItem(itemDefinition, itemDefinition.getItemName());
    }

    public <IV extends PrismValue, ID extends ItemDefinition<?>> Item<IV, ID> getParsedItem(ID itemDefinition, QName itemName) throws SchemaException {
        Validate.notNull(itemDefinition);
        Validate.notNull(itemName);
        @SuppressWarnings("unchecked")
        Item<IV, ID> item = (Item<IV, ID>) itemDefinition.instantiate();
        IV newValue = getParsedValue(itemDefinition, itemName);
        if (newValue != null) {
            // TODO: Is clone necessary?
            //noinspection unchecked
            item.add((IV) newValue.clone());
        }
        return item;
    }

    /**
     * This method always returns a mutable XNode. For example, the serializer sometimes needs to set
     * type QName on the returned XNode. (The cloning might be an overkill, harming the performance.
     * This will be resolved later, if needed.)
     */
    public synchronized XNode serializeToXNode() throws SchemaException {
        return serializeToXNode(null);
    }

    public synchronized XNode serializeToXNode(SerializationContext sc) throws SchemaException {
        XNode xNode = current().toXNode(sc);
        return xNode.isImmutable() ? xNode.clone() : xNode;
    }
    //endregion

    //region Cloning, comparing, dumping
    @Override
    public synchronized RawType clone() {
        // FIXME:  MID-6833 RawType is semi-immutable, we can reuse raw state class
        // Currently we can not reuse parsed, since lot of code assumes
        // clone contract returns mutable (unfrozen) copy.
        return state.performClone();
    }

    @Override
    public int hashCode(StructuredHashCodeStrategy strategy) {
        return hashCode();
    }

    @Override
    public synchronized int hashCode() {
        return current().hashCode();
    }

    @Override
    public synchronized boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        RawType other = (RawType) obj;
        return current().equals(other.current());
    }

    @Override
    public boolean equals(Object that, StructuredEqualsStrategy equalsStrategy) {
        return equals(that);
    }

    public static RawType create(String value) {
        var xnode = PrismContext.get().xnodeFactory().primitive(value).frozen();
        return new RawType(xnode);
    }

    public static RawType create(XNode node) {
        return new RawType(node);
    }

    @Override
    public synchronized String toString() {
        return "RawType: " + current().toString() + ")";
    }

    @Override
    public synchronized void shortDump(StringBuilder sb) {
        current().shortDump(sb);
    }

    public boolean isParsed() {
        return current().isParsed();
    }

    // avoid if possible
    public synchronized String guessFormattedValue() throws SchemaException {
        var current = current();
        if (current instanceof Parsed) {
            return ((Parsed<?>) current).realValue().toString();
        }
        if (current instanceof Raw) {
            var node = current.toXNode();
            if (node instanceof PrimitiveXNode) {
                return ((PrimitiveXNode<?>) node).getGuessedFormattedValue();
            }
        }
        return null;
    }

    @Override
    public void accept(JaxbVisitor visitor) {
        Object value;

        if (isParsed() || getExplicitTypeName() != null) {
            // (Potentially) parsing the value before visiting it.
            try {
                value = getValue(true);
            } catch (SchemaException e) {
                throw new TunnelException(e);
            }
        } else {
            value = null;
        }
        visitor.visit(this);

        if (value instanceof JaxbVisitable && !(value instanceof RawType)) {
            // The value can be RawType if the "real value" couldn't be obtained during parsing the value.
            // Fortunately, the value is most probably already parsed, so the visitor already had the chance to act upon it
            // during the above "visitor.visit" call (even if it has no "real value" to act on).
            ((JaxbVisitable) value).accept(visitor);
        }
    }

    /**
     * Sets the new raw content, defined by an XNode. The value must be immutable.
     * Experimental. Use with the greatest care.
     */
    @Experimental
    public void setRawValue(XNode replacement) {
        this.state = new Raw(replacement);
    }
    //endregion

    private static abstract class State implements Serializable {

        private static final long serialVersionUID = 1L;

        protected abstract RawType performClone();

        boolean isTransient() {
            return false;
        }

        protected abstract <T> T realValue(@NotNull Class<T> clazz) throws SchemaException;

        protected abstract boolean isParsed();

        protected abstract void shortDump(StringBuilder sb);

        protected abstract QName explicitTypeName();

        protected abstract PrismValue parsedValueNullable();

        protected abstract Parsed<?> asParsed();

        protected void revive(PrismContext prismContext) {
            // NOOP
        }

        abstract <IV extends PrismValue> Parsed<IV> parse(@Nullable ItemDefinition<?> itemDefinition, @Nullable QName itemName) throws SchemaException;

        XNode xNodeNullable() {
            return null;
        }

        abstract XNode toXNode() throws SchemaException;

        abstract XNode toXNode(SerializationContext sc) throws SchemaException;

        protected abstract Parsed<PrismValue> parse() throws SchemaException;

        protected abstract String extractString(Supplier<String> defaultValue);

        protected void toStringExplicitType(StringBuilder sb) {
            if (explicitTypeDeclaration()) {
                sb.append(":");
                if (explicitTypeName() == null) {
                    sb.append("null");
                } else {
                    sb.append(explicitTypeName().getLocalPart());
                }
            }
        }

        protected abstract boolean explicitTypeDeclaration();

        @Override
        public abstract String toString();

        @Override
        public abstract boolean equals(Object other);

        boolean equalsXNode(State other) {
            try {
                return Objects.equals(toXNode(), other.toXNode());
            } catch (SchemaException e) {
                // or should we silently return false?
                throw new SystemException("Couldn't serialize RawType to XNode when comparing them", e);
            }
        }
    }

    private static class Parsed<V extends PrismValue> extends State {

        private static final long serialVersionUID = 1L;
        private final V value;
        private final QName explicitTypeName;

        public Parsed(V parsed, QName explicitTypeName) {
            this.value = parsed;
            if (explicitTypeName != null) {
                this.explicitTypeName = explicitTypeName;
            } else if (parsed != null && parsed.getTypeName() != null) {
                this.explicitTypeName = parsed.getTypeName();
            } else {
                this.explicitTypeName = null;
            }
        }

        public V value() {
            return value;
        }

        @Override
        protected boolean isParsed() {
            return true;
        }

        @Override
        protected String extractString(Supplier<String> defaultValue) {
            Object object = value.getRealValue();
            return String.valueOf(object);
        }

        @SuppressWarnings("unchecked")
        @Override
        protected Parsed<PrismValue> parse() {
            return (Parsed<PrismValue>) this;
        }

        @SuppressWarnings("unchecked")
        public <T> T realValue() {
            return value.getRealValue();
        }

        @Override
        protected <T> T realValue(@NotNull Class<T> clazz) throws SchemaException {
            Object realValue = realValue();
            if (realValue == null) {
                return null;
            }
            Preconditions.checkArgument(clazz.isInstance(realValue), "Parsed value (%s) is not assignable to %s", realValue.getClass(), clazz);
            return clazz.cast(realValue);
        }

        @Override
        XNode toXNode() throws SchemaException {
            return toXNode(null);
        }

        @Override
        XNode toXNode(SerializationContext sc) throws SchemaException {
            PrismSerializer<RootXNode> serializer = PrismContext.get().xnodeSerializer();
            if (sc != null) {
                serializer = serializer.context(sc);
            }
            XNode rv = serializer.root(new QName("dummy")).serialize(value).getSubnode();
            PrismContext.get().xnodeMutator().setXNodeType(rv, explicitTypeName, explicitTypeDeclaration());
            return rv;
        }

        @Override
        protected Parsed<?> asParsed() {
            return this;
        }

        @Override
        protected PrismValue parsedValueNullable() {
            return value;
        }

        @Override
        protected void shortDump(StringBuilder sb) {
            if (value instanceof ShortDumpable) {
                ((ShortDumpable) value).shortDump(sb);
            } else {
                Object realValue = value.getRealValue();
                if (realValue == null) {
                    sb.append("null");
                } else if (realValue instanceof ShortDumpable) {
                    ((ShortDumpable) realValue).shortDump(sb);
                } else {
                    sb.append(realValue);
                }
            }
        }

        @Override
        protected boolean explicitTypeDeclaration() {
            return explicitTypeName() != null;
        }

        @Override
        protected QName explicitTypeName() {
            return explicitTypeName;
        }

        @Override
        protected void revive(PrismContext prismContext) {
            value.revive(prismContext);
        }

        @SuppressWarnings("unchecked")
        @Override
        <IV extends PrismValue> Parsed<IV> parse(@Nullable ItemDefinition<?> itemDefinition, @Nullable QName itemName) {
            // No need to reparse
            return (Parsed<IV>) this;
        }

        @Override
        public boolean equals(Object other) {
            if (other == this) {
                return true;
            }
            if (other instanceof Parsed) {
                return Objects.equals(value, ((Parsed<?>) other).value());
            }
            if (other instanceof State) {
                return equalsXNode((State) other);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return value.hashCode();
        }

        @Override
        public String toString() {
            var sb = new StringBuilder("(parsed");
            toStringExplicitType(sb);
            return sb.append("): ").append(value).toString();
        }

        @Override
        protected RawType performClone() {
            // FIXME: MID-6833 We clone value since Midpoint assumes clone to mutable contract
            return new RawType(new Parsed<>(value.clone(), explicitTypeName));
        }
    }

    private static class Transient<V extends PrismValue> extends Parsed<V> {

        @Serial private static final long serialVersionUID = 1L;

        public Transient(V parsed) {
            super(parsed, null);
        }

        @Override
        boolean isTransient() {
            return true;
        }
    }

    private static class Raw extends State {

        private static final long serialVersionUID = 1L;

        private final XNode node;

        public Raw(XNode node) {
            Preconditions.checkArgument(node.isImmutable(), "Supplied XNode must be immutable");
            this.node = node;
        }

        @Override
        protected boolean isParsed() {
            return false;
        }

        @Override
        protected String extractString(Supplier<String> defaultValue) {
            if (node instanceof PrimitiveXNode) {
                return ((PrimitiveXNode<?>) node).getStringValue();
            }
            return defaultValue.get();
        }

        @Override
        protected PrismValue parsedValueNullable() {
            return null;
        }

        @Override
        protected Parsed<PrismValue> parse() throws SchemaException {
            if (node.getTypeQName() != null) {
                TypeDefinition typeDefinition = PrismContext.get().getSchemaRegistry().findTypeDefinitionByType(node.getTypeQName());
                Class<?> javaClass = null;
                if (typeDefinition != null && typeDefinition.getCompileTimeClass() != null) {
                    javaClass = typeDefinition.getCompileTimeClass();
                }
                if (javaClass != null) {
                    javaClass = XsdTypeMapper.getXsdToJavaMapping(node.getTypeQName());
                }
                if (javaClass != null) {
                    return new Parsed<>(valueFor(realValue(javaClass)), node.getTypeQName());
                }
                PrismValue asValue = PrismContext.get().parserFor(node.toRootXNode()).parseItemValue();
                return new Parsed<>(asValue, node.getTypeQName());
            }

            // unknown or null type -- try parsing as string
            if (!(node instanceof PrimitiveXNode<?>)) {
                throw new SchemaException("Trying to parse non-primitive XNode as type '" + node.getTypeQName() + "'");
            }
            String stringValue = ((PrimitiveXNode<?>) node).getStringValue();

            // We return transient, so state is not updated.
            return new Transient<>(valueFor(stringValue));
        }

        @Override
        protected <T> T realValue(Class<T> javaClass) throws SchemaException {
            return PrismContext.get().parserFor(node.toRootXNode()).parseRealValue(javaClass);
        }

        @Override
        <IV extends PrismValue> Parsed<IV> parse(@Nullable ItemDefinition<?> itemDefinition, @Nullable QName itemName) throws SchemaException {
            IV value;
            if (itemDefinition != null && !(itemDefinition instanceof PrismPropertyDefinition && ((PrismPropertyDefinition<?>) itemDefinition).isAnyType())) {
                if (itemName == null) {
                    itemName = itemDefinition.getItemName();
                }
                var rootNode = PrismContext.get().xnodeFactory().root(itemName, node);
                Item<IV, ItemDefinition<?>> subItem =
                        PrismContext.get().parserFor(rootNode).name(itemName).definition(itemDefinition).parseItem();
                if (!subItem.isEmpty()) {
                    value = subItem.getAnyValue();
                } else {
                    value = null;
                }
                checkDefinitionMatches(value, itemDefinition);
                return new Parsed<>(value, itemDefinition.getTypeName());
            }
            // we don't really want to set 'parsed', as we didn't perform real parsing
            @SuppressWarnings("unchecked")
            Parsed<IV> ret = (Parsed<IV>) new Transient<>(PrismContext.get().itemFactory().createPropertyValue(node));
            return ret;
        }

        /**
         * Checks whether the value matches the type of the definition. Actually, it should be so, because the value
         * was created by parsing according to that definition! But let's check to be sure.
         *
         * Note that this code was originally in "canBeDefinitionOf" pair of methods in {@link ItemDefinition}
         * and its subtypes. Probably too complex for this simple use.
         * */
        private static <IV extends PrismValue> void checkDefinitionMatches(
                @Nullable PrismValue value, @NotNull ItemDefinition<?> itemDefinition)
                throws SchemaException {
            var parent = value != null ? value.getParent() : null;
            var definition = parent != null ? parent.getDefinition() : null;
            MiscUtil.schemaCheck(
                    definition == null || QNameUtil.match(definition.getTypeName(), itemDefinition.getTypeName()),
                    "Attempt to parse raw value into %s that does not match provided definition %s",
                    value, itemDefinition);
        }

        @Override
        XNode toXNode() {
            return node;
        }

        @Override
        XNode toXNode(SerializationContext sc) {
            return node;
        }

        @Override
        XNode xNodeNullable() {
            return node;
        }

        @Override
        protected Parsed<?> asParsed() {
            return null;
        }

        @Override
        protected void shortDump(StringBuilder sb) {
            sb.append("(raw").append("):").append(node);
        }

        @Override
        protected QName explicitTypeName() {
            return node.getTypeQName();
        }

        @Override
        protected void revive(PrismContext prismContext) {
            super.revive(prismContext);
        }

        @Override
        public String toString() {
            var sb = new StringBuilder("(raw");
            toStringExplicitType(sb);
            return sb.append("): ").append(node).toString();
        }

        @Override
        protected boolean explicitTypeDeclaration() {
            return node.isExplicitTypeDeclaration();
        }

        @Override
        public boolean equals(Object other) {
            if (other == this) {
                return true;
            }
            if (other instanceof State) {
                return equalsXNode((State) other);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return node.hashCode();
        }

        private PrismValue valueFor(Object parsedValue) {
            if (parsedValue instanceof Containerable) {
                return ((Containerable) parsedValue).asPrismContainerValue();
            }
            if (parsedValue instanceof Referencable) {
                return ((Referencable) parsedValue).asReferenceValue();
            }
            if (parsedValue instanceof PolyStringType) {
                return PrismContext.get().itemFactory().createPropertyValue(PolyString.toPolyString((PolyStringType) parsedValue));   // hack
            }
            return PrismContext.get().itemFactory().createPropertyValue(parsedValue);
        }

        @Override
        protected RawType performClone() {
            // Raw XNode form is effectively immutable all the time, so we can reuse our state.
            return new RawType(this);
        }
    }
}
