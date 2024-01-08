package com.evolveum.axiom.lang.antlr;

import org.antlr.v4.runtime.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dominik.
 */
public class AxiomQuerySyntaxErrorListener extends BaseErrorListener {
    public List<AxiomQueryError> errorList = new ArrayList<>();

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
            int line, int charPositionInLine,
            String msg, RecognitionException e)
    {
        errorList.add(new AxiomQueryError(recognizer, offendingSymbol, line, charPositionInLine, 0, msg, e));
    }
}
