/*
 * Copyright (C) 2010-2024 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism;

import javax.xml.namespace.QName;
import java.util.List;
import java.util.Objects;

public class Merge {

    private String merger;

    private List<QName> identifiers;

    public Merge() {
    }

    public Merge(String merger, List<QName> identifiers) {
        this.merger = merger;
        this.identifiers = identifiers;
    }

    public String getMerger() {
        return merger;
    }

    public void setMerger(String merger) {
        this.merger = merger;
    }

    public List<QName> getIdentifiers() {
        if (identifiers == null) {
            identifiers = List.of();
        }
        return identifiers;
    }

    public void setIdentifiers(List<QName> identifiers) {
        this.identifiers = identifiers;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Merge mergeType = (Merge) o;
        return Objects.equals(merger, mergeType.merger) && Objects.equals(identifiers, mergeType.identifiers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(merger, identifiers);
    }

    @Override
    public String toString() {
        return "MergeType{" +
                "merger='" + merger + '\'' +
                ", identifiers=" + identifiers +
                '}';
    }
}
