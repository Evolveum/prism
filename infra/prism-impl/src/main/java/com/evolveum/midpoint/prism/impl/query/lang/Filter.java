/*
 * Copyright (C) 2020-2021 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.prism.impl.query.lang;

import java.util.Map;
import java.util.Optional;
import javax.xml.namespace.QName;

import com.evolveum.axiom.lang.antlr.query.AxiomQueryParser;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;

public class Filter {

    public static final String QUERY_NS = "http://prism.evolveum.com/xml/ns/public/query-3";

    public static final String MATCHING_RULE_NS = "http://prism.evolveum.com/xml/ns/public/matching-rule-3";

    public enum Infra {

        TYPE("@type"),
        PATH("@path"),
        RELATION("@relation");

        private final String name;

        Infra(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    public enum Name {

        AND("and"),
        OR("or"),
        EQUAL("equal"),
        LESS("less"),
        GREATER("greater"),
        LESS_OR_EQUAL("lessOrEqual"),
        GREATER_OR_EQUAL("greaterOrEqual"),
        CONTAINS("contains"),
        STARTS_WITH("startsWith"),
        ENDS_WITH("endsWith"),
        MATCHES("matches"),
        EXISTS("exists"),
        FULL_TEXT("fullText"),
        IN_OID("inOid"),
        OWNED_BY_OID("ownedByOid"),
        IN_ORG("inOrg"),
        IS_ROOT("isRoot"),
        NOT("not"),
        NOT_EQUAL("notEqual"),
        TYPE("type"),
        OWNED_BY("ownedBy"),
        REFERENCED_BY("referencedBy"),
        ANY_IN("anyIn"),
        LEVENSHTEIN("levenshtein"),
        SIMILARITY("similarity");

        private final QName name;

        Name(String name) {
            this.name = new QName(QUERY_NS, name);
        }

        public QName getName() {
            return name;
        }

        public String getLocalPart() {
            return name.getLocalPart();
        }
    }

    public enum Alias {

        EQUAL("="),
        LESS("<"),
        GREATER(">"),
        LESS_OR_EQUAL("<="),
        GREATER_OR_EQUAL(">="),
        NOT_EQUAL("!=");

        private final String name;

        Alias(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    public enum ReferencedKeyword {

        OID("oid"),
        TARGET_TYPE("targetType"),
        RELATION("relation"),
        TARGET("target");

        private final String name;

        ReferencedKeyword(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    public enum PolyStringKeyword {

        ORIG("orig"),
        NORM("norm");

        private final String name;

        PolyStringKeyword(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    public enum Token {
        REF_TARGET_ALIAS("@"),
        SELF_PATH("."),
        PARENT(".."),
        DOLLAR("$"),
        SHARP("#"),
        SLASH("/");

        private final String name;

        Token(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        // Pair Tokens
        public enum Pair {
            ROUND_BRACKET("(", ")"),
            SQUARE_BRACKET("[", "]"),
            BRACE("{", "}"),
            SQOUTE("'", "'"),
            DQOUTE("\"", "\""),
            BACKTICK("`", "`");

            private final String open;
            private final String close;

            Pair(String open, String close) {
                this.open = open;
                this.close = close;
            }

            public String getOpen() {
                return open;
            }

            public String getClose() {
                return close;
            }
        }
    }

    public enum RulesWithoutSep {
        PATH(AxiomQueryParser.RULE_path),
        MATCHING_RULE(AxiomQueryParser.RULE_matchingRule);

        private final int index;

        RulesWithoutSep(int index) {
            this.index = index;
        }

        public int getIndex() {
            return index;
        }
    }

    static final BiMap<Alias, Name> ALIAS_TO_NAME = ImmutableBiMap.<Alias, Name>builder()
            .put(Alias.EQUAL, Name.EQUAL)
            .put(Alias.LESS, Name.LESS)
            .put(Alias.GREATER, Name.GREATER)
            .put(Alias.LESS_OR_EQUAL, Name.LESS_OR_EQUAL)
            .put(Alias.GREATER_OR_EQUAL, Name.GREATER_OR_EQUAL)
            .put(Alias.NOT_EQUAL, Name.NOT_EQUAL)
            .build();

    static final Map<Name, Alias> NAME_TO_ALIAS = ALIAS_TO_NAME.inverse();

    private static final BiMap<String, QName> ALIAS_TO_NAME_SIMPLE = ALIAS_TO_NAME.entrySet().stream()
            .collect(ImmutableBiMap.toImmutableBiMap(e -> e.getKey().getName(), e -> e.getValue().getName()));

    private static final Map<QName, String> NAME_TO_ALIAS_SIMPLE = ALIAS_TO_NAME_SIMPLE.inverse();

    static Optional<Name> fromAlias(Alias alias) {
        return Optional.ofNullable(ALIAS_TO_NAME.get(alias));
    }

    static Optional<Alias> aliasFor(Name name) {
        return Optional.ofNullable(NAME_TO_ALIAS.get(name));
    }

    static Optional<QName> fromAlias(String alias) {
        return Optional.ofNullable(ALIAS_TO_NAME_SIMPLE.get(alias));
    }

    static Optional<String> aliasFor(QName name) {
        return Optional.ofNullable(NAME_TO_ALIAS_SIMPLE.get(name));
    }
}
