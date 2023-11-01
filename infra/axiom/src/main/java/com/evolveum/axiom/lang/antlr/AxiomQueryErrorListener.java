package com.evolveum.axiom.lang.antlr;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;

/**
 * Created by Dominik.
 */
public class AxiomQueryErrorListener extends BaseErrorListener {
    public static AxiomQueryErrorListener INSTANCE = new AxiomQueryErrorListener();
    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
            int line, int charPositionInLine,
            String msg, RecognitionException e)
    {
        String sourceName = recognizer.getInputStream().getSourceName();

        if (!sourceName.isEmpty()) {
            sourceName = String.format("%s:%d:%d: ", sourceName, line, charPositionInLine);
        }

        System.err.println(sourceName + "line " + ": " + charPositionInLine + " - " + msg);
    }
}
