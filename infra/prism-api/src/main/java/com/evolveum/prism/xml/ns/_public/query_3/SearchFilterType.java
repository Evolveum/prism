/*
 * Copyright (c) 2010-2020 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.prism.xml.ns._public.query_3;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.evolveum.midpoint.prism.xnode.*;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.binding.PlainStructured;
import com.evolveum.midpoint.prism.binding.StructuredEqualsStrategy;
import com.evolveum.midpoint.prism.binding.StructuredHashCodeStrategy;
import com.evolveum.midpoint.util.DOMUtil;
import com.evolveum.midpoint.util.DebugDumpable;
import com.evolveum.midpoint.util.DebugUtil;
import com.evolveum.midpoint.util.QNameUtil;
import com.evolveum.midpoint.util.exception.SchemaException;

@XmlAccessorType(XmlAccessType.NONE)        // we select getters/fields to expose via JAXB individually
@XmlType(name = "SearchFilterType", propOrder = {       // no prop order, because we serialize this class manually
        // BTW, the order is the following: description, filterClause
})

public class SearchFilterType extends AbstractFreezable implements PlainStructured, Serializable, Cloneable, DebugDumpable, Freezable, JaxbVisitable { // FIXME: Still we need also old Equals, HashCode until switch is made in midpoint

    private static final long serialVersionUID = 201303040000L;

    public static final QName COMPLEX_TYPE = new QName(PrismConstants.NS_QUERY, "SearchFilterType");
    public static final QName F_DESCRIPTION = new QName(PrismConstants.NS_QUERY, "description");
    public static final QName F_TEXT = new QName(PrismConstants.NS_QUERY, "text");

    @XmlElement
    protected String description;

    @XmlElement
    protected String text;

    // this one is not exposed via JAXB
    protected MapXNode filterClauseXNode; // single-subnode map node (key = filter element qname, value = contents)

    /**
     * Creates a new {@code QueryType} instance.
     */
    public SearchFilterType() {
        // CC-XJC Version 2.0 Build 2011-09-16T18:27:24+0000
        super();
    }

    /**
     * Creates a new {@code QueryType} instance by deeply copying a given {@code QueryType} instance.
     *
     * @param o The instance to copy.
     * @throws NullPointerException if {@code o} is {@code null}.
     */
    public SearchFilterType(final SearchFilterType o) {
        // CC-XJC Version 2.0 Build 2011-09-16T18:27:24+0000
        super();
        Objects.requireNonNull(o, "Cannot create a copy of 'SearchFilterType' from 'null'.");
        this.description = o.description;
        this.text = o.text;
        this.filterClauseXNode = o.filterClauseXNode.clone();
    }

    public SearchFilterType(String text, PrismNamespaceContext namespaceContext) {
        super();
        this.text = text;
        var xnodeFactory = PrismContext.get().xnodeFactory();
        var textNode = xnodeFactory.primitive(text, namespaceContext.inherited());
        filterClauseXNode = xnodeFactory.map(namespaceContext, F_TEXT, textNode);
    }

    public String getDescription() {
        return description;
    }

    public String getText() {
        return text;
    }

    public void setDescription(String description) {
        checkMutable();
        this.description = description;
    }

    public void setText(String text) {
        checkMutable();
        this.text = text;
    }

    public boolean containsFilterClause() {
        return filterClauseXNode != null && !filterClauseXNode.isEmpty();
    }

    public void setFilterClauseXNode(MapXNode filterClauseXNode) {
        checkMutable();
        this.filterClauseXNode = filterClauseXNode;
    }

    public void setFilterClauseXNode(RootXNode filterClauseNode) {
        checkMutable();
        if (filterClauseNode == null) {
            this.filterClauseXNode = null;
        } else {
            this.filterClauseXNode = filterClauseNode.toMapXNode();
        }
    }

    public MapXNode getFilterClauseXNode() {
        if (this.filterClauseXNode == null) {
            return null;
        } else {
            return this.filterClauseXNode.clone();
        }
    }

    public RootXNode getFilterClauseAsRootXNode() throws SchemaException {
        MapXNode clause = getFilterClauseXNode();
        return clause != null ? clause.getSingleSubEntryAsRoot("getFilterClauseAsRootXNode") : null;
    }

    public static SearchFilterType createFromParsedXNode(XNode xnode, ParsingContext pc) throws SchemaException {
        SearchFilterType filter = new SearchFilterType();
        filter.parseFromXNode(xnode, pc);
        return filter;
    }

    public void parseFromXNode(XNode xnode, ParsingContext pc) throws SchemaException {
        checkMutable();
        if (xnode == null || xnode.isEmpty()) {
            this.filterClauseXNode = null;
            this.description = null;
            this.text = null;
        } else {
            if (!(xnode instanceof MapXNode xmap)) {
                throw new SchemaException("Cannot parse filter from " + xnode);
            }

            setDescription(parseString(xmap, F_DESCRIPTION, "Description"));
            setText(parseString(xmap, F_TEXT, "Text"));
            Map<QName, XNode> filterMap = new HashMap<>();
            for (QName key : xmap.keySet()) {
                if (!QNameUtil.match(key, SearchFilterType.F_DESCRIPTION) && !QNameUtil.match(key, new QName("condition"))) {
                    if (text == null || QNameUtil.match(key, F_TEXT)) {
                        // Add elements if Axiom is null, or add only axiom variant to filterMap
                        filterMap.put(key, xmap.get(key));
                    }
                }
            }
            if (filterMap.size() > 1) {
                throw new SchemaException("Filter clause has more than one item: " + filterMap);
            }
            this.filterClauseXNode = PrismContext.get().xnodeFactory().map(xmap.namespaceContext(), filterMap);
            PrismContext.get().getQueryConverter().parseFilterPreliminarily(this.filterClauseXNode, pc);
        }
    }

    private static String parseString(MapXNode xmap, QName name, String displayName) throws SchemaException {
        XNode xdesc = xmap.get(name);
        if (xdesc != null) {
            if (xdesc instanceof PrimitiveXNode<?>) {
                return ((PrimitiveXNode<?>) xdesc).getParsedValue(DOMUtil.XSD_STRING, String.class);
            } else {
                throw new SchemaException(displayName + " must have a primitive value");
            }
        }
        return null;
    }

    public MapXNode serializeToXNode(PrismContext prismContext) throws SchemaException {
        MapXNode xmap = getFilterClauseXNode();
        if (description == null && text == null) {
            return xmap;
        } else {
            // we have to serialize the map in correct order (see MID-1847): description first, text second, filter clause next
            Map<QName, XNode> newXMap = new HashMap<>();
            if (description != null) {
                newXMap.put(SearchFilterType.F_DESCRIPTION, prismContext.xnodeFactory().primitive(description));
            }
            if (text != null) {
                newXMap.put(SearchFilterType.F_TEXT, prismContext.xnodeFactory().primitive(text));
            }
            if (xmap != null && !xmap.isEmpty()) {
                Map.Entry<QName, ? extends XNode> filter = xmap.getSingleSubEntry("search filter");
                newXMap.put(filter.getKey(), filter.getValue());
            }
            return prismContext.xnodeFactory().map(newXMap);
        }
    }

    /**
     * Generates a String representation of the contents of this type.
     * This is an extension method, produced by the 'ts' xjc plugin
     */
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode(StructuredHashCodeStrategy strategy) {
        int currentHashCode = 1;
        MapXNode theFilter = this.filterClauseXNode;
        currentHashCode = strategy.hashCode(currentHashCode, theFilter);
        return currentHashCode;
    }

    @Override
    public int hashCode() {
        return this.hashCode(StructuredHashCodeStrategy.DEFAULT);
    }

    @Override
    public boolean equals(Object object, StructuredEqualsStrategy strategy) {
        if (!(object instanceof SearchFilterType that)) {
            return false;
        }
        if (this == object) {
            return true;
        }

        if (filterClauseXNode == null) {
            if (that.filterClauseXNode != null) { return false; }
        } else if (!filterClauseXNode.equals(that.filterClauseXNode)) { return false; }

        if (text == null) {
            if (that.text != null) { return false; }
        } else if (!text.equals(that.text)) { return false; }

        return true;
    }

    @Override
    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    public boolean equals(Object object) {
        return equals(object, StructuredEqualsStrategy.DOM_AWARE);
    }

    /**
     * Creates and returns a deep copy of this object.
     *
     * @return A deep copy of this object.
     */
    @SuppressWarnings("MethodDoesntCallSuperMethod")
    @Override
    public SearchFilterType clone() {
        final SearchFilterType clone;
        try {
            clone = this.getClass().newInstance(); // TODO fix this using super.clone()
        } catch (InstantiationException | IllegalAccessException e) {
            throw new IllegalStateException("Couldn't instantiate " + this.getClass() + ": " + e.getMessage(), e);
        }
        clone.description = this.description;
        clone.text = this.text;
        if (this.filterClauseXNode != null) {
            clone.filterClauseXNode = this.filterClauseXNode.clone();
        }
        return clone;
    }

    @Override
    public String debugDump(int indent) {
        StringBuilder sb = new StringBuilder();
        DebugUtil.indentDebugDump(sb, indent);
        sb.append("SearchFilterType");
        if (description != null) {
            sb.append("\n");
            DebugUtil.debugDumpWithLabel(sb, "description", description, indent + 1);
        }
        if (text != null) {
            sb.append("\n");
            DebugUtil.debugDumpWithLabel(sb, "text", text, indent + 1);
        }
        if (filterClauseXNode != null) {
            sb.append("\n");
            DebugUtil.debugDumpWithLabel(sb, "filterClauseXNode", filterClauseXNode, indent + 1);
        }
        return sb.toString();
    }

    @Override
    protected void performFreeze() {
        if (filterClauseXNode != null) {
            filterClauseXNode.freeze();
        }
    }

    @Override
    public void accept(JaxbVisitor visitor) {
        visitor.visit(this);
    }
}
