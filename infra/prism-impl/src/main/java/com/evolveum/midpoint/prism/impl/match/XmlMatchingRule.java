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
import com.evolveum.midpoint.prism.impl.polystring.XmlNormalizer;
import com.evolveum.midpoint.prism.match.MatchingRule;
import com.evolveum.midpoint.util.DOMUtil;

/**
 * String matching rule that compares strings as XML snippets.
 * The XML comparison is not schema aware. It will not handle
 * QNames in values correctly. The comparison ignores XML formatting
 * (whitespaces between elements).
 *
 * @author Radovan Semancik
 */
public class XmlMatchingRule implements MatchingRule<String> {

    @Override
    public QName getName() {
        return PrismConstants.XML_MATCHING_RULE_NAME;
    }

    @Override
    public boolean supports(QName xsdType) {
        return (DOMUtil.XSD_STRING.equals(xsdType));
    }

    @Override
    public @NotNull XmlNormalizer getNormalizer() {
        return XmlNormalizer.instance();
    }

    @Override
    public String toString() {
        return "XmlMatchingRule{}";
    }
}
