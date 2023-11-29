package com.evolveum.axiom.lang.antlr;

import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

/**
 * Created by Dominik.
 */
public class AxiomQueryError {
    private final Recognizer<?, ?> recognizer;
    private final Object offendingSymbol;
    private final int line;
    private final int charPositionInLineStart;
    private final int charPositionInLineStop;

    private final String message;
    private final RecognitionException e;

    public AxiomQueryError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLineStart, int charPositionInLineStop, String msg, RecognitionException e)
    {
        this.recognizer = recognizer;
        this.offendingSymbol = offendingSymbol;
        this.line = line;
        this.charPositionInLineStart = charPositionInLineStart;
        this.charPositionInLineStop = charPositionInLineStop;
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
    public int getCharPositionInLineStart()
    {
        return charPositionInLineStart;
    }

    public int getCharPositionInLineStop()
    {
        return charPositionInLineStop;
    }

    public String getMessage()
    {
        return message;
    }

    public RecognitionException getException()
    {
        return e;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof AxiomQueryError)) {
            return false;
        }

        AxiomQueryError axiomQueryError = (AxiomQueryError) obj;

        if (axiomQueryError.recognizer == recognizer &&
                axiomQueryError.offendingSymbol == offendingSymbol &&
                axiomQueryError.line == line &&
                axiomQueryError.charPositionInLineStart == charPositionInLineStart &&
                axiomQueryError.charPositionInLineStop == charPositionInLineStop &&
                axiomQueryError.message.equals(message) &&
                axiomQueryError.e == e
        ) {
            return true;
        }

        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
