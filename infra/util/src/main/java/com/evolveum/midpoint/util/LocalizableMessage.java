/*
 * Copyright (c) 2017 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.util;

import java.io.Serializable;

/**
 * @author semancik
 */
public interface LocalizableMessage extends Serializable, ShortDumpable {

    String getFallbackMessage();

    boolean isEmpty();

    static boolean isEmpty(LocalizableMessage msg) {
        return msg == null || msg.isEmpty();
    }
}
