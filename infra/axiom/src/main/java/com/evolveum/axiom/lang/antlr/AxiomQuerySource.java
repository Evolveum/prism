package com.evolveum.axiom.lang.antlr;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;

import com.evolveum.axiom.lang.antlr.query.AxiomQueryParser;
import com.evolveum.axiom.lang.antlr.query.AxiomQueryLexer;

public class AxiomQuerySource {

    private final AxiomQueryParser.RootContext root;

    public AxiomQuerySource(AxiomQueryParser.RootContext root) {
        this.root = root;
    }

    public static final AxiomQuerySource from(String query) {
        CodePointCharStream stream = CharStreams.fromString(query);
        AxiomQueryLexer lexer = new AxiomQueryLexer(stream);
        AxiomQueryParser parser = new AxiomQueryParser(new CommonTokenStream(lexer));
        // DO NOT log to STDIN
        lexer.removeErrorListeners();
        parser.removeErrorListeners();
        parser.addErrorListener(AxiomQueryErrorListener.INSTANCE);

        var root = parser.root();
        if (root.filter() == null) {
            throw new IllegalArgumentException("Unable to parse query: " + query);
        }
        return new AxiomQuerySource(root);
    }

    public AxiomQueryParser.RootContext root() {
        return root;
    }
}
