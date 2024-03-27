/*
 * Copyright (C) 2010-2022 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism;

import java.util.*;
import javax.xml.namespace.QName;

import com.evolveum.midpoint.prism.ItemDefinition.ItemDefinitionLikeBuilder;
import com.evolveum.midpoint.prism.PrismPropertyDefinition.PrismPropertyLikeDefinitionBuilder;
import com.evolveum.midpoint.prism.PrismPropertyDefinition.PrismPropertyDefinitionMutator;
import com.evolveum.midpoint.prism.schema.DefinitionFeature;

import com.sun.xml.xsom.XSComplexType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.util.annotation.Experimental;

/**
 * Provides a definition for a complex type, i.e. type that prescribes inner items.
 * It's instances may be container values or property values, depending on container/object
 * markers presence.
 *
 * @author semancik
 */
public interface ComplexTypeDefinition
        extends TypeDefinition,
        LocalItemDefinitionStore {

    /**
     * Returns definitions for all inner items.
     *
     * These are of type {@link ItemDefinition}. However, very often subtypes of this type are used,
     * e.g. ResourceAttributeDefinition, RefinedAttributeDefinition, LayerRefinedAttributeDefinition, and so on.
     *
     * The returned structure is a {@link List} because the ordering is significant, e.g. for serialization purposes.
     *
     * The list is unmodifiable.
     */
    @Override
    @NotNull
    List<? extends ItemDefinition<?>> getDefinitions();

    /**
     * If not null, indicates that this type defines the structure of `extension` element of a given type.
     * E.g. `getExtensionForType()` == `c:UserType` means that this complex type defines structure of
     * `extension` elements of `UserType` objects.
     */
    @Nullable
    QName getExtensionForType();

    /**
     * Flag indicating whether this type was marked as "objectReference" in the original schema.
     */
    boolean isReferenceMarker();

    /**
     * Flag indicating whether this type was marked as "container"
     * in the original schema. Does not provide any information to
     * schema processing logic, just conveys the marker from original
     * schema so we can serialize and deserialize the schema without
     * loss of information.
     */
    boolean isContainerMarker();

    /**
     * Flag indicating whether this type was marked as "object"
     * in the original schema. Does not provide any information to
     * schema processing logic, just conveys the marker from original
     * schema so we can serialized and deserialize the schema without
     * loss of information.
     */
    boolean isObjectMarker();

    /**
     * True if the complex type definition contains xsd:any (directly or indirectly).
     */
    boolean isXsdAnyMarker();

    /**
     * True if the complex type definition is a type dedicated to hold so-called
     * https://docs.evolveum.com/midpoint/devel/design/xml-json-yaml-vs-xnode-vs-internal-data/heterogeneous-lists/[heterogeneous
     * lists]. See also {@link com.evolveum.midpoint.util.DOMUtil#IS_LIST_ATTRIBUTE_NAME} and
     * {@link ItemDefinition#isHeterogeneousListItem()}.
     */
    @Experimental
    boolean isListMarker();

    /** Type name for items that are not explicitly defined in this CTD. */
    @Nullable QName getDefaultItemTypeName();

    /**
     * When resolving unqualified names for items contained in this CTD, what should be the default namespace
     * to look into at first. Currently does NOT apply recursively (to inner CTDs).
     *
     * Set by parsing `defaultNamespace` XSD annotation.
     */
    @Nullable
    String getDefaultNamespace();

    /**
     * When resolving unqualified names for items contained in this CTD, what namespace(s) should be ignored.
     * Names in this list are interpreted as a namespace prefixes.
     * Currently does NOT apply recursively (to inner CTDs).
     *
     * Set by parsing `ignoredNamespace` XSD annotations.
     */
    @NotNull
    List<String> getIgnoredNamespaces();

    /**
     * Copies cloned definitions from the other type definition into this one.
     * (TODO remove from the interface?)
     */
    void merge(ComplexTypeDefinition otherComplexTypeDef);

    /**
     * Returns true if there are no item definitions.
     */
    boolean isEmpty();

    /**
     * Does a shallow clone of this definition (i.e. item definitions themselves are NOT cloned).
     */
    @Override
    @NotNull
    ComplexTypeDefinition clone();

    /**
     * Does a deep clone of this definition.
     */
    @NotNull
    ComplexTypeDefinition deepClone(@NotNull DeepCloneOperation operation);

    /**
     * Trims the definition (and any definitions it refers to) to contain only items related to given paths.
     * USE WITH CARE. Be sure no shared definitions would be affected by this operation!
     */
    void trimTo(@NotNull Collection<ItemPath> paths);

    @Experimental
    boolean hasSubstitutions();

    /**
     * Returns true if item has substitutions in current container definition
     */
    @Experimental
    default boolean hasSubstitutions(QName qName) {
        // Impl for backwards compatibility
        return false;
    }

    @Experimental
    Optional<ItemDefinition<?>> substitution(QName name);

    @Experimental
    default Optional<ItemDefinition<?>> itemOrSubstitution(QName name) {
        ItemDefinition<?> itemDef = findLocalItemDefinition(name);
        if (itemDef != null) {
            return Optional.of(itemDef);
        }
        return substitution(name);
    }

    @Override
    ComplexTypeDefinitionMutator mutator();

    @Experimental
    default List<PrismPropertyDefinition<?>> getXmlAttributeDefinitions() {
        return Collections.emptyList();
    }

    @Experimental
    default boolean isStrictAnyMarker() {
        return false;
    }

    /** This allows to distinguish between missing and explicitly removed definitions. */
    default boolean isItemDefinitionRemoved(QName itemName) {
        return false;
    }

    @Experimental
    default boolean hasOperationalOnlyItems() {
        return false;
    }

    default List<PrismPropertyDefinition<?>> getPropertyDefinitions() {
        List<PrismPropertyDefinition<?>> props = new ArrayList<>();
        for (ItemDefinition<?> def : getDefinitions()) {
            if (def instanceof PrismPropertyDefinition<?> propertyDefinition) {
                props.add(propertyDefinition);
            }
        }
        return props;
    }

    /** A hook to migrate the value after this definition was applied to it. */
    @Experimental
    default @NotNull <C extends Containerable> PrismContainerValue<C> migrateIfNeeded(@NotNull PrismContainerValue<C> value) {
        return value;
    }

    /** Accepts information about this complex type definition during schema parsing. */
    interface ComplexTypeDefinitionLikeBuilder
            extends TypeDefinitionLikeBuilder,
            PrismPresentationDefinition.Mutable,
            PrismLifecycleDefinition.Mutable {

        // getters

        @NotNull QName getTypeName();
        boolean isRuntimeSchema();
        boolean isContainerMarker();

        // setters

        void setAbstract(boolean value);
        void setContainerMarker(boolean value);
        void setObjectMarker(boolean value);
        void setReferenceMarker(boolean value);
        void setListMarker(boolean value);

        void setExtensionForType(QName typeName);
        void setDefaultItemTypeName(QName value);
        void setDefaultNamespace(String value);
        void setIgnoredNamespaces(List<String> ignoredNamespaces);
        void setXsdAnyMarker(boolean value);
        void setStrictAnyMarker(boolean marker);

        void addXmlAttributeDefinition(PrismPropertyDefinition<?> attributeDef);

        void setRuntimeSchema(boolean value);

        /** Should provide and register compile time class. */
        void add(DefinitionFragmentBuilder builder);

        // creating other builders

        <T> PrismPropertyLikeDefinitionBuilder<T> newPropertyLikeDefinition(QName elementName, QName typeName);
        ItemDefinitionLikeBuilder newContainerLikeDefinition(QName itemName, AbstractTypeDefinition ctd);
        ItemDefinitionLikeBuilder newObjectLikeDefinition(QName itemName, AbstractTypeDefinition ctd);

        /**
         * Returns a set of "extra" features for the CTD-like definition currently being built.
         * These are features that are not processed by the standard parser; they are known only to the upper layers.
         * The input for parsing CTD-like definitions is {@link XSComplexType}, so they must accept it.
         *
         * All these features must be applicable to "this" builder. I am not sure how to state this in Java.
         * The workaround is {@link DefinitionFeature#asForBuilder(Class)} method.
         */
        default Collection<DefinitionFeature<?, ?, ? super XSComplexType, ?>> getExtraFeaturesToParse() {
            return List.of();
        }
    }

    /**
     * An interface to mutate the definition of a complex type.
     *
     * TODO document the interface (e.g. what should {@link #add(ItemDefinition)} method do
     *   in the case of duplicate definitions, etc)
     */
    interface ComplexTypeDefinitionMutator extends TypeDefinitionMutator {

        void add(ItemDefinition<?> definition);

        void delete(QName itemName);

        // TODO return builder instead of mutator
        PrismPropertyDefinitionMutator<?> createPropertyDefinition(QName name, QName typeName);

        // TODO return builder instead of mutator
        PrismPropertyDefinitionMutator<?> createPropertyDefinition(String name, QName typeName);

        @NotNull
        ComplexTypeDefinition clone();

        /**
         * Replaces a definition for an item with given name.
         *
         * TODO specify the behavior more precisely
         */
        void replaceDefinition(@NotNull QName itemName, ItemDefinition<?> newDefinition);

        @Experimental
        void addSubstitution(ItemDefinition<?> itemDef, ItemDefinition<?> maybeSubst);

        default void setValueMigrator(ValueMigrator valueMigrator) {
            // no-op to avoid the need of implementing in various implementations
        }
    }

    /** TODO decide about this */
    @Experimental
    interface ValueMigrator {
        @NotNull <C extends Containerable> PrismContainerValue<C> migrateIfNeeded(@NotNull PrismContainerValue<C> value);
    }
}
