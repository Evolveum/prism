/*
 * Copyright (C) 2010-2022 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.prism;

import java.util.Optional;
import javax.xml.namespace.QName;

import org.jetbrains.annotations.NotNull;

import com.evolveum.midpoint.prism.delta.ItemDelta;
import com.evolveum.midpoint.prism.equivalence.ParameterizedEquivalenceStrategy;
import com.evolveum.midpoint.prism.path.ItemName;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.util.annotation.Experimental;
import com.evolveum.midpoint.util.exception.SchemaException;

/**
 * A definition of a specific item (as opposed to a type).
 *
 * @see TypeDefinition
 */
public interface ItemDefinition<I extends Item>
        extends Definition, PrismItemAccessDefinition {

    /**
     * Gets the "canonical" name of the item for the definition.
     * Should be qualified, if at all possible.
     */
    @NotNull ItemName getItemName();

    /**
     * Return the number of minimal value occurrences.
     */
    int getMinOccurs();

    /**
     * Return the number of maximal value occurrences. Any negative number means "unbounded".
     */
    int getMaxOccurs();

    default boolean isSingleValue() {
        int maxOccurs = getMaxOccurs();
        return maxOccurs >= 0 && maxOccurs <= 1;
    }

    default boolean isMultiValue() {
        int maxOccurs = getMaxOccurs();
        return maxOccurs < 0 || maxOccurs > 1;
    }

    default boolean isMandatory() {
        return getMinOccurs() > 0;
    }

    default boolean isOptional() {
        return getMinOccurs() == 0;
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
     * If true, this item is not stored in XML representation in repo.
     *
     * TODO better name
     */
    @Experimental
    boolean isIndexOnly();

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
     * Returns true if this definition is valid for given element name and definition class,
     * in either case-sensitive (the default) or case-insensitive way.
     *
     * Used e.g. for "slow" path lookup where we iterate over all definitions in a complex type.
     */
    boolean isValidFor(@NotNull QName elementQName, @NotNull Class<? extends ItemDefinition<?>> clazz, boolean caseInsensitive);

    /**
     * Used to find a matching item definition _within_ this definition.
     * Treats e.g. de-referencing in prism references.
     */
    <T extends ItemDefinition<?>> T findItemDefinition(@NotNull ItemPath path, @NotNull Class<T> clazz);

    /**
     * Transfers selected parts of the definition (currently item name, min/max occurs) from another definition.
     *
     * TODO used only on few places, consider removing
     */
    void adoptElementDefinitionFrom(ItemDefinition<?> otherDef);

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
     * TODO document
     */
    ItemDefinition<I> deepClone(@NotNull DeepCloneOperation operation);

    /**
     * Used in debugDumping items. Does not need to have name in it as item already has it. Does not need
     * to have class as that is just too much info that is almost anytime pretty obvious anyway.
     */
    void debugDumpShortToString(StringBuilder sb);

    /**
     * TODO document
     */
    boolean canBeDefinitionOf(I item);

    /**
     * TODO document
     */
    boolean canBeDefinitionOf(PrismValue pvalue);

    @Override
    MutableItemDefinition<I> toMutable();

    /**
     * Returns complex type definition of item, if underlying value is possible structured.
     *
     * NOTE: This seems weird, since properties and references are simple values,
     * but actually object reference is serialized as structured value and some of properties
     * are also.
     */
    @Experimental
    Optional<ComplexTypeDefinition> structuredType();

    /**
     * Returns true if item definition is searchable.
     * @return
     */
    @Experimental
    default boolean isSearchable() {
        return false;
    }
}
