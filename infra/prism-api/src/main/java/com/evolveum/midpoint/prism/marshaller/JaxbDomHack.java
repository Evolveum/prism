/*
 * Copyright (c) 2010-2018 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.prism.marshaller;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.util.exception.SchemaException;

/**
 *
 */
public interface JaxbDomHack {

    <IV extends PrismValue,ID extends ItemDefinition<?>,C extends Containerable> Item<IV,ID> parseRawElement(Object element,
            PrismContainerDefinition<C> definition) throws SchemaException;

    Object toAny(PrismValue value) throws SchemaException;
}
