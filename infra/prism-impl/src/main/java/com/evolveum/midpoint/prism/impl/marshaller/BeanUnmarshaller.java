/*
 * Copyright (c) 2010-2019 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.prism.impl.marshaller;

import com.evolveum.concepts.*;
import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.impl.PrismContextImpl;
import com.evolveum.midpoint.prism.impl.lex.dom.DomLexicalProcessor;
import com.evolveum.midpoint.prism.impl.xnode.*;
import com.evolveum.midpoint.prism.marshaller.ParsingMigrator;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.prism.polystring.PolyString;
import com.evolveum.midpoint.prism.schema.SchemaRegistry;
import com.evolveum.midpoint.prism.xml.XsdTypeMapper;
import com.evolveum.midpoint.prism.xnode.XNode;
import com.evolveum.midpoint.util.DOMUtil;
import com.evolveum.midpoint.util.QNameUtil;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.util.exception.SystemException;
import com.evolveum.midpoint.util.logging.LoggingUtils;
import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;
import com.evolveum.prism.xml.ns._public.query_3.SearchFilterType;
import com.evolveum.prism.xml.ns._public.types_3.*;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Element;

import jakarta.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import java.lang.reflect.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import static java.util.Collections.emptySet;

/**
 * Analogous to PrismUnmarshaller, this class unmarshals atomic values from XNode tree structures.
 * Atomic values are values that can be used as property values (i.e. either simple types, or
 * beans that are not containerables).
 */
public class BeanUnmarshaller {

    private static final Trace LOGGER = TraceManager.getTrace(BeanUnmarshaller.class);

    @NotNull private final PrismBeanInspector inspector;
    @NotNull private final BeanMarshaller beanMarshaller;
    @NotNull private final PrismContext prismContext;
    @NotNull private final Map<Class,PrimitiveUnmarshaller> specialPrimitiveUnmarshallers = new HashMap<>();
    @NotNull private final Map<Class,MapUnmarshaller> specialMapUnmarshallers = new HashMap<>();

    public BeanUnmarshaller(@NotNull PrismContext prismContext, @NotNull PrismBeanInspector inspector,
            @NotNull BeanMarshaller beanMarshaller) {
        this.prismContext = prismContext;
        this.inspector = inspector;
        this.beanMarshaller = beanMarshaller;
        createSpecialUnmarshallerMaps();
    }

    @FunctionalInterface
    private interface PrimitiveUnmarshaller<T> {
        T unmarshal(PrimitiveXNodeImpl node, Class<T> beanClass, ParsingContext pc) throws SchemaException;
    }

    @FunctionalInterface
    private interface MapUnmarshaller<T> {
        T unmarshal(MapXNodeImpl node, Class<T> beanClass, ParsingContext pc) throws SchemaException;
    }

    private void add(Class<?> beanClass, PrimitiveUnmarshaller primitive, MapUnmarshaller map) {
        specialPrimitiveUnmarshallers.put(beanClass, primitive);
        specialMapUnmarshallers.put(beanClass, map);
    }

    private void createSpecialUnmarshallerMaps() {
        add(XmlAsStringType.class, this::unmarshalXmlAsStringFromPrimitive, this::unmarshalXmlAsStringFromMap);
        add(RawType.class, this::unmarshalRawType, this::unmarshalRawType);
        add(RawObjectType.class, this::unmarshalRawObjectType, this::unmarshalRawObjectType);
        add(PolyString.class, this::unmarshalPolyStringFromPrimitive, this::unmarshalPolyStringFromMap);
        add(PolyStringType.class, this::unmarshalPolyStringFromPrimitive, this::unmarshalPolyStringFromMap);
        add(ItemPathType.class, this::unmarshalItemPath, this::notSupported);
        add(ProtectedStringType.class, this::unmarshalProtectedString, this::unmarshalProtectedString);
        add(ProtectedByteArrayType.class, this::unmarshalProtectedByteArray, this::unmarshalProtectedByteArray);
        add(SchemaDefinitionType.class, this::notSupported, this::unmarshalSchemaDefinitionType);
    }

    //region Main entry ==========================================================================
    /*
     *  Preconditions:
     *   1. typeName is processable by unmarshaller - i.e. it corresponds to simple or complex type NOT of containerable character
     */

    <T> T unmarshal(@NotNull XNodeImpl xnode, @NotNull QName typeQName, @NotNull ParsingContext pc) throws SchemaException {
        Class<T> classType = getSchemaRegistry().determineClassForType(typeQName);        // TODO use correct method!
        if (classType == null) {
            TypeDefinition td = getSchemaRegistry().findTypeDefinitionByType(typeQName);
            if (td instanceof SimpleTypeDefinition) {
                // most probably dynamically defined enum (TODO clarify)
                classType = (Class<T>) String.class;
            } else {
                ValidationLog validationLog = new ValidationLog(ValidationLogType.ERROR, ValidationLogType.Specification.UNKNOW, xnode.getSourceLocation(),
                        new TechnicalMessage("Couldn't unmarshal '%s'. Type definition = '%s'",
                                new Argument(typeQName, Argument.ArgumentType.QNAME),
                                new Argument(td, Argument.ArgumentType.DEFINITION)),
                        "Couldn't unmarshal '%s'. Type definition = '%s'".formatted(typeQName.getLocalPart(), td));
                pc.warn(LOGGER, validationLog);
                throw new IllegalArgumentException(validationLog.message());
            }
        }
        return unmarshal(xnode, classType, pc);
    }

    /**
     * TODO: decide if this method should be marked @NotNull.
     * Basically the problem is with primitives. When parsed, they sometimes return null. The question is if it's correct.
     */
    <T> T unmarshal(@NotNull XNodeImpl xnode, @NotNull Class<T> beanClass, @NotNull ParsingContext pc) throws SchemaException {
        T value = unmarshalInternal(xnode, beanClass, pc);
        if (value != null) {
            Class<?> requested = ClassUtils.primitiveToWrapper(beanClass);
            Class<?> actual = ClassUtils.primitiveToWrapper(value.getClass());
            if (!requested.isAssignableFrom(actual)) {
                pc.warnOrThrow(LOGGER, new ValidationLog(ValidationLogType.ERROR, ValidationLogType.Specification.UNKNOW, xnode.getSourceLocation(),
                        new TechnicalMessage("Unmarshal returned a value of '%s' ('%s') which is not of requested type ('%s')",
                                new Argument(value, Argument.ArgumentType.UNKNOW),
                                new Argument(actual, Argument.ArgumentType.UNKNOW),
                                new Argument(requested, Argument.ArgumentType.UNKNOW)),
                        "Unmarshal returned a value of '%s' ('%s') which is not of requested type ('%s')".formatted(value, actual, requested)));
            }
        }
        return value;
    }

    private <T> T unmarshalInternal(@NotNull XNodeImpl xnode, @NotNull Class<T> beanClass, @NotNull ParsingContext pc) throws SchemaException {
        if (beanClass == null) {
            ValidationLog validationLog = new ValidationLog(ValidationLogType.ERROR, ValidationLogType.Specification.UNKNOW, xnode.getSourceLocation(),
                    new TechnicalMessage("No bean class for node: '%s'", new Argument(xnode.debugDump(), Argument.ArgumentType.RAW)),
                    "No bean class for node");
            pc.warn(LOGGER, validationLog);
            throw new IllegalStateException(validationLog.message());
        }
        if (xnode instanceof RootXNodeImpl) {
            XNodeImpl subnode = ((RootXNodeImpl) xnode).getSubnode();
            if (subnode == null) {
                ValidationLog validationLog = new ValidationLog(ValidationLogType.ERROR, ValidationLogType.Specification.UNKNOW, xnode.getSourceLocation(),
                        new TechnicalMessage("Couldn't parse '%s' from a root node with a null content: '%s'",
                                new Argument(beanClass, Argument.ArgumentType.UNKNOW),
                                new Argument(xnode.debugDump(), Argument.ArgumentType.RAW)),
                        "Couldn't parse '%s' from a root node with a null content".formatted(beanClass));
                pc.warn(LOGGER, validationLog);
                throw new IllegalStateException(validationLog.message());
            } else {
                return unmarshal(subnode, beanClass, pc);
            }
        } else if (!(xnode instanceof MapXNodeImpl) && !(xnode instanceof PrimitiveXNodeImpl) && !xnode.isHeterogeneousList()) {
            ValidationLog validationLog = new ValidationLog(ValidationLogType.ERROR, ValidationLogType.Specification.UNKNOW, xnode.getSourceLocation(),
                    new TechnicalMessage("Couldn't parse '%s' from non-map/non-primitive/non-hetero-list node: '%s'",
                            new Argument(beanClass, Argument.ArgumentType.UNKNOW),
                            new Argument(xnode.debugDump(), Argument.ArgumentType.RAW)),
                    "Couldn't parse '%s' from non-map/non-primitive/non-hetero-list node".formatted(beanClass));
            pc.warn(LOGGER, validationLog);
            throw new IllegalStateException(validationLog.message());
        }

        // only maps and primitives and heterogeneous lists after this point

        if (xnode instanceof PrimitiveXNodeImpl) {
            //noinspection unchecked
            PrimitiveXNodeImpl<T> prim = (PrimitiveXNodeImpl) xnode;
            QName xsdType = XsdTypeMapper.getJavaToXsdMapping(beanClass);
            if (xsdType != null) {
                return prim.getParsedValue(xsdType, beanClass);
            } else if (beanClass.isEnum()) {
                return unmarshalEnumFromPrimitive(prim, beanClass, pc);
            } else {
                @SuppressWarnings("unchecked")
                PrimitiveUnmarshaller<T> unmarshaller = specialPrimitiveUnmarshallers.get(beanClass);
                if (unmarshaller != null) {
                    return unmarshaller.unmarshal(prim, beanClass, pc);
                } else {
                    return unmarshalPrimitiveOther(prim, beanClass, pc);
                }
            }
        } else {

            if (beanClass.getPackage() == null || beanClass.getPackage().getName().equals("java.lang")) {
                // We obviously have primitive data type, but we are asked to unmarshal from map xnode
                // NOTE: this may happen in XML when we have "empty" element, but it has some whitespace in it
                //       such as those troublesome newlines. This also happens if there is "empty" element
                //       but it contains an expression (so it is not PrimitiveXNode but MapXNode).
                // TODO: more robust implementation
                // TODO: look for "value" subnode with primitive value and try that.

                // This is most likely attempt to parse primitive value with dynamic expression.
                // Therefore just ignore entire map content.

                // This may also happen when schema elements are renamed. E.g. in 4.0 the complex "iteration" element of
                // object template was replaced by s primitive int "iteration" element from AssignmentHolderType.
                // Currently we do not have any good idea how to graciously handle such situation. Therefore we have
                // to live with that, just make sure it is clearly documented in the release notes.
                // MID-5198
                return null;
            }

            @SuppressWarnings("unchecked")
            MapUnmarshaller<T> unmarshaller = specialMapUnmarshallers.get(beanClass);
            if (xnode instanceof MapXNodeImpl && unmarshaller != null) {        // TODO: what about special unmarshaller + hetero list?
                return unmarshaller.unmarshal((MapXNodeImpl) xnode, beanClass, pc);
            }
            return unmarshalFromMapOrHeteroList(xnode, beanClass, pc);
        }
    }

