/*
 * Copyright (C) 2010-2024 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.prism;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;

import com.evolveum.midpoint.prism.annotation.ItemDiagramSpecification;
import com.evolveum.midpoint.prism.util.CloneUtil;
import com.evolveum.midpoint.util.MiscUtil;
import com.evolveum.midpoint.util.annotation.Experimental;

/** Presentation and documentation related aspects (originally from {@link Definition}). */
public interface PrismPresentationDefinition {

    /**
     * Enumeration annotation that specifies how/whether the item should be displayed.
     *
     * It is also a replacement for the old "emphasized" annotation.
     */
    DisplayHint getDisplayHint();

    /**
     * True for definitions that are more important than others and that should be emphasized
     * during presentation. E.g. the emphasized definitions will always be displayed in the user
     * interfaces (even if they are empty), they will always be included in the dumps, etc.
     *
     * TODO probably deprecated, isn't it?
     */
    boolean isEmphasized();

    /**
     * Returns display name.
     *
     * Specifies the printable name of the object class or attribute. It must
     * contain a printable string. It may also contain a key to catalog file.
     *
     * Returns null if no display name is set.
     *
     * Corresponds to "displayName" XSD annotation.
     *
     * @return display name string or catalog key
     */
    String getDisplayName();

    /**
     * Specifies an order in which the item should be displayed relative to other items
     * at the same level. The items will be displayed by sorting them by the
     * values of displayOrder annotation (ascending). Items that do not have
     * any displayOrder annotation will be displayed last. The ordering of
     * values with the same displayOrder is undefined and it may be arbitrary.
     */
    Integer getDisplayOrder();

    /**
     * Returns help string.
     *
     * Specifies the help text or a key to catalog file for a help text. The
     * help text may be displayed in any suitable way by the GUI. It should
     * explain the meaning of an attribute or object class.
     *
     * Returns null if no help string is set.
     *
     * Corresponds to "help" XSD annotation.
     *
     * @return help string or catalog key
     */
    String getHelp();

    /**
     * Must contains <documentation> tag because of html tags used in text.
     */
    String getDocumentation();

    @Experimental
    List<ItemDiagramSpecification> getDiagrams();

    /**
     * Returns only a first sentence of documentation.
     */
    String getDocumentationPreview();

    interface Delegable extends PrismPresentationDefinition {

        @NotNull PrismPresentationDefinition prismPresentationDefinition();

        @Override
        default DisplayHint getDisplayHint() {
            return prismPresentationDefinition().getDisplayHint();
        }

        @Override
        default boolean isEmphasized() {
            return prismPresentationDefinition().isEmphasized();
        }

        @Override
        default String getDisplayName() {
            return prismPresentationDefinition().getDisplayName();
        }

        @Override
        default Integer getDisplayOrder() {
            return prismPresentationDefinition().getDisplayOrder();
        }

        @Override
        default String getHelp() {
            return prismPresentationDefinition().getHelp();
        }

        @Override
        default String getDocumentation() {
            return prismPresentationDefinition().getDocumentation();
        }

        @Override
        default List<ItemDiagramSpecification> getDiagrams() {
            return prismPresentationDefinition().getDiagrams();
        }

        @Override
        default String getDocumentationPreview() {
            return prismPresentationDefinition().getDocumentationPreview();
        }
    }

    interface Mutable {

        void setDisplayHint(DisplayHint displayHint);
        void setEmphasized(boolean emphasized);
        void setDisplayName(String displayName);
        void setDisplayOrder(Integer displayOrder);
        void setHelp(String help);
        void setDocumentation(String documentation);
        void setDiagrams(List<ItemDiagramSpecification> value);

        interface Delegable extends Mutable {

            @NotNull PrismPresentationDefinition.Mutable prismPresentationDefinition();

            @Override
            default void setDisplayHint(DisplayHint displayHint) {
                prismPresentationDefinition().setDisplayHint(displayHint);
            }

