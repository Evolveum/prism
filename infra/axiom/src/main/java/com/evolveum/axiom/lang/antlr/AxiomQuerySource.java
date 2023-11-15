package com.evolveum.axiom.lang.antlr;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;

import com.evolveum.axiom.lang.antlr.query.AxiomQueryParser;
import com.evolveum.axiom.lang.antlr.query.AxiomQueryLexer;

import java.util.List;

public class AxiomQuerySource {

    private final AxiomQueryParser.RootContext root;
    private List<AxiomQueryError> syntaxErrorList;

    public AxiomQuerySource(AxiomQueryParser.RootContext root, List<AxiomQueryError> syntaxErrorList) {
        this.root = root;
        this.syntaxErrorList = syntaxErrorList;
    }

    public static final AxiomQuerySource from(String query) {
        CodePointCharStream stream = CharStreams.fromString(query);
        AxiomQueryLexer lexer = new AxiomQueryLexer(stream);
        AxiomQueryParser parser = new AxiomQueryParser(new CommonTokenStream(lexer));
        AxiomQueryErrorListener axiomQueryErrorListener = new AxiomQueryErrorListener();
        // DO NOT log to STDIN
        lexer.removeErrorListeners();
        parser.removeErrorListeners();
        parser.addErrorListener(axiomQueryErrorListener);

        var root = parser.root();
        if (root.filter() == null) {
            throw new IllegalArgumentException("Unable to parse query: " + query);
        }

        return new AxiomQuerySource(root, axiomQueryErrorListener.errorList);
    }

    public AxiomQueryParser.RootContext root() {
        return root;
    }

    public List<AxiomQueryError> getSyntaxError() {
        return syntaxErrorList;
    }
}
