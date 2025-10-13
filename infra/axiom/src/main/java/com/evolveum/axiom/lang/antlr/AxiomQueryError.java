/*
 * Copyright (c) 2010-2025 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.axiom.lang.antlr;

import com.evolveum.midpoint.util.LocalizableMessage;

import java.util.Objects;

/**
 * Created by Dominik.
 */
public record AxiomQueryError(int lineStart, int lineStop,
                              int charPositionInLineStart, int charPositionInLineStop,
                              LocalizableMessage localizableMessage,
                              String message) {

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof AxiomQueryError axiomQueryError)) {
            return false;
        }

        return axiomQueryError.lineStart == lineStart &&
                axiomQueryError.lineStop == lineStop &&
                axiomQueryError.charPositionInLineStart == charPositionInLineStart &&
                axiomQueryError.charPositionInLineStop == charPositionInLineStop &&
                axiomQueryError.message.equals(message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lineStart, lineStop, charPositionInLineStart, charPositionInLineStop, message);
    }

    @Override
    public String toString() {
        return lineStart
                + ":"
                + charPositionInLineStart
                + "-"
                + lineStop
                + ":"
                + charPositionInLineStop
                + " "
                + message;
    }
}
