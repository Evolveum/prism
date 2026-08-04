/*
 * Copyright (c) 2026 Evolveum and contributors
 * 
 * This work is licensed under European Union Public License v1.2. See LICENSE file for details.
 * 
 */
package com.evolveum.concepts;

/**
 * Created by Dominik.
 */
public record TechnicalMessage(
        String message,
        Argument... arguments
) {
}
