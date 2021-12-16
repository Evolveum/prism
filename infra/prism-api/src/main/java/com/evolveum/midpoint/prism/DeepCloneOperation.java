/*
 * Copyright (C) 2010-2021 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Represents the "deep clone" and "ultra deep clone" operations on items and complex type definitions.
 *
 * TODO description
 */
public class DeepCloneOperation {

    /**
     * Already cloned definitions when 'ultra deep cloning' is not requested.
     * Each definition is then cloned only once. Null means that we want to go ultra deep,
     * i.e. we want to clone each definition.
     */
    @Nullable private final Map<QName, ComplexTypeDefinition> alreadyCloned;

    /**
     * Already cloned definitions on the path from root to current node;
     * in order to prevent infinite loops when doing ultra deep cloning.
     */
    @NotNull private final Map<QName, ComplexTypeDefinition> alreadyClonedAncestors;

    /**
     * An action that should be invoked after cloning.
     */
    @Nullable private final Consumer<ItemDefinition<?>> postCloneAction;

    private DeepCloneOperation(
            boolean ultraDeep,
            @Nullable Consumer<ItemDefinition<?>> postCloneAction) {
        this.alreadyCloned = ultraDeep ? null : new HashMap<>();
        this.alreadyClonedAncestors = new HashMap<>();
        this.postCloneAction = postCloneAction;
    }

    /**
     * Deep but not ultra-deep clone operation. We will reuse already cloned definitions.
     */
    public static @NotNull DeepCloneOperation notUltraDeep() {
        return new DeepCloneOperation(false, null);
    }

    /**
     * Ultra-deep clone operation.
     */
    public static DeepCloneOperation ultraDeep() {
        return new DeepCloneOperation(true, null);
    }

    /**
     * Deep OR ultra-deep clone operation, with given post clone action.
     */
    public static DeepCloneOperation operation(boolean ultraDeep, Consumer<ItemDefinition<?>> postCloneAction) {
        return new DeepCloneOperation(ultraDeep, postCloneAction);
    }

    /**
     * Executes the deep clone operation.
     *
     * @param original Definition that is to be cloned
     * @param cloneSupplier Creates a clone (copy) of the definition
     * @param cloneProcessor Does the custom processing on the clone, e.g. traversing through individual items
     *
     * This method manages the whole process, e.g. by checking if the definition was already cloned, etc.
     *
     * Note that it is clone processor responsibility to invoke {@link #executePostCloneAction(ItemDefinition)}
     * on individual item definitions.
     */
    public ComplexTypeDefinition execute(
            @NotNull ComplexTypeDefinition original,
            @NotNull Supplier<ComplexTypeDefinition> cloneSupplier,
            @NotNull Consumer<ComplexTypeDefinition> cloneProcessor) {
        if (alreadyCloned != null) {
            ComplexTypeDefinition clone = alreadyCloned.get(original.getTypeName());
            if (clone != null) {
                return clone; // already cloned
            }
        }
        ComplexTypeDefinition cloneInParent = alreadyClonedAncestors.get(original.getTypeName());
        if (cloneInParent != null) {
            return cloneInParent;
        }
        ComplexTypeDefinition clone = cloneSupplier.get();
        if (alreadyCloned != null) {
            alreadyCloned.put(original.getTypeName(), clone);
        }
        alreadyClonedAncestors.put(original.getTypeName(), clone);
        cloneProcessor.accept(clone);
        alreadyClonedAncestors.remove(original.getTypeName());

        return clone;
    }

    public void executePostCloneAction(ItemDefinition<?> itemClone) {
        if (postCloneAction != null) {
            postCloneAction.accept(itemClone);
        }
    }
}
