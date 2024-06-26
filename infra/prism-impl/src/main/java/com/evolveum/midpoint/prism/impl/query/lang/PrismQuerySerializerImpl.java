/*
 * Copyright (C) 2020-2021 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.prism.impl.query.lang;

import java.util.Map;

import com.evolveum.axiom.concepts.Builder;
import com.evolveum.midpoint.prism.PrismConstants;
import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.PrismNamespaceContext;
import com.evolveum.midpoint.prism.impl.marshaller.QueryConverterImpl;
import com.evolveum.midpoint.prism.query.ObjectFilter;
import com.evolveum.midpoint.prism.query.PrismQueryExpressionFactory;
import com.evolveum.midpoint.prism.query.PrismQuerySerialization;
import com.evolveum.midpoint.prism.query.PrismQuerySerialization.NotSupportedException;
import com.evolveum.midpoint.prism.query.PrismQuerySerializer;
import com.evolveum.prism.xml.ns._public.query_3.SearchFilterType;

public class PrismQuerySerializerImpl implements PrismQuerySerializer {

    private final PrismQueryExpressionFactory expressionFactory;

    public PrismQuerySerializerImpl() {
        this(null);
    }

    public PrismQuerySerializerImpl(PrismQueryExpressionFactory factory) {
        this.expressionFactory = factory;
    }

    @Override
    public PrismQuerySerialization serialize(ObjectFilter filter, PrismNamespaceContext context, boolean useDefaultPrefix)
            throws NotSupportedException {
        QueryWriter output = new QueryWriter(new SimpleBuilder(context), expressionFactory, useDefaultPrefix);
        output.writeFilter(filter);
        return output.build();
    }

    static class Result implements PrismQuerySerialization {

        private final PrismNamespaceContext prefixes;
        private final String query;

        public Result(PrismNamespaceContext prefixes, String query) {
            this.prefixes = prefixes;
            this.query = query;
        }

        @Override
        public String filterText() {
            return query;
        }

        @Override
        public PrismNamespaceContext namespaceContext() {
            return prefixes;
        }

        @Override
        public SearchFilterType toSearchFilterType() {
            return new SearchFilterType(filterText(), namespaceContext());
        }
    }


    static class SimpleBuilder implements Builder<PrismQuerySerialization> {

        private final PrismNamespaceContext.Builder prefixes;
        private final StringBuilder query = new StringBuilder();
        private boolean spaceRequired = false;

        public SimpleBuilder(PrismNamespaceContext context) {
            this.prefixes = context.childBuilder();
        }

        public void emitWord(String pathSelf) {
            emitSpace();
            emit(pathSelf);
        }

        public String filter() {
            return query.toString();
        }

        public PrismNamespaceContext context() {
            return prefixes.build();
        }

        public void addPrefixes(Map<String, String> undeclaredPrefixes) {
            prefixes.addPrefixes(undeclaredPrefixes);
        }

        public void emitSpace() {
            if(spaceRequired) {
                emitSeparator(" ");
            }
        }

        public void emitSeparator(String string) {
            query.append(string);
            spaceRequired = false;
        }

        public void emit(String prefix) {
            query.append(prefix);
            spaceRequired = true;
        }

        public String prefixFor(String namespaceURI, String prefix) {
            return prefixes.assignPrefixFor(namespaceURI, prefix);
        }

        @Override
        public Result build() {
            return new Result(prefixes.build(), query.toString());
        }
    }
}
