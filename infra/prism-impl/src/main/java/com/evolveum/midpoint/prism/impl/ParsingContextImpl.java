/*
 * Copyright (c) 2010-2018 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.prism.impl;

import com.evolveum.concepts.SourceLocation;
import com.evolveum.concepts.TechnicalMessage;
import com.evolveum.concepts.ValidationLog;
import com.evolveum.concepts.ValidationLogType;
import com.evolveum.midpoint.prism.ParsingContext;
import com.evolveum.midpoint.prism.marshaller.XNodeProcessorEvaluationMode;
import com.evolveum.midpoint.prism.xnode.XNode;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.util.exception.ValidationException;
import com.evolveum.midpoint.util.logging.Trace;

import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 */
public class ParsingContextImpl implements ParsingContext, Serializable {

    private XNodeProcessorEvaluationMode evaluationMode = XNodeProcessorEvaluationMode.STRICT;
    private boolean allowMissingRefTypes;
    private boolean convertUnknownTypesToRaw;
    private final List<String> warnings = new ArrayList<>();
    /** Not checking for duplicates when adding parsed data. For trusted sources. */
    private boolean fastAddOperations;
    private boolean preserveNamespaceContext;
    private Set<QName> lazyDeserialization = new HashSet<>();
    private boolean isValidation = false;
    private final List<ValidationLog> validationLogs = new ArrayList<>();

    private ParsingContextImpl() {
    }

    static ParsingContext allowMissingRefTypes() {
        ParsingContextImpl pc = new ParsingContextImpl();
        pc.setAllowMissingRefTypes(true);
        return pc;
    }

    static ParsingContext createForCompatibilityMode() {
        return forMode(XNodeProcessorEvaluationMode.COMPAT);
    }

    private static ParsingContext forMode(XNodeProcessorEvaluationMode mode) {
        ParsingContextImpl pc = new ParsingContextImpl();
        pc.setEvaluationMode(mode);
        return pc;
    }

    public static ParsingContext createDefault() {
        return new ParsingContextImpl();
    }

    @SuppressWarnings("SameParameterValue")
    void setAllowMissingRefTypes(boolean allowMissingRefTypes) {
        this.allowMissingRefTypes = allowMissingRefTypes;
    }

    void setEvaluationMode(XNodeProcessorEvaluationMode evaluationMode) {
        this.evaluationMode = evaluationMode;
    }

    @Override
    public boolean isAllowMissingRefTypes() {
        return allowMissingRefTypes;
    }

    @Override
    public XNodeProcessorEvaluationMode getEvaluationMode() {
        return evaluationMode;
    }

    @Override
    public boolean isCompat() {
        return evaluationMode == XNodeProcessorEvaluationMode.COMPAT;
    }

    @Override
    public boolean isStrict() {
        return evaluationMode == XNodeProcessorEvaluationMode.STRICT;
    }

    @Override
    public void warn(Trace logger, String message) {
        logger.warn("{}", message);
        warn(message);
    }

    @Override
    public void warnOrThrow(Trace logger, String message) throws SchemaException {
        warnOrThrow(logger, message, null);
    }

    @Override
    public void warnOrThrow(Trace logger, String message, Throwable t) throws SchemaException {
        // FIXME maybe it would be good ide remove Warning from prism parsing and to keep just validations logs with exact position of log !!! start using validation logs instead of Warning everywhere
        if (!isValidation()) {
            if (isCompat()) {
                logger.warn("{}", message, t);
                warn(message);
            } else {
                throw new SchemaException(message, t);
            }
        }
    }

    @Override
    public void warn(String message) {
        warnings.add(message);
    }

    @Override
    public List<String> getWarnings() {
        return warnings;
    }

    @Override
    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }

    @Override
    public ParsingContext clone() {
        ParsingContextImpl clone;
        try {
            clone = (ParsingContextImpl) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException(e);
        }
        clone.evaluationMode = evaluationMode;
        clone.allowMissingRefTypes = allowMissingRefTypes;
        clone.warnings.addAll(warnings);
        clone.lazyDeserialization.addAll(lazyDeserialization);
        return clone;
    }

    @Override
    public ParsingContext preserveNamespaceContext() {
        this.preserveNamespaceContext = true;
        return this;
    }

    @Override
    public boolean isPreserveNamespaceContext() {
        return preserveNamespaceContext;
    }

    @Override
    public ParsingContext strict() {
        this.setEvaluationMode(XNodeProcessorEvaluationMode.STRICT);
        return this;
    }

    @Override
    public ParsingContext compat() {
        this.setEvaluationMode(XNodeProcessorEvaluationMode.COMPAT);
        return this;
    }

    @Override
    public boolean isConvertUnknownTypes() {
        return convertUnknownTypesToRaw;
    }

    @Override
    public ParsingContext convertUnknownTypes(boolean value) {
        convertUnknownTypesToRaw = value;
        return this;
    }

    @Override
    public boolean isFastAddOperations() {
        return fastAddOperations;
    }

    @Override
    public ParsingContext fastAddOperations() {
        fastAddOperations = true;
        return this;
    }

    @Override
    public boolean isUseLazyDeserializationFor(QName typeName) {
        return lazyDeserialization.contains(typeName);
    }

    @Override
    public ParsingContext enableLazyDeserializationFor(QName typeName) {
        lazyDeserialization.add(typeName);
        return this;
    }

    @Override
    public boolean isValidation() {
        return isValidation;
    }

    @Override
    public ParsingContext validation() {
        isValidation = true;
        return this;
    }

    @Override
    public List<ValidationLog> getValidationLogs() {
        return validationLogs;
    }

    @Override
    public void validationLogger(boolean expression,
            ValidationLogType validationLogType,
            SourceLocation sourceLocation,
            TechnicalMessage technicalMessage,
            String message, Object... info
    ) {

        if (!expression && isValidation()) {

            if (sourceLocation == null) {
                sourceLocation = SourceLocation.unknown();
            }

            this.validationLogs.add(
                new ValidationLog(
                        validationLogType,
                        sourceLocation,
                        technicalMessage,
                        message.formatted(info)
                )
            );

            if (validationLogType.equals(ValidationLogType.ERROR)) {
                throw new ValidationException(this.validationLogs);
            }
        }
    }
}
