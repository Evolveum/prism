/*
 * Copyright (c) 2010-2018 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.prism.impl;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.equivalence.ParameterizedEquivalenceStrategy;
import com.evolveum.midpoint.prism.impl.schemaContext.SchemaContextImpl;
import com.evolveum.midpoint.prism.impl.xjc.PrismForJAXBUtil;
import com.evolveum.midpoint.prism.lazy.FlyweightClonedValue;
import com.evolveum.midpoint.prism.path.*;
import com.evolveum.midpoint.prism.polystring.PolyString;
import com.evolveum.midpoint.prism.schema.SchemaRegistry;
import com.evolveum.midpoint.prism.schemaContext.SchemaContext;
import com.evolveum.midpoint.util.DOMUtil;
import com.evolveum.midpoint.util.DebugUtil;
import com.evolveum.midpoint.util.PrettyPrinter;
import com.evolveum.midpoint.util.QNameUtil;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.util.exception.SystemException;
import com.evolveum.prism.xml.ns._public.query_3.SearchFilterType;
import com.evolveum.prism.xml.ns._public.types_3.EvaluationTimeType;
import com.evolveum.prism.xml.ns._public.types_3.PolyStringType;
import com.evolveum.prism.xml.ns._public.types_3.ReferentialIntegrityType;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.xml.namespace.QName;
import java.io.Serial;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * BEWARE! When adding data to this class, do not forget to update {@link PrismForJAXBUtil#setReferenceValueAsRef(
 * PrismContainerValue, QName, PrismReferenceValue)}!
 *
 * TODO ... or, even better, provide "updateFrom(sourceReferenceValue)" method right here
 *
 * @author Radovan Semancik
 */
public class PrismReferenceValueImpl extends PrismValueImpl implements PrismReferenceValue {
    @Serial private static final long serialVersionUID = 1L;

    private static final QName F_OID = new QName(PrismConstants.NS_TYPES, "oid");
    private static final QName F_TYPE = new QName(PrismConstants.NS_TYPES, "type");
    private static final QName F_RELATION = new QName(PrismConstants.NS_TYPES, "relation");
    private static final String NAME_LOCAL_PART = "name";
    private String oid;
    private PrismObject<?> object = null;
    private QName targetType = null;
    private QName relation = null;
    private String description = null;
    private SearchFilterType filter = null;
    private EvaluationTimeType resolutionTime;
    private ReferentialIntegrityType referentialIntegrity;
    private PolyString targetName = null;

    private Referencable referencable;

    public PrismReferenceValueImpl() {
        this(null,null,null);
    }

    public PrismReferenceValueImpl(String oid) {
        this(oid, null, null);
    }

    public PrismReferenceValueImpl(String oid, QName targetType) {
        this(oid, null, null);
        this.targetType = targetType;
    }

    public PrismReferenceValueImpl(String oid, OriginType type, Objectable source) {
        super(type,source);
        this.oid = oid;
    }

    /**
     * OID of the object that this reference refers to (reference target).
     *
     * May return null, but the reference is in that case incomplete and
     * unusable.
     *
     * @return the target oid
     */
    @Override
    public String getOid() {
        if (oid != null) {
            return oid;
        }
        if (object != null) {
            return object.getOid();
        }
        return null;
    }

    @Override
    public void setOid(String oid) {
        checkMutable();
        this.oid = oid;
    }

    /**
     * Returns object that this reference points to. The object is supposed to be used
     * for caching and optimizations. Only oid and type of the object really matters for
     * the reference.
     *
     * The object is transient. It will NOT be serialized. Therefore the client must
     * expect that the object can disappear when serialization boundary is crossed.
     * The client must expect that the object is null.
     */
    @Override
    public <O extends Objectable> PrismObject<O> getObject() {
        //noinspection unchecked
        return (PrismObject<O>) object;
    }

    @Override
    public void setObject(PrismObject object) {
        checkMutable();
        this.object = object;
    }

    /**
     * Returns XSD type of the object that this reference refers to. It may be
     * used in XPath expressions and similar filters.
     *
     * May return null if the type name is not set.
     *
     * @return the target type name
     */
    @Override
    public QName getTargetType() {
        if (targetType != null) {
            return targetType;
        }
        if (object != null && object.getDefinition() != null) {
            return object.getDefinition().getTypeName();
        }
        return null;
    }

