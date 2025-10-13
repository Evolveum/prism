/*
 * Copyright (c) 2010-2018 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.prism.impl.match;

import com.evolveum.midpoint.prism.match.MatchingRuleRegistry;

/**
 * Creates MatchingRuleRegistry populated with standard matching rules.
 *
 * @author Radovan Semancik
 *
 */
public class MatchingRuleRegistryFactory {

    public static MatchingRuleRegistry createRegistry() {

        MatchingRuleRegistryImpl registry = new MatchingRuleRegistryImpl();
        registry.registerMatchingRule(new StringIgnoreCaseMatchingRule());
        registry.registerMatchingRule(new PolyStringStrictMatchingRule());
        registry.registerMatchingRule(new PolyStringOrigMatchingRule());
        registry.registerMatchingRule(new PolyStringNormMatchingRule());
        registry.registerMatchingRule(new ExchangeEmailAddressesMatchingRule());
        registry.registerMatchingRule(new DistinguishedNameMatchingRule());
        registry.registerMatchingRule(new XmlMatchingRule());
        registry.registerMatchingRule(new UuidMatchingRule());
        registry.registerMatchingRule(new DefaultMatchingRule<>());

        return registry;
    }

}
