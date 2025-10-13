/*
 * Copyright (c) 2010-2013 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.util.aspect;

/**
 * @author Radovan Semancik
 *
 */
@FunctionalInterface
public interface ObjectFormatter {

    String format(Object o);

}
