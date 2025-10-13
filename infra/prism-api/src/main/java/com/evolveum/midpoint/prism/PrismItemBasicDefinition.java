/*
 * Copyright (C) 2010-2024 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.prism;

import com.evolveum.midpoint.prism.path.ItemName;

import org.jetbrains.annotations.NotNull;

import javax.xml.namespace.QName;
import java.io.Serializable;
import java.util.Objects;

/** "Basic" aspect of a prism item definition (item/type name, cardinality). */
public interface PrismItemBasicDefinition {

    /**
     * Gets the "canonical" name of the item for the definition.
     * Should be qualified, if at all possible.
     */
    @NotNull ItemName getItemName();

    /** See {@link Definition#getTypeName()}. */
    @NotNull QName getTypeName();

    /** Returns the number of minimal value occurrences. */
    int getMinOccurs();

    /** Returns the number of maximal value occurrences. Any negative number means "unbounded". */
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

    interface Delegable extends PrismItemBasicDefinition {

        @NotNull PrismItemBasicDefinition itemBasicDefinition();

        default @NotNull ItemName getItemName() {
            return itemBasicDefinition().getItemName();
        }

        default @NotNull QName getTypeName() {
            return itemBasicDefinition().getTypeName();
        }

        default int getMinOccurs() {
            return itemBasicDefinition().getMinOccurs();
        }

        default int getMaxOccurs() {
            return itemBasicDefinition().getMaxOccurs();
        }
    }

    interface Mutable {

        void setMinOccurs(int value);
        void setMaxOccurs(int value);

        interface Delegable extends Mutable {

            @NotNull PrismItemBasicDefinition.Mutable itemBasicDefinition();

            default void setMinOccurs(int value) {
                itemBasicDefinition().setMinOccurs(value);
            }

            default void setMaxOccurs(int value) {
                itemBasicDefinition().setMaxOccurs(value);
            }
        }
    }

    class Data
            extends AbstractFreezable
            implements PrismItemBasicDefinition, PrismItemBasicDefinition.Mutable, Serializable {

        @NotNull private final ItemName itemName;
        @NotNull private final QName typeName;
        private int minOccurs = 1;
        private int maxOccurs = 1;

        public Data(@NotNull ItemName itemName, @NotNull QName typeName) {
            this.itemName = itemName;
            this.typeName = typeName;
        }

        @Override
        public @NotNull ItemName getItemName() {
            return itemName;
        }

        @Override
        public @NotNull QName getTypeName() {
            return typeName;
        }

        @Override
        public int getMinOccurs() {
            return minOccurs;
        }

        @Override
        public int getMaxOccurs() {
            return maxOccurs;
        }

        @Override
        public void setMinOccurs(int value) {
            checkMutable();
            minOccurs = value;
        }

        @Override
        public void setMaxOccurs(int value) {
            checkMutable();
            maxOccurs = value;
        }

        public void copyFrom(Data source) {
            checkMutable();
            minOccurs = source.minOccurs;
            maxOccurs = source.maxOccurs;
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
            return minOccurs == data.minOccurs
                    && maxOccurs == data.maxOccurs
                    && Objects.equals(itemName, data.itemName)
                    && Objects.equals(typeName, data.typeName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(itemName, typeName, minOccurs, maxOccurs);
        }
    }
}
