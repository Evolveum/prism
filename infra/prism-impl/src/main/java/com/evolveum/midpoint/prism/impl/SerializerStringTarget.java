/*
 * Copyright (c) 2010-2018 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.prism.impl;

import com.evolveum.midpoint.prism.SerializationContext;
import com.evolveum.midpoint.prism.impl.lex.LexicalProcessor;
import com.evolveum.midpoint.prism.impl.xnode.RootXNodeImpl;
import com.evolveum.midpoint.prism.impl.xnode.XNodeImpl;
import com.evolveum.midpoint.util.exception.SchemaException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SerializerStringTarget extends SerializerTarget<String> {

    @NotNull private final String language;

    SerializerStringTarget(@NotNull PrismContextImpl prismContext, @NotNull String language) {
        super(prismContext);
        this.language = language;
    }

    @NotNull
    @Override
    public String write(@NotNull RootXNodeImpl xroot, SerializationContext context) throws SchemaException {
        LexicalProcessor<String> lexicalProcessor = prismContext.getLexicalProcessorRegistry().processorFor(language);
        return lexicalProcessor.write(xroot, context);
    }

    @Override
    public @NotNull String write(@NotNull XNodeImpl xNode, SerializationContext context) throws SchemaException {
        LexicalProcessor<String> lexicalProcessor = prismContext.getLexicalProcessorRegistry().processorFor(language);
        return lexicalProcessor.write(xNode, context);
    }

    @NotNull
    @Override
    public String write(@NotNull List<RootXNodeImpl> roots, @Nullable SerializationContext context)
            throws SchemaException {
        LexicalProcessor<String> lexicalProcessor = prismContext.getLexicalProcessorRegistry().processorFor(language);
        return lexicalProcessor.write(roots, context);
    }
}
