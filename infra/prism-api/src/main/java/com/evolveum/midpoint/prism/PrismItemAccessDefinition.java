/*
 * Copyright (c) 2021 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.prism;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Objects;

public interface PrismItemAccessDefinition {

    /**
     * Returns true if this item can be read (displayed).
     * In case of containers this flag is, strictly speaking, not applicable. Container is an
     * empty shell. What matters is access to individual sub-item. However, for containers this
     * value has a "hint" meaning.  It means that the container itself contains something that is
     * readable. Which can be used as a hint by the presentation to display container label or block.
     * This usually happens if the container contains at least one readable item.
     * This does NOT mean that also all the container items can be displayed. The sub-item permissions
     * are controlled by similar properties on the items. This property only applies to the container
     * itself: the "shell" of the container.
     * <p>
     * Note: It was considered to use a different meaning for this flag - a meaning that would allow
     * canRead()=false containers to have readable items. However, this was found not to be very useful.
     * Therefore the "something readable inside" meaning was confirmed instead.
     */
    boolean canRead();

    /**
     * Returns true if this item can be modified (updated).
     * In case of containers this means that the container itself should be displayed in modification forms
     * E.g. that the container label or block should be displayed. This usually happens if the container
     * contains at least one modifiable item.
     * This does NOT mean that also all the container items can be modified. The sub-item permissions
     * are controlled by similar properties on the items. This property only applies to the container
     * itself: the "shell" of the container.
     */
    boolean canModify();

    /**
     * Returns true if this item can be added: it can be part of an object that is created.
     * In case of containers this means that the container itself should be displayed in creation forms
     * E.g. that the container label or block should be displayed. This usually happens if the container
     * contains at least one createable item.
     * This does NOT mean that also all the container items can be created. The sub-item permissions
     * are controlled by similar properties on the items. This property only applies to the container
     * itself: the "shell" of the container.
     */
    boolean canAdd();

    default @NotNull Info getInfo() {
        return new PrismItemAccessDefinition.Info(canAdd(), canModify(), canRead());
    }

    interface Delegable extends PrismItemAccessDefinition {

        @NotNull PrismItemAccessDefinition itemAccessDefinition();

        @Override
        default boolean canRead() {
            return itemAccessDefinition().canRead();
        }

        @Override
        default boolean canModify() {
            return itemAccessDefinition().canModify();
        }

        @Override
        default boolean canAdd() {
            return itemAccessDefinition().canAdd();
        }
    }

    class Data
            extends AbstractFreezable
            implements PrismItemAccessDefinition, PrismItemAccessDefinition.Mutable, Serializable {

        boolean canRead = true;
        boolean canModify = true;
        boolean canAdd = true;

        @Override
        public boolean canRead() {
            return canRead;
        }

        @Override
        public void setCanRead(boolean canRead) {
            checkMutable();
            this.canRead = canRead;
        }

        @Override
        public boolean canModify() {
            return canModify;
        }

        @Override
        public void setCanModify(boolean canModify) {
            checkMutable();
            this.canModify = canModify;
        }

        @Override
        public boolean canAdd() {
            return canAdd;
        }

        @Override
        public void setCanAdd(boolean canAdd) {
            checkMutable();
            this.canAdd = canAdd;
        }

        public void copyFrom(Data source) {
            checkMutable();
            canRead = source.canRead;
            canModify = source.canModify;
            canAdd = source.canAdd;
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
            return canRead == data.canRead
                    && canModify == data.canModify
                    && canAdd == data.canAdd;
        }

        @Override
        public int hashCode() {
            return Objects.hash(canRead, canModify, canAdd);
        }
    }

    interface Mutable extends PrismItemAccessDefinition {

        void setCanRead(boolean val);

        void setCanModify(boolean val);

        void setCanAdd(boolean val);

        default void setInfo(@NotNull Info info) {
            setCanRead(info.canRead());
            setCanModify(info.canModify());
            setCanAdd(info.canAdd());
        }

        interface Delegable extends Mutable {

            @NotNull PrismItemAccessDefinition.Mutable itemAccessDefinition();

            @Override
            default void setCanRead(boolean val) {
                itemAccessDefinition().setCanRead(val);
            }

            @Override
            default void setCanModify(boolean val) {
                itemAccessDefinition().setCanModify(val);
            }

            @Override
            default void setCanAdd(boolean val) {
                itemAccessDefinition().setCanAdd(val);
            }
        }
    }

    /** Just a DTO. */
    record Info(boolean canAdd, boolean canModify, boolean canRead) {
        public static Info full() {
            return new Info(true, true, true);
        }
    }
}
