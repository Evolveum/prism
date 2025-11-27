package com.evolveum.concepts;

public record ValidationLog(
        /*
         * Kind of log
         */
        ValidationLogType validationLogType,

        /*
         * Localization of log
         */
        SourceLocation location,

        /*
         * More detailed message for technical processing (maybe using AI model)
         */
        TechnicalMessage technicalMessage,

        /*
         * User friendly message
         */
        String message
) {}