    /**
     * For cases when XSD complex type has a simple content. In that case the resulting class has @XmlValue annotation.
     */
    private <T> T unmarshalPrimitiveOther(PrimitiveXNodeImpl<T> prim, Class<T> beanClass, ParsingContext pc) throws SchemaException {
        if (prim.isEmpty()) {
            return instantiateWithSubtypeGuess(beanClass, pc, prim, emptySet());        // Special case. Just return empty object
        }

        Field valueField = XNodeProcessorUtil.findXmlValueField(beanClass);

        if (valueField == null) {
            ParsingMigrator parsingMigrator = prismContext.getParsingMigrator();
            if (parsingMigrator != null) {
                T bean = parsingMigrator.tryParsingPrimitiveAsBean(prim, beanClass, pc);
                if (bean != null) {
                    return bean;
                }
            }
            pc.warnOrThrow(LOGGER, new ValidationLog(ValidationLogType.ERROR, ValidationLogType.Specification.UNKNOW, prim.getSourceLocation(),
                    new TechnicalMessage("Cannot convert primitive value to bean of type '%s'",
                            new Argument(beanClass, Argument.ArgumentType.UNKNOW)),
                    "Cannot convert primitive value to bean of type '%s'".formatted(beanClass)));
        }

        T instance = instantiate(beanClass, pc, prim);

        if (!valueField.isAccessible()) {
            valueField.setAccessible(true);
        }

        T value;
        if (prim.isParsed()) {
            value = prim.getValue();
        } else {
            Class<?> fieldType = valueField.getType();
            QName xsdType = XsdTypeMapper.toXsdType(fieldType);
            value = prim.getParsedValue(xsdType, (Class<T>) fieldType);
        }

        try {
            valueField.set(instance, value);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            ValidationLog validationLog = new ValidationLog(ValidationLogType.ERROR, ValidationLogType.Specification.UNKNOW, prim.getSourceLocation(),
                    new TechnicalMessage("Cannot set primitive value to field '%s' of bean '%s': '%s'",
                            new Argument(valueField.getName(), Argument.ArgumentType.STRING),
                            new Argument(beanClass, Argument.ArgumentType.UNKNOW),
                            new Argument(e.getMessage(), Argument.ArgumentType.STRING)),
                    "Cannot set primitive value to field '%s' of bean '%s': '%s'".formatted(valueField.getName(), beanClass, e.getMessage()));
            pc.warn(LOGGER, validationLog);
            throw new SchemaException(validationLog.message(), e);
        }

        return instance;
    }

    boolean canProcess(QName typeName) {
        return beanMarshaller.canProcess(typeName);
    }

    boolean canProcess(Class<?> clazz) {
        return beanMarshaller.canProcess(clazz);
    }

    //endregion

    @NotNull
    private <T> T unmarshalFromMapOrHeteroList(@NotNull XNodeImpl mapOrList, @NotNull Class<T> beanClass, @NotNull ParsingContext pc) throws SchemaException {

        if (Containerable.class.isAssignableFrom(beanClass)) {
            // This could have come from inside; note we MUST NOT parse this as PrismValue, because for objects we would lose oid/version
            return prismContext
                    .parserFor(mapOrList.toRootXNode())
                    .context(pc)
                    .type(beanClass)
                    .parseRealValue();
        } else if (SearchFilterType.class.isAssignableFrom(beanClass)) {
            if (mapOrList instanceof MapXNodeImpl) {
                T bean = (T) unmarshalSearchFilterType((MapXNodeImpl) mapOrList, (Class<? extends SearchFilterType>) beanClass, pc);
                // TODO fix this BRUTAL HACK - it is here because of c:ConditionalSearchFilterType
                return unmarshalFromMapOrHeteroListToBean(bean, mapOrList, Collections.singleton("condition"), pc);
            } else {
                ValidationLog validationLog = new ValidationLog(ValidationLogType.ERROR, ValidationLogType.Specification.UNKNOW, mapOrList.getSourceLocation(),
                        new TechnicalMessage("SearchFilterType is not supported in combination of heterogeneous list."),
                        "SearchFilterType is not supported in combination of heterogeneous list.");
                pc.warn(LOGGER, validationLog);
                throw new SchemaException(validationLog.message());
            }
        } else {
            T bean = instantiateWithSubtypeGuess(beanClass, pc, mapOrList);
            return unmarshalFromMapOrHeteroListToBean(bean, mapOrList, null, pc);
        }
    }

    private <T> T instantiateWithSubtypeGuess(@NotNull Class<T> beanClass, ParsingContext pc, XNodeImpl mapOrList) throws SchemaException {
        if (!(mapOrList instanceof MapXNodeImpl)) {
            return instantiate(beanClass, pc, mapOrList);          // guessing is supported only for traditional maps now
        }
        return instantiateWithSubtypeGuess(beanClass, pc, mapOrList, ((MapXNodeImpl) mapOrList).keySet());
    }

    private <T> T instantiateWithSubtypeGuess(@NotNull Class<T> beanClass, ParsingContext pc, XNode xNode, Collection<QName> fields) throws SchemaException {
        if (!Modifier.isAbstract(beanClass.getModifiers())) {
            return instantiate(beanClass, pc, xNode);          // non-abstract classes are currently instantiated directly (could be changed)
        }
        Class<? extends T> subclass = inspector.findMatchingSubclass(beanClass, fields);
        return instantiate(subclass, pc, xNode);
    }

    private <T> T instantiate(@NotNull Class<T> beanClass, ParsingContext pc, XNode xnode) {
        T bean;
        try {
            bean = beanClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            ValidationLog validationLog = new ValidationLog(ValidationLogType.ERROR, ValidationLogType.Specification.UNKNOW, xnode.getSourceLocation(),
                    new TechnicalMessage("Cannot instantiate bean of type '%s': '%s'",
                            new Argument(beanClass, Argument.ArgumentType.UNKNOW),
                            new Argument(e.getMessage(), Argument.ArgumentType.STRING)),
                    "Cannot instantiate bean of type '%s': '%s'".formatted(beanClass, e.getMessage()));
            pc.warn(LOGGER, validationLog);
            throw new SystemException(validationLog.message(), e);
        }
        return bean;
    }

    @NotNull
    private <T> T unmarshalFromMapOrHeteroListToBean(@NotNull T bean, @NotNull XNodeImpl mapOrList, @Nullable Collection<String> keysToParse, @NotNull ParsingContext pc) throws SchemaException {
        @SuppressWarnings("unchecked")
        Class<T> beanClass = (Class<T>) bean.getClass();
        if (mapOrList instanceof MapXNodeImpl) {
            MapXNodeImpl map = (MapXNodeImpl) mapOrList;
            for (Entry<QName, XNodeImpl> entry : map.entrySet()) {
                QName key = entry.getKey();
                if (keysToParse != null && !keysToParse.contains(key.getLocalPart())) {
                    continue;
                }
                if (entry.getValue() == null) {
                    continue;
                }
                unmarshalEntry(bean, beanClass, entry.getKey(), entry.getValue(), mapOrList, false, pc);
            }
        } else if (mapOrList.isHeterogeneousList()) {
            QName keyQName = beanMarshaller.getHeterogeneousListPropertyName(beanClass);
            unmarshalEntry(bean, beanClass, keyQName, mapOrList, mapOrList, true, pc);
        } else {
            ValidationLog validationLog = new ValidationLog(ValidationLogType.ERROR, ValidationLogType.Specification.UNKNOW, mapOrList.getSourceLocation(),
                    new TechnicalMessage("Not a map nor heterogeneous list: '%s'", new Argument(mapOrList.debugDump(), Argument.ArgumentType.RAW)),
                    "Not a map nor heterogeneous list");
            throw new IllegalStateException(validationLog.message());
        }
        return bean;
    }

