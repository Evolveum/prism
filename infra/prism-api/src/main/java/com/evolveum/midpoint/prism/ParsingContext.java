/*
 * Copyright (c) 2010-2017 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.prism;

import com.evolveum.concepts.ValidationLog;
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

    @Deprecated
    void warn(Trace logger, String message);

    @Deprecated
    void warnOrThrow(Trace logger, String message) throws SchemaException;

    @Deprecated
    void warnOrThrow(Trace logger, String message, Throwable t) throws SchemaException;

    void warn(Trace logger, ValidationLog validationLog);

    void warnOrThrow(Trace logger, ValidationLog validationLog) throws SchemaException;

    void warnOrThrow(Trace logger, ValidationLog validationLog, Throwable t) throws SchemaException;

    @Deprecated
    void warn(String message);

    void warn(ValidationLog validationLog);

    List<ValidationLog> getWarnings();

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

    ParsingContext validation();
    boolean isValidation();
}
