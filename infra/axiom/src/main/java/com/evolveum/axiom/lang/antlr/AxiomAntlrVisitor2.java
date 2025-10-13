/*
 * Copyright (c) 2020 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.axiom.lang.antlr;

public class AxiomAntlrVisitor2<T> extends AbstractAxiomAntlrVisitor<T> {

    private final AntlrStreamToItemStream adapter;

    public AxiomAntlrVisitor2(String sourceName, AntlrStreamToItemStream adapter) {
        super(sourceName);
        this.adapter = adapter;
    }

    @Override
    protected AntlrStreamToItemStream delegate() {
        return adapter;
    }
}
