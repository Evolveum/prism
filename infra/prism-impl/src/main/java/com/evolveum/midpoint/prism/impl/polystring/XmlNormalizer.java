/*
 * Copyright (C) 2010-2023 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism.impl.polystring;

import javax.xml.namespace.QName;

import com.evolveum.midpoint.prism.impl.match.XmlMatchingRule;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;

import com.evolveum.midpoint.prism.PrismConstants;
import com.evolveum.midpoint.util.DOMUtil;
import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;

/**
 * Normalizer for XML values.
 *
 * @see XmlMatchingRule
 */
public class XmlNormalizer extends BaseStringNormalizer {

    public static final Trace LOGGER = TraceManager.getTrace(XmlNormalizer.class);

    private static final XmlNormalizer INSTANCE = new XmlNormalizer();

    @Override
    public boolean match(String a, String b) {
        if (a == null && b == null) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }
        try {

            Document docA = DOMUtil.parseDocument(a);
            Document docB = DOMUtil.parseDocument(b);
            return DOMUtil.compareDocument(docA, docB, false, false);

        } catch (IllegalStateException | IllegalArgumentException e) {
            LOGGER.warn("Invalid XML in XML matching rule: {}", e.getMessage());
            // Invalid XML. We do not want to throw the exception from matching rule.
            // So fall back to ordinary string comparison.
            return StringUtils.equals(a, b);
        }
    }

    @Override
    public boolean matchRegex(String a, String regex) {
        LOGGER.warn("Regular expression matching is not supported for XML data types");
        return false;
    }

    @Override
    public String normalize(String original) {
        if (original == null) {
            return null;
        }
        try {

            Document doc = DOMUtil.parseDocument(original);
            DOMUtil.normalize(doc, false);
            String out = DOMUtil.printDom(doc, false, true).toString();
            return out.trim();

        } catch (IllegalStateException | IllegalArgumentException e) {
            LOGGER.warn("Invalid XML: {}", e.getMessage());
            return original.trim();
        }
    }

    @Override
    public @NotNull QName getName() {
        return PrismConstants.XML_NORMALIZER;
    }

    public static @NotNull XmlNormalizer instance() {
        return INSTANCE;
    }
}
