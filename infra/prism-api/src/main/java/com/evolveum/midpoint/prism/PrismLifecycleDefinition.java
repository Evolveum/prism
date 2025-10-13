/*
 * Copyright (C) 2010-2024 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.prism;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/** Presentation and documentation related aspects (originally from {@link Definition}). */
public interface PrismLifecycleDefinition {

    boolean isDeprecated();

    String getDeprecatedSince();

    /**
     * Version of data model in which the item is likely to be removed.
     * This annotation is used for deprecated item to indicate imminent incompatibility in future versions of data model.
     */
    String getPlannedRemoval();

    boolean isRemoved();

    String getRemovedSince();

    /**
     * Experimental functionality is not stable and it may be changed in any
     * future release without any warning. Use at your own risk.
     */
    boolean isExperimental();

    @Nullable List<SchemaMigration> getSchemaMigrations();

    interface Delegable extends PrismLifecycleDefinition {

        @NotNull PrismLifecycleDefinition prismLifecycleDefinition();

        @Override
        default boolean isDeprecated() {
            return prismLifecycleDefinition().isDeprecated();
        }

        @Override
        default boolean isRemoved() {
            return prismLifecycleDefinition().isRemoved();
        }

        @Override
        default String getRemovedSince() {
            return prismLifecycleDefinition().getRemovedSince();
        }

        @Override
        default boolean isExperimental() {
            return prismLifecycleDefinition().isExperimental();
        }

        @Override
        default String getPlannedRemoval() {
            return prismLifecycleDefinition().getPlannedRemoval();
        }

        @Override
        default String getDeprecatedSince() {
            return prismLifecycleDefinition().getDeprecatedSince();
        }

        @Override
        default @Nullable List<SchemaMigration> getSchemaMigrations() {
            return prismLifecycleDefinition().getSchemaMigrations();
        }
    }

    interface Mutable {

        void setDeprecated(boolean deprecated);
        void setRemoved(boolean removed);
        void setRemovedSince(String removedSince);
        void setExperimental(boolean experimental);
        void setPlannedRemoval(String plannedRemoval);
        void setDeprecatedSince(String deprecatedSince);
        void addSchemaMigration(SchemaMigration value);
        void setSchemaMigrations(List<SchemaMigration> value);

        interface Delegable extends Mutable {

            @NotNull PrismLifecycleDefinition.Mutable prismLifecycleDefinition();

            @Override
            default void setDeprecated(boolean value) {
                prismLifecycleDefinition().setDeprecated(value);
            }

            @Override
            default void setRemoved(boolean value) {
                prismLifecycleDefinition().setRemoved(value);
            }

            @Override
            default void setRemovedSince(String value) {
                prismLifecycleDefinition().setRemovedSince(value);
            }

            @Override
            default void setExperimental(boolean value) {
                prismLifecycleDefinition().setExperimental(value);
            }

            @Override
            default void setPlannedRemoval(String value) {
                prismLifecycleDefinition().setPlannedRemoval(value);
            }

            @Override
            default void setDeprecatedSince(String value) {
                prismLifecycleDefinition().setDeprecatedSince(value);
            }

            @Override
            default void addSchemaMigration(SchemaMigration value) {
                prismLifecycleDefinition().addSchemaMigration(value);
            }
        }
    }

    class Data
            extends AbstractFreezable
            implements PrismLifecycleDefinition, PrismLifecycleDefinition.Mutable {

        private boolean deprecated;
        private boolean removed;
        private String removedSince;
        private boolean experimental;
        private String plannedRemoval;
        private String deprecatedSince;
        private List<SchemaMigration> schemaMigrations;

        @Override
        public boolean isDeprecated() {
            return deprecated;
        }

        @Override
        public void setDeprecated(boolean deprecated) {
            checkMutable();
            this.deprecated = deprecated;
        }

        @Override
        public boolean isRemoved() {
            return removed;
        }

        @Override
        public void setRemoved(boolean removed) {
            checkMutable();
            this.removed = removed;
        }

        @Override
        public String getRemovedSince() {
            return removedSince;
        }

        @Override
        public void setRemovedSince(String removedSince) {
            checkMutable();
            this.removedSince = removedSince;
        }

        @Override
        public boolean isExperimental() {
            return experimental;
        }

        @Override
        public void setExperimental(boolean experimental) {
            checkMutable();
            this.experimental = experimental;
        }

        @Override
        public String getPlannedRemoval() {
            return plannedRemoval;
        }

        @Override
        public void setPlannedRemoval(String plannedRemoval) {
            checkMutable();
            this.plannedRemoval = plannedRemoval;
        }

        @Override
        public String getDeprecatedSince() {
            return deprecatedSince;
        }

        @Override
        public void setDeprecatedSince(String deprecatedSince) {
            checkMutable();
            this.deprecatedSince = deprecatedSince;
        }

        @Override
        public @Nullable List<SchemaMigration> getSchemaMigrations() {
            return schemaMigrations;
        }

        @Override
        public void addSchemaMigration(SchemaMigration value) {
            if (schemaMigrations == null) {
                schemaMigrations = new ArrayList<>();
            }
            if (!schemaMigrations.contains(value)) {
                schemaMigrations.add(value);
            }
        }

        @Override
        public void setSchemaMigrations(List<SchemaMigration> value) {
            checkMutable();
            schemaMigrations = value;
        }

        // TODO copyFrom?
    }
}
