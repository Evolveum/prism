/*
 * Copyright (c) 2020 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.prism.impl.lex.json.reader;

import com.evolveum.midpoint.prism.ItemDefinition;
import com.evolveum.midpoint.prism.impl.ParsingContextImpl;
import com.evolveum.midpoint.prism.impl.lex.LexicalProcessor;
import com.evolveum.midpoint.prism.impl.xnode.XNodeDefinition;
import com.evolveum.midpoint.prism.schema.SchemaRegistry;
import com.fasterxml.jackson.core.JsonParser;

import org.jetbrains.annotations.NotNull;

/**
 * TODO
 */
class JsonReadingContext {

    @NotNull final JsonParser parser;
    @NotNull final ParsingContextImpl prismParsingContext;
    @NotNull final LexicalProcessor.RootXNodeHandler objectHandler;
    @NotNull final AbstractReader.YamlTagResolver yamlTagResolver;

    private boolean aborted;
    private final XNodeDefinition.Root rootContext;

    JsonReadingContext(@NotNull JsonParser parser, @NotNull ParsingContextImpl prismParsingContext,
            @NotNull LexicalProcessor.RootXNodeHandler objectHandler, @NotNull AbstractReader.YamlTagResolver yamlTagResolver,
            @NotNull SchemaRegistry schemaRegistry, ItemDefinition<?> initialDefinition) {
        this.parser = parser;
        this.prismParsingContext = prismParsingContext;
        this.objectHandler = objectHandler;
        this.yamlTagResolver = yamlTagResolver;
        this.rootContext = initialDefinition != null ?  XNodeDefinition.rootWithDefinition(schemaRegistry, initialDefinition) : XNodeDefinition.root(schemaRegistry);
    }


    public boolean isAborted() {
        return aborted;
    }

    public void setAborted() {
        this.aborted = true;
    }

    String getPositionSuffix() {
        return String.valueOf(parser.getCurrentLocation());
    }

    @NotNull
    String getPositionSuffixIfPresent() {
        return " At: " + getPositionSuffix();
    }

    public XNodeDefinition.Root rootDefinition() {
        return rootContext;
    }



}
