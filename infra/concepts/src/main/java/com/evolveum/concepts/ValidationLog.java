/*
 * Copyright (c) 2025 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 *
 */

package com.evolveum.concepts;

public record ValidationLog(
        /*
         * Kind of log
         */
        ValidationLogType validationLogType,

        ValidationLogType.Specification specification,

        /*
         * Localization of log
         */
        SourceLocation location,

        /*
         * More detailed message for technical processing
         */
        TechnicalMessage technicalMessage,

        /*
         * User friendly message
         */
        String message
) {}
