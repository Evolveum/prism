/*
 * Copyright (c) 2010-2025 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.axiom.lang.antlr;

import org.antlr.v4.runtime.*;

import com.evolveum.axiom.lang.antlr.query.AxiomQueryParser;
import com.evolveum.axiom.lang.antlr.query.AxiomQueryLexer;

import org.antlr.v4.runtime.atn.ATN;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record AxiomQuerySource(@NotNull AxiomQueryParser.RootContext root, @NotNull ATN atn, List<AxiomQueryError> syntaxErrors) {

    public static AxiomQuerySource from(String query) {
        CodePointCharStream stream = CharStreams.fromString(query);

        AxiomQueryLexer lexer = new AxiomQueryLexer(stream);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        AxiomQueryParser parser = new AxiomQueryParser(tokenStream);
        AxiomQueryErrorListener axiomQueryErrorListener = new AxiomQueryErrorListener(parser.getVocabulary());

        lexer.removeErrorListeners();
        parser.removeErrorListeners();
        lexer.addErrorListener(axiomQueryErrorListener);
        parser.addErrorListener(axiomQueryErrorListener);

        // Get all tokens from the token stream
        tokenStream.fill();
        return new AxiomQuerySource(parser.root(), parser.getATN(), axiomQueryErrorListener.getSyntaxErrors());
    }
}
