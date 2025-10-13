/*
 * Copyright (c) 2010-2013 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.schema.xjc;

import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.outline.Outline;
import org.xml.sax.ErrorHandler;

/**
 * @author lazyman
 */
@FunctionalInterface
public interface Processor {

    boolean run(Outline outline, Options opt, ErrorHandler errorHandler) throws Exception;
}
