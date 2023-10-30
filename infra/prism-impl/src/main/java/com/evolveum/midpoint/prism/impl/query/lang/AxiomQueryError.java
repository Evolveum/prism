package com.evolveum.midpoint.prism.impl.query.lang;

import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

/**
 * Created by Dominik.
 */
public class AxiomQueryError {
    private final Recognizer<?, ?> recognizer;
    private final Object offendingSymbol;
    private final int line;
    private final int charPositionInLine;
    private final String message;
    private final RecognitionException e;

    AxiomQueryError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e)
    {
        this.recognizer = recognizer;
        this.offendingSymbol = offendingSymbol;
        this.line = line;
        this.charPositionInLine = charPositionInLine;
        this.message = msg;
        this.e = e;
    }

    public Recognizer<?, ?> getRecognizer()
    {
        return recognizer;
    }

    public Object getOffendingSymbol()
    {
        return offendingSymbol;
    }

    public int getLine()
    {
        return line;
    }

    public int getCharPositionInLine()
    {
        return charPositionInLine;
    }

    public String getMessage()
    {
        return message;
    }

    public RecognitionException getException()
    {
        return e;
    }
}
