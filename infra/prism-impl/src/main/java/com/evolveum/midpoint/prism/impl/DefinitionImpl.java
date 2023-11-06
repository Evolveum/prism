/*
 * Copyright (c) 2010-2018 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism.impl;

import java.util.*;

import javax.xml.namespace.QName;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.annotation.ItemDiagramSpecification;

import com.evolveum.midpoint.util.DOMUtil;

import org.jetbrains.annotations.NotNull;

import com.evolveum.midpoint.prism.xml.XsdTypeMapper;
import com.evolveum.midpoint.util.DebugUtil;
import com.evolveum.midpoint.util.MiscUtil;
import com.evolveum.midpoint.util.PrettyPrinter;

/**
 * Abstract definition in the schema.
 *
 * This is supposed to be a superclass for all definitions. It defines common
 * properties for all definitions.
 *
 * The definitions represent data structures of the schema. Therefore instances
 * of Java objects from this class represent specific <em>definitions</em> from
 * the schema, not specific properties or objects. E.g the definitions does not
 * have any value.
 *
 * To transform definition to a real property or object use the explicit
 * instantiate() methods provided in the definition classes. E.g. the
 * instantiate() method will create instance of Property using appropriate
 * PropertyDefinition.
 *
 * The convenience methods in Schema are using this abstract class to find
 * appropriate definitions easily.
 *
 * @author Radovan Semancik
 *
 */
public abstract class DefinitionImpl extends AbstractFreezable implements MutableDefinition {

    private static final long serialVersionUID = -2643332934312107274L;
    @NotNull protected QName typeName;
    protected boolean isAbstract = false;
    private Map<QName, Object> annotations;
    private List<SchemaMigration> schemaMigrations = null;
    private List<ItemDiagramSpecification> diagrams = null;

    /**
     * This means that this particular definition (of an item or of a type) is part of the runtime schema, e.g.
     * extension schema, resource schema or connector schema or something like that. I.e. it is not defined statically.
     */
    protected boolean isRuntimeSchema;

    DefinitionImpl(@NotNull QName typeName) {
        this.typeName = typeName;
    }

    @Override
    @NotNull
    public QName getTypeName() {
        return typeName;
    }

    @Override
    public void setTypeName(@NotNull QName typeName) {
        checkMutable();
        this.typeName = typeName;
    }

    @Override
    public boolean isIgnored() {
        return getProcessing() == ItemProcessing.IGNORE;
    }

    @Override
    public ItemProcessing getProcessing() {
        return getAnnotation(PrismConstants.A_PROCESSING);
    }

    @Override
    public void setProcessing(ItemProcessing processing) {
        checkMutable();
        setAnnotation(PrismConstants.A_PROCESSING, processing);
    }

    @Override
    public boolean isAbstract() {
        return isAbstract;
    }

    public void setAbstract(boolean isAbstract) {
        checkMutable();
        this.isAbstract = isAbstract;
    }

    @Override
    public boolean isDeprecated() {
        return getAnnotation(PrismConstants.A_DEPRECATED, false);
    }

    @Override
    public void setDeprecated(boolean deprecated) {
        checkMutable();
        setAnnotation(PrismConstants.A_DEPRECATED, deprecated);
    }

    @Override
    public String getDeprecatedSince() {
        return getAnnotation(PrismConstants.A_DEPRECATED_SINCE);
    }

    public void setDeprecatedSince(String deprecatedSince) {
        checkMutable();
        setAnnotation(PrismConstants.A_DEPRECATED_SINCE, deprecatedSince);
    }

    @Override
    public boolean isRemoved() {
        return getAnnotation(PrismConstants.A_REMOVED, false);
    }

    public void setRemoved(boolean removed) {
        checkMutable();
        setAnnotation(PrismConstants.A_REMOVED, removed);
    }

    @Override
    public String getRemovedSince() {
        return getAnnotation(PrismConstants.A_REMOVED_SINCE);
    }

