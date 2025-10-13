/*
 * Copyright (C) 2010-2023 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.prism.normalization;

import com.evolveum.midpoint.prism.polystring.PolyString;

import com.evolveum.midpoint.util.exception.SchemaException;

import org.jetbrains.annotations.Contract;

/**
 * A {@link Normalizer} that operates on {@link String} instances.
 *
 * This is a special case, used because the majority of normalization is currently supported only for strings.
 * (Maybe a temporary workaround. We will see.)
 */
public interface StringNormalizer extends Normalizer<String> {

    /** Creates a {@link PolyString} from given "orig" {@link java.lang.String}. */
    @Contract("null -> null; !null -> !null")
    PolyString poly(String orig) throws SchemaException;

}
