/*
 * Copyright (c) 2010-2020 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism.impl;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;

import com.evolveum.midpoint.prism.normalization.Normalizer;
import com.evolveum.midpoint.prism.xml.XmlTypeConverter;

import com.evolveum.midpoint.util.*;

import com.google.common.primitives.Primitives;
import jakarta.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Element;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.binding.PlainStructured;
import com.evolveum.midpoint.prism.binding.StructuredEqualsStrategy;
import com.evolveum.midpoint.prism.crypto.EncryptionException;
import com.evolveum.midpoint.prism.equivalence.ParameterizedEquivalenceStrategy;
import com.evolveum.midpoint.prism.impl.marshaller.BeanMarshaller;
import com.evolveum.midpoint.prism.impl.util.PrismUtilInternal;
import com.evolveum.midpoint.prism.impl.xnode.XNodeImpl;
import com.evolveum.midpoint.prism.match.MatchingRule;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.prism.polystring.PolyString;
import com.evolveum.midpoint.prism.util.CloneUtil;
import com.evolveum.midpoint.prism.util.PrismPrettyPrinter;
import com.evolveum.midpoint.prism.util.PrismUtil;
import com.evolveum.midpoint.prism.xnode.XNode;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.util.exception.SystemException;
import com.evolveum.prism.xml.ns._public.types_3.PolyStringType;
import com.evolveum.prism.xml.ns._public.types_3.ProtectedStringType;
import com.evolveum.prism.xml.ns._public.types_3.RawType;
import com.evolveum.prism.xml.ns._public.types_3.SchemaDefinitionType;

import static com.evolveum.midpoint.util.MiscUtil.q;

/**
 * @author lazyman
 */
