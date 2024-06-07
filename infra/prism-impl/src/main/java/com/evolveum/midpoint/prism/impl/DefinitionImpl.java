/*
 * Copyright (c) 2010-2018 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism.impl;

import java.io.Serial;
import java.util.*;
import javax.xml.namespace.QName;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.annotation.ItemDiagramSpecification;
import com.evolveum.midpoint.prism.delta.ItemMerger;
import com.evolveum.midpoint.prism.impl.key.NaturalKeyDefinitionImpl;
import com.evolveum.midpoint.prism.impl.schema.SchemaRegistryStateAware;
import com.evolveum.midpoint.prism.key.NaturalKeyDefinition;
import com.evolveum.midpoint.prism.schemaContext.SchemaContextDefinition;
import com.evolveum.midpoint.util.DebugUtil;
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
 */
public abstract class DefinitionImpl
        extends SchemaRegistryStateAware
        implements Definition, Definition.DefinitionMutator {

    @Serial private static final long serialVersionUID = -2643332934312107274L;

    /** Final because it's sometimes used as a key in maps; moreover, it forms an identity of the definition somehow. */
    @NotNull protected final QName typeName;

    protected boolean isAbstract = false;
    protected DisplayHint displayHint;
    protected String displayName;
    protected Integer displayOrder;
    protected String help;
    protected String documentation;
    protected boolean deprecated = false;
    protected String deprecatedSince;
    protected boolean removed = false;
    protected String removedSince;
    private boolean optionalCleanup;
    protected String plannedRemoval;
    protected boolean experimental = false;
    protected boolean elaborate = false;
    private Map<QName, Object> annotations;
    private List<SchemaMigration> schemaMigrations = null;
    private List<ItemDiagramSpecification> diagrams = null;
    private String mergerIdentifier;
    private List<QName> naturalKeyConstituents;
    private SchemaContextDefinition schemaContextDefinition;

    /**
     * This means that this particular definition (of an item or of a type) is part of the runtime schema, e.g.
     * extension schema, resource schema or connector schema or something like that. I.e. it is not defined statically.
     */
    protected boolean isRuntimeSchema;

    /**
     * Set true for definitions that are more important than others and that should be emphasized
     * during presentation. E.g. the emphasized definitions will always be displayed in the user
     * interfaces (even if they are empty), they will always be included in the dumps, etc.
     */
    protected boolean emphasized = false;

    DefinitionImpl(@NotNull QName typeName) {
        this.typeName = typeName;
    }

    @Override
    @NotNull
    public QName getTypeName() {
        return typeName;
    }

    @Override
    public @Nullable String getMergerIdentifier() {
        return mergerIdentifier;
    }

    @Override
    public @Nullable ItemMerger getMergerInstance(@NotNull MergeStrategy strategy, @Nullable OriginMarker originMarker) {
        return null;
    }

    @Override
    public @Nullable List<QName> getNaturalKeyConstituents() {
        return naturalKeyConstituents;
    }

    @Override
    public @Nullable NaturalKeyDefinition getNaturalKeyInstance() {
        // todo how to create proper NaturalKey instance, implementations could be outside of prism api/impl
        List<QName> naturalKeyConstituents = getNaturalKeyConstituents();
        if (naturalKeyConstituents != null && !naturalKeyConstituents.isEmpty()) {
            return NaturalKeyDefinitionImpl.of(naturalKeyConstituents);
        }

        return null;
    }

    @Override
    public void setMergerIdentifier(String mergerIdentifier) {
        checkMutable();
        this.mergerIdentifier = mergerIdentifier;
    }

    @Override
    public void setNaturalKeyConstituents(List<QName> naturalKeyConstituents) {
        checkMutable();
        this.naturalKeyConstituents = naturalKeyConstituents;
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
        return deprecated;
    }

    @Override
    public void setDeprecated(boolean deprecated) {
        checkMutable();
        this.deprecated = deprecated;
    }

    @Override
    public String getDeprecatedSince() {
        return deprecatedSince;
    }

    public void setDeprecatedSince(String deprecatedSince) {
        checkMutable();
        this.deprecatedSince = deprecatedSince;
    }

    @Override
    public boolean isRemoved() {
        return removed;
    }

    public void setRemoved(boolean removed) {
        checkMutable();
        this.removed = removed;
    }

    @Override
    public boolean isOptionalCleanup() {
        return optionalCleanup;
    }

    @Override
    public void setOptionalCleanup(boolean optionalCleanup) {
        checkMutable();
        this.optionalCleanup = optionalCleanup;
    }

    @Override
    public String getRemovedSince() {
        return removedSince;
    }

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

    public void setPlannedRemoval(String plannedRemoval) {
        checkMutable();
        this.plannedRemoval = plannedRemoval;
    }

    @Override
    public boolean isElaborate() {
        return elaborate;
    }

    public void setElaborate(boolean elaborate) {
        checkMutable();
        this.elaborate = elaborate;
    }

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
    public String getDocumentationPreview() {
        return PrismPresentationDefinition.toDocumentationPreview(documentation);
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
    public void setSchemaMigrations(List<SchemaMigration> value) {
        checkMutable();
        schemaMigrations = value;
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
    public @Nullable SchemaContextDefinition getSchemaContextDefinition() {
        return schemaContextDefinition;
    }

    @Override
    public void setSchemaContextDefinition(SchemaContextDefinition schemaContextDefinition) {
        checkMutable();
        this.schemaContextDefinition = schemaContextDefinition;
    }

    @Override
    public abstract void revive(PrismContext prismContext);

    protected void copyDefinitionDataFrom(Definition source) {
        this.displayName = source.getDisplayName();
        this.displayOrder = source.getDisplayOrder();
        this.help = source.getHelp();
        this.documentation = source.getDocumentation();
        this.isAbstract = source.isAbstract();
        this.deprecated = source.isDeprecated();
        this.isRuntimeSchema = source.isRuntimeSchema();
        this.emphasized = source.isEmphasized();
        this.experimental = source.isExperimental();
        this.elaborate = source.isElaborate();
        this.removed = source.isRemoved();
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
        this.schemaContextDefinition = source.getSchemaContextDefinition();
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((typeName == null) ? 0 : typeName.hashCode());
        return result;
    }

    @SuppressWarnings({ "ConstantConditions", "RedundantIfStatement" })
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {return true;}
        if (obj == null) {return false;}
        if (getClass() != obj.getClass()) {return false;}
        DefinitionImpl other = (DefinitionImpl) obj;
        if (typeName == null) {
            if (other.typeName != null) {return false;}
        } else if (!typeName.equals(other.typeName)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return getDebugDumpClassName() + getMutabilityFlag() + " (" + PrettyPrinter.prettyPrint(getTypeName()) + ")";
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