    /**
     * Parses either a map entry, or a fictitious heterogeneous list property.
     *
     * It makes sure that a 'key' property is inserted into 'bean' object, being sourced from 'node' structure.
     * Node itself can be single-valued or multi-valued, corresponding to single or multi-valued 'key' property.
     * ---
     * A notable (and quite ugly) exception is processing of fictitious heterogeneous lists.
     * In this case we have a ListXNode that should be interpreted as a MapXNode, inserting fictitious property
     * named after abstract multivalued property in the parent bean.
     *
     * For example, when we have (embedded in ExecuteScriptType):
     *   {
     *     pipeline: *[
     *       { element: search, ... },
     *       { element: sequence, ... }
     *     ]
     *   }
     *
     * ...it should be, in fact, read as if it would be:
     *
     *   {
     *     pipeline: {
     *        scriptingExpression: [
     *          { type: SearchExpressionType, ... },
     *          { type: ExpressionSequenceType, ... }
     *        ]
     *     }
     *   }
     *
     * (The only difference is in element names, which are missing in the latter snippet, but let's ignore that here.)
     *
     * Fictitious heterogeneous list entry here is "scriptingExpression", a property of pipeline (ExpressionPipelineType).
     *
     * We have to create the following data structure (corresponding to latter snippet):
     *
     * instance of ExecuteScriptType:
     *   scriptingExpression = instance of JAXBElement(pipeline, ExpressionPipelineType):            [1]
     *     scriptingExpression = List of                                                            [2]
     *       - JAXBElement(search, SearchExpressionType)
     *       - JAXBElement(sequence, ExpressionSequenceType)
     *
     * We in fact invoke this method twice with the same node (a two-entry list, marked as '*' in the first snippet):
     * 1) bean=ExecuteScriptType, key=pipeline, node=HList(*), isHeteroListProperty=false
     * 2) bean=ExpressionPipelineType, key=scriptingExpression, node=HList(*), isHeteroListProperty=true        <<<
     *
     * During the first call we fill in scriptingExpression (single value) in ExecuteScriptType [1]; during the second one
     * we fill in scriptingExpression (multivalued) in ExpressionPipelineType [2].
     *
     * Now let's expand the sample.
     *
     * This XNode tree:
     *   {
     *     pipeline: *[
     *       { element: search, type: RoleType, searchFilter: {...}, action: log },
     *       { element: sequence, value: **[
     *           { element: action, type: delete },
     *           { element: action, type: assign, parameter: {...} },
     *           { element: search, type: UserType }
     *       ] }
     *     ]
     *   }
     *
     * Should be interpreted as:
     *   {
     *     pipeline: {
     *       scriptingExpression: [
     *          { type: SearchExpressionType, type: RoleType, searchFilter: {...}, action: log }
     *          { type: ExpressionSequenceType, scriptingExpression: [
     *                { type: ActionExpressionType, type: delete },
     *                { type: ActionExpressionType, type: assign, parameter: {...} },
     *                { type: SearchExpressionType, type: UserType }
     *            ] }
     *       ]
     *     }
     *   }
     *
     * Producing the following data:
     *
     * instance of ExecuteScriptType:
     *   scriptingExpression = instance of JAXBElement(pipeline, ExpressionPipelineType):            [1]
     *     scriptingExpression = List of                                                            [2]
     *       - JAXBElement(search, instance of SearchExpressionType):
     *           type: RoleType,
     *           searchFilter: (...),
     *           action: log,
     *       - JAXBElement(sequence, instance of ExpressionSequenceType):
     *           scriptingExpression = List of
     *             - JAXBElement(action, instance of ActionExpressionType):
     *                 type: delete
     *             - JAXBElement(action, instance of ActionExpressionType):
     *                 type: assign
     *                 parameter: (...),
     *             - JAXBElement(search, instance of SearchExpressionType):
     *                 type: UserType
     *
     * Invocations of this method will be:
     *  1) bean=ExecuteScriptType, key=pipeline, node=HList(*), isHeteroListProperty=false
     *  2) bean=ExpressionPipelineType, key=scriptingExpression, node=HList(*), isHeteroListProperty=true            <<<
     *  3) bean=SearchExpressionType, key=type, node='type: c:RoleType', isHeteroListProperty=false
     *  4) bean=SearchExpressionType, key=searchFilter, node=XNode(map:1 entries), isHeteroListProperty=false
     *  5) bean=SearchExpressionType, key=action, node=XNode(map:1 entries), isHeteroListProperty=false
     *  6) bean=ActionExpressionType, key=type, node='type: log', isHeteroListProperty=false
     *  7) bean=ExpressionSequenceType, key=scriptingExpression, node=HList(**), isHeteroListProperty=true           <<<
     *  8) bean=ActionExpressionType, key=type, node='type: delete', isHeteroListProperty=false
     *  9) bean=ActionExpressionType, key=type, node='type: assign', isHeteroListProperty=false
     * 10) bean=ActionExpressionType, key=parameter, node=XNode(map:2 entries), isHeteroListProperty=false
     * 11) bean=ActionParameterValueType, key=name, node='name: role', isHeteroListProperty=false
     * 12) bean=ActionParameterValueType, key=value, node='value: rome555c-7797-11e2-94a6-001e8c717e5b', isHeteroListProperty=false
     * 13) bean=SearchExpressionType, key=type, node='type: UserType', isHeteroListProperty=false
     *
     * Here we have 2 calls with isHeteroListProperty=true; first for pipeline.scriptingExpression, second for
     * sequence.scriptingExpression.
     */
    private <T> void unmarshalEntry(@NotNull T bean, @NotNull Class<T> beanClass,
            @NotNull QName key, @NotNull XNodeImpl node, @NotNull XNodeImpl containingNode,
            boolean isHeteroListProperty, @NotNull ParsingContext pc) throws SchemaException {

        //System.out.println("bean=" + bean.getClass().getSimpleName() + ", key=" + key.getLocalPart() + ", node=" + node + ", isHeteroListProperty=" + isHeteroListProperty);
        final String propName = key.getLocalPart();

        // this code is just to keep this method reasonably short
        PropertyAccessMechanism mechanism = new PropertyAccessMechanism();
        if (!mechanism.compute(bean, beanClass, propName, key, node, pc)) {
            return;
        }

        final String actualPropertyName = mechanism.actualPropertyName;
        final boolean storeAsRawType = mechanism.storeAsRawType;

        final Method getter = mechanism.getter;
        final Method setter = mechanism.setter;
        final boolean wrapInJaxbElement = mechanism.wrapInJaxbElement;

        if (Element.class.isAssignableFrom(mechanism.paramType)) {
            ValidationLog validationLog = new ValidationLog(ValidationLogType.ERROR, ValidationLogType.Specification.UNKNOW, node.getSourceLocation(),
                    new TechnicalMessage("DOM not supported in field '%s' in '%s'",
                            new Argument(actualPropertyName, Argument.ArgumentType.STRING),
                            new Argument(beanClass, Argument.ArgumentType.UNKNOW)),
                    "DOM not supported in field '%s' in '%s'".formatted(actualPropertyName, beanClass));
            pc.warn(LOGGER, validationLog);
            throw new IllegalArgumentException(validationLog.message());
        }

        // The type T that is expected by the bean, i.e. either by
        //   - setMethod(T value), or
        //   - Collection<T> getMethod()
        // We use it to retrieve the correct value when parsing the node.
        // We might specialize it using the information derived from the node (to deal with inclusive polymorphism,
        // i.e. storing ExclusionPolicyConstraintType where AbstractPolicyConstraintType is expected).
        @NotNull Class<?> paramType;

        if (!storeAsRawType && !isHeteroListProperty) {
            Class<?> t = specializeParamType(node,  mechanism.paramType, pc);
            if (t == null) {    // indicates a problem
                return;
            } else {
                paramType = t;
            }
        } else {
            paramType = mechanism.paramType;
        }

        if (!(node instanceof ListXNodeImpl) && Object.class.equals(paramType) && !storeAsRawType) {
            ValidationLog validationLog = new ValidationLog(ValidationLogType.ERROR, ValidationLogType.Specification.UNKNOW, node.getSourceLocation(),
                    new TechnicalMessage("Object property (without @Raw) not supported in field '%s' in '%s'",
                            new Argument(actualPropertyName, Argument.ArgumentType.STRING),
                            new Argument(beanClass, Argument.ArgumentType.UNKNOW)),
                    "Object property (without @Raw) not supported in field '%s' in '%s'".formatted(actualPropertyName, beanClass));
            pc.warn(LOGGER, validationLog);
            throw new IllegalArgumentException(validationLog.message());
        }

//        String paramNamespace = inspector.determineNamespace(paramType);

        boolean problem = false;
        Object propValue = null;
        Collection<Object> propValues = null;
        // For heterogeneous lists we have to create multi-valued fictitious property first. So we have to treat node as a map
        // (instead of list) and process it as a single value. Only when
        if (node instanceof ListXNodeImpl && (!node.isHeterogeneousList() || isHeteroListProperty)) {
            ListXNodeImpl xlist = (ListXNodeImpl) node;
            if (setter != null) {
                try {
                    Object value = unmarshalSinglePropValue(node, actualPropertyName, paramType, storeAsRawType, beanClass, pc);
                    if (wrapInJaxbElement) {
                        propValue = wrapInJaxbElement(value, mechanism.objectFactory,
                                mechanism.elementFactoryMethod, propName, beanClass, pc);
                    } else {
                        propValue = value;
                    }
                } catch (SchemaException e) {
                    problem = processSchemaException(e, node, pc);
                }
            } else {
                // No setter, we have to use collection getter
                propValues = new ArrayList<>(xlist.size());
                for (XNodeImpl xsubsubnode: xlist) {
                    try {
                        Object valueToAdd;
                        Object value = unmarshalSinglePropValue(xsubsubnode, actualPropertyName, paramType, storeAsRawType, beanClass, pc);
                        if (value != null) {
                            if (isHeteroListProperty) {
                                QName elementName = xsubsubnode.getElementName();
                                if (elementName == null) {
                                    pc.warnOrThrow(LOGGER, new ValidationLog(ValidationLogType.ERROR, ValidationLogType.Specification.UNKNOW, xsubsubnode.getSourceLocation(),
                                            new TechnicalMessage("Heterogeneous list with a no-elementName node: '%s'", new Argument(xsubsubnode, Argument.ArgumentType.XNODE)),
                                            "Heterogeneous list with a no-elementName node"));
                                }
                                Class valueClass = value.getClass();
                                QName jaxbElementName;
                                if (QNameUtil.hasNamespace(elementName)) {
                                    jaxbElementName = elementName;
                                } else {
                                    // Approximate solution: find element in schema registry - check for type compatibility
                                    // in order to exclude accidental name matches (like c:expression/s:expression).
                                    Optional<ItemDefinition> itemDefOpt = getSchemaRegistry().findItemDefinitionsByElementName(elementName)
                                            .stream()
                                            .filter(def ->
                                                    getSchemaRegistry().findTypeDefinitionsByType(def.getTypeName()).stream()
                                                            .anyMatch(typeDef -> typeDef.getCompileTimeClass() != null
                                                                    && typeDef.getCompileTimeClass()
                                                                    .isAssignableFrom(valueClass)))
                                            .findFirst();
                                    if (itemDefOpt.isPresent()) {
                                        jaxbElementName = itemDefOpt.get().getItemName();
                                    } else {
                                        pc.warn(LOGGER, new ValidationLog(ValidationLogType.ERROR, ValidationLogType.Specification.UNKNOW, xsubsubnode.getSourceLocation(),
                                                new TechnicalMessage("Heterogeneous list member with unknown element name '%s': '%s'",
                                                        new Argument(elementName, Argument.ArgumentType.STRING),
                                                        new Argument(value, Argument.ArgumentType.UNKNOW)),
                                                "Heterogeneous list member with unknown element name '%s': '%s'".formatted(elementName, value)));
                                        jaxbElementName = elementName;        // unqualified
                                    }
                                }
                                @SuppressWarnings("unchecked")
                                JAXBElement jaxbElement = new JAXBElement<>(jaxbElementName, valueClass, value);
                                valueToAdd = jaxbElement;
                            } else {
                                if (wrapInJaxbElement) {
                                    valueToAdd = wrapInJaxbElement(value, mechanism.objectFactory,
                                            mechanism.elementFactoryMethod, propName, beanClass, pc);
                                } else {
                                    valueToAdd = value;
                                }
                            }
                            propValues.add(valueToAdd);
                        }
                    } catch (SchemaException e) {
                        problem = processSchemaException(e, xsubsubnode, pc);
                    }
                }
            }
        } else {
            try {
                propValue = unmarshalSinglePropValue(node, actualPropertyName, paramType, storeAsRawType, beanClass, pc);
                if (wrapInJaxbElement) {
                    propValue = wrapInJaxbElement(propValue, mechanism.objectFactory,
                            mechanism.elementFactoryMethod, propName, beanClass, pc);
                }
            } catch (SchemaException e) {
                problem = processSchemaException(e, node, pc);
            }
        }

        if (setter != null) {
            try {
                setter.invoke(bean, propValue);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                ValidationLog validationLog = new ValidationLog(ValidationLogType.ERROR, ValidationLogType.Specification.UNKNOW, node.getSourceLocation(),
                        new TechnicalMessage("Cannot invoke setter '%s' on bean of type '%s': '%s'",
                                new Argument(setter, Argument.ArgumentType.UNKNOW),
                                new Argument(beanClass, Argument.ArgumentType.UNKNOW),
                                new Argument(e.getMessage(), Argument.ArgumentType.STRING)),
                        "Cannot invoke setter '%s' on bean of type '%s': '%s'".formatted(setter, beanClass, e.getMessage()));
                pc.warn(LOGGER, validationLog);
                throw new SystemException(validationLog.message(), e);
            }
        } else if (getter != null) {
            Object getterReturn;
            Collection<Object> col;
            try {
                getterReturn = getter.invoke(bean);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                ValidationLog validationLog = new ValidationLog(ValidationLogType.ERROR, ValidationLogType.Specification.UNKNOW, node.getSourceLocation(),
                        new TechnicalMessage("Cannot invoke getter '%s' on bean of type '%s': '%s'",
                                new Argument(getter, Argument.ArgumentType.UNKNOW),
                                new Argument(beanClass, Argument.ArgumentType.UNKNOW),
                                new Argument(e.getMessage(), Argument.ArgumentType.STRING)),
                        "Cannot invoke getter '%s' on bean of type '%s': '%s'".formatted(getter, beanClass, e.getMessage()));
                pc.warn(LOGGER, validationLog);
                throw new SystemException(validationLog.message(), e);
            }
            try {
                col = (Collection<Object>)getterReturn;
            } catch (ClassCastException e) {
                ValidationLog validationLog = new ValidationLog(ValidationLogType.ERROR, ValidationLogType.Specification.UNKNOW, node.getSourceLocation(),
                        new TechnicalMessage("Getter '%s' on bean of type '%s' returned '%s' instead of collection",
                                new Argument(getter, Argument.ArgumentType.UNKNOW),
                                new Argument(beanClass, Argument.ArgumentType.UNKNOW),
                                new Argument(getterReturn, Argument.ArgumentType.UNKNOW)),
                        "Getter '%s' on bean of type '%s' returned '%s' instead of collection".formatted(getter, beanClass, getterReturn));
                pc.warn(LOGGER, validationLog);
                throw new SystemException(validationLog.message());
            }
            if (propValue != null) {
                col.add(propValue);
            } else if (propValues != null) {
                for (Object propVal: propValues) {
                    col.add(propVal);
                }
            } else if (!problem) {
                ValidationLog validationLog = new ValidationLog(ValidationLogType.ERROR, ValidationLogType.Specification.UNKNOW, node.getSourceLocation(),
                        new TechnicalMessage("Strange. Multival property '%s' in '%s' produced null values list, parsed from '%s'",
                                new Argument(propName, Argument.ArgumentType.UNKNOW),
                                new Argument(beanClass, Argument.ArgumentType.UNKNOW),
                                new Argument(containingNode, Argument.ArgumentType.UNKNOW)),
                        "Strange. Multival property '%s' in '%s' produced null values list, parsed from '%s'".formatted(propName, beanClass, containingNode));
                pc.warn(LOGGER, validationLog);
                throw new IllegalStateException(validationLog.message());
            }
            if (!isHeteroListProperty) {
                checkJaxbElementConsistence(col, pc);
            }
        } else {
            ValidationLog validationLog = new ValidationLog(ValidationLogType.ERROR, ValidationLogType.Specification.UNKNOW, node.getSourceLocation(),
                    new TechnicalMessage("Uh? No setter nor getter."),"Uh? No setter nor getter.");
            pc.warn(LOGGER, validationLog);
            throw new IllegalStateException(validationLog.message());
        }
    }

