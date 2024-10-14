package com.evolveum.axiom.lang.antlr;

import org.antlr.v4.runtime.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dominik.
 */
public class AxiomQueryErrorListener extends BaseErrorListener {
    private final Vocabulary vocabulary;
    List<AxiomQueryError> syntaxErrors = new ArrayList<>();

    public AxiomQueryErrorListener(Vocabulary vocabulary) {
        this.vocabulary = vocabulary;
    }

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer,
            Object offendingSymbol,
            int line,
            int charPositionInLine,
            String msg,
            RecognitionException e) {

        String errorMessage;

        if (offendingSymbol instanceof Token token) {
            errorMessage = "Syntax error: Unexpected " + getDisplayTokenName(token.getType(), vocabulary) + " '" + token.getText() + "'";
        } else {
            errorMessage = "Syntax error: " + msg;
        }

        syntaxErrors.add(new AxiomQueryError(line, line, charPositionInLine, charPositionInLine, errorMessage));
    }

    public List<AxiomQueryError> getSyntaxErrors() {
        return syntaxErrors;
    }

    private String getDisplayTokenName(int tokenType, Vocabulary vocabulary) {
        String literalName = vocabulary.getLiteralName(tokenType);

        if (literalName != null) {
            return literalName;
        }

        String symbolicName = vocabulary.getSymbolicName(tokenType);

        if (symbolicName != null) {
            return switch (symbolicName) {
                case "SEP" -> "a separator";
                case "SEMICOLON" -> "a semicolon";
                case "IDENTIFIER" -> "an identifier language concept";
                case "AND_KEYWORD" -> "the AND language concept";
                case "OR_KEYWORD" -> "the OR language concept";
                case "NOT_KEYWORD" -> "the NOT language concept";
                case "EOF" -> "end of file";
                default -> symbolicName.toLowerCase();
            };
        }

        return "unknown token";
    }

}
