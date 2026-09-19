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
import java.util.Set;
import java.util.function.Supplier;

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

    void warnOrThrow(Trace logger, Supplier<ValidationLog> validationLog) throws SchemaException;

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

    /**
     * Enables the compatibility (relaxed) mode of operation for the given object type.
     * <p>
     * The parser remains strict for all other object types. Objects of the given type, and their
     * entire subtrees, are parsed leniently - inconsistencies are reported as warnings instead of
     * causing a {@code SchemaException}.
     * @param typeName Object type for which the compatibility mode should be enabled.
     * @return Updated context.
     */
    ParsingContext enableCompatFor(QName typeName);

    /**
     * Returns true if the compatibility mode has been enabled for the given object type.
     * @param typeName Object type to check. May be null.
     */
    boolean isCompatFor(QName typeName);

    /**
     * Returns the set of object types for which the compatibility mode has been enabled.
     */
    Set<QName> getCompatForTypes();

    /**
     * Registers the start of parsing an object of the given type. Used internally by the parser
     * to determine the effective mode (strict/compat) of the current parsing scope.
     * @param typeName Type of the object being parsed.
     */
    void pushObjectType(QName typeName);

    /**
     * Registers the end of parsing an object. Counterpart of {@link #pushObjectType(QName)}.
     */
    void popObjectType();

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
