package com.evolveum.axiom.lang.antlr;

import org.antlr.v4.runtime.*;

import com.evolveum.axiom.lang.antlr.query.AxiomQueryParser;
import com.evolveum.axiom.lang.antlr.query.AxiomQueryLexer;

import org.antlr.v4.runtime.atn.PredictionMode;
import org.antlr.v4.runtime.misc.ParseCancellationException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AxiomQuerySource {

    private final AxiomQueryParser.RootContext root;
    private final AxiomQueryParser parser;
    private final AxiomQueryErrorStrategy.ErrorTokenContext errorTokenContext;

    public AxiomQuerySource(AxiomQueryParser.RootContext root, AxiomQueryParser parser, AxiomQueryErrorStrategy.ErrorTokenContext errorTokenContext) {
        this.root = root;
        this.parser = parser;
        this.errorTokenContext = errorTokenContext;
    }

    public static AxiomQuerySource from(String query) {
        CodePointCharStream stream = CharStreams.fromString(query);
        AxiomQueryLexer lexer = new AxiomQueryLexer(stream);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        AxiomQueryParser parser = new AxiomQueryParser(tokenStream);
        AxiomQueryErrorStrategy errorStrategy = new AxiomQueryErrorStrategy();
        parser.setErrorHandler(errorStrategy);
        var root = parser.root();
        if (root.filter() == null) {
            throw new IllegalArgumentException("Unable to parse query: " + query);
        }
        // Get all tokens from the token stream
        tokenStream.fill();
        return new AxiomQuerySource(root, parser, errorStrategy.getErrorTokenContext());
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
}
