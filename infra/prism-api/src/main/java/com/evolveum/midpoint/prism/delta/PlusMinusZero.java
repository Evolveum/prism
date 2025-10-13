/*
 * Copyright (c) 2014 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.prism.delta;

/**
 * Simple enumeration that refers to the plus, minus or zero concepts
 * used in delta set triples.
 *
 * @author Radovan Semancik
 *
 */
public enum PlusMinusZero {

    PLUS, MINUS, ZERO;

    public static PlusMinusZero compute(PlusMinusZero mode1, PlusMinusZero mode2) {
        if (mode1 == null || mode2 == null) {
            return null;
        }
        return switch (mode1) {
            case PLUS -> switch (mode2) {
                case PLUS, ZERO -> PlusMinusZero.PLUS;
                case MINUS -> null;
            };
            case ZERO -> switch (mode2) {
                case PLUS -> PlusMinusZero.PLUS;
                case ZERO -> PlusMinusZero.ZERO;
                case MINUS -> PlusMinusZero.MINUS;
            };
            case MINUS -> switch (mode2) {
                case PLUS -> null;
                case ZERO, MINUS -> PlusMinusZero.MINUS;
            };
        };
    }
}
