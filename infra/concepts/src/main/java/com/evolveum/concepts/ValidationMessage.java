package com.evolveum.concepts;

public record ValidationMessage(
        ValidationMessageType validationMessageType,
        String message,
        String technicalMessage,
        SourceLocation location
) {}
