package com.evolveum.axiom.lang.antlr;

import org.antlr.v4.runtime.*;

import com.evolveum.axiom.lang.antlr.query.AxiomQueryParser;
import com.evolveum.axiom.lang.antlr.query.AxiomQueryLexer;

import java.util.List;

public class AxiomQuerySource {

    private final AxiomQueryParser.RootContext root;
    private final List<AxiomQueryError> syntaxErrorList;

    public AxiomQuerySource(AxiomQueryParser.RootContext root, List<AxiomQueryError> syntaxErrorList) {
        this.root = root;
        this.syntaxErrorList = syntaxErrorList;
    }

    public static final AxiomQuerySource from(String query) {
        CodePointCharStream stream = CharStreams.fromString(query);
        AxiomQueryLexer lexer = new AxiomQueryLexer(stream);
        AxiomQueryParser parser = new AxiomQueryParser(new CommonTokenStream(lexer));
        AxiomQuerySyntaxErrorListener axiomQuerySyntaxErrorListener = new AxiomQuerySyntaxErrorListener();
        // DO NOT log to STDIN
        lexer.removeErrorListeners();
        lexer.addErrorListener(axiomQuerySyntaxErrorListener);
        parser.removeErrorListeners();
        parser.addErrorListener(axiomQuerySyntaxErrorListener);

        var root = parser.root();
        if (root.filter() == null) {
            throw new IllegalArgumentException("Unable to parse query: " + query);
        }

        return new AxiomQuerySource(root, axiomQuerySyntaxErrorListener.errorList);
    }

    public AxiomQueryParser.RootContext root() {
        return root;
    }

    public List<AxiomQueryError> getSyntaxError() {
        return syntaxErrorList;
    }
}
