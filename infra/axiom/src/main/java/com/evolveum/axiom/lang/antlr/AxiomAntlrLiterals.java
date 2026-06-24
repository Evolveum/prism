/*
 * Copyright (c) 2010-2025 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.axiom.lang.antlr;

import com.evolveum.axiom.lang.antlr.query.AxiomQueryParser.BooleanValueContext;
import com.evolveum.axiom.lang.antlr.query.AxiomQueryParser.DoubleQuoteStringContext;
import com.evolveum.axiom.lang.antlr.query.AxiomQueryParser.FloatValueContext;
import com.evolveum.axiom.lang.antlr.query.AxiomQueryParser.IntValueContext;
import com.evolveum.axiom.lang.antlr.query.AxiomQueryParser.LiteralValueContext;
import com.evolveum.axiom.lang.antlr.query.AxiomQueryParser.MultilineStringContext;
import com.evolveum.axiom.lang.antlr.query.AxiomQueryParser.NullValueContext;
import com.evolveum.axiom.lang.antlr.query.AxiomQueryParser.SingleQuoteStringContext;
import com.evolveum.axiom.lang.antlr.query.AxiomQueryParser.StringLiteralContext;
import com.evolveum.axiom.lang.antlr.query.AxiomQueryParser.StringValueContext;

public class AxiomAntlrLiterals {

    @Deprecated
    public static String convertSingleQuote(String text) {
        return AxiomStrings.fromSingleQuoted(text);
    }

    @Deprecated
    public static String convertDoubleQuote(String text) {
        return AxiomStrings.fromDoubleQuoted(text);
    }

    public static String convertMultiline(String text) {
        return text.replace("\"\"\"", "");
    }

    public static Object convert(LiteralValueContext value) {
        if (value instanceof StringValueContext stringContext) {
            return convertString(stringContext);
        } else if(value instanceof IntValueContext intContext) {
            return convertInteger(intContext);
        } else if (value instanceof BooleanValueContext booleanContext) {
            return convertBoolean(booleanContext);
        } else if (value instanceof NullValueContext nullContext) {
            return convertNull(nullContext);
        } else if (value instanceof FloatValueContext floatContext) {
            return convertFloat(floatContext);
        }
        throw new UnsupportedOperationException("Unknown type of literal" + value.getClass());
    }


    private static Number convertFloat(FloatValueContext value) {
        return Double.parseDouble(value.getText());
    }

    public static String convertString(StringValueContext value) {
        return convertString(value.stringLiteral());
    }

    private static String convertString(StringLiteralContext string) {
        if(string instanceof SingleQuoteStringContext) {
            return convertSingleQuote(string.getText());
        } else if (string instanceof MultilineStringContext) {
            return convertMultiline(string.getText());
        } else if (string instanceof DoubleQuoteStringContext) {
            return convertDoubleQuote(string.getText());
        }
        throw new UnsupportedOperationException("Unknown String type" + string.getClass());
    }

    private static Void convertNull(NullValueContext value) {
        return null;
    }

    private static Boolean convertBoolean(BooleanValueContext value) {
        return Boolean.parseBoolean(value.getText());
    }

    private static Number  convertInteger(IntValueContext value) {
        return Integer.parseInt(value.getText());
    }

}
