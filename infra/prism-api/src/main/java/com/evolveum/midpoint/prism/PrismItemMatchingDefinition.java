/*
 * Copyright (C) 2010-2024 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism;

import javax.xml.namespace.QName;

import org.jetbrains.annotations.NotNull;

import com.evolveum.midpoint.prism.match.MatchingRule;
import com.evolveum.midpoint.prism.normalization.Normalizer;
import com.evolveum.midpoint.prism.polystring.PolyString;

import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.Objects;

/** How values are matched/compared. Currently used only for properties, but nothing precludes the use for other items. */
public interface PrismItemMatchingDefinition<T> {

    /** Should be provided elsewhere. Here it's just used. */
    QName getTypeName();

    default Class<T> getTypeClass() {
        return PrismContext.get().getSchemaRegistry().determineJavaClassForType(getTypeName());
    }

    /**
     * Returns matching rule name. Matching rules are algorithms that specify
     * how to compare, normalize and/or order the values. E.g. there are matching
     * rules for case insensitive string comparison, for LDAP DNs, etc.
     *
     * TODO describe the semantics where special normalizations are to be used
     *  Use with care until this description is complete.
     *
     * @return matching rule name
     */
    QName getMatchingRuleQName();

    /** Returns the resolved {@link MatchingRule} for this property. */
    default @NotNull MatchingRule<T> getMatchingRule() {
        return PrismContext.get().getMatchingRuleRegistry()
                .getMatchingRuleSafe(getMatchingRuleQName(), getTypeName());
    }

    /**
     * Returns the normalizer that is to be applied when the normalized form of this property is to be computed.
     * For polystring-typed properties (that are assumed to be already normalized) it returns "no-op" normalizer.
     */
    default @NotNull Normalizer<T> getNormalizer() {
        return getMatchingRule().getNormalizer();
    }

    /** Returns the normalizer that is to be applied for {@link PolyString} properties. Throws an exception if not applicable. */
    default @NotNull Normalizer<String> getStringNormalizerForPolyStringProperty() {
        if (PolyString.class.equals(getTypeClass())) {
            // This is the default for PolyString properties
            return PrismContext.get().getDefaultPolyStringNormalizer();
        } else {
            throw new UnsupportedOperationException(
                    "Asking for a string normalizer is not supported for non-PolyString property " + this);
        }
    }

    default @Nullable Normalizer<String> getStringNormalizerIfApplicable() {
        return PolyString.class.equals(getTypeClass()) ? // TODO is this OK?
                getStringNormalizerForPolyStringProperty() : null;
    }

    /** TODO */
    default boolean isCustomPolyString() {
        return false;
    }

    interface Delegable<T> extends PrismItemMatchingDefinition<T> {

        PrismItemMatchingDefinition<T> prismItemMatchingDefinition();

        @Override
        default QName getMatchingRuleQName() {
            return prismItemMatchingDefinition().getMatchingRuleQName();
        }

    }

    interface Mutator {

        void setMatchingRuleQName(QName value);

        interface Delegable extends Mutator {

            Mutator prismItemMatchingDefinition();

            @Override
            default void setMatchingRuleQName(QName value) {
                prismItemMatchingDefinition().setMatchingRuleQName(value);
            }
        }
    }

    class Data<T>
            extends AbstractFreezable
            implements PrismItemMatchingDefinition<T>, Mutator, Serializable {

        @NotNull private final QName typeName;

        private QName matchingRuleQName;

        public Data(@NotNull QName typeName) {
            this.typeName = typeName;
        }

        @Override
        public @NotNull QName getTypeName() {
            return typeName;
        }

        @Override
        public QName getMatchingRuleQName() {
            return matchingRuleQName;
        }

        @Override
        public void setMatchingRuleQName(QName matchingRuleQName) {
            checkMutable();
            this.matchingRuleQName = matchingRuleQName;
        }

        public void copyFrom(PrismItemMatchingDefinition<T> source) {
            checkMutable();
            matchingRuleQName = source.getMatchingRuleQName();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Data<?> data = (Data<?>) o;
            return Objects.equals(typeName, data.typeName)
                    && Objects.equals(matchingRuleQName, data.matchingRuleQName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(typeName, matchingRuleQName);
        }
    }
}
