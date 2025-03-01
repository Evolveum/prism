/*
 * Copyright (c) 2010-2017 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism;

import com.evolveum.midpoint.prism.marshaller.XNodeProcessorEvaluationMode;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.util.logging.Trace;

import javax.xml.namespace.QName;
import java.util.List;

/**
 * TODO TODO TODO
 */
public interface ParsingContext extends Cloneable {

    boolean isAllowMissingRefTypes();

    boolean isCompat();

    boolean isStrict();

    void warn(Trace logger, String message);

    void warnOrThrow(Trace logger, String message) throws SchemaException;

    void warnOrThrow(Trace logger, String message, Throwable t) throws SchemaException;

    void warn(String message);

    List<String> getWarnings();

    boolean hasWarnings();

    ParsingContext preserveNamespaceContext();

    boolean isPreserveNamespaceContext();

    ParsingContext clone();

    ParsingContext strict();

    ParsingContext compat();

    XNodeProcessorEvaluationMode getEvaluationMode();

    boolean isConvertUnknownTypes();

    ParsingContext convertUnknownTypes(boolean value);

    boolean isFastAddOperations();

    ParsingContext fastAddOperations();

    boolean isUseLazyDeserializationFor(QName typeName);
    ParsingContext enableLazyDeserializationFor(QName typeName);
}
