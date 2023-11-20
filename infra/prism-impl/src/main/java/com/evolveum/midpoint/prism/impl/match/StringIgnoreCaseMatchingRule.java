/*
 * Copyright (c) 2010-2018 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.prism.impl.match;

import javax.xml.namespace.QName;

import org.jetbrains.annotations.NotNull;

import com.evolveum.midpoint.prism.PrismConstants;
import com.evolveum.midpoint.prism.impl.polystring.LowercaseStringNormalizer;
import com.evolveum.midpoint.prism.match.MatchingRule;
import com.evolveum.midpoint.prism.normalization.Normalizer;
import com.evolveum.midpoint.util.DOMUtil;

/**
 * String matching rule that ignores the case.
 *
 * @author Radovan Semancik
 */
public class StringIgnoreCaseMatchingRule implements MatchingRule<String> {

    @Override
    public QName getName() {
        return PrismConstants.STRING_IGNORE_CASE_MATCHING_RULE_NAME;
    }

    @Override
    public boolean supports(QName xsdType) {
        return (DOMUtil.XSD_STRING.equals(xsdType));
    }

    @Override
    public @NotNull Normalizer<?> getNormalizer() {
        return LowercaseStringNormalizer.instance();
    }

    @Override
    public String toString() {
        return "StringIgnoreCaseMatchingRule{}";
    }
}
