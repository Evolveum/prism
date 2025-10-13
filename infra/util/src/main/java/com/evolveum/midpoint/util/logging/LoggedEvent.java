/*
 * Copyright (c) 2010-2019 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.util.logging;

/**
 *
 */
public class LoggedEvent {

    private final String text;

    LoggedEvent(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
