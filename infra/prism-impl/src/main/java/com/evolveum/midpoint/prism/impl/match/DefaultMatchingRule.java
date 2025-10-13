/*
 * Copyright (c) 2010-2018 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.prism.impl.match;

import javax.xml.namespace.QName;

import com.evolveum.midpoint.prism.normalization.Normalizer;

import org.jetbrains.annotations.NotNull;

import com.evolveum.midpoint.prism.PrismConstants;
import com.evolveum.midpoint.prism.impl.polystring.NoOpNormalizer;
import com.evolveum.midpoint.prism.match.MatchingRule;

/**
 * Default matching rule used as a fall-back if no explicit matching rule is specified.
 * It is simply using java equals() method to match values.
 *
 * @author Radovan Semancik
 */
public class DefaultMatchingRule<T> implements MatchingRule<T> {

    @Override
    public QName getName() {
        return PrismConstants.DEFAULT_MATCHING_RULE_NAME;
    }

    @Override
    public boolean supports(QName xsdType) {
        // We support everything. We are the default.
        return true;
    }

    @Override
    public @NotNull Normalizer<T> getNormalizer() {
        //noinspection unchecked
        return (Normalizer<T>) NoOpNormalizer.instance();
    }

    @Override
    public String toString() {
        return "DefaultMatchingRule{}";
    }
}
