package com.evolveum.axiom.lang.antlr;

import org.antlr.v4.runtime.*;

import com.evolveum.axiom.lang.antlr.query.AxiomQueryParser;
import com.evolveum.axiom.lang.antlr.query.AxiomQueryLexer;

import org.antlr.v4.runtime.atn.PredictionMode;
import org.antlr.v4.runtime.misc.IntervalSet;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AxiomQuerySource {

    private final AxiomQueryParser.RootContext root;
    private final AxiomQueryParser parser;
    private final Map<ParseTree, RecognitionsSet> recognitionsSet;

    public AxiomQuerySource(AxiomQueryParser.RootContext root, AxiomQueryParser parser, Map<ParseTree, RecognitionsSet> recognitionsSet) {
        this.root = root;
        this.parser = parser;
        this.recognitionsSet = recognitionsSet;
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
        return new AxiomQuerySource(parser.root(), parser, errorStrategy.recognitionsSet);
    }

    public AxiomQueryParser.RootContext root() {
        return root;
    }

    public AxiomQueryParser getParser() {
        return parser;
    }

    public Map<ParseTree, RecognitionsSet> getRecognitionsSet() {
        return recognitionsSet;
    }
}
