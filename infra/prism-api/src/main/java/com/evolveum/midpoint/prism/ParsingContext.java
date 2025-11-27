/*
 * Copyright (c) 2010-2017 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.prism;

import com.evolveum.concepts.SourceLocation;
import com.evolveum.concepts.TechnicalMessage;
import com.evolveum.concepts.ValidationLog;
import com.evolveum.concepts.ValidationLogType;
import com.evolveum.midpoint.prism.marshaller.XNodeProcessorEvaluationMode;
import com.evolveum.midpoint.prism.xnode.XNode;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.util.exception.ValidationException;
import com.evolveum.midpoint.util.logging.Trace;

import org.jetbrains.annotations.NotNull;

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

    ParsingContext validation();
    boolean isValidation();
    List<ValidationLog> getValidationLogs();
    void validationLogger(boolean expression, ValidationLogType validationLogType, SourceLocation sourceLocation, TechnicalMessage technicalMessage, String message, Object... info) throws ValidationException;
}