    @Override
    public void setTargetType(QName targetType) {
        setTargetType(targetType, false);
    }

    /**
     * @param allowEmptyNamespace This is an ugly hack. See comment in DOMUtil.validateNonEmptyQName.
     */
    @Override
    public void setTargetType(QName targetType, boolean allowEmptyNamespace) {
        checkMutable();
        // Null value is OK
        if (targetType != null) {
            // But non-empty is not ..
            Itemable item = getParent();
            DOMUtil.validateNonEmptyQName(targetType, " in target type in reference "+ (item == null ? "(unknown)" : item.getElementName()), allowEmptyNamespace);
        }
        this.targetType = targetType;
    }

    /**
     * Returns cached name of the target object.
     * This is a ephemeral value. It is usually not stored.
     * It may be computed at object retrieval time or it may not be present at all.
     * This is NOT an authoritative information. Setting it or changing it will
     * not influence the reference meaning. OID is the only authoritative linking
     * mechanism.
     * @return cached name of the target object.
     */
    @Override
    public PolyString getTargetName() {
        if (targetName != null) {
            return targetName;
        }
        if (object != null) {
            return object.getName();
        }
        return null;
    }

    @Override
    public void setTargetName(PolyString name) {
        checkMutable();
        this.targetName = name;
    }

    @Override
    public void setTargetName(PolyStringType name) {
        checkMutable();
        if (name == null) {
            this.targetName = null;
        } else {
            this.targetName = name.toPolyString();
        }
    }

    // The PRV (this object) should have a parent with a prism context
    @Override
    public Class<Objectable> getTargetTypeCompileTimeClass() {
        QName type = getTargetType();
        if (type == null) {
            return null;
        } else {
            PrismObjectDefinition<Objectable> objDef = PrismContext.get().getSchemaRegistry().findObjectDefinitionByType(type);
            return objDef != null ? objDef.getCompileTimeClass() : null;
        }
    }

    @Override
    public QName getRelation() {
        return relation;
    }

    @Override
    public void setRelation(QName relation) {
        checkMutable();
        this.relation = relation;
    }

    @Override
    public PrismReferenceValueImpl relation(QName relation) {
        setRelation(relation);
        return this;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String description) {
        checkMutable();
        this.description = description;
    }

    @Override
    public SearchFilterType getFilter() {
        return filter;
    }

    @Override
    public void setFilter(SearchFilterType filter) {
        checkMutable();
        this.filter = filter;
    }

    @Override
    public EvaluationTimeType getResolutionTime() {
        return resolutionTime;
    }

    @Override
    public void setResolutionTime(EvaluationTimeType resolutionTime) {
        checkMutable();
        this.resolutionTime = resolutionTime;
    }

    @Override
    public ReferentialIntegrityType getReferentialIntegrity() {
        return referentialIntegrity;
    }

    @Override
    public void setReferentialIntegrity(ReferentialIntegrityType referentialIntegrity) {
        checkMutable();
        this.referentialIntegrity = referentialIntegrity;
    }

    @Override
    public PrismReferenceDefinition getDefinition() {
        return (PrismReferenceDefinition) super.getDefinition();
    }

    @Override
    public boolean isRaw() {
        // Reference value cannot be raw
        return false;
    }

    @Override
    public Object find(ItemPath path) {
        if (path == null || path.isEmpty()) {
            return this;
        }
        Object first = path.first();
        if (!ItemPath.isName(first)) {
            throw new IllegalArgumentException("Attempt to resolve inside the reference value using a non-name path "+path+" in "+this);
        }
        ItemName subName = ItemPath.toName(first);
        if (compareLocalPart(F_OID, subName)) {
            return this.getOid();
        } else if (compareLocalPart(F_TYPE, subName)) {
            return this.getTargetType();
        } else if (compareLocalPart(F_RELATION, subName)) {
            return this.getRelation();
        } else {
            throw new IllegalArgumentException("Attempt to resolve inside the reference value using a unrecognized path "+path+" in "+this);
        }
    }

    @Override
    public <IV extends PrismValue,ID extends ItemDefinition<?>> PartiallyResolvedItem<IV,ID> findPartial(ItemPath path) {
        if (path == null || path.isEmpty()) {
            //noinspection unchecked
            return new PartiallyResolvedItem<>((Item<IV, ID>) getParent(), null);
        }
        //noinspection unchecked
        return new PartiallyResolvedItem<>((Item<IV, ID>) getParent(), path);
    }

