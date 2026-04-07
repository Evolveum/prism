/*
 * Copyright (c) 2025 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 *
 */

package com.evolveum.concepts;

public record ValidationLog(

        /*
         * Kind of Validation log
         */
        ValidationLogType validationLogType,

        /*
         * Specification of Validation log
         */
        ValidationLogType.Specification specification,

        /*
         * Localization of Validation log
         */
        SourceLocation location,

        /*
         * More detailed message for technical processing
         */
        TechnicalMessage technicalMessage,

        /*
         * User-friendly message
         */
        String message
) {}
