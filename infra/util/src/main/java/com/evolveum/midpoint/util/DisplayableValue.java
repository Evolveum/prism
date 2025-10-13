/*
 * Copyright (c) 2010-2014 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.util;

/**
 * @author Radovan Semancik
 *
 */
public interface DisplayableValue<T> {

    /**
     * Retuns actual value. This may not be user-friendly.
     */
    T getValue();

    /**
     * Returns short user-friendly label.
     * Catalog key may be returned instead of actual text.
     */
    String getLabel();

    /**
     * Returns longer description that can be used as a help text,
     * tooltip or for similar purpose.
     * Catalog key may be returned instead of actual text.
     */
    String getDescription();

}
