package com.evolveum.axiom.lang.antlr;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dominik.
 */
public class AxiomQueryErrorListener extends BaseErrorListener {

    List<AxiomQueryError> syntaxErrors = new ArrayList<>();

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer,
            Object offendingSymbol,
            int line,
            int charPositionInLine,
            String msg,
            RecognitionException e) {
        syntaxErrors.add(new AxiomQueryError(line, line, charPositionInLine, charPositionInLine, msg));
    }

    public List<AxiomQueryError> getSyntaxErrors() {
        return syntaxErrors;
    }
}
