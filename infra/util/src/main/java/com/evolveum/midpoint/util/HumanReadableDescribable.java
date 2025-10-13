/*
 * Copyright (c) 2017 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.util;

/**
 * Object that can provide short, human-readable description. The description should
 * be suitable for logging and presentation to users (non-developers).
 *
 * @author semancik
 */
@FunctionalInterface
public interface HumanReadableDescribable {

    String toHumanReadableDescription();

}
