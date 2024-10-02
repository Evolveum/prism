package com.evolveum.axiom.lang.antlr;

import org.antlr.v4.runtime.*;

import com.evolveum.axiom.lang.antlr.query.AxiomQueryParser;
import com.evolveum.axiom.lang.antlr.query.AxiomQueryLexer;

import org.antlr.v4.runtime.atn.ATN;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 *
 * @param root
 * @param atn
 */
public record AxiomQuerySource(@NotNull AxiomQueryParser.RootContext root, @NotNull ATN atn, List<AxiomQueryError> syntaxErrors) {

    public static AxiomQuerySource from(String query) {
        CodePointCharStream stream = CharStreams.fromString(query);
        AxiomQueryErrorListener axiomQueryErrorListener = new AxiomQueryErrorListener();

        AxiomQueryLexer lexer = new AxiomQueryLexer(stream);
        lexer.removeErrorListeners();
        lexer.addErrorListener(axiomQueryErrorListener);

        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        AxiomQueryParser parser = new AxiomQueryParser(tokenStream);
        parser.removeErrorListeners();
        parser.addErrorListener(axiomQueryErrorListener);

        // Get all tokens from the token stream
        tokenStream.fill();
        return new AxiomQuerySource(parser.root(), parser.getATN(), axiomQueryErrorListener.getSyntaxErrors());
    }
}
