package com.evolveum.concepts;

/**
 * Created by Dominik.
 */
public record TechnicalMessage(
        String message,
        Argument... arguments
) {
}