    public void setRemovedSince(String removedSince) {
        checkMutable();
        setAnnotation(PrismConstants.A_REMOVED_SINCE, removedSince);
    }

    @Override
    public boolean isExperimental() {
        return getAnnotation(PrismConstants.A_EXPERIMENTAL, false);
    }

    @Override
    public void setExperimental(boolean experimental) {
        checkMutable();
        setAnnotation(PrismConstants.A_EXPERIMENTAL, experimental);
    }

    @Override
    public String getPlannedRemoval() {
        return getAnnotation(PrismConstants.A_PLANNED_REMOVAL);
    }

    public void setPlannedRemoval(String plannedRemoval) {
        checkMutable();
        setAnnotation(PrismConstants.A_PLANNED_REMOVAL, plannedRemoval);
    }

    @Override
    public boolean isElaborate() {
        return getAnnotation(PrismConstants.A_ELABORATE, false);
    }

    public void setElaborate(boolean elaborate) {
        checkMutable();
        setAnnotation(PrismConstants.A_ELABORATE, elaborate);
    }

    @Override
    public boolean isEmphasized() {
        return getAnnotation(PrismConstants.A_DISPLAY, null) == Display.EMPHASIZED;
    }

    @Override
    public void setEmphasized(boolean emphasized) {
        checkMutable();
        setAnnotation(PrismConstants.A_DISPLAY, emphasized ? Display.EMPHASIZED : null);
    }

    @Override
    public String getDisplayName() {
        return getAnnotation(PrismConstants.A_DISPLAY_NAME);
    }

    @Override
    public void setDisplayName(String displayName) {
        checkMutable();
        setAnnotation(PrismConstants.A_DISPLAY_NAME, displayName);
    }

    @Override
    public Integer getDisplayOrder() {
        return getAnnotation(PrismConstants.A_DISPLAY_ORDER);
    }

    @Override
    public void setDisplayOrder(Integer displayOrder) {
        checkMutable();
        setAnnotation(PrismConstants.A_DISPLAY_ORDER, displayOrder);
    }

    @Override
    public String getHelp() {
        return getAnnotation(PrismConstants.A_HELP);
    }

    @Override
    public void setHelp(String help) {
        checkMutable();
        setAnnotation(PrismConstants.A_HELP, help);
    }

    @Override
    public String getDocumentation() {
        return getAnnotation(DOMUtil.XSD_DOCUMENTATION_ELEMENT);
    }

    @Override
    public void setDocumentation(String documentation) {
        checkMutable();
        setAnnotation(DOMUtil.XSD_DOCUMENTATION_ELEMENT, documentation);
    }

    @Override
    public String getDocumentationPreview() {
        String documentation = getDocumentation();
        if (documentation == null || documentation.isEmpty()) {
            return documentation;
        }
        String plainDoc = MiscUtil.stripHtmlMarkup(documentation);
        int i = plainDoc.indexOf('.');
        if (i<0) {
            return plainDoc;
        }
        return plainDoc.substring(0,i+1);
    }

    @Override
    public boolean isRuntimeSchema() {
        return isRuntimeSchema;
    }

    @Override
    public void setRuntimeSchema(boolean isRuntimeSchema) {
        checkMutable();
        this.isRuntimeSchema = isRuntimeSchema;
    }

    @Override
    public PrismContext getPrismContext() {
        return PrismContext.get();
    }

    @Override
    public Class getTypeClass() {
        // This would be perhaps more appropriate on PrismPropertyDefinition, not here
        return XsdTypeMapper.toJavaTypeIfKnown(getTypeName());
    }

    public <A> A getAnnotation(QName qname, A defaultValue) {
        A rv = getAnnotation(qname);
        return rv != null ? rv : defaultValue;
    }

    @Override
    public <A> A getAnnotation(QName qname) {
        if (annotations == null) {
            return null;
        }

        //noinspection unchecked
        return (A) annotations.get(qname);
    }

    @Override
    public Map<QName, Object> getAnnotations() {
        return annotations != null ?
                Collections.unmodifiableMap(annotations) : null;
    }

