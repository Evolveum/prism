/*
 * Copyright (C) 2010-2022 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.prism;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import javax.xml.namespace.QName;

import com.evolveum.midpoint.prism.schema.SchemaLookup;

import com.sun.xml.xsom.XSAnnotation;
import com.sun.xml.xsom.XSComponent;
import org.jetbrains.annotations.NotNull;

import com.evolveum.midpoint.prism.delta.ItemDelta;
import com.evolveum.midpoint.prism.equivalence.ParameterizedEquivalenceStrategy;
import com.evolveum.midpoint.prism.path.ItemName;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.prism.schema.DefinitionFeature;
import com.evolveum.midpoint.util.annotation.Experimental;
import com.evolveum.midpoint.util.exception.SchemaException;

/**
 * A definition of a specific item (as opposed to a type).
 *
 * @see TypeDefinition
 */
public interface ItemDefinition<I extends Item<?, ?>>
        extends Definition,
        PrismItemBasicDefinition,
        PrismItemStorageDefinition,
        PrismItemAccessDefinition,
        LivePrismItemDefinition,
        SchemaLookup.Aware {

    ItemProcessing getProcessing();

    default boolean isIgnored() {
        return getProcessing() == ItemProcessing.IGNORE;
    }

    /**
     * Marks operational item. Operational properties are auxiliary data (or meta-data) that are usually
     * not modifiable by the end user. They are generated and maintained by the system.
     * Operational items are also not usually displayed unless it is explicitly requested.
     *
     * The example of operational items are modification timestamps, create timestamps, user that
     * made the last change, etc.
     *
     * They are also treated in a special way when comparing values. See {@link ParameterizedEquivalenceStrategy}.
     */
    boolean isOperational();

    /**
     * Item which should always be used for equals even if it is operational and strategy does not
     * consider operational items.
     *
     * EXPERIMENTAL
     */
    @Experimental
    boolean isAlwaysUseForEquals();

    /**
     * Whether the item is inherited from a supertype.
     */
    boolean isInherited();

    /**
     * Returns true if definition was created during the runtime based on a dynamic information
     * such as xsi:type attributes in XML. This means that the definition needs to be stored
     * alongside the data to have a successful serialization "roundtrip". The definition is not
     * part of any schema and therefore cannot be determined. It may even be different for every
     * instance of the associated item (element name).
     */
    boolean isDynamic();

    /**
     * Returns the name of an element this one can be substituted for (e.g. c:user -&gt; c:object,
     * s:pipeline -&gt; s:expression, etc). EXPERIMENTAL
     */
    @Experimental
    QName getSubstitutionHead();

    /**
     * Can be used in heterogeneous lists as a list item. EXPERIMENTAL.
     */
    @Experimental
    boolean isHeterogeneousListItem();

    /**
     * Reference to an object that directly or indirectly represents possible values for
     * this item. We do not define here what exactly the object has to be. It can be a lookup
     * table, script that dynamically produces the values or anything similar.
     * The object must produce the values of the correct type for this item otherwise an
     * error occurs.
     */
    PrismReferenceValue getValueEnumerationRef();

    /**
     * Create an item instance. Definition name or default name will
     * be used as an element name for the instance. The instance will otherwise be empty.
     */
    @NotNull I instantiate() throws SchemaException;

    /**
     * Create an item instance. Definition name will use provided name.
     * for the instance. The instance will otherwise be empty.
     */
    @NotNull
    I instantiate(QName name) throws SchemaException;

    /**
     * Creates an empty delta (with appropriate implementation class), pointing to this item definition, with a given path.
     */
    @NotNull ItemDelta<?, ?> createEmptyDelta(ItemPath path);

    @Override
    @NotNull ItemDefinition<I> clone();

    /**
     * Returns a clone of this definition, but with name changed to the provided one.
     *
     * @see PrismContainerDefinition#cloneWithNewDefinition(QName, ItemDefinition)
     */
    @NotNull ItemDefinition<I> cloneWithNewName(@NotNull ItemName itemName);

    /**
     * TODO document
     */
    ItemDefinition<I> deepClone(@NotNull DeepCloneOperation operation);

    /**
     * Used in debugDumping items. Does not need to have name in it as item already has it. Does not need
     * to have class as that is just too much info that is almost anytime pretty obvious anyway.
     */
    void debugDumpShortToString(StringBuilder sb);

    @Override
    ItemDefinitionMutator mutator();

    /**
     * Returns complex type definition of item, if underlying value is possible structured.
     *
     * NOTE: This seems weird, since properties and references are simple values,
     * but actually object reference is serialized as structured value and some of properties
     * are also.
     */
    @Experimental
    Optional<ComplexTypeDefinition> structuredType();

    interface ItemDefinitionMutator
            extends
            DefinitionMutator,
            PrismPresentationDefinition.Mutable,
            PrismItemBasicDefinition.Mutable,
            PrismItemAccessDefinition.Mutable,
            PrismItemStorageDefinition.Mutable,
            PrismLifecycleDefinition.Mutable {

        void setProcessing(ItemProcessing processing);

        /** A bit dubious. Should be removed eventually. */
        default void setIgnored(boolean value) {
            if (value) {
                setProcessing(ItemProcessing.IGNORE);
            }
        }

        void setValueEnumerationRef(PrismReferenceValue valueEnumerationRef);

        void setOperational(boolean operational);

        void setAlwaysUseForEquals(boolean alwaysUseForEquals);

        void setDynamic(boolean value);

        void setReadOnly();

        void setDeprecatedSince(String value);

        void setPlannedRemoval(String value);

        void setElaborate(boolean value);

        void setHeterogeneousListItem(boolean value);

        void setSubstitutionHead(QName value);

        void setIndexOnly(boolean value);

        void setInherited(boolean value);

        void setSearchable(boolean value);
    }

    /** To be seen if useful. */
    interface ItemDefinitionLikeBuilder extends ItemDefinitionMutator, DefinitionBuilder {

        /**
         * See {@link ComplexTypeDefinition.ComplexTypeDefinitionLikeBuilder#getExtraFeaturesToParse()}. These annotations
         * must accept {@link XSComponent} or {@link XSAnnotation} as source.
         */
        default Collection<DefinitionFeature<?, ?, Object, ?>> getExtraFeaturesToParse() {
            return List.of();
        }
    }
}
