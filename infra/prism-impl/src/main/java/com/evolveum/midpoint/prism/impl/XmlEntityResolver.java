/*
 * Copyright (c) 2010-2018 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.prism.impl;

import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.EntityResolver;

/**
 * Combined resolver used when parsing schemas.
 */
public interface XmlEntityResolver extends EntityResolver, LSResourceResolver {
}
