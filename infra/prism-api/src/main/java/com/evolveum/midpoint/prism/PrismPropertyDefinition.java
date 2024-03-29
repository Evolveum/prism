/*
 * Copyright (c) 2010-2017 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism;

import java.util.Collection;
import java.util.List;
import javax.xml.namespace.QName;

import com.evolveum.midpoint.prism.schema.SchemaRegistry;
import com.evolveum.midpoint.prism.xml.XsdTypeMapper;
import com.evolveum.midpoint.util.exception.SchemaException;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.evolveum.midpoint.prism.delta.PropertyDelta;
import com.evolveum.midpoint.prism.match.MatchingRule;
import com.evolveum.midpoint.prism.normalization.Normalizer;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.prism.polystring.PolyString;
import com.evolveum.midpoint.util.DOMUtil;
import com.evolveum.midpoint.util.DisplayableValue;

/**
 * Definition of a prism property.
 */
public interface PrismPropertyDefinition<T> extends ItemDefinition<PrismProperty<T>> {

    /**
     * Returns allowed values for this property.
     */
    @Nullable Collection<? extends DisplayableValue<T>> getAllowedValues();

    /**
     * Returns suggested values for this property.
     */
    @Nullable Collection<? extends DisplayableValue<T>> getSuggestedValues();

    @Nullable T defaultValue();

    /**
     * This is XSD annotation that specifies whether a property should
     * be indexed in the storage. It can only apply to properties. It
     * has following meaning:
     *
     * true: the property must be indexed. If the storage is not able to
     * index the value, it should indicate an error.
     *
     * false: the property should not be indexed.
     *
     * null: data store decides whether to index the property or
     * not.
     */
    Boolean isIndexed();

    default boolean isAnyType() {
        return DOMUtil.XSD_ANYTYPE.equals(getTypeName());
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
    @NotNull MatchingRule<T> getMatchingRule();

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
            throw new UnsupportedOperationException("Cannot get string normalizer for non-PolyString property " + this);
        }
    }

    /** TODO */
    default boolean isCustomPolyString() {
        return false;
    }

    @Override
    @NotNull
    PropertyDelta<T> createEmptyDelta(ItemPath path);

    @NotNull
    @Override
    PrismProperty<T> instantiate();

    @NotNull
    @Override
    PrismProperty<T> instantiate(QName name);

    @NotNull
    @Override
    PrismPropertyDefinition<T> clone();

    @Override
    Class<T> getTypeClass();

    /**
     * This is the original implementation. Moving to more comprehensive one (as part of MID-2119 implementation) broke some
     * things especially in the prism query language parser, so we temporarily provide the legacy (partial) implementation here.
     *
     * The difference is that the partial implementation covers only basic types.
     * The comprehensive one covers also complex types via {@link SchemaRegistry#determineJavaClassForType(QName)}.
     */
    default Class<T> getTypeClassLegacy() {
        return XsdTypeMapper.toJavaTypeIfKnown(getTypeName());
    }

    @Override
    MutablePrismPropertyDefinition<T> toMutable();

    /** TEMPORARY! Used only for normalization-aware resource attribute storage. FIXME as part of MID-2119. */
    default @NotNull List<T> adoptRealValues(@NotNull Collection<?> realValues) throws SchemaException {
        throw new UnsupportedOperationException();
    }
}
