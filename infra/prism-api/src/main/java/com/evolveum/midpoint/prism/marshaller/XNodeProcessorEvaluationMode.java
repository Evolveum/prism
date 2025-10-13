/*
 * Copyright (c) 2010-2018 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.prism.marshaller;

/**
 * @author semancik
 *
 */
public enum XNodeProcessorEvaluationMode {
    /**
     * Strict mode. Any inconsistency of data with the schema is considered to be an error.
     */
    STRICT,

    /**
     * Compatibility mode. The processing will go on as long as the data are roughly compatible
     * with the schema. E.g. illegal values and unknown elements are silently ignored.
     */
    COMPAT
}