            @Override
            default void setEmphasized(boolean emphasized) {
                prismPresentationDefinition().setEmphasized(emphasized);
            }

            @Override
            default void setDisplayName(String displayName) {
                prismPresentationDefinition().setDisplayName(displayName);
            }

            @Override
            default void setDisplayOrder(Integer displayOrder) {
                prismPresentationDefinition().setDisplayOrder(displayOrder);
            }

            @Override
            default void setHelp(String help) {
                prismPresentationDefinition().setHelp(help);
            }

            @Override
            default void setDocumentation(String documentation) {
                prismPresentationDefinition().setDocumentation(documentation);
            }

            @Override
            default void setDiagrams(List<ItemDiagramSpecification> value) {
                prismPresentationDefinition().setDiagrams(value);
            }
        }
    }

    class Data
            extends AbstractFreezable
            implements PrismPresentationDefinition, PrismPresentationDefinition.Mutable, Serializable {

        private DisplayHint displayHint;
        private boolean emphasized;
        private String displayName;
        private Integer displayOrder;
        private String help;
        private String documentation;
        private List<ItemDiagramSpecification> diagrams;

        @Override
        public DisplayHint getDisplayHint() {
            return displayHint;
        }

        @Override
        public void setDisplayHint(DisplayHint displayHint) {
            checkMutable();
            this.displayHint = displayHint;
        }

        @Override
        public boolean isEmphasized() {
            return emphasized || displayHint == DisplayHint.EMPHASIZED;
        }

        @Override
        public void setEmphasized(boolean emphasized) {
            checkMutable();
            this.emphasized = emphasized;
        }

        @Override
        public String getDisplayName() {
            return displayName;
        }

        @Override
        public void setDisplayName(String displayName) {
            checkMutable();
            this.displayName = displayName;
        }

        @Override
        public Integer getDisplayOrder() {
            return displayOrder;
        }

        @Override
        public void setDisplayOrder(Integer displayOrder) {
            checkMutable();
            this.displayOrder = displayOrder;
        }

        @Override
        public String getHelp() {
            return help;
        }

        @Override
        public void setHelp(String help) {
            checkMutable();
            this.help = help;
        }

        @Override
        public String getDocumentation() {
            return documentation;
        }

        @Override
        public void setDocumentation(String documentation) {
            checkMutable();
            this.documentation = documentation;
        }

        @Override
        public List<ItemDiagramSpecification> getDiagrams() {
            return diagrams;
        }

        @Override
        public void setDiagrams(List<ItemDiagramSpecification> value) {
            checkMutable();
            diagrams = value;
        }

        @Override
        public String getDocumentationPreview() {
            return toDocumentationPreview(documentation);
        }

        public void copyFrom(Data source) {
            checkMutable();
            this.displayHint = source.displayHint;
            this.emphasized = source.emphasized;
            this.displayName = source.displayName;
            this.displayOrder = source.displayOrder;
            this.help = source.help;
            this.documentation = source.documentation;
            this.diagrams = CloneUtil.cloneCollectionMembers(source.diagrams);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            Data data = (Data) o;
            return emphasized == data.emphasized
                    && displayHint == data.displayHint
                    && Objects.equals(displayName, data.displayName)
                    && Objects.equals(displayOrder, data.displayOrder)
                    && Objects.equals(help, data.help)
                    && Objects.equals(documentation, data.documentation)
                    && Objects.equals(diagrams, data.diagrams);
        }

        @Override
        public int hashCode() {
            return Objects.hash(displayHint, emphasized, displayName, displayOrder, help, documentation, diagrams);
        }
    }

    static String toDocumentationPreview(String documentation) {
        if (documentation == null || documentation.isEmpty()) {
            return documentation;
        }
        String plainDoc = MiscUtil.stripHtmlMarkup(documentation);
        int i = plainDoc.indexOf('.');
        if (i < 0) {
            return plainDoc;
        }
        return plainDoc.substring(0, i + 1);
    }
}
