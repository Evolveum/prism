package com.evolveum.concepts;

public record ValidationLog(
        ValidationLogType validationLogType,
        SourceLocation location,
        String technicalMessage,
        String message
) {}