    private Class<?> specializeParamType(@NotNull XNodeImpl node, @NotNull Class<?> expectedType, @NotNull ParsingContext pc)
            throws SchemaException {
        if (node.getTypeQName() != null) {
            Class explicitType = getSchemaRegistry().determineClassForType(node.getTypeQName());
            return explicitType != null && expectedType.isAssignableFrom(explicitType) ? explicitType : expectedType;
            // (if not assignable, we hope the adaptation will do it)
        } else if (node.getElementName() != null) {
            Collection<TypeDefinition> candidateTypes = getSchemaRegistry()
                    .findTypeDefinitionsByElementName(node.getElementName(), TypeDefinition.class);
            List<TypeDefinition> suitableTypes = candidateTypes.stream()
                    .filter(def -> def.getCompileTimeClass() != null && expectedType.isAssignableFrom(def.getCompileTimeClass()))
                    .collect(Collectors.toList());
            if (suitableTypes.isEmpty()) {
                pc.warnOrThrow(LOGGER, new ValidationLog(ValidationLogType.ERROR, ValidationLogType.Specification.UNKNOW, node.getSourceLocation(),
                        new TechnicalMessage("Couldn't derive suitable type based on element name ('%s'). Candidate types:  '%s'; expected type: '%s'",
                                new Argument(node.getElementName(), Argument.ArgumentType.STRING),
                                new Argument(candidateTypes, Argument.ArgumentType.UNKNOW),
                                new Argument(expectedType, Argument.ArgumentType.UNKNOW)),
                        "Couldn't derive suitable type based on element name ('%s'). Candidate types: '%s'; expected type: '%s'".formatted(node.getElementName(), candidateTypes, expectedType)));
                return null;
            } else if (suitableTypes.size() > 1) {
                pc.warnOrThrow(LOGGER, new ValidationLog(ValidationLogType.ERROR, ValidationLogType.Specification.UNKNOW, node.getSourceLocation(),
                        new TechnicalMessage("Couldn't derive single suitable type based on element name ('%s'). Suitable types: '%s'",
                                new Argument(node.getElementName(), Argument.ArgumentType.STRING),
                                new Argument(suitableTypes, Argument.ArgumentType.UNKNOW)),
                        "Couldn't derive single suitable type based on element name ('%s'). Suitable types: '%s'".formatted(node.getElementName(), suitableTypes)));
                return null;
            }
            return suitableTypes.get(0).getCompileTimeClass();
        } else {
            return expectedType;
        }
    }

    private class PropertyAccessMechanism {

        Class<?> beanClass;

        // phase1
        String actualPropertyName;      // This is the name of property we will really use. (Considering e.g. substitutions.)
        boolean storeAsRawType;            // Whether the data will be stored as RawType.
        Object objectFactory;            // JAXB object factory instance (e.g. xxxx.common-3.ObjectFactory).
        Method elementFactoryMethod;    // Method in object factory that creates a given JAXB element (e.g. createAsIs(value))

        // phase2
        Method getter, setter;            // Getter or setter that will be used to put a value (getter in case of collections)
        Class<?> paramType;                // Actual parameter type; unwrapped: Collection<X> -> X, JAXBElement<X> -> X
        boolean wrapInJaxbElement;        // If the paramType contained JAXBElement, i.e. if the value should be wrapped into it before using

        // returns true if the processing is to be continued;
        // false in case of using alternative way of unmarshalling (e.g. use of "any" method), or in case of error (in COMPAT mode)
        private <T> boolean compute(T bean, Class<T> beanClass, String propName, QName key, XNodeImpl node, ParsingContext pc)
                throws SchemaException {

            this.beanClass = beanClass;

            // phase1
            if (!computeActualPropertyName(bean, propName, key, node, pc)) {
                return false;
            }
            // phase2
            return computeGetterAndSetter(propName, node, pc);
        }

        // computes actualPropertyName + storeAsRawType
        // if necessary, fills-in also objectFactory + elementFactoryMethod
        private <T> boolean computeActualPropertyName(T bean, String propName, QName key, XNodeImpl node, ParsingContext pc)
                throws SchemaException {
            Field propertyField = inspector.findPropertyField(beanClass, propName);
            Method propertyGetter = null;
            if (propertyField == null) {
                propertyGetter = inspector.findPropertyGetter(beanClass, propName);
            }

            // Maybe this is not used in this context, because node.elementName is filled-in only for heterogeneous list
            // members - and they are iterated through elsewhere. Nevertheless, it is more safe to include it also here.
//            QName realElementName = getRealElementName(node, key, pc);
//            String realElementLocalName = realElementName.getLocalPart();

            elementFactoryMethod = null;
            objectFactory = null;
            if (propertyField == null && propertyGetter == null) {
                // We have to try to find a more generic field, such as xsd:any or substitution element
                // check for global element definition first
                elementFactoryMethod = findElementFactoryMethod(propName, pc);            // realElementLocalName
                if (elementFactoryMethod != null) {
                    // great - global element found, let's look up the field
                    propertyField = inspector.lookupSubstitution(beanClass, elementFactoryMethod);
                    if (propertyField == null) {
                        propertyField = inspector.findAnyField(beanClass);        // Check for "any" field
                        if (propertyField != null) {
                            unmarshalToAnyUsingField(bean, propertyField, key, node, pc);
                        } else {
                            unmarshalToAnyUsingGetterIfExists(bean, key, node, pc, propName);
                        }
                        return false;
                    }
                } else {
                    unmarshalToAnyUsingGetterIfExists(bean, key, node, pc, propName);        // e.g. "getAny()"
                    return false;
                }
            }

            // At this moment, property getter is the exact getter matching key.localPart (propName).
            // Property field may be either exact field matching key.localPart (propName), or more generic one (substitution, any).
            //noinspection ConstantConditions
            assert propertyGetter != null || propertyField != null;

            if (elementFactoryMethod != null) {
                storeAsRawType = elementFactoryMethod.getAnnotation(Raw.class) != null;
            } else if (propertyGetter != null) {
                storeAsRawType = propertyGetter.getAnnotation(Raw.class) != null;
            } else {
                storeAsRawType = propertyField.getAnnotation(Raw.class) != null;
            }

            if (propertyField != null) {
                actualPropertyName = propertyField.getName();
            } else {
                actualPropertyName = propName;
            }

            return true;
        }

        private Method findElementFactoryMethod(String propName, ParsingContext pc) {
            Class objectFactoryClass = inspector.getObjectFactoryClass(beanClass.getPackage());
            objectFactory = instantiateObjectFactory(objectFactoryClass, pc);
            return inspector.findElementMethodInObjectFactory(objectFactoryClass, propName);
        }

