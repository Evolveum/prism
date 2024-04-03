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
import com.evolveum.midpoint.prism.impl.polystring.UuidNormalizer;
import com.evolveum.midpoint.prism.match.MatchingRule;
import com.evolveum.midpoint.util.DOMUtil;

/**
 * Matching rule for universally unique identifier (UUID).
 *
 * Currently it is (almost) simple case ignore matching.
 *
 * @author Radovan Semancik
 */
public class UuidMatchingRule implements MatchingRule<String> {

    @Override
    public QName getName() {
        return PrismConstants.UUID_MATCHING_RULE_NAME;
    }

    @Override
    public boolean supports(QName xsdType) {
        return (DOMUtil.XSD_STRING.equals(xsdType));
    }

    @Override
    public @NotNull UuidNormalizer getNormalizer() {
        return UuidNormalizer.instance();
    }

    @Override
    public String toString() {
        return "UuidMatchingRule{}";
    }
}
