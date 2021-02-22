/*
e * Copyright (C) 2020 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.axiom.api.stream;

import java.util.Optional;
import java.util.function.Supplier;

import com.evolveum.axiom.api.AxiomName;
import com.evolveum.axiom.api.AxiomItem;
import com.evolveum.axiom.api.AxiomItemBuilder;
import com.evolveum.axiom.api.AxiomValue;
import com.evolveum.axiom.api.AxiomValueBuilder;
import com.evolveum.axiom.api.schema.AxiomItemDefinition;
import com.evolveum.axiom.api.schema.AxiomSchemaContext;
import com.evolveum.axiom.api.schema.AxiomTypeDefinition;
import com.evolveum.axiom.concepts.Lazy;
import com.evolveum.axiom.lang.api.AxiomBuiltIn;
import com.evolveum.axiom.lang.spi.AxiomNameResolver;
import com.evolveum.axiom.lang.spi.AxiomSemanticException;
import com.evolveum.concepts.SourceLocation;
import com.google.common.base.Preconditions;

public class AxiomItemTarget extends AxiomBuilderStreamTarget implements Supplier<AxiomItem<?>>, AxiomItemStream.TargetWithContext {

    private final AxiomSchemaContext context;
    private final AxiomTypeDefinition infraType;
    private Item<?> result;

    public AxiomItemTarget(AxiomSchemaContext context) {
        offer(new Root());
        this.context = context;
        infraType = context.valueInfraType();
    }

    @Override
    public AxiomItem<?> get() {
        return result.get();
    }

    private final class Root implements ValueBuilder {

        @Override
        public AxiomName name() {
            return AxiomName.axiom("AbstractRoot");
        }

        @Override
        public Optional<AxiomItemDefinition> childItemDef(AxiomName statement) {
            return context.getRoot(statement);
        }

        @Override
        public Optional<AxiomItemDefinition> infraItemDef(AxiomName item) {
            return infraType.itemDefinition(item);
        }

        @Override
        public ItemBuilder startItem(AxiomName name, SourceLocation loc) {
            result = new Item<>(childItemDef(name).get());
            return result;
        }

        @Override
        public void endValue(SourceLocation loc) {

        }

        @Override
        public AxiomTypeDefinition currentInfra() {
            return infraType;
        }

        @Override
        public AxiomTypeDefinition currentType() {
            return VirtualRootType.from(context);
        }


        @Override
        public ItemBuilder startInfra(AxiomName name, SourceLocation loc) {
            // TODO Auto-generated method stub
            return null;
        }

    }

    private class Item<V> implements ItemBuilder, Supplier<AxiomItem<V>> {

        protected final AxiomItemBuilder<V> builder;

        public Item(AxiomItemDefinition definition) {
            this.builder = new AxiomItemBuilder<>(definition);
        }

        @Override
        public AxiomName name() {
            return builder.definition().name();
        }

        protected Value<V> onlyValue() {
            return (Value<V>) builder.onlyValue();
        }

        @Override
        public Value<V> startValue(Object value, SourceLocation loc) {
            Value<V> newValue = new Value<>((V) value, builder.definition().typeDefinition());
            addValue(newValue);
            return newValue;
        }

        protected void addValue(Value<V> value) {
            builder.addValue(value);
        }

        @Override
        public void endNode(SourceLocation loc) {
            // Noop for now
        }

        @Override
        public AxiomItem<V> get() {
            return builder.get();
        }

        @Override
        public AxiomTypeDefinition currentInfra() {
            return infraType;
        }

        @Override
        public AxiomTypeDefinition currentType() {
            return builder.definition().typeDefinition();
        }

    }

    private final class SubstitutionItem<V> extends Item<V> {

        private Lazy<? extends Item<V>> target;

        public SubstitutionItem(AxiomItemDefinition definition, Lazy<? extends Item<V>> target) {
            super(definition);
            this.target = target;
        }



        @Override
        public Value<V> startValue(Object value, SourceLocation loc) {
            Value<V> valueCtx = super.startValue(value, loc);
            target.get().addValue(valueCtx);
            return valueCtx;
        }

    }

    private final class ValueItem<V> implements ItemBuilder, Supplier<AxiomItem<V>> {

        private Value<V> value;

        public ValueItem(Value<V> value) {
            this.value = value;
        }

        @Override
        public AxiomName name() {
            return AxiomValue.VALUE;
        }

        @Override
        public ValueBuilder startValue(Object value, SourceLocation loc) {
            this.value.setValue((V) value);
            return this.value;
        }


        @Override
        public void endNode(SourceLocation loc) {
            // Noop for now
        }

        @Override
        public AxiomTypeDefinition currentInfra() {
            return infraType;
        }

        @Override
        public AxiomTypeDefinition currentType() {
            return value.currentType();
        }

        @Override
        public AxiomItem<V> get() {
            throw new UnsupportedOperationException("Should not be called");
        }
    }

    private final class TypeItem extends Item<AxiomName> {

        private Value<?> value;

        public TypeItem(AxiomItemDefinition definition) {
            super(definition);
        }

        public TypeItem(Value<?> value, AxiomItemDefinition definition) {
            super(definition);
            this.value = value;
        }

        @Override
        public void endNode(SourceLocation loc) {
            AxiomName typeName = (AxiomName) onlyValue().get().asComplex().get().item(AxiomTypeDefinition.NAME).get().onlyValue().value();
            Optional<AxiomTypeDefinition> typeDef = context.getType(typeName);
            AxiomSemanticException.check(typeDef.isPresent(), loc, "% type is not defined.", typeName);
            this.value.setType(typeDef.get(),loc);
            super.endNode(loc);
        }
    }


    private final class Value<V> implements ValueBuilder, Supplier<AxiomValue<V>> {

        private final AxiomValueBuilder<V> builder;
        private AxiomTypeDefinition type;

        public Value(V value, AxiomTypeDefinition type) {
            this.type = type;
            builder = AxiomValueBuilder.from(type);
            if(value != null) {
                setValue(value);
            }

        }

        public void setType(AxiomTypeDefinition type, SourceLocation start) {
            AxiomSemanticException.check(type.isSubtypeOf(this.type), start, "%s is not subtype of %s", type.name(), this.type.name());
            this.type = type;
            builder.setType(type);
        }

        void setValue(V value) {
            if(type.argument().isPresent()) {
                startItem(type.argument().get().name(), null).startValue(value, null);
            } else {
                builder.setValue(value);
            }

        }

        @Override
        public AxiomName name() {
            return builder.type().name();
        }

        @Override
        public Optional<AxiomItemDefinition> childItemDef(AxiomName statement) {
            return builder.type().itemDefinition(statement);
        }

        @Override
        public Optional<AxiomItemDefinition> infraItemDef(AxiomName item) {
            return infraType.itemDefinition(item);
        }

        @Override
        public AxiomTypeDefinition currentInfra() {
            return infraType;
        }

        @Override
        public AxiomTypeDefinition currentType() {
            return type;
        }

        @Override
        public Item<?> startItem(AxiomName name, SourceLocation loc) {
            @SuppressWarnings({ "unchecked", "rawtypes" })
            Object itemImpl = builder.get(name, (id) -> {
                AxiomItemDefinition childDef = childItemDef(name).get();
                if(childDef.substitutionOf().isPresent()) {
                    Lazy<Item<?>> targetItem = Lazy.from(() -> startItem(childDef.substitutionOf().get(), loc));
                    // Because of JAVA 8 can not infer types correctly
                    return new SubstitutionItem(childDef, targetItem);
                }
                return new Item<>(childItemDef(name).get());
            });
            return (Item<?>) (itemImpl);
        }

        @Override
        public void endValue(SourceLocation loc) {
            // Noop for now
        }

        @Override
        public AxiomValue<V> get() {
            return builder.get();
        }

        @Override
        public ItemBuilder startInfra(AxiomName name, SourceLocation loc) {
            if(AxiomValue.VALUE.equals(name)) {
                return new ValueItem(this);
            } else if (AxiomValue.TYPE.equals(name)) {
                return new TypeItem(this, infraItemDef(name).get());
            }
            Supplier<? extends AxiomItem<?>> itemImpl = builder.getInfra(name, (id) -> {
                return new Item<>(infraItemDef(name).get());
            });
            return (Item) itemImpl;
        }

    }

}