        private boolean computeGetterAndSetter(String propName, XNode xNode, ParsingContext pc) throws SchemaException {
            setter = inspector.findSetter(beanClass, actualPropertyName);
            wrapInJaxbElement = false;
            paramType = null;
            if (setter == null) {
                // No setter. But if the property is multi-value we need to look
                // for a getter that returns a collection (Collection<Whatever>)
                getter = inspector.findPropertyGetter(beanClass, actualPropertyName);
                if (getter == null) {
                    pc.warnOrThrow(LOGGER, new ValidationLog(ValidationLogType.ERROR, ValidationLogType.Specification.UNKNOW, xNode.getSourceLocation(),
                            new TechnicalMessage("Cannot find setter or getter for field '%s' in '%s'",
                                    new Argument(actualPropertyName, Argument.ArgumentType.STRING),
                                    new Argument(beanClass, Argument.ArgumentType.UNKNOW)),
                            "Cannot find setter or getter for field '%s' in '%s'".formatted(actualPropertyName, beanClass)));
                    return false;
                }
                computeParamTypeFromGetter(propName, getter.getReturnType(), xNode, pc);
            } else {
                getter = null;
                Class<?> setterType = setter.getParameterTypes()[0];
                computeParamTypeFromSetter(propName, setterType, xNode, pc);
            }
            return true;
        }

        private void computeParamTypeFromSetter(String propName, Class<?> setterParamType, XNode xNode, ParsingContext pc) {
            if (JAXBElement.class.equals(setterParamType)) {
                //                    TODO some handling for the returned generic parameter types
                Type[] genericTypes = setter.getGenericParameterTypes();
                if (genericTypes.length != 1) {
                    ValidationLog validationLog = new ValidationLog(ValidationLogType.ERROR, ValidationLogType.Specification.UNKNOW, xNode.getSourceLocation(),
                            new TechnicalMessage("Too lazy to handle this."),"Too lazy to handle this.");
                    pc.warn(LOGGER, validationLog);
                    throw new IllegalArgumentException(validationLog.message());
                }
                Type genericType = genericTypes[0];
                if (genericType instanceof ParameterizedType) {
                    Type actualType = inspector.getTypeArgument(genericType, "add some description");
                    if (actualType instanceof WildcardType) {
                        if (elementFactoryMethod == null) {
                            elementFactoryMethod = findElementFactoryMethod(propName, pc);
                        }
                        // This is the case of Collection<JAXBElement<?>>
                        // we need to extract the specific type from the factory method
                        if (elementFactoryMethod == null) {
                            ValidationLog validationLog = new ValidationLog(ValidationLogType.ERROR, ValidationLogType.Specification.UNKNOW, xNode.getSourceLocation(),
                                    new TechnicalMessage("Wildcard type in JAXBElement field specification and no factory method found for field '%s' in '%s', cannot determine collection type (inner type argument)",
                                            new Argument(actualPropertyName, Argument.ArgumentType.STRING),
                                            new Argument(beanClass, Argument.ArgumentType.UNKNOW)),
                                    "Wildcard type in JAXBElement field specification and no factory method found for field '%s' in '%s', cannot determine collection type (inner type argument)".formatted(actualPropertyName, beanClass));
                            pc.warn(LOGGER, validationLog);
                            throw new IllegalArgumentException(validationLog.message());
                        }
                        Type factoryMethodGenericReturnType = elementFactoryMethod.getGenericReturnType();
                        Type factoryMethodTypeArgument = inspector.getTypeArgument(factoryMethodGenericReturnType,
                                "in factory method " + elementFactoryMethod + " return type for field " + actualPropertyName
                                        + " in " + beanClass + ", cannot determine collection type");
                        if (factoryMethodTypeArgument instanceof Class) {
                            // This is the case of JAXBElement<Whatever>
                            paramType = (Class<?>) factoryMethodTypeArgument;
                            if (Object.class.equals(paramType) && !storeAsRawType) {
                                ValidationLog validationLog = new ValidationLog(ValidationLogType.ERROR, ValidationLogType.Specification.UNKNOW, xNode.getSourceLocation(),
                                        new TechnicalMessage("Factory method '%s' type argument is Object (without @Raw) for field '%s' in '%s', property '%s'",
                                                new Argument(elementFactoryMethod, Argument.ArgumentType.UNKNOW),
                                                new Argument(actualPropertyName, Argument.ArgumentType.STRING),
                                                new Argument(beanClass, Argument.ArgumentType.UNKNOW),
                                                new Argument(propName, Argument.ArgumentType.STRING)),
                                        "Factory method '%s' type argument is Object (without @Raw) for field '%s' in '%s', property '%s'".formatted(elementFactoryMethod, actualPropertyName, beanClass, propName));
                                pc.warn(LOGGER, validationLog);
                                throw new IllegalArgumentException(validationLog.message());
                            }
                        } else {
                            ValidationLog validationLog = new ValidationLog(ValidationLogType.ERROR, ValidationLogType.Specification.UNKNOW, xNode.getSourceLocation(),
                                    new TechnicalMessage("Cannot determine factory method return type, got '%s' - for field '%s' in '%s', cannot determine collection type (inner type argument)",
                                            new Argument(factoryMethodTypeArgument, Argument.ArgumentType.UNKNOW),
                                            new Argument(actualPropertyName, Argument.ArgumentType.STRING),
                                            new Argument(beanClass, Argument.ArgumentType.UNKNOW)),
                                    "Cannot determine factory method return type, got '%s' - for field '%s' in '%s', cannot determine collection type (inner type argument)".formatted(factoryMethodTypeArgument, actualPropertyName, beanClass));
                            pc.warn(LOGGER, validationLog);
                            throw new IllegalArgumentException(validationLog.message());
                        }
                    }
                }
                //                    Class enclosing = paramType.getEnclosingClass();
                //                    Class clazz = paramType.getClass();
                //                    Class declaring = paramType.getDeclaringClass();
                wrapInJaxbElement = true;
            } else {
                paramType = setterParamType;
            }
        }

        private void computeParamTypeFromGetter(String propName, Class<?> getterReturnType, XNode xNode, ParsingContext pc) throws SchemaException {
            if (!Collection.class.isAssignableFrom(getterReturnType)) {
                ValidationLog validationLog = new ValidationLog(ValidationLogType.ERROR, ValidationLogType.Specification.UNKNOW, xNode.getSourceLocation(),
                        new TechnicalMessage("Cannot find setter for field '%s' in '%s'. The getter was found, but it does not return collection - so it cannot be used to set the value.",
                                new Argument(actualPropertyName, Argument.ArgumentType.STRING),
                                new Argument(beanClass, Argument.ArgumentType.UNKNOW)),
                        "Cannot find setter for field '%s' in '%s'. The getter was found, but it does not return collection - so it cannot be used to set the value.".formatted(actualPropertyName, beanClass));
                pc.warn(LOGGER, validationLog);
                throw new SchemaException(validationLog.message());
            }
            // getter.genericReturnType = Collection<...>
            Type typeArgument = inspector.getTypeArgument(getter.getGenericReturnType(),
                    "for field " + actualPropertyName + " in " + beanClass + ", cannot determine collection type");
            if (typeArgument instanceof Class) {
                paramType = (Class<?>) typeArgument;    // ok, like Collection<AssignmentType>
            } else if (typeArgument instanceof ParameterizedType) {        // something more complex
                ParameterizedType paramTypeArgument = (ParameterizedType) typeArgument;
                Type rawTypeArgument = paramTypeArgument.getRawType();
                if (rawTypeArgument.equals(JAXBElement.class)) {
                    // This is the case of Collection<JAXBElement<....>>
                    wrapInJaxbElement = true;
                    Type innerTypeArgument = inspector.getTypeArgument(typeArgument,
                            "for field " + actualPropertyName + " in " + beanClass
                                    + ", cannot determine collection type (inner type argument)");
                    if (innerTypeArgument instanceof Class) {
                        // This is the case of Collection<JAXBElement<Whatever>> (note that wrapInJaxbElement is now true)
                        paramType = (Class<?>) innerTypeArgument;
                    } else if (innerTypeArgument instanceof WildcardType) {
                        // This is the case of Collection<JAXBElement<?>>
                        // we need to extract the specific type from the factory method
                        if (elementFactoryMethod == null) {
                            elementFactoryMethod = findElementFactoryMethod(propName, pc);
                            if (elementFactoryMethod == null) {
                                ValidationLog validationLog = new ValidationLog(ValidationLogType.ERROR, ValidationLogType.Specification.UNKNOW, xNode.getSourceLocation(),
                                        new TechnicalMessage("Wildcard type in JAXBElement field specification and no factory method found for field '%s' in '%s', cannot determine collection type (inner type argument)",
                                                new Argument(actualPropertyName, Argument.ArgumentType.STRING),
                                                new Argument(beanClass, Argument.ArgumentType.UNKNOW)),
                                        "Wildcard type in JAXBElement field specification and no factory method found for field '%s' in '%s', cannot determine collection type (inner type argument)".formatted(actualPropertyName, beanClass));
                                pc.warn(LOGGER, validationLog);
                                throw new IllegalArgumentException(validationLog.message());
                            }
                        }
                        // something like JAXBElement<AsIsExpressionEvaluatorType>
                        Type factoryMethodGenericReturnType = elementFactoryMethod.getGenericReturnType();
                        Type factoryMethodTypeArgument = inspector.getTypeArgument(factoryMethodGenericReturnType,
                                "in factory method " + elementFactoryMethod + " return type for field " + actualPropertyName
                                        + " in " + beanClass + ", cannot determine collection type");
                        if (factoryMethodTypeArgument instanceof Class) {
                            // This is the case of JAXBElement<Whatever>
                            paramType = (Class<?>) factoryMethodTypeArgument;
                            if (Object.class.equals(paramType) && !storeAsRawType) {
                                ValidationLog validationLog = new ValidationLog(ValidationLogType.ERROR, ValidationLogType.Specification.UNKNOW, xNode.getSourceLocation(),
                                        new TechnicalMessage("Factory method '%s' type argument is Object (and not @Raw) for field '%s' in '%s', property '%s'",
                                                new Argument(elementFactoryMethod, Argument.ArgumentType.UNKNOW),
                                                new Argument(actualPropertyName, Argument.ArgumentType.STRING),
                                                new Argument(beanClass, Argument.ArgumentType.UNKNOW),
                                                new Argument(propName, Argument.ArgumentType.STRING)),
                                        "Factory method '%s' type argument is Object (and not @Raw) for field '%s' in '%s', property '%s'".formatted(elementFactoryMethod, actualPropertyName, beanClass, propName));
                                pc.warn(LOGGER, validationLog);
                                throw new IllegalArgumentException(validationLog.message());
                            }
                        } else {
                            ValidationLog validationLog = new ValidationLog(ValidationLogType.ERROR, ValidationLogType.Specification.UNKNOW, xNode.getSourceLocation(),
                                    new TechnicalMessage("Cannot determine factory method return type, got '%s' - for field '%s' in '%s', cannot determine collection type (inner type argument)",
                                            new Argument(factoryMethodTypeArgument, Argument.ArgumentType.UNKNOW),
                                            new Argument(actualPropertyName, Argument.ArgumentType.STRING),
                                            new Argument(beanClass, Argument.ArgumentType.UNKNOW)),
                                    "Cannot determine factory method return type, got '%s' - for field '%s' in '%s', cannot determine collection type (inner type argument)".formatted(factoryMethodTypeArgument, actualPropertyName, beanClass));
                            pc.warn(LOGGER, validationLog);
                            throw new IllegalArgumentException(validationLog.message());
                        }
                    } else {
                        ValidationLog validationLog = new ValidationLog(ValidationLogType.ERROR, ValidationLogType.Specification.UNKNOW, xNode.getSourceLocation(),
                                new TechnicalMessage("Ejha! '%s' '%s' from '%s' from '%s' in '%s' '%s'",
                                        new Argument(innerTypeArgument, Argument.ArgumentType.UNKNOW),
                                        new Argument(innerTypeArgument.getClass(), Argument.ArgumentType.UNKNOW),
                                        new Argument(getterReturnType, Argument.ArgumentType.UNKNOW),
                                        new Argument(actualPropertyName, Argument.ArgumentType.STRING),
                                        new Argument(propName, Argument.ArgumentType.STRING),
                                        new Argument(beanClass, Argument.ArgumentType.UNKNOW)),
                                "Ejha! '%s' '%s' from '%s' from '%s' in '%s' '%s'".formatted(innerTypeArgument, innerTypeArgument.getClass(), getterReturnType, actualPropertyName, propName, beanClass));
                        pc.warn(LOGGER, validationLog);
                        throw new IllegalArgumentException(validationLog.message());
                    }
                } else {
                    // The case of Collection<Whatever<Something>>
                    if (rawTypeArgument instanceof Class) {        // ??? rawTypeArgument is the 'Whatever' part
                        paramType = (Class<?>) rawTypeArgument;
                    } else {
                        ValidationLog validationLog = new ValidationLog(ValidationLogType.ERROR, ValidationLogType.Specification.UNKNOW, xNode.getSourceLocation(),
                                new TechnicalMessage("EH? Eh!? '%s' '%s' from '%s' from '%s' in '%s' '%s'",
                                        new Argument(typeArgument, Argument.ArgumentType.UNKNOW),
                                        new Argument(typeArgument.getClass(), Argument.ArgumentType.UNKNOW),
                                        new Argument(getterReturnType, Argument.ArgumentType.UNKNOW),
                                        new Argument(actualPropertyName, Argument.ArgumentType.STRING),
                                        new Argument(propName, Argument.ArgumentType.STRING),
                                        new Argument(beanClass, Argument.ArgumentType.UNKNOW)),
                                "EH? Eh!? '%s' '%s' from '%s' from '%s' in '%s' '%s'".formatted(typeArgument, typeArgument.getClass(), getterReturnType, actualPropertyName, propName, beanClass));
                        pc.warn(LOGGER, validationLog);
                        throw new IllegalArgumentException(validationLog.message());
                    }
                }
            } else {
                ValidationLog validationLog = new ValidationLog(ValidationLogType.ERROR, ValidationLogType.Specification.UNKNOW, xNode.getSourceLocation(),
                        new TechnicalMessage("EH? '%s' '%s' from '%s' from '%s' in '%s' '%s'",
                                new Argument(typeArgument, Argument.ArgumentType.UNKNOW),
                                new Argument(typeArgument.getClass(), Argument.ArgumentType.UNKNOW),
                                new Argument(getterReturnType, Argument.ArgumentType.UNKNOW),
                                new Argument(actualPropertyName, Argument.ArgumentType.STRING),
                                new Argument(propName, Argument.ArgumentType.STRING),
                                new Argument(beanClass, Argument.ArgumentType.UNKNOW)),
                        "EH? '%s' '%s' from '%s' from '%s' in '%s' '%s'".formatted(typeArgument, typeArgument.getClass(), getterReturnType, actualPropertyName, propName, beanClass));
                pc.warn(LOGGER, validationLog);
                throw new IllegalArgumentException(validationLog.message());
            }
        }
    }