public class PrismPropertyValueImpl<T> extends PrismValueImpl
        implements DebugDumpable, Serializable, PrismPropertyValue<T> {

    private T value;

    // The rawElement is set during a schema-less parsing, e.g. during parsing without a definition.
    // We can't do anything smarter, as we don't have definition nor prism context. So we store the raw
    // elements here and process them later (e.g. during applyDefinition or getting a value with explicit type).
    private XNodeImpl rawElement;

    @Nullable private ExpressionWrapper expression;

    public PrismPropertyValueImpl(T value) {
        this(value, null, null, null);
    }

    public PrismPropertyValueImpl(T value, OriginType type, Objectable source) {
        this(value, type, source, null);
    }

    public PrismPropertyValueImpl(T value, OriginType type, Objectable source, @Nullable ExpressionWrapper expression) {
        super(type, source, null);
        if (value instanceof PrismPropertyValue) {
            throw new IllegalArgumentException(
                    "Probably problem somewhere, encapsulating property value object to another property value.");
        }
        this.value = value;
        this.expression = expression;
        checkValue();
    }

    /**
     * Private constructor just for cloning.
     */
    private PrismPropertyValueImpl(OriginType type, Objectable source) {
        super(type, source);
    }

    PrismPropertyValueImpl() {
    }

    @Override
    public void setValue(T value) {
        checkMutable();
        try {
            notifyParent(PrismPropertyImpl::valueChangeStart);
            this.value = value;
            this.rawElement = null;
            checkValue();
            notifyParent(PrismPropertyImpl::valueChangeEnd);
        } catch (Exception e) {
            notifyParent(PrismPropertyImpl::valueChangeFailed);
            throw e;
        }
    }

    private void notifyParent(BiConsumer<PrismPropertyImpl<T>, PrismPropertyValue<T>> consumer) {
        Itemable parent = this.getParent();
        if (parent instanceof PrismPropertyImpl) {
            consumer.accept(((PrismPropertyImpl<T>) parent), this);
        }
    }

    @Override
    public T getValue() {
        if (rawElement != null) {
            ItemDefinition<?> def = null;
            Itemable parent = getParent();
            if (parent != null && parent.getDefinition() != null) {
                def = getParent().getDefinition();
            }
            if (def != null) {
                try {
                    applyDefinitionLegacy(def);
                } catch (SchemaException e) {
                    throw new IllegalStateException(e.getMessage(), e);
                }
            }
            if (rawElement != null) {
                //noinspection unchecked
                return (T) RawType.create(rawElement.frozen());
            }
        }
        return value;
    }

    @Override
    public XNodeImpl getRawElement() {
        return rawElement;
    }

    @Override
    public void setRawElement(XNode rawElement) {
        this.rawElement = (XNodeImpl) rawElement;
    }

    @Override
    public boolean isRaw() {
        return rawElement != null;
    }

    @Override
    @Nullable
    public ExpressionWrapper getExpression() {
        return expression;
    }

    @Override
    public void setExpression(@Nullable ExpressionWrapper expression) {
        this.expression = expression;
    }

    @Override
    public PrismValue applyDefinition(@NotNull ItemDefinition<?> definition, boolean force) throws SchemaException {
        // "force" is currently ignored (this is as it was before)
        PrismPropertyDefinition<?> propertyDefinition = (PrismPropertyDefinition<?>) definition;
        if (!propertyDefinition.isAnyType()) {
            if (rawElement != null) {
                //noinspection unchecked
                T maybeValue = parseRawElementToNewRealValue(this, (PrismPropertyDefinition<T>) propertyDefinition);
                if (maybeValue != null) {
                    setValue(maybeValue);
                } else {
                    // Be careful here. Expression element can be legal sub-element of complex properties.
                    // Therefore parse expression only if there is no legal value.
                    expression = PrismUtilInternal.parseExpression(rawElement);
                }
                rawElement = null;
            }
            if (value != null && propertyDefinition.getTypeClass() != null) {
                var type = propertyDefinition.getTypeClass();
                if (PolyStringType.class.equals(type)) {
                    type = PolyString.class;
                }
                if (type.isPrimitive()) {
                    type = Primitives.wrap(type);
                }
                // TEMPORARY HACKS FIXME as part of MID-2119
                if (PolyString.class.equals(type) && value instanceof String stringValue) {
                    //noinspection unchecked
                    value = (T) propertyDefinition.convertStringValueToPolyString(stringValue);
                } else if (String.class.equals(type) && value instanceof PolyString polyString) {
                    //noinspection unchecked
                    value = (T) polyString.getOrig();
                } else if (PolyString.class.equals(type) && value instanceof Map<?, ?> map) {
                    // HACK because of polystring attributes and new repo; FIXME as part of MID-2119
                    //noinspection unchecked
                    value = (T) new PolyString((String) map.get("o"), (String) map.get("n"));
                } else if (!type.isInstance(value)) {
                    // Here if the schema is runtime and type is string, type was lost somewhere along the way.
                    if (XmlTypeConverter.canConvert(type)
                            && propertyDefinition.isRuntimeSchema()
                            && value instanceof String stringValue) {
                        //noinspection unchecked
                        value = (T) XmlTypeConverter.toJavaValue(stringValue, type);
                    } else {
                        throw new SchemaException(
                                "Incorrect value type. Expected %s (%s) for property '%s', current is: %s".formatted(
                                        definition.getTypeName(), type, definition.getItemName(), value.getClass()));
                    }
                }
            }
        }
        return this;
    }

    @Override
    public void revive(PrismContext prismContext) {
        super.revive(prismContext);
        if (value != null) {
            if (value instanceof Revivable) {
                ((Revivable) value).revive(prismContext);
            } else {
                BeanMarshaller marshaller = ((PrismContextImpl) prismContext).getBeanMarshaller();
                if (marshaller.canProcess(value.getClass())) {
                    marshaller.revive(value, prismContext);
                }
            }
        }
    }

    @Override
    public void recompute(PrismContext prismContext) {
        if (isRaw()) {
            return;
        }
        T realValue = getValue();
        if (realValue == null) {
            return;
        }
        checkMutable(); // TODO reconsider this
        PrismUtil.recomputeRealValue(realValue);
    }

    @Override
    public Object find(ItemPath path) {
        if (path == null || path.isEmpty()) {
            return this;
        }
        T value = getValue();
        if (value instanceof Structured) {
            return ((Structured) value).resolve(path);
        } else {
            throw new IllegalArgumentException("Attempt to resolve sub-path '" + path + "' on non-structured property value " + value);
        }
    }

    @Override
    public <IV extends PrismValue, ID extends ItemDefinition<?>> PartiallyResolvedItem<IV, ID> findPartial(ItemPath path) {
        throw new UnsupportedOperationException("Attempt to invoke findPartialItem on a property value");
    }

    void checkValue() {
        if (isRaw()) {
            // Cannot really check raw values
            return;
        }
        if (expression != null) {
            return;
        }
        if (value == null) {
            // can be used not because of prism forms in gui (will be fixed later [lazyman]
            // throw new IllegalArgumentException("Null value in "+this);
            return;
        }
        if (value instanceof PolyStringType) {
            // This is illegal. PolyString should be there instead.
            throw new IllegalArgumentException("PolyStringType found where PolyString should be in " + this);
        }
        if (value instanceof Serializable) {
            // This is OK (covers also primitives, SchemaDefinitionType, RawType)
            return;
        }
        throw new IllegalArgumentException("Unsupported value " + value + " (" + value.getClass() + ") in " + this);
    }

    @Override
    public void checkConsistenceInternal(Itemable rootItem, boolean requireDefinitions, boolean prohibitRaw, ConsistencyCheckScope scope) {
        if (!scope.isThorough()) {
            return;
        }

        ItemPath myPath = getPath();
        if (prohibitRaw && rawElement != null) {
            throw new IllegalStateException(
                    "Raw element in property value %s (%s in %s)".formatted(this, myPath, rootItem));
        }
        if (value == null && rawElement == null && expression == null) {
            throw new IllegalStateException(
                    "Neither value, expression nor raw element specified in property value %s (%s in %s)".formatted(
                            this, myPath, rootItem));
        }
        if (value != null && rawElement != null) {
            throw new IllegalStateException(
                    "Both value and raw element specified in property value %s (%s in %s)".formatted(
                            this, myPath, rootItem));
        }
        if (value != null) {
            if (value instanceof Recomputable recomputable) {
                try {
                    recomputable.checkConsistence();
                } catch (IllegalStateException e) {
                    throw new IllegalStateException(
                            "%s in property value %s (%s in %s)".formatted(
                                    e.getMessage(), this, myPath, rootItem),
                            e);
                }
            }
            if (value instanceof PolyStringType) {
                throw new IllegalStateException(
                        "PolyStringType found in property value %s (%s in %s)".formatted(this, myPath, rootItem));
            }
            if (value instanceof ProtectedStringType) {
                if (((ProtectedStringType) value).isEmpty()) {
                    throw new IllegalStateException(
                            "Empty ProtectedStringType found in property value %s (%s in %s)".formatted(
                                    this, myPath, rootItem));
                }
            }
            if (value instanceof PolyString poly) {
                ItemDefinition<?> definition = getDefinition();
                MiscUtil.stateCheck(definition == null || definition instanceof PrismPropertyDefinition<?>,
                        "Definition is not a property definition: %s in %s", definition, this);
                PrismPropertyDefinition<?> propDef = (PrismPropertyDefinition<?>) definition;

                // We allow 'null' norm values; these are probably not normalized yet.
                // We skip dynamic definitions, unless we know they are "custom polystring", i.e. carrying the correct normalizer
                if (poly.getNorm() != null
                        && propDef != null
                        && (!propDef.isDynamic() || propDef.isCustomPolyString())) {

                    // The normalizer must be String-based one. If not, the normalizing method will throw an exception,
                    // which is a desired behavior here.
                    Normalizer<String> normalizer = propDef.getStringNormalizerForPolyStringProperty();

                    String orig = poly.getOrig();
                    String norm = poly.getNorm();
                    String expectedNorm;
                    try {
                        expectedNorm = normalizer.normalize(orig);
                    } catch (SchemaException e) {
                        throw new IllegalStateException(
                                "Exception during a normalization while checking " + this + ": " + e.getMessage(), e);
                    }
                    if (!Objects.equals(norm, expectedNorm)) {
                        throw new IllegalStateException(
                                String.format("PolyString has inconsistent orig (%s) and norm (%s, should be %s) "
                                                + "in %s (%s in %s); normalizer is: %s",
                                        q(orig), q(norm), q(expectedNorm), this, myPath, rootItem, normalizer));
                    }
                }
            }
        }
    }

    @Override
    public boolean isEmpty() {
        return value == null;
    }

    @Override
    public PrismPropertyValue<T> clone() {
        return cloneComplex(CloneStrategy.LITERAL);
    }

    @Override
    public PrismPropertyValue<T> cloneComplex(CloneStrategy strategy) {
        PrismPropertyValueImpl<T> clone = new PrismPropertyValueImpl<>(getOriginType(), getOriginObject());
        copyValues(strategy, clone);
        return clone;
    }

    protected void copyValues(CloneStrategy strategy, PrismPropertyValueImpl<T> clone) {
        super.copyValues(strategy, clone);
        clone.value = CloneUtil.clone(this.value);
        if (this.expression != null) {
            clone.expression = this.expression.clone();
        }
        clone.rawElement = this.rawElement;
    }

    /**
     * Takes the definition from the definitionSource parameter and uses it to parse raw elements in origValue.
     * It returns a new parsed value without touching the original value.
     */
    private PrismPropertyValue<T> parseRawElementToNewValue(
            PrismPropertyValue<T> origValue, PrismPropertyValue<T> definitionSource) throws SchemaException {
        ItemDefinition<?> definition = definitionSource.getParent() != null ? definitionSource.getParent().getDefinition() : null;
        if (definition != null) {
            //noinspection unchecked
            return new PrismPropertyValueImpl<>(
                    parseRawElementToNewRealValue(origValue, (PrismPropertyDefinition<T>) definition));
        } else if (definitionSource.getRealClass() != null) {
            //noinspection unchecked
            return new PrismPropertyValueImpl<>(
                    parseRawElementToNewRealValue(origValue, (Class<T>) definitionSource.getRealClass()));
        } else {
            throw new IllegalArgumentException(
                    "Attempt to use property " + origValue.getParent() + " values in a raw parsing state (raw elements)"
                            + " with parsed value that has no definition nor class with prism context");
        }
    }

    private T parseRawElementToNewRealValue(PrismPropertyValue<T> prismPropertyValue, PrismPropertyDefinition<T> definition)
            throws SchemaException {
        return PrismContext.get()
                .parserFor(prismPropertyValue.getRawElement().toRootXNode())
                .definition(definition)
                .parseRealValue();
    }

    private T parseRawElementToNewRealValue(PrismPropertyValue<T> prismPropertyValue, Class<T> clazz)
            throws SchemaException {
        return PrismContext.get()
                .parserFor(prismPropertyValue.getRawElement().toRootXNode())
                .parseRealValue(clazz);
    }

    @Override
    public boolean equals(PrismValue other, @NotNull ParameterizedEquivalenceStrategy strategy) {
        return other instanceof PrismPropertyValue && equals((PrismPropertyValue<?>) other, strategy, null);
    }

    @Override
    public boolean equals(
            PrismPropertyValue<?> other,
            @NotNull ParameterizedEquivalenceStrategy strategy,
            @Nullable MatchingRule<T> matchingRule) {
        if (!super.equals(other, strategy)) {
            return false;
        }

        if (this.rawElement != null && other.getRawElement() != null) {
            return equalsRawElements((PrismPropertyValue<T>) other);
        }

        PrismPropertyValue<T> otherProcessed = (PrismPropertyValue<T>) other;
        PrismPropertyValue<T> thisProcessed = this;
        if (this.rawElement != null || other.getRawElement() != null) {
            try {
                if (this.rawElement == null) {
                    otherProcessed = parseRawElementToNewValue((PrismPropertyValue<T>) other, this);
                } else if (other.getRawElement() == null) {
                    thisProcessed = parseRawElementToNewValue(this, (PrismPropertyValue<T>) other);
                }
            } catch (SchemaException e) {
                // TODO: Maybe just return false?
                throw new IllegalArgumentException("Error parsing the value of property " + getParent() + " using the 'other' definition " +
                        "during a compare: " + e.getMessage(), e);
            }
        }

        T otherRealValue = otherProcessed.getValue();
        T thisRealValue = thisProcessed.getValue();
        if (otherRealValue == null && thisRealValue == null) {
            return true;
        }
        if (otherRealValue == null || thisRealValue == null) {
            return false;
        }

        if (matchingRule != null) {
            try {
                return matchingRule.match(thisRealValue, otherRealValue);
            } catch (SchemaException e) {
                // At least one of the values is invalid. But we do not want to throw exception from
                // a comparison operation. That will make the system very fragile. Let's fall back to
                // ordinary equality mechanism instead.
                return thisRealValue.equals(otherRealValue);
            }
        } else {

            if (thisRealValue instanceof Element && otherRealValue instanceof Element) {
                return DOMUtil.compareElement((Element) thisRealValue, (Element) otherRealValue, strategy.isLiteralDomComparison());
            }

            if (thisRealValue instanceof SchemaDefinitionType && otherRealValue instanceof SchemaDefinitionType) {
                SchemaDefinitionType thisSchema = (SchemaDefinitionType) thisRealValue;
                return thisSchema.equals(otherRealValue, strategy.isLiteralDomComparison());
            }

            if (thisRealValue instanceof ProtectedStringType && otherRealValue instanceof ProtectedStringType) {
                PrismContext prismContext = PrismContext.get();
                if (prismContext == null || prismContext.getDefaultProtector() == null) {
                    // Slightly dangerous, may get wrong results. See javadoc of ProtectedDataType.equals()
                    // But what else can we do?
                    return thisRealValue.equals(otherRealValue);
                } else {
                    try {
                        return prismContext.getDefaultProtector().areEquivalent((ProtectedStringType) thisRealValue, (ProtectedStringType) otherRealValue);
                    } catch (SchemaException | EncryptionException e) {
                        // Not absolutely correct. But adding those throws clauses to all equals(...) signature will wreak havoc.
                        throw new SystemException("Error comparing protected string values: " + e.getMessage(), e);
                    }
                }
            }

            if (thisRealValue instanceof byte[] && otherRealValue instanceof byte[]) {
                return Arrays.equals((byte[]) thisRealValue, (byte[]) otherRealValue);
            }

            if (strategy.isLiteralDomComparison()) {
                if (thisRealValue instanceof QName && otherRealValue instanceof QName) {
                    // we compare prefixes as well
                    return thisRealValue.equals(otherRealValue) &&
                            StringUtils.equals(((QName) thisRealValue).getPrefix(), ((QName) otherRealValue).getPrefix());
                } else if (thisRealValue instanceof PlainStructured && otherRealValue instanceof PlainStructured) {
                    return ((PlainStructured) thisRealValue).equals(otherRealValue, StructuredEqualsStrategy.LITERAL);
                }
            }
            return thisRealValue.equals(otherRealValue);
        }
    }

    private boolean equalsRawElements(PrismPropertyValue<T> other) {
        return this.rawElement.equals(other.getRawElement());
    }

    @Override
    public int hashCode(@NotNull ParameterizedEquivalenceStrategy strategy) {
        final int prime = 31;
        int result = super.hashCode(strategy);
        if (value != null && value instanceof Element) {
            // We need special handling here. We haven't found out the proper way now.
            // so we just do not include this in the hashcode now.
        } else {
            result = prime * result + ((value == null) ? 0 : value.hashCode());
        }
        return result;
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
    public String debugDump(int indent, boolean detailedDump) {
        detailedDump = detailedDump || DebugUtil.isDetailedDebugDump();
        StringBuilder sb = new StringBuilder();
        boolean wasIndent = false;
        if (detailedDump) {
            DebugUtil.indentDebugDump(sb, indent);
            wasIndent = true;
            sb.append("PPV(");
            dumpSuffix(sb);
            sb.append("):");
        }
        if (value != null) {
            if (detailedDump) {
                sb.append(" ").append(value.getClass().getSimpleName()).append(":");
            }
            if (value instanceof DebugDumpable dumpable) {
                if (wasIndent) {
                    sb.append("\n");
                }
                sb.append(dumpable.debugDump(indent + 1));
            } else {
                if (!wasIndent) {
                    DebugUtil.indentDebugDump(sb, indent);
                }
                PrismPrettyPrinter.debugDumpValue(sb, indent, value, null, null);
            }
        } else if (expression != null) {
            if (!wasIndent) {
                DebugUtil.indentDebugDump(sb, indent);
            }
            sb.append("expression: ");
            // TODO: nicer output
            sb.append(expression);
        } else if (rawElement != null) {
            if (wasIndent) {
                sb.append("\n");
            }
            sb.append(rawElement.debugDump(indent + 1));
        } else {
            if (!wasIndent) {
                DebugUtil.indentDebugDump(sb, indent);
            }
            sb.append("null");
        }

        ValueMetadata valueMetadata = getValueMetadata();
        if (!valueMetadata.isEmpty()) {
            sb.append(", meta: ").append(valueMetadata.shortDump());
        }

        return sb.toString();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("PPV(");
        // getValue() must not be here. getValue() contains exception that in turn causes a call to toString()
        if (value != null) {
            builder.append(value.getClass().getSimpleName()).append(":");
            builder.append(PrettyPrinter.prettyPrint(value));
        } else if (isRaw()) {
            builder.append("[raw]");
        } else if (expression != null) {
            builder.append("[expression]");
        } else {
            builder.append("null");
        }
        dumpSuffix(builder);
        builder.append(")");

        return builder.toString();
    }

    private void dumpSuffix(StringBuilder builder) {
        appendOriginDump(builder);
        ValueMetadata valueMetadata = getValueMetadata();
        if (!valueMetadata.isEmpty()) {
            builder.append(", meta: ").append(valueMetadata.shortDump());
        }
        if (getRawElement() != null) {
            builder.append(", raw element: ");
            builder.append(PrettyPrinter.prettyPrint(getRawElement()));
        }
        if (getExpression() != null) {
            builder.append(", expression: ");
            builder.append(getExpression());
        }
        if (isTransient()) {
            builder.append(", transient");
        }
    }

    @Override
    public String toHumanReadableString() {
        ValueMetadata valueMetadata = getValueMetadata();
        if (valueMetadata.isEmpty()) {
            return toHumanReadableStringInternal();
        } else {
            return toHumanReadableStringInternal() + " [meta: " + valueMetadata.shortDump() + "]";
        }
    }

    private String toHumanReadableStringInternal() {
        if (value == null && expression != null) {
            return ("expression(" + expression + ")");
        } else if (value instanceof PolyString ps) {
            // We intentionally do not put this code into PrettyPrinter, to avoid unwanted side effects
            // (displaying the aux information in user-visible context). But for e.g. deltas we need this information.
            return ps.shortDump();
        } else {
            return PrettyPrinter.prettyPrint(value);
        }
    }

    /**
     * Returns JAXBElement corresponding to the this value.
     * Name of the element is the name of parent property; its value is the real value of the property.
     *
     * @return Created JAXBElement.
     */
    @Override
    public JAXBElement<T> toJaxbElement() {
        Itemable parent = getParent();
        if (parent == null) {
            throw new IllegalStateException("Couldn't represent parent-less property value as a JAXBElement");
        }
        Object realValue = getValue();
        return new JAXBElement<T>(parent.getElementName(), (Class) realValue.getClass(), (T) realValue);
    }

    @Override
    public Class<?> getRealClass() {
        return value != null ? value.getClass() : null;
    }

    @Nullable
    @Override
    public T getRealValue() {
        return getValue();
    }

    @Override
    public void performFreeze() {
        if (value instanceof Freezable) {
            ((Freezable) value).freeze();
        } else if (value instanceof JaxbVisitable) {
            ((JaxbVisitable) value).accept(v -> {
                if (v instanceof Freezable) {
                    ((Freezable) v).freeze();
                }
            });
        }
        if (rawElement != null) {
            rawElement.freeze();
        }
        if (expression != null) {
            expression.freeze();
        }
        super.performFreeze();
    }

    @Override
    public @Nullable Object getRealValueOrRawType() {
        if (value != null && !hasValueMetadata()) {
            return value;
        } else if (rawElement != null || hasValueMetadata()) {
            return new RawType(this, getTypeName());
        } else {
            return null;
        }
    }

    @Override
    public @Nullable Object getRealValueIfExists() {
        if (value != null) {
            return value;
        } else {
            return null;
        }
    }

    @Override
    public void transformDefinition(ComplexTypeDefinition parentDef, ItemDefinition<?> itemDef,
            ItemDefinitionTransformer transformation) {
        // NOOP
    }
}
