package com.evolveum.axiom.lang.antlr;

import org.antlr.v4.runtime.*;

import com.evolveum.axiom.lang.antlr.query.AxiomQueryParser;
import com.evolveum.axiom.lang.antlr.query.AxiomQueryLexer;

import org.antlr.v4.runtime.atn.PredictionMode;
import org.antlr.v4.runtime.misc.IntervalSet;
import org.antlr.v4.runtime.misc.ParseCancellationException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AxiomQuerySource {

    private final AxiomQueryParser.RootContext root;
    private final AxiomQueryParser parser;
    private final AxiomQueryErrorStrategy.ErrorTokenContext errorTokenContext;
    private final IntervalSet recognitionsSet;

    public AxiomQuerySource(AxiomQueryParser.RootContext root, AxiomQueryParser parser, IntervalSet recognitionsSet, AxiomQueryErrorStrategy.ErrorTokenContext errorTokenContext) {
        this.root = root;
        this.parser = parser;
        this.recognitionsSet = recognitionsSet;
        this.errorTokenContext = errorTokenContext;
    }

    public static AxiomQuerySource from(String query) {
        CodePointCharStream stream = CharStreams.fromString(query);
        AxiomQueryLexer lexer = new AxiomQueryLexer(stream);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        AxiomQueryParser parser = new AxiomQueryParser(tokenStream);
        AxiomQueryErrorStrategy errorStrategy = new AxiomQueryErrorStrategy();
        parser.setErrorHandler(errorStrategy);
        // Get all tokens from the token stream
        tokenStream.fill();
        return new AxiomQuerySource(parser.root(), parser, errorStrategy.recognitionsSet, errorStrategy.getErrorTokenContext());
    }

    public AxiomQueryParser.RootContext root() {
        return root;
    }

    public AxiomQueryParser getParser() {
        return parser;
    }

    public AxiomQueryErrorStrategy.ErrorTokenContext getErrorTokenContextMap() {
        return errorTokenContext;
    }

    public IntervalSet getRecognitionsSet() {
        return recognitionsSet;
    }
}
