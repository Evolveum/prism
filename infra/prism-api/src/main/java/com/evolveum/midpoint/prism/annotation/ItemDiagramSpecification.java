/*
 * Copyright (c) 2021 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.prism.annotation;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

import com.evolveum.midpoint.util.annotation.Experimental;

@SuppressWarnings("ClassCanBeRecord")
@Experimental
public class ItemDiagramSpecification implements Serializable {

    @Serial private static final long serialVersionUID = 1L;

    private final String name;
    private final DiagramElementFormType form;
    private final DiagramElementInclusionType inclusion;
    private final DiagramElementInclusionType subitemInclusion;

    public ItemDiagramSpecification(String name, DiagramElementFormType form, DiagramElementInclusionType inclusion, DiagramElementInclusionType subitemInclusion) {
        this.name = name;
        this.form = form;
        this.inclusion = inclusion;
        this.subitemInclusion = subitemInclusion;
    }

    public String getName() {
        return name;
    }

    public DiagramElementFormType getForm() {
        return form;
    }

    public DiagramElementInclusionType getInclusion() { return inclusion; }

    public DiagramElementInclusionType getSubitemInclusion() { return subitemInclusion; }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ItemDiagramSpecification that = (ItemDiagramSpecification) o;
        return Objects.equals(name, that.name) && form == that.form && inclusion == that.inclusion && subitemInclusion == that.subitemInclusion;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, form, inclusion, subitemInclusion);
    }
}