    @Override
    public <A> void setAnnotation(QName qname, A value) {
        if (annotations == null) {
            // Lazy init. Most definitions will not have any annotations.
            // We do not want to fill memory with empty hashmaps.
            annotations = new HashMap<>();
        }
        annotations.put(qname, value);
    }

    @Override
    public List<SchemaMigration> getSchemaMigrations() {
        return schemaMigrations;
    }

    @Override
    public void addSchemaMigration(SchemaMigration schemaMigration) {
        checkMutable();
        if (schemaMigrations == null) {
            schemaMigrations = new ArrayList<>();
        }
        if (!schemaMigrations.contains(schemaMigration)) {
            schemaMigrations.add(schemaMigration);
        }
    }

    @Override
    public List<ItemDiagramSpecification> getDiagrams() {
        return diagrams;
    }

    @Override
    public void addDiagram(ItemDiagramSpecification diagram) {
        checkMutable();
        if (diagrams == null) {
            diagrams = new ArrayList<>();
        }
        if (!diagrams.contains(diagram)) {
            diagrams.add(diagram);
        }
    }

    @Override
    public abstract void revive(PrismContext prismContext);

    protected void copyDefinitionDataFrom(Definition source) {
        this.typeName = source.getTypeName();
        this.isAbstract = source.isAbstract();
        this.isRuntimeSchema = source.isRuntimeSchema();
        Map<QName, Object> annotations = source.getAnnotations();
        if (annotations != null) {
            if (this.annotations == null) {
                this.annotations = new HashMap<>();
            }
            this.annotations.putAll(annotations);
        }
        List<SchemaMigration> migrations = source.getSchemaMigrations();
        if (migrations != null) {
            this.schemaMigrations = new ArrayList<>(migrations);
        }
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        ItemProcessing processing = getProcessing();
        result = prime * result + ((processing == null) ? 0 : processing.hashCode());
        result = prime * result + ((typeName == null) ? 0 : typeName.hashCode());
        return result;
    }

    @SuppressWarnings({ "ConstantConditions", "RedundantIfStatement" })
    @Override
    public boolean equals(Object obj) {
        if (this == obj)  return true;
        if (obj == null)  return false;
        if (getClass() != obj.getClass()) return false;
        DefinitionImpl other = (DefinitionImpl) obj;
        if (getProcessing() != other.getProcessing()) return false;
        if (typeName == null) {
            if (other.typeName != null) return false;
        } else if (!typeName.equals(other.typeName)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return getDebugDumpClassName() + getMutabilityFlag() + " ("+PrettyPrinter.prettyPrint(getTypeName())+")";
    }

    @Override
    public String debugDump(int indent) {
        StringBuilder sb = DebugUtil.createIndentedStringBuilder(indent);
        sb.append(this);
        extendDumpHeader(sb);
        return sb.toString();
    }

    protected void extendDumpHeader(StringBuilder sb) {
        if (getSchemaMigrations() != null && !getSchemaMigrations().isEmpty()) {
            sb.append(", ").append(getSchemaMigrations().size()).append(" schema migrations");
        }
    }

    /**
     * Return a human readable name of this class suitable for logs. (e.g. "PPD")
     */
    protected abstract String getDebugDumpClassName();

    /**
     * Returns human-readable name of this class suitable for documentation. (e.g. "property")
     */
    public abstract String getDocClassName();

    @NotNull
    @Override
    public abstract Definition clone();

    protected void checkMutableOnExposing() {
        if (!isMutable()) {
            throw new IllegalStateException("Definition couldn't be exposed as mutable because it is immutable: " + this);
        }
    }

    @Override
    public void accept(Visitor<Definition> visitor) {
        accept(visitor, new SmartVisitationImpl<>());
    }

    @Override
    public boolean accept(Visitor<Definition> visitor, SmartVisitation<Definition> visitation) {
        if (visitation.alreadyVisited(this)) {
            return false;
        } else {
            visitation.registerVisit(this);
            visitor.visit(this);
            return true;
        }
    }
}
