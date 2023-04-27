/*
 * Copyright (c) 2010-2018 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism.impl;

import com.evolveum.midpoint.prism.ParsingContext;
import com.evolveum.midpoint.prism.marshaller.XNodeProcessorEvaluationMode;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.util.logging.Trace;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class ParsingContextImpl implements ParsingContext {

    private XNodeProcessorEvaluationMode evaluationMode = XNodeProcessorEvaluationMode.STRICT;
    private boolean allowMissingRefTypes;
    private boolean convertUnknownTypesToRaw;
    private final List<String> warnings = new ArrayList<>();
    /** Not checking for duplicates when adding parsed data. For trusted sources. */
    private boolean fastAddOperations;

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
        if (isCompat()) {
            logger.warn("{}", message, t);
            warn(message);
        } else {
            throw new SchemaException(message, t);
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
        return clone;
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
}
