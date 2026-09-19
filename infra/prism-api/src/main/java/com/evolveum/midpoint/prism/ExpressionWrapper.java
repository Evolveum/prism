/*
 * Copyright (c) 2010-2020 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.prism;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

import javax.xml.namespace.QName;

import com.evolveum.midpoint.prism.util.CloneUtil;
import com.evolveum.midpoint.util.PrettyPrinter;

import org.jetbrains.annotations.NotNull;

/**
 * Contains the expression that can be part of e.g. prism filters (or other data).
 */
public class ExpressionWrapper implements Cloneable, Serializable, Freezable {

    @Serial private static final long serialVersionUID = 1L;

    /**
     * Name of the expression root element (e.g. "expression").
     */
    @NotNull private final QName elementName;

    /**
     * Content of the expression.
     * TODO specify more precisely
     */
    @NotNull private final TrustDescriptorAware expression;

    public ExpressionWrapper(@NotNull QName elementName, @NotNull TrustDescriptorAware expression) {
        super();
        this.elementName = elementName;
        this.expression = expression;
    }

    public @NotNull QName getElementName() {
        return elementName;
    }

    public @NotNull TrustDescriptorAware getExpression() {
        return expression;
    }

    public ExpressionWrapper clone() {
        // todo call super.clone?
        TrustDescriptorAware expressionClone = CloneUtil.clone(expression);
        return new ExpressionWrapper(elementName, expressionClone);
    }

    @Override
    public String toString() {
        return "ExpressionWrapper(" + PrettyPrinter.prettyPrint(elementName) + ":" + PrettyPrinter.prettyPrint(expression);
    }

    @Override
    public boolean isImmutable() {
        return (expression instanceof Freezable freezable) && freezable.isImmutable();
    }

    @Override
    public void freeze() {
        if (expression instanceof Freezable freezable) {
            freezable.freeze();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ExpressionWrapper expressionWrapper)) {
            return false;
        }
        return Objects.equals(elementName, expressionWrapper.elementName)
                && Objects.equals(expression, expressionWrapper.expression);
    }

    @Override
    public int hashCode() {
        return Objects.hash(elementName, expression);
    }
}
