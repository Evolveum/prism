/*
 * Copyright (C) 2020-2022 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.prism.impl.query.lang;

import static com.evolveum.midpoint.prism.query.PrismQuerySerialization.NotSupportedException;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import javax.xml.namespace.QName;

import com.evolveum.midpoint.prism.ExpressionWrapper;

import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.binding.TypeSafeEnum;
import com.evolveum.midpoint.prism.query.PrismQueryExpressionFactory;

import com.evolveum.midpoint.util.exception.SchemaException;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.jetbrains.annotations.Nullable;

import com.evolveum.axiom.concepts.Builder;
import com.evolveum.axiom.lang.antlr.AxiomStrings;
import com.evolveum.midpoint.prism.PrismNamespaceContext;
import com.evolveum.midpoint.prism.PrismPropertyValue;
import com.evolveum.midpoint.prism.impl.marshaller.ItemPathSerialization;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.prism.path.UniformItemPath;
import com.evolveum.midpoint.prism.query.ObjectFilter;
import com.evolveum.midpoint.prism.query.PrismQuerySerialization;

public class QueryWriter implements Builder<PrismQuerySerialization>, PrismQueryExpressionFactory.ExpressionWriter {

    public static final String SELF_PATH_SYMBOL = ".";

    private static final String MATCHING_RULE_NS = PrismQueryLanguageParserImpl.MATCHING_RULE_NS;

    private static final String BACKTICK = "`";
    private final PrismQuerySerializerImpl.SimpleBuilder target;
    private final PrismQueryExpressionFactory expressionFactory;


    public QueryWriter(PrismQuerySerializerImpl.SimpleBuilder target, PrismQueryExpressionFactory expressionFactory) {
        this.target = target;
        this.expressionFactory = expressionFactory;
    }

    public void writeSelf() {
        target.emitWord(SELF_PATH_SYMBOL);
    }

    void writePath(String string) {
        target.emitSpace();
        target.emit(string);
    }

    public void writePath(ItemPath path) {
        ItemPathSerialization pathSer = ItemPathSerialization.serialize(UniformItemPath.from(path), target.context());
        target.addPrefixes(pathSer.undeclaredPrefixes());
        target.emitSpace();
        target.emit(pathSer.getXPathWithoutDeclarations());
    }

    public void writeMatchingRule(@Nullable QName matchingRule) {
        if (matchingRule == null) {
            return;
        }
        target.emit("[");
        emitQName(matchingRule, MATCHING_RULE_NS);

        target.emit("]");

    }

    public void writeFilterName(QName filter) {
        var alias = lookupAlias(filter);
        target.emitSpace();
        if (alias.isPresent()) {
            target.emit(alias.get());
        } else {
            emitQName(filter, FilterNames.QUERY_NS);
        }
    }

    public void writeFilter(ObjectFilter filter) throws NotSupportedException {
        writeFilter(filter, this);
    }

    public void writeNestedFilter(ObjectFilter condition) throws NotSupportedException {
        startNestedFilter();
        writeFilter(condition);
        endNestedFilter();
    }

    public void writeNegatedFilter(ObjectFilter filter) throws NotSupportedException {
        writeFilter(filter, negated());
    }

    public void writeValues(@Nullable List<? extends PrismPropertyValue<?>> values) {
        Preconditions.checkArgument(values != null && !values.isEmpty(), "Value must be specified");
        writeList(values, this::writeValue);
    }

    public void startNestedFilter() {
        target.emitSpace();
        target.emitSeparator("(");
    }

    public void endNestedFilter() {
        target.emit(")");
    }

    public void writeRawValue(Object rawValue) {
        target.emitSpace();
        if (rawValue instanceof ItemPath) {
            writePath((ItemPath) rawValue);
            return;
        }
        if (rawValue instanceof QName) {
            writeQName((QName) rawValue);
            return;
        }
        if (rawValue instanceof TypeSafeEnum enumValue) {
            writeString(enumValue.value());
            return;
        }
        if (rawValue instanceof Number || rawValue instanceof Boolean) {
            // FIXME: we should have some common serialization utility
            target.emit(rawValue.toString());
            return;
        }
        // FIXME: Common utility should also detect enums, ideally statically (not programmatically)
        writeString(rawValue);
    }

    public void writeRawValues(Collection<?> oids) {
        writeList(oids, this::writeRawValue);
    }

    @Override
    public PrismQuerySerialization build() {
        return target.build();
    }

    private Optional<String> lookupAlias(QName filter) {
        return FilterNames.aliasFor(filter);
    }

    private void emitQName(QName filter, String additionalDefaultNs) {
        String prefix = resolvePrefix(filter, additionalDefaultNs);
        if (!Strings.isNullOrEmpty(prefix)) {
            target.emit(prefix);
            target.emit(":");
        }
        target.emit(filter.getLocalPart());
    }

    private String resolvePrefix(QName name, String additionalDefaultNs) {
        if (Strings.isNullOrEmpty(name.getNamespaceURI()) || Objects.equals(additionalDefaultNs, name.getNamespaceURI())) {
            return PrismNamespaceContext.DEFAULT_PREFIX;
        }
        return target.prefixFor(name.getNamespaceURI(), name.getPrefix());
    }

    private void writeFilter(ObjectFilter filter, QueryWriter output) throws NotSupportedException {
        FilterSerializers.write(filter, output);
    }

    QueryWriter negated() {
        return new Negated(target, expressionFactory);
    }

    private <T> void writeList(Collection<? extends T> values, Consumer<? super T> writer) {
        target.emitSpace();
        if (values.size() == 1) {
            writer.accept(values.iterator().next());
        } else {
            target.emitSeparator("(");
            var valuesIter = values.iterator();
            while (valuesIter.hasNext()) {
                writer.accept(valuesIter.next());
                if (valuesIter.hasNext()) {
                    target.emitSeparator(", ");
                }
            }
            target.emit(")");
        }
    }

    private void writeValue(PrismPropertyValue<?> prismPropertyValue) {
        // Now we emit values
        Object rawValue = prismPropertyValue.getValue();
        //QName typeName = prismPropertyValue.getTypeName();

        writeRawValue(rawValue);
    }

    public void writeExpression(ExpressionWrapper wrapper) throws NotSupportedException {
        if (expressionFactory == null) {
            throw new PrismQuerySerialization.NotSupportedException("Expressions not supported");
        }
        try {
            target.emitSpace();
            expressionFactory.serializeExpression(this, wrapper);
        } catch (SchemaException e) {
            throw new NotSupportedException("External serialization failed.",e);
        }
    }

    private void writeString(Object rawValue) {
        target.emit(AxiomStrings.toSingleQuoted(rawValue.toString()));
    }

    private void writeQName(QName rawValue) {
        emitQName(rawValue, null);
    }

    @Override
    public void writeConst(String name) {
        target.emit("@");
        target.emit(name);
    }

    @Override
    public void writeScript(String language, String script) {
        target.emitSpace();
        if (language != null) {
            target.emit(language);
        }
        if (isSingleLine(script)) {
            target.emit(AxiomStrings.toSingleBacktick(script));
        } else {
            target.emit(AxiomStrings.TRIPLE_BACKTICK);
            target.emit("\n");
            target.emit(script);
            target.emit(AxiomStrings.TRIPLE_BACKTICK);
        }
    }

    private boolean isSingleLine(String script) {
        return !script.contains("\n");
    }

    @Override
    public void writeVariable(ItemPath path) {
        writePath(path);
    }

    class Negated extends QueryWriter {

        public Negated(PrismQuerySerializerImpl.SimpleBuilder target, PrismQueryExpressionFactory expressionFactory) {
            super(target, expressionFactory);
        }

        @Override
        public void writeFilterName(QName filter) {
            target.emitWord("not");
            super.writeFilterName(filter);
        }

        @Override
        public void writeFilter(ObjectFilter filter) throws NotSupportedException {
            super.writeFilter(filter, QueryWriter.this);
        }

        @Override
        QueryWriter negated() {
            return this;
        }

    }

}
