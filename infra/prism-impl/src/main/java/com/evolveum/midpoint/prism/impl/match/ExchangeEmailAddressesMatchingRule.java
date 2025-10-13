/*
 * Copyright (c) 2010-2018 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.prism.impl.match;

import javax.xml.namespace.QName;

import org.jetbrains.annotations.NotNull;

import com.evolveum.midpoint.prism.PrismConstants;
import com.evolveum.midpoint.prism.impl.polystring.ExchangeEmailAddressNormalizer;
import com.evolveum.midpoint.prism.match.MatchingRule;
import com.evolveum.midpoint.util.DOMUtil;

/**
 * A matching rule for Microsoft Exchange EmailAddresses attributes.
 *
 * @see ExchangeEmailAddressNormalizer
 */
public class ExchangeEmailAddressesMatchingRule implements MatchingRule<String> {

    @Override
    public QName getName() {
        return PrismConstants.EXCHANGE_EMAIL_ADDRESSES_MATCHING_RULE_NAME;
    }

    @Override
    public boolean supports(QName xsdType) {
        return DOMUtil.XSD_STRING.equals(xsdType);
    }

    @Override
    public @NotNull ExchangeEmailAddressNormalizer getNormalizer() {
        return ExchangeEmailAddressNormalizer.instance();
    }

    @Override
    public String toString() {
        return "ExchangeEmailAddressesMatchingRule{}";
    }
}
