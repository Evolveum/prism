/*
 * Copyright (c) 2010-2018 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.prism.schema;

import org.jetbrains.annotations.NotNull;

// TODO consider removal
public interface SchemaFactory {

    PrismSchema createPrismSchema(@NotNull String namespace);
}
