package com.evolveum.axiom.lang.antlr;

/**
 * Created by Dominik.
 */
public class AxiomQueryError {
    private final int lineStart;
    private final int lineStop;
    private final int charPositionInLineStart;
    private final int charPositionInLineStop;

    private final String message;

    public AxiomQueryError(int lineStart, int lineStop, int charPositionInLineStart, int charPositionInLineStop, String msg)
    {
        this.lineStart = lineStart;
        this.lineStop = lineStop;
        this.charPositionInLineStart = charPositionInLineStart;
        this.charPositionInLineStop = charPositionInLineStop;
        this.message = msg;
    }

    public int getLineStart()
    {
        return lineStart;
    }
    public int getLineStop()
    {
        return lineStop;
    }
    public int getCharPositionStart()
    {
        return charPositionInLineStart;
    }
    public int getCharPositionStop()
    {
        return charPositionInLineStop;
    }

    public String getMessage()
    {
        return message;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof AxiomQueryError axiomQueryError)) {
            return false;
        }

        if (axiomQueryError.lineStart == lineStart &&
                axiomQueryError.lineStop == lineStop &&
                axiomQueryError.charPositionInLineStart == charPositionInLineStart &&
                axiomQueryError.charPositionInLineStop == charPositionInLineStop &&
                axiomQueryError.message.equals(message)
        ) {
            return true;
        }

        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public String toString() {
        return new StringBuilder()
                .append(lineStart)
                .append(":")
                .append(charPositionInLineStart)
                .append("-")
                .append(lineStop)
                .append(":")
                .append(charPositionInLineStop)
                .append(" ")
                .append(message)
                .toString();
    }
}