    private <T> void unmarshalToAnyUsingGetterIfExists(@NotNull T bean, @NotNull QName key, @NotNull XNodeImpl node,
            @NotNull ParsingContext pc, String propName) throws SchemaException {
        Method elementMethod = inspector.findAnyMethod(bean.getClass());
        if (elementMethod != null) {
            unmarshallToAnyUsingGetter(bean, elementMethod, key, node, pc);
        } else {
            ValidationLog validationLog = new ValidationLog(ValidationLogType.ERROR, ValidationLogType.Specification.UNKNOW, node.getSourceLocation(),
                    new TechnicalMessage("No field '%s' in class '%s' (and no element method in object factory too)",
                            new Argument(propName, Argument.ArgumentType.STRING),
                            new Argument(bean.getClass(), Argument.ArgumentType.UNKNOW)),
                    "No field '%s' in class '%s' (and no element method in object factory too)".formatted(propName, bean.getClass()));
            pc.warnOrThrow(LOGGER, validationLog);
        }
    }

    // Prepares value to be stored into the bean - e.g. converts PolyString->PolyStringType, wraps a value to JAXB if specified, ...
    private Object wrapInJaxbElement(Object propVal, Object objectFactory, Method factoryMethod, String propName,
            Class beanClass, ParsingContext pc) {
        if (factoryMethod == null) {
            ValidationLog validationLog = new ValidationLog(ValidationLogType.ERROR, ValidationLogType.Specification.UNKNOW, SourceLocation.unknown(),
                    new TechnicalMessage("Param type is JAXB element but no factory method found for it, property '%s' in '%s'",
                            new Argument(propName, Argument.ArgumentType.STRING),
                            new Argument(beanClass, Argument.ArgumentType.UNKNOW)),
                    "Param type is JAXB element but no factory method found for it, property '%s' in '%s'".formatted(propName, beanClass));
            pc.warn(LOGGER, validationLog);
            throw new IllegalArgumentException(validationLog.message());
        }
        try {
            return factoryMethod.invoke(objectFactory, propVal);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            ValidationLog validationLog = new ValidationLog(ValidationLogType.ERROR, ValidationLogType.Specification.UNKNOW, SourceLocation.unknown(),
                    new TechnicalMessage("Unable to invoke factory method '%s' on '%s' for property '%s' in '%s'",
                            new Argument(factoryMethod, Argument.ArgumentType.UNKNOW),
                            new Argument(objectFactory.getClass(), Argument.ArgumentType.UNKNOW),
                            new Argument(propName, Argument.ArgumentType.STRING),
                            new Argument(beanClass, Argument.ArgumentType.UNKNOW)),
                    "Unable to invoke factory method '%s' on '%s' for property '%s' in '%s'".formatted(factoryMethod, objectFactory.getClass(), propName, beanClass));
            pc.warn(LOGGER, validationLog);
            throw new SystemException(validationLog.message());
        }
    }

    /*
     *  We want to avoid this:
     *    <expression>
     *      <script>
     *        <code>'up'</code>
     *      </script>
     *      <value>up</value>
     *    </expression>
     *
     *  Because it cannot be reasonably serialized in XNode (<value> gets changed to <script>).
     */
    private void checkJaxbElementConsistence(Collection<Object> collection, ParsingContext pc) throws SchemaException {
        QName elementName = null;
        for (Object object : collection) {
            if (!(object instanceof JAXBElement)) {
                continue;
            }
            JAXBElement element = (JAXBElement) object;
            if (elementName == null) {
                elementName = element.getName();
            } else {
                if (!QNameUtil.match(elementName, element.getName())) {
                    String m = "Mixing incompatible element names in one property: "
                            + elementName + " and " + element.getName();
                    if (pc.isStrict()) {
                        throw new SchemaException(m);
                    } else {
                        pc.warn(LOGGER, new ValidationLog(ValidationLogType.ERROR, ValidationLogType.Specification.UNKNOW, SourceLocation.unknown(), new TechnicalMessage(m), m));
                    }
                }
            }
        }
    }

