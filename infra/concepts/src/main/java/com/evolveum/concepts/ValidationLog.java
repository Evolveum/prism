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
