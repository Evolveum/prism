/*
 * Copyright (C) 2010-2024 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Objects;

/** All other aspects of an item definition. */
public interface PrismItemMiscDefinition {

    /**
     * Level of processing (ignore, minimal, auto, full) for this item/type.
     */
    ItemProcessing getProcessing();

    interface Delegable extends PrismItemMiscDefinition {

        @NotNull PrismItemMiscDefinition itemMiscDefinition();

        @Override
        default ItemProcessing getProcessing() {
            return itemMiscDefinition().getProcessing();
        }
    }

    interface Mutable {

        void setProcessing(ItemProcessing value);

        interface Delegable extends Mutable {

            @NotNull PrismItemMiscDefinition.Mutable itemMiscDefinition();

            @Override
            default void setProcessing(ItemProcessing value) {
                itemMiscDefinition().setProcessing(value);
            }
        }
    }

    class Data
            extends AbstractFreezable
            implements PrismItemMiscDefinition, PrismItemMiscDefinition.Mutable, Serializable {

        private ItemProcessing processing;

        @Override
        public ItemProcessing getProcessing() {
            return processing;
        }

        @Override
        public void setProcessing(ItemProcessing value) {
            checkMutable();
            this.processing = value;
        }

        public void copyFrom(Data source) {
            checkMutable();
            this.processing = source.processing;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Data data = (Data) o;
            return processing == data.processing;
        }

        @Override
        public int hashCode() {
            return Objects.hash(processing);
        }
    }
}