    private boolean processSchemaException(SchemaException e, XNodeImpl xsubnode, ParsingContext pc) throws SchemaException {
        if (pc.isStrict()) {
            throw e;
        } else {
            LoggingUtils.logException(LOGGER, "Couldn't parse part of the document. It will be ignored. Document part: {}", e, xsubnode);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Part that couldn't be parsed:\n{}", xsubnode.debugDump());
            }
            pc.warn(new ValidationLog(ValidationLogType.ERROR, ValidationLogType.Specification.UNKNOW, xsubnode.getSourceLocation(),
                    new TechnicalMessage("Couldn't parse part of the document. It will be ignored. Document part:\n '%s'", new Argument(xsubnode, Argument.ArgumentType.XNODE)),
                    "Couldn't parse part of the document. It will be ignored."));

            return true;
        }
    }

    private <T,S> void unmarshallToAnyUsingGetter(T bean, Method getter, QName elementName, XNodeImpl xsubnode, ParsingContext pc) throws SchemaException{
        Class<T> beanClass = (Class<T>) bean.getClass();

        Class objectFactoryClass = inspector.getObjectFactoryClass(elementName.getNamespaceURI());
        Object objectFactory = instantiateObjectFactory(objectFactoryClass, pc);
        Method elementFactoryMethod = inspector.findElementMethodInObjectFactory(objectFactoryClass, elementName.getLocalPart());
        Class<S> subBeanClass = (Class<S>) elementFactoryMethod.getParameterTypes()[0];

        if (xsubnode instanceof ListXNodeImpl){
            for (XNodeImpl xsubSubNode : ((ListXNodeImpl) xsubnode)){
                S subBean = unmarshal(xsubSubNode, subBeanClass, pc);
                unmarshallToAnyValue(bean, beanClass, subBean, objectFactoryClass, objectFactory, elementFactoryMethod, getter, pc);
            }
        } else{
            S subBean = unmarshal(xsubnode, subBeanClass, pc);
            unmarshallToAnyValue(bean, beanClass, subBean, objectFactoryClass, objectFactory, elementFactoryMethod, getter, pc);
        }
    }

    private <T, S> void unmarshallToAnyValue(T bean, Class beanClass, S subBean, Class objectFactoryClass, Object objectFactory,
            Method elementFactoryMethod, Method getter, ParsingContext pc) {
        JAXBElement<S> subBeanElement;
        try {
            subBeanElement = (JAXBElement<S>) elementFactoryMethod.invoke(objectFactory, subBean);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e1) {
            ValidationLog validationLog = new ValidationLog(ValidationLogType.ERROR, ValidationLogType.Specification.UNKNOW, SourceLocation.unknown(),
                    new TechnicalMessage("Cannot invoke factory method '%s' on '%s' with '%s': '%s'"),
                    "Cannot invoke factory method '%s' on '%s' with '%s': '%s'".formatted(elementFactoryMethod, objectFactoryClass, subBean, e1));
            pc.warn(LOGGER, validationLog);
            throw new IllegalArgumentException(validationLog.message(), e1);
        }

        Collection<Object> col;
        Object getterReturn;
        try {
            getterReturn = getter.invoke(bean);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            ValidationLog validationLog = new ValidationLog(ValidationLogType.ERROR, ValidationLogType.Specification.UNKNOW, SourceLocation.unknown(),
                    new TechnicalMessage("Cannot invoke getter '%s' on bean of type '%s': '%s'",
                            new Argument(getter, Argument.ArgumentType.UNKNOW),
                            new Argument(beanClass, Argument.ArgumentType.UNKNOW),
                            new Argument(e.getMessage(), Argument.ArgumentType.STRING)),
                    "Cannot invoke getter '%s' on bean of type '%s': '%s'".formatted(getter, beanClass, e.getMessage()));
            throw new SystemException(validationLog.message(), e);
        }
        try {
            col = (Collection<Object>)getterReturn;
        } catch (ClassCastException e) {
            ValidationLog validationLog = new ValidationLog(ValidationLogType.ERROR, ValidationLogType.Specification.UNKNOW, SourceLocation.unknown(),
                    new TechnicalMessage("Getter '%s' on bean of type '%s' returned '%s' instead of collection",
                            new Argument(getter, Argument.ArgumentType.UNKNOW),
                            new Argument(beanClass, Argument.ArgumentType.UNKNOW),
                            new Argument(getterReturn, Argument.ArgumentType.UNKNOW)),
                    "Getter '%s' on bean of type '%s' returned '%s' instead of collection".formatted(getter, beanClass, getterReturn));
            pc.warn(LOGGER, validationLog);
            throw new SystemException(validationLog.message());
        }
        col.add(subBeanElement != null ? subBeanElement.getValue() : null);
    }

    private <T,S> void unmarshalToAnyUsingField(T bean, Field anyField, QName elementName, XNodeImpl xsubnode, ParsingContext pc) throws SchemaException{
        Method getter = inspector.findPropertyGetter(bean.getClass(), anyField.getName());
        unmarshallToAnyUsingGetter(bean, getter, elementName, xsubnode, pc);
    }

    private Object instantiateObjectFactory(Class objectFactoryClass, ParsingContext pc) {
        try {
            return objectFactoryClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            ValidationLog validationLog = new ValidationLog(ValidationLogType.ERROR, ValidationLogType.Specification.UNKNOW, SourceLocation.unknown(),
                    new TechnicalMessage("Getter '%s' on bean of type '%s' returned '%s' instead of collection",
                            new Argument(objectFactoryClass.getName(), Argument.ArgumentType.UNKNOW),
                            new Argument(e.getMessage(), Argument.ArgumentType.STRING)),
                    "Cannot instantiate object factory class '%s': '%s'".formatted(objectFactoryClass.getName(), e.getMessage()));
            pc.warn(LOGGER, validationLog);
            throw new SystemException(validationLog.message(), e);
        }
    }

    private Object unmarshalSinglePropValue(XNodeImpl xsubnode, String fieldName, Class paramType, boolean storeAsRawType,
            Class classType, ParsingContext pc) throws SchemaException {
        Object propValue;
        if (xsubnode == null) {
            propValue = null;
        } else if (paramType.equals(XNodeImpl.class)) {
            propValue = xsubnode;
        } else if (storeAsRawType || paramType.equals(RawType.class)) {
            // We freeze XNode
            xsubnode.freeze();
            RawType raw = new RawType(xsubnode);
            // FIXME UGLY HACK: parse value if possible
            if (xsubnode.getTypeQName() != null) {
                PrismValue value = prismContext
                        .parserFor(xsubnode.toRootXNode())
                        .context(pc)
                        .parseItemValue();// TODO what about objects? oid/version will be lost here
                if (value != null && !value.isRaw()) {
                    raw = new RawType(value, xsubnode.getTypeQName());
                } else if (pc.isConvertUnknownTypes() && value.isRaw() && value instanceof PrismPropertyValue<?>) {
                    // This is in case that value is raw & we support convert unknown types
                    // We can not use original rawType created, since it contains type name
                    // of type not supported, but we should use raw element, returned from parse
                    // which did migration
                    XNode rawElem = ((PrismPropertyValue<?>)value).getRawElement();
                    raw = new RawType(rawElem.frozen());
                }
            }
            propValue = raw;
        } else {
            // paramType is what we expect e.g. based on parent definition
            // but actual type (given by xsi:type/@typeDef) may be different, e.g. more specific
            // (although we already specialized paramType within the caller, we do it again here, because the subnode
            // used here might be a child of node used in the caller)
            paramType = specializeParamType(xsubnode, paramType, pc);
            if (paramType == null) {
                return null;                // skipping this element in case of error
            }
            if (xsubnode instanceof PrimitiveXNodeImpl<?> || xsubnode instanceof MapXNodeImpl || xsubnode.isHeterogeneousList()) {
                propValue = unmarshal(xsubnode, paramType, pc);
            } else if (xsubnode instanceof ListXNodeImpl) {
                ListXNodeImpl xlist = (ListXNodeImpl)xsubnode;
                if (xlist.size() > 1) {
                    ValidationLog validationLog = new ValidationLog(ValidationLogType.ERROR, ValidationLogType.Specification.UNKNOW, xsubnode.getSourceLocation(),
                            new TechnicalMessage("Cannot set multi-value value to a single valued property '%s' of '%s'",
                                    new Argument(fieldName, Argument.ArgumentType.STRING),
                                    new Argument(classType, Argument.ArgumentType.UNKNOW)),
                            "Cannot set multi-value value to a single valued property '%s' of '%s'".formatted(fieldName, classType));
                    pc.warn(LOGGER, validationLog);
                    throw new SchemaException(validationLog.message());
                } else {
                    if (xlist.isEmpty()) {
                        propValue = null;
                    } else {
                        propValue = xlist.get(0);
                    }
                }
            } else {
                ValidationLog validationLog = new ValidationLog(ValidationLogType.ERROR, ValidationLogType.Specification.UNKNOW, xsubnode.getSourceLocation(),
                        new TechnicalMessage("Cannot parse '%s' to a bean '%s'",
                                new Argument(xsubnode, Argument.ArgumentType.XNODE),
                                new Argument(classType, Argument.ArgumentType.UNKNOW)),
                        "Cannot parse '%s' to a bean '%s'".formatted(fieldName, classType));
                pc.warn(LOGGER, validationLog);
                throw new IllegalArgumentException(validationLog.message());
            }
        }
        if (propValue instanceof PolyString) {
            propValue = new PolyStringType((PolyString) propValue);
        }
        return propValue;
    }

    private <T> T postConvertUnmarshal(Object parsedPrimValue, ParsingContext pc) {
        if (parsedPrimValue == null) {
            return null;
        }
        if (parsedPrimValue instanceof ItemPath) {
            return (T) new ItemPathType((ItemPath)parsedPrimValue);
        } else {
            return (T) parsedPrimValue;
        }
    }

    private SchemaDefinitionType unmarshalSchemaDefinitionType(MapXNodeImpl xmap, Class<?> beanClass, ParsingContext pc) throws SchemaException {
        Entry<QName, XNodeImpl> subEntry = xmap.getSingleSubEntry("schema element");
        if (subEntry == null) {
            return null;
        }
        XNodeImpl xsub = subEntry.getValue();
        if (xsub == null) {
            return null;
        }
        if (!(xsub instanceof SchemaXNodeImpl)) {
            ValidationLog validationLog = new ValidationLog(ValidationLogType.ERROR, ValidationLogType.Specification.UNKNOW, xsub.getSourceLocation(),
                    new TechnicalMessage("Cannot parse '%s' to a bean '%s'", new Argument(xsub, Argument.ArgumentType.XNODE)), "Cannot parse schema from node.");
            pc.warn(LOGGER, validationLog);
            throw new IllegalArgumentException(validationLog.message());
        }
        return unmarshalSchemaDefinitionType((SchemaXNodeImpl) xsub, pc);
    }

    SchemaDefinitionType unmarshalSchemaDefinitionType(SchemaXNodeImpl xsub, ParsingContext pc) throws SchemaException{
        Element schemaElement = xsub.getSchemaElement();
        if (schemaElement == null) {
            ValidationLog validationLog = new ValidationLog(ValidationLogType.ERROR, ValidationLogType.Specification.UNKNOW, xsub.getSourceLocation(),
                    new TechnicalMessage("Empty schema in '%s'", new Argument(xsub, Argument.ArgumentType.XNODE)), "Empty schema in node");
            pc.warn(LOGGER, validationLog);
            throw new SchemaException(validationLog.message());
        }
        SchemaDefinitionType schemaDefType = new SchemaDefinitionType();
        schemaDefType.setSchema(schemaElement);
        return schemaDefType;
    }

    @NotNull
    public PrismContext getPrismContext() {
        return prismContext;
    }

    @NotNull
    private SchemaRegistry getSchemaRegistry() {
        return prismContext.getSchemaRegistry();
    }


    //region Specific unmarshallers =========================================================

    // parses any subtype of SearchFilterType
    private <T extends SearchFilterType> T unmarshalSearchFilterType(MapXNodeImpl xmap, Class<T> beanClass, ParsingContext pc) throws SchemaException {
        if (xmap == null) {
            return null;
        }
        T filterType = instantiate(beanClass, pc, xmap);
        filterType.parseFromXNode(xmap, pc);
        return filterType;
    }

    private ItemPathType unmarshalItemPath(PrimitiveXNodeImpl<ItemPathType> primitiveXNode, Class beanClass, ParsingContext parsingContext)
            throws SchemaException {
        return primitiveXNode.getParsedValue(ItemPathType.COMPLEX_TYPE, ItemPathType.class);
    }

    private Object unmarshalPolyStringFromPrimitive(PrimitiveXNodeImpl<?> node, Class<?> beanClass, ParsingContext parsingContext)
            throws SchemaException {
        Object value;
        if (node.isParsed()) {
            value = node.getValue(); // there can be e.g. PolyString there
        } else {
            //noinspection unchecked
            value = node.getParsedValue(DOMUtil.XSD_STRING, String.class);
        }
        return toCorrectPolyStringClass(value, beanClass, node, parsingContext);
    }

    private Object unmarshalPolyStringFromMap(MapXNodeImpl map, Class<?> beanClass, ParsingContext pc) throws SchemaException {

        String orig = map.getParsedPrimitiveValue(QNameUtil.nullNamespace(PolyString.F_ORIG), DOMUtil.XSD_STRING);

        String norm = map.getParsedPrimitiveValue(QNameUtil.nullNamespace(PolyString.F_NORM), DOMUtil.XSD_STRING);

        PolyStringTranslationType translation;
        XNodeImpl xTranslation = map.get(PolyString.F_TRANSLATION_LOCAL_PART);
        if (xTranslation != null) {
            translation = unmarshal(xTranslation, PolyStringTranslationType.class, pc);
        } else {
            translation = null;
        }

        XNodeImpl xLang = map.get(PolyString.F_LANG_LOCAL_PART);
        Map<String,String> lang = unmarshalLang(xLang, pc);

        if (orig == null && translation == null && lang == null) {
            ValidationLog validationLog = new ValidationLog(ValidationLogType.ERROR, ValidationLogType.Specification.UNKNOW, map.getSourceLocation(),
                    new TechnicalMessage("Null polystring orig (no translation nor lang) in '%s'", new Argument(map, Argument.ArgumentType.XNODE)),
                    "Null polystring orig (no translation nor lang) in map node.");
            pc.warn(LOGGER, validationLog);
            throw new SchemaException(validationLog.message());
        }

        Object value = new PolyStringType(new PolyString(orig, norm, translation, lang));
        return toCorrectPolyStringClass(value, beanClass, map, pc);
    }

    private Map<String, String> unmarshalLang(XNodeImpl xLang, ParsingContext pc) throws SchemaException {
        if (xLang == null) {
            return null;
        }
        if (xLang instanceof PrimitiveXNodeImpl && xLang.isEmpty()) {
            return null;
        }
        if (!(xLang instanceof MapXNodeImpl)) {
            ValidationLog validationLog = new ValidationLog(ValidationLogType.ERROR, ValidationLogType.Specification.UNKNOW, xLang.getSourceLocation(),
                    new TechnicalMessage("Null polystring orig (no translation nor lang) in '%s'", new Argument(xLang, Argument.ArgumentType.XNODE)),
                    "Polystring lang is not a map nor empty primitive node, it is lang node");
            pc.warn(LOGGER, validationLog);
            throw new SchemaException(validationLog.message());
        }
        MapXNodeImpl xLangMap = (MapXNodeImpl)xLang;
        Map<String, String> lang = new HashMap<>();
        for (Entry<QName, XNodeImpl> xLangEntry : xLangMap.entrySet()) {
            QName key = xLangEntry.getKey();
            XNodeImpl xLangEntryVal = xLangEntry.getValue();
            if (!(xLangEntryVal instanceof PrimitiveXNodeImpl)) {
                ValidationLog validationLog = new ValidationLog(ValidationLogType.ERROR, ValidationLogType.Specification.UNKNOW, xLangEntryVal.getSourceLocation(),
                        new TechnicalMessage("Polystring lang for key '%s' is not primitive, it is '%s'", new Argument(xLang, Argument.ArgumentType.XNODE)),
                        "Polystring lang for key '%s' is not primitive, it is lang node".formatted(key.getLocalPart()));
                pc.warn(LOGGER, validationLog);
                throw new SchemaException(validationLog.message());
            }
            //noinspection unchecked
            String value = ((PrimitiveXNodeImpl<String>)xLangEntryVal).getParsedValue(DOMUtil.XSD_STRING, String.class);
            lang.put(key.getLocalPart(), value);
        }
        return lang;
    }

    private Object toCorrectPolyStringClass(Object value, Class<?> beanClass, XNodeImpl node, ParsingContext pc) {
        PolyString polyString;
        if (value instanceof String) {
            polyString = new PolyString((String) value);
        } else if (value instanceof PolyStringType) {
            polyString = ((PolyStringType) value).toPolyString();
        } else if (value instanceof PolyString) {
            polyString = (PolyString) value;        // TODO clone?
        } else if (value == null) {
            polyString = null;
        } else {
            ValidationLog validationLog = new ValidationLog(ValidationLogType.ERROR, ValidationLogType.Specification.UNKNOW, node.getSourceLocation(),
                    new TechnicalMessage("Couldn't convert '%s' to a PolyString; while parsing '%s'",
                            new Argument(value, Argument.ArgumentType.XNODE),
                            new Argument(node.debugDump(), Argument.ArgumentType.RAW)),
                    "Couldn't convert '%s' to a PolyString; while parsing node".formatted(value));
            pc.warn(LOGGER, validationLog);
            throw new IllegalStateException(validationLog.message());
        }
        if (polyString != null && polyString.getNorm() == null) {
            // TODO should we always use default normalizer?
            polyString.recompute(prismContext.getDefaultPolyStringNormalizer());
        }
        if (PolyString.class.equals(beanClass)) {
            return polyString;
        } else if (PolyStringType.class.equals(beanClass)) {
            return new PolyStringType(polyString);
        } else {
            ValidationLog validationLog = new ValidationLog(ValidationLogType.ERROR, ValidationLogType.Specification.UNKNOW, node.getSourceLocation(),
                    new TechnicalMessage("Wrong class for PolyString value:  '%s'",
                            new Argument(beanClass, Argument.ArgumentType.UNKNOW)),
                    "Wrong class for PolyString value:  '%s'".formatted(beanClass));
            pc.warn(LOGGER, validationLog);
            throw new IllegalArgumentException(validationLog.message());
        }
    }

    private Object notSupported(XNodeImpl node, Class<?> beanClass, ParsingContext pc) {
        ValidationLog validationLog = new ValidationLog(ValidationLogType.ERROR, ValidationLogType.Specification.UNKNOW, node.getSourceLocation(),
                new TechnicalMessage("The following couldn't be parsed as  '%s': '%s'",
                        new Argument(beanClass, Argument.ArgumentType.UNKNOW),
                        new Argument(node.debugDump(), Argument.ArgumentType.RAW)),
                "The following couldn't be parsed as  '%s': node".formatted(beanClass));
        pc.warn(LOGGER, validationLog);
        throw new IllegalArgumentException(validationLog.message());
    }

    private XmlAsStringType unmarshalXmlAsStringFromPrimitive(PrimitiveXNodeImpl node, Class<XmlAsStringType> beanClass, ParsingContext parsingContext) throws SchemaException {
        //noinspection unchecked
        return new XmlAsStringType(((PrimitiveXNodeImpl<String>) node).getParsedValue(DOMUtil.XSD_STRING, String.class));
    }

    private XmlAsStringType unmarshalXmlAsStringFromMap(MapXNodeImpl map, Class<XmlAsStringType> beanClass, ParsingContext pc) throws SchemaException {
        // reading a string represented a XML-style content
        // used e.g. when reading report templates (embedded XML)
        // A necessary condition: there may be only one map entry.
        if (map.size() > 1) {
            ValidationLog validationLog = new ValidationLog(ValidationLogType.ERROR, ValidationLogType.Specification.UNKNOW, map.getSourceLocation(),
                    new TechnicalMessage("Map with more than one item cannot be parsed as a string: '%s'",
                            new Argument(map, Argument.ArgumentType.XMAP)),
                    "Map with more than one item cannot be parsed as a string: map node");
            pc.warn(LOGGER, validationLog);
            throw new SchemaException(validationLog.message());
        } else if (map.isEmpty()) {
            return new XmlAsStringType();
        } else {
            Entry<QName, XNodeImpl> entry = map.entrySet().iterator().next();
            DomLexicalProcessor domParser = ((PrismContextImpl) prismContext).getParserDom();
            String value = domParser.write(entry.getValue(), entry.getKey(), null);
            return new XmlAsStringType(value);
        }
    }

    private RawObjectType unmarshalRawObjectType(XNodeImpl node, Class<RawObjectType> beanClass, ParsingContext parsingContext) {
        return new RawObjectType(unmarshalRawType(node, RawType.class, parsingContext));
    }

    private RawType unmarshalRawType(XNodeImpl node, Class<RawType> beanClass, ParsingContext parsingContext) {
        // TODO We could probably try to parse the raw node content using information from explicit node type.
        return new RawType(node.frozen());
    }

    private <T> T unmarshalEnumFromPrimitive(PrimitiveXNodeImpl<?> prim, Class<T> beanClass, ParsingContext pc)
            throws SchemaException {

        String primValue = StringUtils.trim(prim.getParsedValue(DOMUtil.XSD_STRING, String.class));
        if (StringUtils.isEmpty(primValue)) {
            return null;
        }

        String javaEnumString = inspector.findEnumFieldName(beanClass, primValue);
        if (javaEnumString == null) {
            for (Field field: beanClass.getDeclaredFields()) {
                if (field.getName().equals(primValue)) {
                    javaEnumString = field.getName();
                    break;
                }
            }
        }
        if (javaEnumString == null) {
            ValidationLog validationLog = new ValidationLog(ValidationLogType.ERROR, ValidationLogType.Specification.UNKNOW, prim.getSourceLocation(),
                    new TechnicalMessage("Cannot find enum value for string '%s' in '%s'",
                            new Argument(primValue, Argument.ArgumentType.STRING),
                            new Argument(beanClass, Argument.ArgumentType.UNKNOW)),
                    "Cannot find enum value for string '%s' in '%s'".formatted(primValue, beanClass));
            pc.warnOrThrow(LOGGER, validationLog);
            return null;
        }

        @SuppressWarnings("unchecked")
        T bean = (T) Enum.valueOf((Class<Enum>)beanClass, javaEnumString);
        return bean;
    }

    private ProtectedStringType unmarshalProtectedString(MapXNodeImpl map, Class beanClass, ParsingContext pc) throws SchemaException {
        ProtectedStringType protectedType = new ProtectedStringType();
        XNodeProcessorUtil.parseProtectedType(protectedType, map, pc);
        if (protectedType.isEmpty()) {
            // E.g. in case when the xmap is empty or if there are is just an expression
            return null;
        }
        return protectedType;
    }

    private ProtectedStringType unmarshalProtectedString(PrimitiveXNodeImpl<String> prim, Class beanClass, ParsingContext pc) throws SchemaException {
        ProtectedStringType protectedType = new ProtectedStringType();
        protectedType.setClearValue(prim.getParsedValue(DOMUtil.XSD_STRING, String.class));
        return protectedType;
    }

    private ProtectedByteArrayType unmarshalProtectedByteArray(MapXNodeImpl map, Class beanClass, ParsingContext pc) throws SchemaException {
        ProtectedByteArrayType protectedType = new ProtectedByteArrayType();
        XNodeProcessorUtil.parseProtectedType(protectedType, map, pc);
        return protectedType;
    }

    private ProtectedByteArrayType unmarshalProtectedByteArray(PrimitiveXNodeImpl<String> prim, Class beanClass, ParsingContext pc) throws SchemaException {
        ProtectedByteArrayType protectedType = new ProtectedByteArrayType();
        String stringValue = prim.getParsedValue(DOMUtil.XSD_STRING, String.class);
        if (stringValue == null) {
            return null;
        }
        protectedType.setClearValue(ArrayUtils.toObject(stringValue.getBytes(StandardCharsets.UTF_8)));
        return protectedType;
    }
    //endregion
}
