/*
 * Copyright (c) 2010-2018 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.prism.impl.xnode;

import com.evolveum.midpoint.prism.xnode.MapXNode;
import com.evolveum.midpoint.prism.xnode.MetadataAware;
import com.evolveum.midpoint.prism.xnode.SchemaXNode;
import com.evolveum.midpoint.prism.xnode.XNode;

import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Element;

import com.evolveum.midpoint.prism.PrismNamespaceContext;
import com.evolveum.midpoint.prism.Visitor;
import com.evolveum.midpoint.util.DOMUtil;
import com.evolveum.midpoint.util.DebugUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Radovan Semancik
 *
 */
public class SchemaXNodeImpl extends XNodeImpl implements SchemaXNode {



    private Element schemaElement;

    @NotNull private List<MapXNode> metadataNodes = new ArrayList<>();

    public SchemaXNodeImpl() {
        super();
    }

    public SchemaXNodeImpl(PrismNamespaceContext local) {
        super(local);
    }

    public Element getSchemaElement() {
        return schemaElement;
    }

    public void setSchemaElement(Element schemaElement) {
        this.schemaElement = schemaElement;
    }

    @Override
    public boolean isEmpty() {
        return schemaElement == null || DOMUtil.isEmpty(schemaElement);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
        MetadataAware.visitMetadata(this, visitor);
    }

    @Override
    public String debugDump(int indent) {
        StringBuilder sb = new StringBuilder();
        DebugUtil.indentDebugDump(sb, indent);
        if (schemaElement == null) {
            sb.append("Schema: null");
        } else {
            sb.append("Schema: present");
        }
        String dumpSuffix = dumpSuffix();
        if (dumpSuffix != null) {
            sb.append(dumpSuffix);
        }
        appendMetadata(sb, indent, metadataNodes);
        return sb.toString();
    }

    @Override
    public String getDesc() {
        return "schema";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SchemaXNodeImpl that = (SchemaXNodeImpl) o;

        if (!metadataEquals(metadataNodes, that.metadataNodes)) {
            return false;
        }

        if (schemaElement == null) {
            return that.schemaElement == null;
        }
        if (that.schemaElement == null) {
            return false;
        }
        return DOMUtil.compareElement(schemaElement, that.schemaElement, false);
    }

    @Override
    public int hashCode() {
        return 1;               // the same as in DomAwareHashCodeStrategy
    }

    @Override
    public @NotNull List<MapXNode> getMetadataNodes() {
        return metadataNodes;
    }

    @Override
    public void setMetadataNodes(@NotNull List<MapXNode> metadataNodes) {
        this.metadataNodes = metadataNodes;
    }

    @Override
    public @NotNull XNodeImpl clone() {
        SchemaXNodeImpl clone = (SchemaXNodeImpl) super.clone();
        MetadataAware.cloneMetadata(clone, this);
        return clone;
    }

    @Override
    public XNode copy() {
        if(isImmutable()) {
            return this;
        }
        return clone();
    }
}
