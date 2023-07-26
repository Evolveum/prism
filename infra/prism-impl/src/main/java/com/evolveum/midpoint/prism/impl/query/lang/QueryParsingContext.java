/*
 * Copyright (C) 2020-2023 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.prism.impl.query.lang;

import com.evolveum.axiom.lang.antlr.AxiomQuerySource;
import com.evolveum.axiom.lang.antlr.query.AxiomQueryParser;
import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.prism.query.ObjectFilter;
import com.evolveum.midpoint.prism.query.PreparedPrismQuery;
import com.evolveum.midpoint.util.exception.SchemaException;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import javax.xml.namespace.QName;

import java.util.*;

import static com.evolveum.midpoint.util.MiscUtil.schemaCheck;

class QueryParsingContext {

    private final AxiomQuerySource source;
    private Local rootContext;
    private boolean placeholdersSupported;
    private SortedMap<Placeholder, Placeholder> placeholders = new TreeMap<>();

    public QueryParsingContext(AxiomQuerySource source, ItemDefinition<?> contextDef, ComplexTypeDefinition typeDef, boolean supportsPlaceholders) {
        this.source = source;
        this.rootContext = new Local(contextDef, typeDef);
        this.placeholdersSupported = supportsPlaceholders;
    }

    Local root() {
        return this.rootContext;
    }

    Object createOrResolvePlaceholder(AxiomQueryParser.PlaceholderContext placeholder, PrismPropertyDefinition<?> propDef) throws SchemaException {
        // Here we add / register placeholders

        schemaCheck(placeholdersSupported, "Placeholders are not supported.");

        var lookup = new Placeholder(placeholder, propDef);
        if (placeholders.containsKey(lookup)) {
            // Placeholder was already registered we should try to dereference it?
            return placeholders.get(lookup).value;
        }
        // We register placeholder for further use
        placeholders.put(lookup, lookup);
        return null;
    }

    AxiomQuerySource source() {
        return source;
    }

    boolean hasPlaceholders() {
        return !placeholders.isEmpty();
    }

    PreparedPrismQuery completed(ObjectFilter maybeFilter) {
        return new WithoutPlaceholders(maybeFilter);
    }
    PreparedPrismQuery withPlaceholders(PrismQueryLanguageParserImpl parser) {
        return new WithPlaceholders(parser);
    }

    class Placeholder implements Comparable<Placeholder> {

        private final int line;
        private final int character;
        private final PrismPropertyDefinition<?> def;

        private Object value;

        public Placeholder(AxiomQueryParser.PlaceholderContext placeholder, PrismPropertyDefinition<?> propDef) {
            this.line = placeholder.getStart().getLine();
            this.character = placeholder.getStart().getCharPositionInLine();
            this.def = propDef;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            Placeholder that = (Placeholder) o;
            return line == that.line && character == that.character;
        }

        @Override
        public int hashCode() {
            return Objects.hash(line, character);
        }

        @Override
        public int compareTo(@NotNull QueryParsingContext.Placeholder other) {
            if (line == other.line) {
                return Integer.compare(character, other.character);
            }
            return Integer.compare(line, other.line);
        }
        boolean isBound() {
            return value != null;
        }

        public void bindValue(Object realValue) throws SchemaException {
            // TODO: Should we try to create PrismPropertyValue?
            // TODO: Should we try to parse string to actual type? (if type is lost?)
            schemaCheck(def.getTypeClass().isInstance(realValue), "Binding value must be instance of ", def.getTypeClass().getSimpleName());
            this.value = realValue;
        }
    }

    class Local {

        private ItemDefinition<?> itemDef;
        @Nullable
        private ComplexTypeDefinition typeDef;

        public Local(ItemDefinition<?> itemDef, @Nullable ComplexTypeDefinition typeDef) {
            this.itemDef = itemDef;
            this.typeDef = typeDef;
        }

        public QueryParsingContext root() {
            return QueryParsingContext.this;
        }

        public ItemDefinition<?> itemDef() {
            return itemDef;
        }

        public ComplexTypeDefinition typeDef() {
            return typeDef;
        }

        public ComplexTypeDefinition findComplexTypeDefinitionByType(QName type) {
            return PrismContext.get().getSchemaRegistry().findComplexTypeDefinitionByType(type);
        }

        public PrismContainerDefinition<?> findContainerDefinitionByType(QName type) {
            // TODO: Nested contexts could override container definitions (eg. extensions, archetypes, shadows)
            return PrismContext.get().getSchemaRegistry().findContainerDefinitionByType(type);
        }

        public PrismObjectDefinition<?> findObjectDefinitionByType(QName targetType) {
            // TODO: Nested contexts could override container definitions (eg. extensions, archetypes, shadows)
            return PrismContext.get().getSchemaRegistry().findObjectDefinitionByType(targetType);
        }

        public Local referenced(PrismObjectDefinition<?> targetSchema) {
            return new Local(targetSchema, targetSchema.getComplexTypeDefinition());
        }

        <T extends ItemDefinition<?>> T findDefinition(ItemPath path, Class<T> type)
                throws SchemaException {
            if (path.isEmpty() && type.isInstance(itemDef)) {
                return type.cast(itemDef);
            }
            if (itemDef instanceof PrismReferenceDefinition) {
                return itemDef.findItemDefinition(path, type);
            }

            // OR parent == null is necessary to resolve additional conditions in ownedBy (e.g. name = 'xy')
            schemaCheck(typeDef != null && (itemDef instanceof PrismContainerDefinition || itemDef == null),
                    "Only references and containers are supported");
            return typeDef.findItemDefinition(path, type);
        }

        public void typeDef(ComplexTypeDefinition complexType) {
            this.typeDef = complexType;
        }

        public void itemDef(ItemDefinition<?> def) {
            this.itemDef = def;
        }

        public Local referenced(PrismContainerDefinition<?> itemDef, ComplexTypeDefinition typeDef) {
            return new Local(itemDef, typeDef);
        }

        public QName typeName() {
            return typeDef != null ? typeDef.getTypeName() : itemDef.getTypeName();
        }

        public Local nested(PrismContainerDefinition<?> containerDef) {
            return new Local(containerDef, containerDef.getComplexTypeDefinition());
        }
    }

    static class WithoutPlaceholders implements PreparedPrismQuery {

        private static ObjectFilter filter;

        public WithoutPlaceholders(ObjectFilter maybeFilter) {
            filter = maybeFilter;
        }

        @Override
        public void bindValue(Object realValue) {
            throw new IllegalStateException("No values to bind");
        }

        @Override
        public ObjectFilter toFilter() {
            return filter;
        }

        @Override
        public boolean allPlaceholdersBound() {
            return true;
        }
    }

    class WithPlaceholders implements PreparedPrismQuery {

        private final PrismQueryLanguageParserImpl parser;

        public WithPlaceholders(PrismQueryLanguageParserImpl parser) {
            this.parser = parser;

        }

        @Override
        public void bindValue(Object realValue) throws SchemaException {
            var first = placeholders.entrySet().stream().filter(v -> !v.getValue().isBound()).findFirst();
            if (first.isEmpty()) {
                throw new IllegalStateException("All placeholders are already bound");
            }

            first.get().getValue().bindValue(realValue);
        }

        @Override
        public ObjectFilter toFilter() throws SchemaException {
            schemaCheck(allPlaceholdersBound(), "All placeholders must be bound");
            return parser.parseBound(QueryParsingContext.this);
        }

        @Override
        public boolean allPlaceholdersBound() {
            return placeholders.entrySet().stream().allMatch(v -> v.getValue().isBound());
        }
    }

}
