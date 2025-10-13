/*
 * Copyright (C) 2010-2022 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.util;

import java.io.Serializable;

import com.evolveum.midpoint.util.annotation.Experimental;
import com.evolveum.midpoint.util.exception.CommonException;

/**
 * Almost the same as Runnable but this one can throw CommonException and is serializable.
 */
@Experimental
@FunctionalInterface
public interface CheckedCommonRunnable extends Serializable {
    void run() throws CommonException;
}