    @SuppressWarnings("SimplifiableIfStatement")
    private boolean compareLocalPart(QName a, QName b) {
        if (a == null && b == null) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }
        return a.getLocalPart().equals(b.getLocalPart());
    }

    @Override
    public PrismValue applyDefinition(@NotNull ItemDefinition<?> definition, boolean force) throws SchemaException {
        if (!(definition instanceof PrismReferenceDefinition referenceDefinition)) {
            throw new IllegalArgumentException("Cannot apply " + definition + " to a reference value");
        }
        return applyDefinition(referenceDefinition, force);
    }

    @Override
    public PrismReferenceValue applyDefinition(PrismReferenceDefinition definition, boolean force) throws SchemaException {
        checkMutable();

        var migrated = definition.migrateIfNeeded(this);
        applyDefinitionTo(migrated, definition, force);
        return migrated;
    }

    private static void applyDefinitionTo(PrismReferenceValue value, PrismReferenceDefinition definition, boolean force)
            throws SchemaException {

        var defTargetType = definition.getTargetTypeName();
        var targetType = value.getTargetType();
        if (targetType != null && defTargetType != null) {
            // Check if targetType is type or subtype of defTargetType
            PrismContext.get().getSchemaRegistry().isAssignableFrom(defTargetType, targetType);

        }
        var object = value.getObject();
        if (object == null) {
            return;
        }
        if (object.getDefinition() != null && !force) {
            return;
        }
        if (definition.getTargetObjectDefinition() != null) {
            // Temporary hack
            //noinspection unchecked,rawtypes
            object.applyDefinition((PrismObjectDefinition) definition.getTargetObjectDefinition(), force);
        }
        if (object.getDefinition() != null) {
            // All we can get here is a static definition from the schema. We do not want to override the current one,
            // which is presumably at least as good as the static one.
            return;
        }
        var objectDefinitionToApply = determineObjectDefinitionFromSchemaRegistry(object, definition);
        if (objectDefinitionToApply != null) {
            //noinspection unchecked,rawtypes
            object.applyDefinition((PrismObjectDefinition) objectDefinitionToApply, force);
        }
    }

    /** If not found, either returns `null` (meaning "ignore this"), or throws an exception. */
    private static PrismObjectDefinition<? extends Objectable> determineObjectDefinitionFromSchemaRegistry(
            PrismObject<Objectable> object, PrismReferenceDefinition definition)
            throws SchemaException {

        SchemaRegistry schemaRegistry = PrismContext.get().getSchemaRegistry();
        //noinspection ConstantConditions
        var byClass = schemaRegistry.findObjectDefinitionByCompileTimeClass(object.getCompileTimeClass());
        if (byClass != null) {
            return byClass;
        }

        QName targetTypeName = definition.getTargetTypeName();
        if (targetTypeName == null) {
            if (object.getDefinition() != null) {
                // Target type is not specified (e.g. as in task.objectRef) but we have at least some definition;
                // so let's keep it. TODO reconsider this
                return null;
            } else {
                throw new SchemaException(
                        "Cannot apply definition to composite object in reference " + definition
                                + ": the object has no present definition; it's definition cannot be determined from it's class;"
                                + "and target type name is not specified in the reference schema");
            }
        }
        var byTypeName = schemaRegistry.findObjectDefinitionByType(targetTypeName);
        if (byTypeName != null) {
            return byTypeName;
        }
        throw SchemaException.of(
                "Cannot apply definition to composite object in reference %s: no definition for object type %s",
                definition, targetTypeName);
    }

    @Override
    public void recompute(PrismContext prismContext) {
        // Nothing to do
    }

    @Override
    public void checkConsistenceInternal(Itemable rootItem, boolean requireDefinitions, boolean prohibitRaw, ConsistencyCheckScope scope) {
        if (!scope.isThorough()) {
            return;
        }

        ItemPath myPath = getPath();

        // We allow empty references that contain only the target name (see MID-5489)
        if (StringUtils.isBlank(oid) && object == null && filter == null && targetName == null) {
            boolean mayBeEmpty = false;
            if (getParent() != null && getParent().getDefinition() != null) {
                ItemDefinition<?> itemDefinition = getParent().getDefinition();
                if (itemDefinition instanceof PrismReferenceDefinition prismReferenceDefinition) {
                    mayBeEmpty = prismReferenceDefinition.isComposite();
                }
            }
            if (!mayBeEmpty) {
                throw new IllegalStateException(String.format(
                        "Neither OID, object, filter, nor target name specified in reference value %s (%s in %s)",
                        this, myPath, rootItem));
            }
        }

        if (object != null) {
            try {
                object.checkConsistence();
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(e.getMessage()+" in reference "+myPath+" in "+rootItem, e);
            } catch (IllegalStateException e) {
                throw new IllegalStateException(e.getMessage()+" in reference "+myPath+" in "+rootItem, e);
            }
        }
    }

    @Override
    public boolean isEmpty() {
        return oid == null && object == null && filter == null && relation == null && targetType == null;
    }

    /**
     * Returns a version of this value that is canonical, that means it has the minimal form.
     * E.g. it will have only OID and no object.
     */
    @Override
    public PrismReferenceValueImpl toCanonical() {
        PrismReferenceValueImpl can = new PrismReferenceValueImpl();
        can.setOid(getOid());
        // do NOT copy object
        can.setTargetType(getTargetType());
        can.setRelation(getRelation());
        can.setFilter(getFilter());
        can.setResolutionTime(getResolutionTime());
        can.setDescription(getDescription());
        return can;
    }

    @Override
    public boolean equals(PrismValue other, @NotNull ParameterizedEquivalenceStrategy strategy) {
        return other instanceof PrismReferenceValue && equals((PrismReferenceValue) other, strategy);
    }

    @SuppressWarnings({ "RedundantIfStatement" })
    public boolean equals(PrismReferenceValue other, @NotNull ParameterizedEquivalenceStrategy strategy) {
        if (this.getOid() == null) {
            if (other.getOid() != null) {
                return false;
            }
        } else if (!this.getOid().equals(other.getOid())) {
            return false;
        }
        // Special handling: if both oids are null we need to compare embedded objects
        boolean bothOidsNull = this.oid == null && other.getOid() == null;
        if (bothOidsNull) {
            if (this.object != null || other.getObject() != null) {
                if (this.object == null || other.getObject() == null) {
                    // one is null the other is not
                    return false;
                }
                if (!this.object.equals(other.getObject())) {
                    return false;
                }
            }
            if (this.getOriginObject() != null || other.getOriginObject() != null) {
                if (this.getOriginObject() == null || other.getOriginObject() == null) {
                    // one is null the other is not
                    return false;
                }
                if (!this.getOriginObject().equals(other.getOriginObject())) {
                    return false;
                }
            }
        }
        if (!equalsTargetType(other)) {
            return false;
        }
        if (!relationsEquivalent(relation, other.getRelation(), strategy.isLiteralDomComparison())) {
            return false;
        }
        if ((strategy.isConsideringReferenceFilters() || bothOidsNull) && !filtersEquivalent(filter, other.getFilter())) {
            return false;
        }
        if (strategy.isConsideringReferenceOptions()) {
            if (this.getResolutionTime() != other.getResolutionTime()) {
                return false;
            }
            if (this.getReferentialIntegrity() != other.getReferentialIntegrity()) {
                return false;
            }
        }
        // called intentionally at the end, because it is quite expensive if metadata are present
        return super.equals(other, strategy);
    }

    @SuppressWarnings("SimplifiableIfStatement")
    private boolean filtersEquivalent(SearchFilterType filter1, SearchFilterType filter2) {
        if (filter1 == null && filter2 == null) {
            return true;
        } else if (filter1 == null || filter2 == null) {
            return false;
        } else {
            return filter1.equals(filter2);
        }
    }

    private boolean relationsEquivalent(QName r1, QName r2, boolean isLiteral) {
        // todo use equals if isLiteral is true
        return QNameUtil.match(normalizedRelation(r1, isLiteral), normalizedRelation(r2, isLiteral));
    }

    private QName normalizedRelation(QName r, boolean isLiteral) {
        if (r != null) {
            return r;
        }
        if (isLiteral) {
            return null;
        }
        return PrismContext.get().getDefaultRelation();
    }

    private boolean equalsTargetType(PrismReferenceValue other) {
        QName otherTargetType = other.getTargetType();
        if (otherTargetType == null && other.getDefinition() != null) {
            otherTargetType = other.getDefinition().getTargetTypeName();
        }
        QName thisTargetType = this.getTargetType();
        if (thisTargetType == null && this.getDefinition() != null) {
            thisTargetType = this.getDefinition().getTargetTypeName();
        }
        return QNameUtil.match(thisTargetType, otherTargetType);
    }

    // TODO take strategy into account
    @Override
    public int hashCode(@NotNull ParameterizedEquivalenceStrategy strategy) {
        final int prime = 31;
        int result = super.hashCode(strategy);
        result = prime * result + ((oid == null) ? 0 : oid.hashCode());
        QName normalizedRelation = normalizedRelation(relation, false);
        if (normalizedRelation != null) {
            // Take just the local part to avoid problems with incomplete namespaces
            String relationLocal = normalizedRelation.getLocalPart();
            if (relationLocal != null) {
                result = prime * result + relationLocal.hashCode();
            }
        }
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("PRV(");
        if (object == null) {
            sb.append("oid=").append(oid);
            sb.append(", targetType=").append(PrettyPrinter.prettyPrint(targetType));
            if (targetName != null) {
                sb.append(", targetName=").append(PrettyPrinter.prettyPrint(targetName.getOrig()));
            }
        } else {
            sb.append("object=").append(object);
        }
        if (getRelation() != null) {
            sb.append(", relation=").append(PrettyPrinter.prettyPrint(getRelation()));
        }
        if (getOriginType() != null) {
            sb.append(", type=").append(getOriginType());
        }
        if (getOriginObject() != null) {
            sb.append(", source=").append(getOriginObject());
        }
        if (filter != null) {
            sb.append(", filter");
        }
        if (resolutionTime != null) {
            sb.append(", resolutionTime=").append(resolutionTime);
        }
        if (referentialIntegrity != null) {
            sb.append(", RI=").append(referentialIntegrity);
        }
        if (isTransient()) {
            sb.append(", transient");
        }
        sb.append(")");
        return sb.toString();
    }

    @Override
    public Referencable asReferencable() {
        if (referencable != null) {
            return referencable;
        }

        Itemable parent = getParent();

        QName xsdType = null;
        if (parent != null && parent.getDefinition() != null) {
            xsdType = parent.getDefinition().getTypeName();
        }
        if (xsdType == null) {
            xsdType =  PrismContext.get().getDefaultReferenceTypeName();
        }
        if (xsdType != null) {
            Class<?> clazz = PrismContext.get().getSchemaRegistry().getCompileTimeClass(xsdType);
            if (clazz != null) {
                try {
                    referencable = (Referencable) clazz.getDeclaredConstructor().newInstance();
                } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                    throw new SystemException("Couldn't create jaxb object instance of '" + clazz + "': " + e.getMessage(),
                            e);
                }
                referencable.setupReferenceValue(this);
                return referencable;
            }
        }
        // A hack, just to avoid crashes. TODO think about this!
        return new DefaultReferencableImpl(this);
    }

    @SuppressWarnings("unused")
    @NotNull
    public static List<Referencable> asReferencables(@NotNull Collection<PrismReferenceValue> values) {
        return values.stream().map(PrismReferenceValue::asReferencable).collect(Collectors.toList());
    }

    @SuppressWarnings("unused")
    @NotNull
    public static List<PrismReferenceValue> asReferenceValues(@NotNull Collection<? extends Referencable> referencables) {
        return referencables.stream().map(Referencable::asReferenceValue).collect(Collectors.toList());
    }

    @Override
    public String debugDump() {
        return toString();
    }

    @Override
    public String debugDump(int indent) {
        return debugDump(indent, false);
    }

    @Override
    public String debugDump(int indent, boolean expandObject) {
        StringBuilder sb = new StringBuilder();
        DebugUtil.indentDebugDump(sb, indent);
        sb.append(this);
        if (expandObject && object != null) {
            sb.append("\n");
            sb.append(object.debugDump(indent + 1));
        }

        return sb.toString();
    }

    @Override
    public PrismReferenceValue clone() {
        return cloneComplex(CloneStrategy.LITERAL_MUTABLE);
    }

    @Override
    public PrismReferenceValue createImmutableClone() {
        return (PrismReferenceValue) super.createImmutableClone();
    }

    @Override
    public @NotNull PrismReferenceValue cloneComplex(@NotNull CloneStrategy strategy) {
        if (isImmutable() && !strategy.mutableCopy()) {
            return FlyweightClonedValue.from(this);
        }

        PrismReferenceValueImpl clone = new PrismReferenceValueImpl(getOid(), getOriginType(), getOriginObject());
        copyValues(strategy, clone);
        return clone;
    }

    protected void copyValues(CloneStrategy strategy, PrismReferenceValueImpl clone) {
        super.copyValues(strategy, clone);
        clone.targetType = this.targetType;
        if (this.object != null && !strategy.ignoreEmbeddedObjects()) {
            clone.object = this.object.clone();
        }
        clone.description = this.description;
        clone.filter = this.filter;
        clone.resolutionTime = this.resolutionTime;
        clone.referentialIntegrity = this.referentialIntegrity;
        clone.relation = this.relation;
        clone.targetName = this.targetName;
    }

    @Override
    public String toHumanReadableString() {
        StringBuilder sb = new StringBuilder();
        sb.append("oid=");
        shortDump(sb);
        return sb.toString();
    }

    @Override
    public Class<?> getRealClass() {
        return Referencable.class;
    }

    @SuppressWarnings("unchecked")
    @NotNull
    @Override
    public Referencable getRealValue() {
        return asReferencable();
    }

    public static boolean containsOid(Collection<PrismReferenceValue> values, @NotNull String oid) {
        return values.stream().anyMatch(v -> oid.equals(v.getOid()));
    }

    @Override
    public void revive(PrismContext prismContext) {
        super.revive(prismContext);
        if (object != null) {
            object.revive(prismContext);
        }
    }

    @Override
    public void shortDump(StringBuilder sb) {
        sb.append(oid);
        if (getTargetType() != null) {
            sb.append("(");
            sb.append(DebugUtil.formatElementName(getTargetType()));
            sb.append(")");
        }
        var targetName = getTargetName();
        if (targetName != null) {
            sb.append("('").append(targetName).append("')");
        }
        if (getRelation() != null) {
            sb.append("[");
            sb.append(getRelation().getLocalPart());
            sb.append("]");
        }
        if (getObject() != null) {
            sb.append('*');
        }
        if (getReferentialIntegrity() != null) {
            switch (getReferentialIntegrity()) {
                case STRICT: sb.append(" RI=S"); break;
                case RELAXED: sb.append(" RI=R"); break;
                case LAX: sb.append(" RI=L"); break;
            }
        }
    }

    @Override
    public void accept(Visitor visitor) {
        super.accept(visitor);
        if (object != null && ConfigurableVisitor.shouldVisitEmbeddedObjects(visitor)) {
            //noinspection unchecked
            object.accept(visitor);
        }
    }

    @Override
    public void transformDefinition(ComplexTypeDefinition parentDef, ItemDefinition<?> itemDef,
            ItemDefinitionTransformer transformation) {
        // NOOP
    }

    @SuppressWarnings("unchecked")
    @Override
    public <I extends Item<?, ?>> I findReferencedItem(ItemPath path, Class<I> type) {
        if (path.startsWithObjectReference() && getObject() != null) {
            var rest = path.rest();
            if (rest.isEmpty() && type.isInstance(getObject())) {
                return type.cast(getObject());
            }
            //noinspection rawtypes
            return (I) getObject().findItem(path.rest(), (Class) type);
        }
        return null;
    }

    @Override
    public SchemaContext getSchemaContext() {
        QName targetType = getTargetType();
        if (targetType == null && getDefinition() != null) {
            targetType = getDefinition().getTargetTypeName();
        }

        if (targetType != null) {
            var targetDef = schemaLookup().findObjectDefinitionByType(targetType);
            if (targetDef != null) {
                return new SchemaContextImpl(targetDef);
            }
        }

        return super.getSchemaContext();
    }

    @Override
    public @NotNull Collection<PrismValue> getAllValues(ItemPath path) {
        if (path.startsWithObjectReference() && getObject() != null) {
            var rest = path.rest();
            if (rest.isEmpty()) {
                return Collections.singletonList(getObject().getValue());
            }
            return getObject().getAllValues(rest);
        }
        if (path.startsWithObjectReference() && getObject() == null) {
            var rest = path.rest();
            if (rest.isSingleName() && NAME_LOCAL_PART.equals(rest.asSingleName().getLocalPart())) {
                if (targetName != null) {
                    return Collections.singletonList(PrismContext.get().itemFactory().createPropertyValue(targetName));
                } else {
                    return Collections.emptyList();
                }
            }
        }
        return super.getAllValues(path);



    }
}
