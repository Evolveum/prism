/*
 * Copyright (C) 2010-2024 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.prism.impl.marshaller;

import static com.google.common.base.Preconditions.checkArgument;

import static com.evolveum.midpoint.util.Checks.checkSchema;
import static com.evolveum.midpoint.util.Checks.checkSchemaNotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

import com.evolveum.concepts.SourceLocation;
import com.evolveum.concepts.ValidationLogType;
import com.evolveum.midpoint.prism.impl.lazy.LazyPrismContainerValue;

import com.google.common.collect.ImmutableSet;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.impl.*;
import com.evolveum.midpoint.prism.impl.schema.SchemaRegistryImpl;
import com.evolveum.midpoint.prism.impl.util.PrismUtilInternal;
import com.evolveum.midpoint.prism.impl.xnode.*;
import com.evolveum.midpoint.prism.schema.PrismSchema;
import com.evolveum.midpoint.prism.schema.SchemaRegistry;
import com.evolveum.midpoint.prism.util.PrismUtil;
import com.evolveum.midpoint.prism.xnode.*;
import com.evolveum.midpoint.util.DOMUtil;
import com.evolveum.midpoint.util.DebugDumpable;
import com.evolveum.midpoint.util.QNameUtil;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;
import com.evolveum.prism.xml.ns._public.query_3.SearchFilterType;
import com.evolveum.prism.xml.ns._public.types_3.EvaluationTimeType;
import com.evolveum.prism.xml.ns._public.types_3.PolyStringType;
import com.evolveum.prism.xml.ns._public.types_3.ReferentialIntegrityType;
import com.evolveum.prism.xml.ns._public.types_3.SchemaDefinitionType;

public class PrismUnmarshaller {

    private static final Trace LOGGER = TraceManager.getTrace(PrismUnmarshaller.class);

    private static final QName ARTIFICIAL_OBJECT_NAME = new QName(XMLConstants.NULL_NS_URI, "anObject");

    private static final Set<String> REFERENCE_PROPERTIES = ImmutableSet.of(
            XNodeImpl.KEY_REFERENCE_OID.getLocalPart(),
            XNodeImpl.KEY_REFERENCE_TYPE.getLocalPart(),
            XNodeImpl.KEY_REFERENCE_RELATION.getLocalPart(),
            XNodeImpl.KEY_REFERENCE_DESCRIPTION.getLocalPart(),
            XNodeImpl.KEY_REFERENCE_FILTER.getLocalPart(),
            XNodeImpl.KEY_REFERENCE_RESOLUTION_TIME.getLocalPart(),
            XNodeImpl.KEY_REFERENCE_TARGET_NAME.getLocalPart(),
            XNodeImpl.KEY_REFERENCE_OBJECT.getLocalPart(),
            XNodeImpl.KEY_REFERENCE_REFERENTIAL_INTEGRITY.getLocalPart());

    @NotNull private final PrismContext prismContext;
    @NotNull private final BeanUnmarshaller beanUnmarshaller;
    @NotNull private final SchemaRegistryImpl schemaRegistry;
    @NotNull private final DefinitionFactoryImpl definitionFactory;

    public PrismUnmarshaller(
            @NotNull PrismContext prismContext, @NotNull BeanUnmarshaller beanUnmarshaller,
            @NotNull SchemaRegistryImpl schemaRegistry, @NotNull DefinitionFactoryImpl definitionFactory) {
        this.prismContext = prismContext;
        this.beanUnmarshaller = beanUnmarshaller;
        this.schemaRegistry = schemaRegistry;
        this.definitionFactory = definitionFactory;
    }

    //region Public interface ========================================================

    /*
     *  Please note: methods in this section should NOT be called from inside of parsing process!
     *  It is to avoid repeatedly calling ItemInfo.determine, if at all possible.
     *  (An exception is only if we know we have the definition ... TODO ...)
     *
     *  TODO migrate to parseItem eventually (now we treat objects in parseItemInternal!)
     */
    @SuppressWarnings("unchecked")
    <O extends Objectable> PrismObject<O> parseObject(@NotNull RootXNodeImpl root, ItemDefinition<?> itemDefinition, QName itemName,
            QName typeName, Class<?> typeClass, @NotNull ParsingContext pc) throws SchemaException {

        String msg = "Cannot parse object from element %s, the element does not define an object, it is defined as %s";
        pc.validationLogger(itemDefinition == null || itemDefinition instanceof PrismObjectDefinition,
                ValidationLogType.ERROR, root.getSourceLocation(), msg.formatted(itemName, itemDefinition),
                msg, itemName, itemDefinition);
        checkSchema(itemDefinition == null || itemDefinition instanceof PrismObjectDefinition,
                msg, itemName, itemDefinition);

        ItemInfo<?> itemInfo = ItemInfo.determine(itemDefinition,
                root.getRootElementName(), itemName, ARTIFICIAL_OBJECT_NAME,
                root.getTypeQName(), typeName,
                typeClass, PrismObjectDefinition.class, pc, schemaRegistry);

        XNodeImpl child = root.getSubnode();
        msg = "Cannot parse object from element %s, we need Map";
        pc.validationLogger(child instanceof MapXNodeImpl,
                ValidationLogType.ERROR, child.getSourceLocation(), msg.formatted(child), msg, "");
        checkArgument(child instanceof MapXNodeImpl,
                msg, child.getClass());

        msg = "Cannot parse object from element %s, there is no definition for that element";
        String technicalMsg =  "";
        if (pc.isValidation()) {

            List<String> definitions = findParentsByChildrenElement(itemInfo.getItemName()).stream()
                    .filter(person -> person != null && person.getDefaultItemTypeName() != null)
                    .map(item -> item.getDefaultItemTypeName().getLocalPart())
                    .toList();

            technicalMsg = msg + " can you clarify the definition based on the expected definitions from list: " + definitions;
        }

        pc.validationLogger(itemInfo.getItemDefinition() != null,
                ValidationLogType.ERROR, child.getSourceLocation(), technicalMsg, msg, itemInfo.getItemName());
        ItemDefinition<?> itemDef = checkSchemaNotNull(itemInfo.getItemDefinition(),
                msg, itemInfo.getItemName());

        msg = "Cannot parse object from element %s the element does not define an object, it is defined as %s";
        pc.validationLogger(itemDef instanceof PrismObjectDefinition,
                ValidationLogType.ERROR, child.getSourceLocation(), "", msg, itemInfo.getItemName(), itemInfo.getItemDefinition());
        checkSchema(itemDef instanceof PrismObjectDefinition,
                msg, itemInfo.getItemName(), itemInfo.getItemDefinition());
        return (PrismObject<O>) parseItemInternal(child, itemInfo.getItemName(), itemDef, pc);
    }

    // TODO migrate to parseItem eventually
    @SuppressWarnings("unchecked")
    private <O extends Objectable> PrismObject<O> parseObject(MapXNodeImpl map, PrismObjectDefinition<O> objectDefinition,
            ParsingContext pc) throws SchemaException {
        ItemInfo<?> itemInfo = ItemInfo.determine(objectDefinition,
                null, null, ARTIFICIAL_OBJECT_NAME,
                map.getTypeQName(), null,
                null, PrismObjectDefinition.class, pc, schemaRegistry);
        return (PrismObject<O>) parseItemInternal(map, itemInfo.getItemName(), itemInfo.getItemDefinition(), pc);
    }

    Item<?, ?> parseItem(@NotNull RootXNodeImpl root,
            ItemDefinition<?> itemDefinition, QName itemName, QName typeName, Class<?> typeClass,
            @NotNull ParsingContext pc) throws SchemaException {

        ItemInfo<?> itemInfo = ItemInfo.determine(itemDefinition,
                root.getRootElementName(), itemName, ARTIFICIAL_OBJECT_NAME,
                root.getTypeQName(), typeName,
                typeClass, ItemDefinition.class, pc, schemaRegistry);
        ItemDefinition<?> realDefinition;
        if (itemInfo.getItemDefinition() == null && itemInfo.getComplexTypeDefinition() != null) {
            // Why we do not try to create dynamic definition from other (non-complex) type definitions?
            // (Most probably because we simply don't need it. Null is acceptable in that cases.)
            realDefinition = createDynamicDefinitionFromCtd(itemInfo.getItemName(), itemInfo.getComplexTypeDefinition());
        } else {
            realDefinition = itemInfo.getItemDefinition();
        }
        return parseItemInternal(root.getSubnode(), itemInfo.getItemName(), realDefinition, pc);
    }

    @NotNull
    private ItemDefinition<?> createDynamicDefinitionFromCtd(QName itemName, @NotNull ComplexTypeDefinition typeDefinition) {
        QName typeName = typeDefinition.getTypeName();
        ItemDefinition<?> def;
        if (typeDefinition.isContainerMarker()) {
            // TODO what about objects?
            def = definitionFactory.newContainerDefinition(itemName, typeDefinition);
        } else {
            def = definitionFactory.newPropertyDefinition(itemName, typeName);
        }
        def.mutator().setDynamic(true);
        return def;
    }

    Object parseItemOrRealValue(@NotNull RootXNodeImpl root, ParsingContext pc) throws SchemaException {
        // is the type name explicitly specified? (if not, guess that we have a string)
        QName typeName = root.getTypeQName();
        if (typeName != null) {
            ItemDefinition<?> itemDefinition = schemaRegistry.findItemDefinitionByType(typeName);
            if (itemDefinition != null) {
                return parseItem(root, itemDefinition, null, null, null, pc);
            } else {
                return beanUnmarshaller.unmarshal(root, ((SchemaRegistry) schemaRegistry).determineCompileTimeClass(typeName), pc);
            }
        } else {
            // if type name is not known, we have to derive it from the element name
            QName itemName = root.getRootElementName();
            String msg = "Couldn't parse general object with no type name and unknown element name: %s";
            pc.validationLogger(schemaRegistry.findItemDefinitionByElementName(itemName) == null,
                    ValidationLogType.ERROR, root.getSourceLocation(), "", msg, itemName);
            ItemDefinition<?> itemDefinition = checkSchemaNotNull(schemaRegistry.findItemDefinitionByElementName(itemName),
                    msg, itemName);
            return parseItem(root, itemDefinition, itemName, null, null, pc);
        }
    }
    //endregion

    //region Private methods ========================================================

    // The situation of itemDefinition == null && node.typeName != null is allowed ONLY if the definition cannot be derived
    // from the typeName. E.g. if typeName is like xsd:string, xsd:boolean, etc. This rule is because we don't want to repeatedly
    // try to look for missing definitions here.
    //
    // So the caller is responsible for extracting information from node.typeQName - providing a definition if possible.
    @NotNull
    private Item<?, ?> parseItemInternal(@NotNull XNodeImpl node,
            @NotNull QName itemName, ItemDefinition<?> itemDefinition, @NotNull ParsingContext pc) throws SchemaException {
        pc.validationLogger(!(node instanceof RootXNode),
                ValidationLogType.ERROR, node.getSourceLocation(), "", "Node does not RootXNode");
        Validate.isTrue(!(node instanceof RootXNode));

        // TODO execute this only if in checked mode
        if (itemDefinition == null && node.getTypeQName() != null) {
            PrismContainerDefinition<?> pcd = schemaRegistry.findContainerDefinitionByType(node.getTypeQName());
            if (pcd != null) {
                String msg = "Node has an explicit type corresponding to container (%s) but parseItemInternal was called without definition: %s";
                pc.validationLogger(false,
                        ValidationLogType.ERROR, node.getSourceLocation(), "", msg, pcd, node.debugDump());
                throw new IllegalStateException(String.format(msg, pcd, node.debugDump()));
            }
        }

        Item result;
        if (itemDefinition == null || itemDefinition instanceof PrismPropertyDefinition) {
            result = parseProperty(node, itemName, (PrismPropertyDefinition<?>) itemDefinition, pc);
        } else if (itemDefinition instanceof PrismContainerDefinition) {    // also objects go here
            result = parseContainer(node, itemName, (PrismContainerDefinition<?>) itemDefinition, pc);
        } else if (itemDefinition instanceof PrismReferenceDefinition) {
            result = parseReference(node, itemName, (PrismReferenceDefinition) itemDefinition, pc);
        } else {
            String msg = "Attempt to parse unknown definition type %s";
            pc.validationLogger(false,
                    ValidationLogType.ERROR, node.getSourceLocation(), msg.formatted(itemName), msg,"");
            throw new IllegalArgumentException(String.format(msg, itemDefinition.getClass().getName()));
        }

        if (pc.isPreserveNamespaceContext()) {
            result.setUserData(Item.KEY_NAMESPACE_CONTEXT, node.namespaceContext());
        }

        return result;
    }

    @NotNull
    private <C extends Containerable> PrismContainer<C> parseContainer(@NotNull XNodeImpl node, @NotNull QName itemName,
            @NotNull PrismContainerDefinition<C> containerDef, @NotNull ParsingContext pc) throws SchemaException {

        PrismContainer<C> container = containerDef.instantiate(itemName);
        if (node instanceof ListXNodeImpl list) {
            if (containerDef instanceof PrismObject && list.size() > 1) {
                String msg = "Multiple values for a PrismObject: %s";
                pc.validationLogger(false, ValidationLogType.WARNING, node.getSourceLocation(), msg.formatted(node.debugDump()),
                        msg, "");
                pc.warnOrThrow(LOGGER, String.format(msg, node.debugDump()));
                parseContainerValueToContainer(container, list.get(0), pc);
            } else {
                for (XNodeImpl subNode : list) {
                    parseContainerValueToContainer(container, subNode, pc);
                }
            }
        } else {
            parseContainerValueToContainer(container, node, pc);
        }
        return container;
    }

    private <C extends Containerable> void parseContainerValueToContainer(PrismContainer<C> container, XNodeImpl node,
            @NotNull ParsingContext pc) throws SchemaException {
        if (node instanceof IncompleteMarkerXNodeImpl) {
            container.setIncomplete(true);
        } else {
            PrismContainerValue<C> newValue = parseContainerValue(node, container.getDefinition(), pc);
            if (pc.isFastAddOperations()) {
                container.addIgnoringEquivalents(newValue);
            } else {
                container.add(newValue);
            }
            if (node instanceof MapXNodeImpl map && container instanceof PrismObject<?> object) {
                object.setOid(getOid(map));
                object.setVersion(getVersion(map));
            }
        }
    }

    public static String getOid(MapXNodeImpl xmap) throws SchemaException {
        return xmap.getParsedPrimitiveValue(XNodeImpl.KEY_OID, DOMUtil.XSD_STRING);
    }

    public static String getVersion(MapXNodeImpl xmap) throws SchemaException {
        return xmap.getParsedPrimitiveValue(XNodeImpl.KEY_VERSION, DOMUtil.XSD_STRING);
    }

    public static Long getContainerId(MapXNodeImpl xmap, PrismContainerDefinition<?> containerDef) throws SchemaException {
        PrimitiveXNodeImpl<Object> maybeId = xmap.getPrimitive(XNodeImpl.KEY_CONTAINER_ID);
        if (isContainerId(XNodeImpl.KEY_CONTAINER_ID, maybeId, containerDef)) {
            return maybeId.getParsedValue(DOMUtil.XSD_LONG, Long.class);
        }
        return null;
    }

    private static boolean isContainerId(QName itemName, XNodeImpl node, PrismContainerDefinition<?> parentDef) {
        if (node instanceof PrimitiveXNodeImpl<?> && QNameUtil.match(itemName, XNodeImpl.KEY_CONTAINER_ID)) {
            if (((PrimitiveXNodeImpl<?>) node).isAttribute()) {
                return true;
            }
            if (parentDef.isRuntimeSchema() && itemName.getNamespaceURI() != null) {
                return false;
            }
            //noinspection RedundantIfStatement
            if (idDef(parentDef) == null) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns an item with name "id".
     */
    private static ItemDefinition<?> idDef(PrismContainerDefinition<?> containerDef) {
        if (containerDef == null) {
            return null;
        }
        return containerDef.findLocalItemDefinition(XNodeImpl.KEY_CONTAINER_ID);
    }

    private <C extends Containerable> PrismContainerValue<C> parseContainerValue(
            @NotNull XNodeImpl node, @NotNull PrismContainerDefinition<C> containerDef, @NotNull ParsingContext pc)
            throws SchemaException {
        PrismContainerValue<C> rv;
        if (node instanceof MapXNodeImpl mapXNode) {
            rv = parseContainerValueFromMap(mapXNode, containerDef, pc);
        } else if (node instanceof PrimitiveXNodeImpl<?> prim) {
            rv = createContainerValueWithDefinition(node, containerDef, pc).value;
            if (!prim.isEmpty()) {
                String msg = "Cannot parse container value from (non-empty) %s";
                pc.validationLogger(false, ValidationLogType.WARNING, node.getSourceLocation(), msg.formatted(node),
                        msg, "");
                pc.warnOrThrow(LOGGER, String.format(msg, node));
            }
        } else {
            String msg = "Cannot parse container value from %s";
            pc.validationLogger(false, ValidationLogType.WARNING, node.getSourceLocation(), msg.formatted(node),
                    msg, "");
            pc.warnOrThrow(LOGGER, String.format(msg, node));
            rv = containerDef.createValue();
        }
        addMetadataIfPresent(rv, node, pc);
        return rv;
    }

    @NotNull
    private <C extends Containerable> PrismContainerValue<C> parseContainerValueFromMap(
            @NotNull MapXNodeImpl map, @NotNull PrismContainerDefinition<C> containerDef, @NotNull ParsingContext pc)
            throws SchemaException {

        var ctd = refineContainerCtdFromXsiType(containerDef.getComplexTypeDefinition(), map, pc);

        if (pc.isUseLazyDeserializationFor(ctd.getTypeName())) {
            return new LazyPrismContainerValue<>(ctd, pc, map);
        }
        return parseRealContainerValueFromMap(map, containerDef, pc);
    }

    public <C extends Containerable> PrismContainerValue<C> parseRealContainerValueFromMap(
            @NotNull MapXNodeImpl map, @NotNull PrismContainerDefinition<C> containerDef, @NotNull ParsingContext pc) throws SchemaException {
        ValueWithDefinition<C> vd = createContainerValueWithDefinition(map, containerDef, pc);
        parseContainerChildren(vd.value, map, containerDef, vd.complexTypeDefinition, pc);
        return vd.value;
    }

    /** Temporary structure, to pass both value and its CTD (which may be different from what we originally thought). */
    private record ValueWithDefinition<C extends Containerable>(
            PrismContainerValue<C> value,
            ComplexTypeDefinition complexTypeDefinition) {
    }

    private <C extends Containerable> ValueWithDefinition<C> createContainerValueWithDefinition(
            @NotNull XNodeImpl xnode, @NotNull PrismContainerDefinition<C> containerDef, @NotNull ParsingContext pc)
            throws SchemaException {
        ComplexTypeDefinition defaultCtd = containerDef.getComplexTypeDefinition();

        if (containerDef instanceof PrismObjectDefinition<?> prismObjectDefinition) {
            //noinspection unchecked
            return new ValueWithDefinition<>(
                    ((PrismContainerValue<C>) prismObjectDefinition.createValue()),
                    defaultCtd);
        } else {
            Long id = xnode instanceof MapXNodeImpl ? getContainerId(((MapXNodeImpl) xnode), containerDef) : null;
            // override (refine) container definition, if explicit type is specified and is more specific
            var refinedCtd = refineContainerCtdFromXsiType(defaultCtd, xnode, pc);
            return new ValueWithDefinition<>(
                    new PrismContainerValueImpl<>(null, null, null, id, refinedCtd),
                    refinedCtd);
        }
    }

    private ComplexTypeDefinition refineContainerCtdFromXsiType(
            ComplexTypeDefinition containerTypeDef, @NotNull XNodeImpl xnode, @NotNull ParsingContext pc) throws SchemaException {
        QName xsiTypeName = xnode.getTypeQName();
        if (xsiTypeName == null) {
            return containerTypeDef;
        }

        if (xsiTypeName.equals(containerTypeDef.getTypeName())) {
            // Performance: XSI Type is already provided container type, we do not need to search for it in schema registry.
            return containerTypeDef;
        }

        ComplexTypeDefinition xsiTypeDef = schemaRegistry.findComplexTypeDefinitionByType(xsiTypeName);
        if (xsiTypeDef == null) {
            String msg = "Unknown type %s in %s";
            pc.validationLogger(false, ValidationLogType.WARNING, xnode.getSourceLocation(), msg.formatted(xsiTypeName, xnode),
                    msg, "");
            pc.warnOrThrow(LOGGER, String.format(msg, xsiTypeName, xnode));
            return containerTypeDef;
        }

        if (containerTypeDef != null && xsiTypeDef.isAssignableFrom(containerTypeDef, schemaRegistry)) {
            // Existing definition (CTD for PCD) is equal or more specific than the explicitly provided one.
            // Let's then keep using the existing definition. It is not quite clean solution
            // but there seem to exist serialized objects with generic xsi:type="c:ExtensionType" (MID-6474)
            // or xsi:type="c:ShadowAttributesType" (MID-6394). Such abstract definitions could lead to
            // parsing failures because of undefined items.
            String msg = "Ignoring explicit type definition {} because equal or even more specific one is present: {}";
            pc.validationLogger(false, ValidationLogType.WARNING, xnode.getSourceLocation(), "", msg);
            LOGGER.trace(msg,
                    xsiTypeDef, containerTypeDef);
            return containerTypeDef;
        }

        // We learned something from xsi:type, so let's use that information.
        return xsiTypeDef;
    }

    private void parseContainerChildren(PrismContainerValue<?> cval, MapXNodeImpl map, PrismContainerDefinition<?> containerDef, ComplexTypeDefinition complexTypeDefinition, ParsingContext pc) throws SchemaException {
        for (Entry<QName, XNodeImpl> entry : map.entrySet()) {
            final QName itemName = entry.getKey();
            String msg = "Null item name while parsing %s";
            pc.validationLogger(false, ValidationLogType.WARNING, map.getSourceLocation(), msg.formatted(map.debugDumpLazily()),
                    msg, "");
            checkArgument(itemName != null, String.format(msg, map.debugDumpLazily()));

            if (isContainerId(itemName, entry.getValue(), containerDef)) {
                continue;
            }
            if (containerDef instanceof PrismObjectDefinition &&
                    (QNameUtil.match(itemName, XNodeImpl.KEY_OID) || QNameUtil.match(itemName, XNodeImpl.KEY_VERSION))) {
                continue;
            }

            ItemDefinition<?> itemDef = locateItemDefinition(itemName, complexTypeDefinition, entry.getValue());

            if (itemDef == null) {
                boolean shouldContinue = handleMissingDefinition(itemName, containerDef, complexTypeDefinition, pc, map, map.getSourceLocation());
                if (shouldContinue) {
                    continue;
                }

            }
            final Item<?, ?> item;
            if (entry.getValue() == null) {
                if (itemDef != null) {
                    item = itemDef.instantiate();       // TODO or skip the creation altogether?
                } else {
                    item = null;
                }
            } else {
                item = parseItemInternal(entry.getValue(), itemName, itemDef, pc);
            }
            // Merge must be here, not just add. Some items (e.g. references) have alternative
            // names and representations and these cannot be processed as one map or list
            if (item != null) {
                cval.merge(item);
            }
        }
    }

    private boolean handleMissingDefinition(QName itemName, ItemDefinition<?> containerDef, TypeDefinition typeDefinition, ParsingContext pc, DebugDumpable object, SourceLocation sourceLocation) throws SchemaException {
        SchemaMigration migration = determineSchemaMigration(typeDefinition, itemName);
        if (migration != null && pc.isCompat()) {
            if (migration.getOperation() == SchemaMigrationOperation.REMOVED) {
                String msg = "Item %s was removed from the schema, skipped processing of that item";
                pc.validationLogger(false, ValidationLogType.WARNING, sourceLocation, msg.formatted(itemName),
                        msg, "");
                pc.warn(LOGGER, String.format(msg, itemName));
                return true;
            } else {
                String msg = "Unsupported migration operation %s for item %s (in %s) while parsing ";
                pc.validationLogger(false, ValidationLogType.WARNING,
                        sourceLocation, msg.formatted(migration.getOperation(), itemName, containerDef, object.debugDump()),
                        msg, "");
                pc.warnOrThrow(LOGGER, String.format(msg, migration.getOperation(), itemName, containerDef, object.debugDump()));
            }
        }
        boolean anyXsd = typeDefinition instanceof ComplexTypeDefinition && ((ComplexTypeDefinition) typeDefinition).isXsdAnyMarker();
        if (typeDefinition == null || typeDefinition.isRuntimeSchema() || anyXsd) {
            PrismSchema itemSchema = schemaRegistry.findSchemaByNamespace(itemName.getNamespaceURI());
            if (itemSchema != null) {
                // If we already have schema for this namespace then a missing element is
                // an error. We positively know that it is not in the schema.
                String msg = "Item '%s' has no definition (schema present, in %s), while parsing %s";
                pc.validationLogger(false, ValidationLogType.WARNING,
                        sourceLocation, msg.formatted(itemName, containerDef, object.debugDump()),
                        msg, "");
                pc.warnOrThrow(LOGGER, msg.formatted(
                        itemName, containerDef, object.debugDump()));
                // we can go along this item (at least show it in repository pages) - MID-3249
                // TODO make this configurable
            } else {
                // No definition for item, but the schema is runtime. the definition may come later.
                // Null is OK here. The item will be parsed as "raw"
            }
        } else {    // complex type definition is static
            String msg = "Item %s has no definition (in value %s)" + "while parsing %s";
            pc.validationLogger(false, ValidationLogType.WARNING,
                    sourceLocation, msg.formatted(itemName, typeDefinition, object.debugDump()),
                    msg, "");
            pc.warnOrThrow(LOGGER, msg.formatted(itemName, typeDefinition, object.debugDump()));
            return true;   // don't even attempt to parse it
        }
        return false;
    }

    private SchemaMigration determineSchemaMigration(TypeDefinition typeDefinition, QName itemName) {
        if (typeDefinition == null) {
            return null;
        }
        List<SchemaMigration> schemaMigrations = typeDefinition.getSchemaMigrations();
        if (schemaMigrations != null) {
            for (SchemaMigration schemaMigration : schemaMigrations) {
                if (QNameUtil.match(schemaMigration.getElementQName(), itemName)) {
                    return schemaMigration;
                }
            }
        }
        QName superTypeQName = typeDefinition.getSuperType();
        if (superTypeQName == null) {
            return null;
        }
        TypeDefinition superTypeDef = prismContext.getSchemaRegistry().findTypeDefinitionByType(superTypeQName);
        if (superTypeDef == null) {
            return null;
        }
        return determineSchemaMigration(superTypeDef, itemName);
    }

    @NotNull
    private <T> PrismProperty<T> parseProperty(@NotNull XNodeImpl node, @NotNull QName itemName,
            @Nullable PrismPropertyDefinition<T> itemDefinition, @NotNull ParsingContext pc) throws SchemaException {
        Validate.isTrue(!(node instanceof RootXNodeImpl));

        PrismProperty<T> property = itemDefinition != null ?
                itemDefinition.instantiate() :
                new PrismPropertyImpl<>(itemName);

        if (node instanceof ListXNodeImpl listNode && !node.isHeterogeneousList()) {
            String msg = "Attempt to store multiple values in single-valued property %s";
            pc.validationLogger(itemDefinition == null || itemDefinition.isMultiValue() || listNode.size() <= 1, ValidationLogType.ERROR,
                    node.getSourceLocation(), msg.formatted(itemName),
                    msg, "");
            checkSchema(itemDefinition == null || itemDefinition.isMultiValue() || listNode.size() <= 1,
                    msg, itemName);
            for (XNodeImpl subNode : listNode) {
                if (subNode instanceof IncompleteMarkerXNodeImpl) {
                    property.setIncomplete(true);
                } else {
                    PrismPropertyValue<T> pval = parsePropertyValue(subNode, itemDefinition, pc);
                    addItemValueIfPossible(property, pval, pc, subNode.getSourceLocation());
                }
            }
        } else if (node instanceof MapXNodeImpl || node instanceof PrimitiveXNodeImpl || node.isHeterogeneousList()) {
            PrismPropertyValue<T> pval = parsePropertyValue(node, itemDefinition, pc);
            if (pval != null) {
                try {
                    if (pc.isFastAddOperations()) {
                        property.addIgnoringEquivalents(pval);
                    } else {
                        property.add(pval);
                    }
                } catch (SchemaException e) {
                    if (pc.isCompat()) {
                        // Most probably the "apply definition" call while adding the value failed. This occurs for raw
                        // values with (somewhat) incorrect definitions being added. Overall, this is more a hack than serious
                        // solution, because we sometimes want to add static-schema-less property values. TODO investigate this.
                        ((PrismPropertyImpl<T>) property).addForced(pval);
                    } else {
                        throw e;
                    }
                }
            }
        } else if (node instanceof SchemaXNodeImpl) {
            SchemaDefinitionType schemaDefType = beanUnmarshaller.unmarshalSchemaDefinitionType((SchemaXNodeImpl) node);
            @SuppressWarnings("unchecked")
            PrismPropertyValue<T> val = (PrismPropertyValue<T>) new PrismPropertyValueImpl<>(schemaDefType);
            addItemValueIfPossible(property, val, pc, node.getSourceLocation());
        } else if (node instanceof IncompleteMarkerXNodeImpl) {
            property.setIncomplete(true);
        } else {
            String msg = "Cannot parse property from %s";
            pc.validationLogger(false, ValidationLogType.ERROR,
                    node.getSourceLocation(), msg.formatted(node),
                    msg, "");
            throw new IllegalArgumentException(msg.formatted(node));
        }
        return property;
    }

    private <V extends PrismValue, D extends ItemDefinition<?>> void addItemValueIfPossible(
            Item<V, D> item, V value, ParsingContext pc, SourceLocation sourceLocation) throws SchemaException {
        if (value != null) {
            try {
                if (pc.isFastAddOperations()) {
                    item.addIgnoringEquivalents(value);
                } else {
                    item.add(value);
                }
            } catch (SchemaException e) {
                String msg = "Couldn't add a value of %s to the containing item: %s";
                pc.validationLogger(false, ValidationLogType.WARNING,
                        sourceLocation, msg.formatted(value, e.getMessage()),
                        msg, "");
                pc.warnOrThrow(LOGGER, msg.formatted(value, e.getMessage()), e);
            }
        }
    }

    // if definition == null or any AND node has type defined, this type must be non-containerable (fit into PPV)
    private <T> PrismPropertyValue<T> parsePropertyValue(@NotNull XNodeImpl node,
            @Nullable PrismPropertyDefinition<T> definition, @NotNull ParsingContext pc) throws SchemaException {
        QName typeFromDefinition = definition != null && !definition.isAnyType() ? definition.getTypeName() : null;
        QName typeName = ((SchemaRegistry) schemaRegistry).selectMoreSpecific(typeFromDefinition, node.getTypeQName());

        PrismPropertyValue<T> rv;
        if (typeName == null) {
            return createRawPrismPropertyValue(node);
        } else if (beanUnmarshaller.canProcess(typeName)) {
            Object unmarshalled = beanUnmarshaller.unmarshal(node, typeName, pc);
            T realValue = treatPolyStringAndRecompute(unmarshalled);
            if (!isValueAllowed(realValue, definition)) {
                String msg = "Unknown (not allowed) value of type %s. Value: %s. Allowed values: %s";
                pc.validationLogger(false, ValidationLogType.WARNING,
                        node.getSourceLocation(), msg.formatted(typeName, realValue, definition.getAllowedValues()),
                        msg, "");
                pc.warnOrThrow(LOGGER, msg.formatted(typeName, realValue, definition.getAllowedValues()));
                rv = null;
            } else if (realValue == null) {
                rv = deriveValueFromExpression(node);
            } else {
                rv = new PrismPropertyValueImpl<>(realValue);
            }
        } else if (pc.isConvertUnknownTypes()) {
            node = node.clone();
            // We need to remove xsi:type
            node.setTypeQName(null);
            rv = createRawPrismPropertyValue(node);
        } else {
            String msg = "Cannot parse as %s because bean unmarshaller cannot process it (generated bean classes are missing?): %s";
            pc.validationLogger(false, ValidationLogType.WARNING,
                    node.getSourceLocation(), msg.formatted(typeName, node.debugDump()),
                    msg, "");
            pc.warnOrThrow(LOGGER, msg.formatted(typeName, node.debugDump()));
            rv = createRawPrismPropertyValue(node);
        }
        addMetadataIfPresent(rv, node, pc);
        return rv;
    }

    private void addMetadataIfPresent(PrismValue prismValue, XNode node, @NotNull ParsingContext pc) throws SchemaException {
        if (prismValue != null && node instanceof MetadataAware) {
            parseMetadataNodes(prismValue, ((MetadataAware) node).getMetadataNodes(), pc);
        }
    }

    private void parseMetadataNodes(PrismValue prismValue, List<MapXNode> metadataNodes, ParsingContext pc) throws SchemaException {
        if (metadataNodes.isEmpty()) {
            return;
        }
        ValueMetadata valueMetadata = prismValue.getValueMetadata();
        for (MapXNode metadataNode : metadataNodes) {
            // There are issues with contracts ("?" vs "Containerable")
            //noinspection rawtypes
            PrismContainerValue pcv =
                    parseContainerValueFromMap((MapXNodeImpl) metadataNode, schemaRegistry.getValueMetadataDefinition(), pc);
            if (pc.isFastAddOperations()) {
                //noinspection unchecked
                valueMetadata.addIgnoringEquivalents(pcv);
            } else {
                //noinspection unchecked
                valueMetadata.add(pcv);
            }
        }
    }

    @Nullable
    private <T> PrismPropertyValue<T> deriveValueFromExpression(@NotNull XNodeImpl node) throws SchemaException {
        // Be careful here. Expression element can be legal sub-element of complex properties.
        // Therefore parse expression only if there is no legal value.
        ExpressionWrapper expression = PrismUtilInternal.parseExpression(node);
        if (expression != null) {
            return new PrismPropertyValueImpl<>(null, null, null, expression);
        } else {
            // There's no point in returning PPV(null) as it would soon fail on internal PP check.
            // We are probably recovering from an error in COMPAT mode here, so let's just skip this value.
            return null;
        }
    }

    // Postprocessing after returning from unmarshaller. It speaks bean language (e.g. PolyStringType, not PolyString).
    private <T> T treatPolyStringAndRecompute(Object bean) {
        Object rv;
        if (bean instanceof PolyStringType) {
            rv = ((PolyStringType) bean).toPolyString();
        } else {
            rv = bean;
        }
        PrismUtil.recomputeRealValue(rv);
        //noinspection unchecked
        return (T) rv;
    }

    @NotNull
    private <T> PrismPropertyValue<T> createRawPrismPropertyValue(@NotNull XNodeImpl node) {
        return prismContext.itemFactory().createPropertyValue(node);
    }

    @Contract("_, null -> true")
    private <T> boolean isValueAllowed(T realValue, PrismPropertyDefinition<T> definition) {
        if (realValue instanceof Enum) {
            // Statically-defined enums have been already treated. Unless someone overrides the static schema,
            // reducing the set of allowed values. But let's declared this feature as "not supported yet")
            return true;
        } else if (definition == null || CollectionUtils.isEmpty(definition.getAllowedValues())) {
            return true;
        } else if (realValue == null) {
            return true; // TODO: ok?
        } else {
            return definition.getAllowedValues().stream()
                    .anyMatch(displayableValue -> realValue.equals(displayableValue.getValue()));
        }
    }

    @NotNull
    private PrismReference parseReference(@NotNull XNodeImpl node, @NotNull QName itemName,
            @NotNull PrismReferenceDefinition definition, @NotNull ParsingContext pc) throws SchemaException {
        PrismReference ref = definition.instantiate();
        if (node instanceof ListXNodeImpl) {
            for (XNodeImpl subNode : (ListXNodeImpl) node) {
                if (subNode instanceof IncompleteMarkerXNodeImpl) {
                    ref.setIncomplete(true);
                } else {
                    PrismReferenceValue subValue = parseReferenceValueFromXNode(subNode, definition, itemName, pc);
                    if (pc.isFastAddOperations()) {
                        ref.addIgnoringEquivalents(subValue);
                    } else {
                        ref.add(subValue);
                    }
                }
            }
        } else if (node instanceof MapXNodeImpl) {
            PrismReferenceValue newValue = parseReferenceValueFromXNode(node, definition, itemName, pc);
            if (pc.isFastAddOperations()) {
                ref.addIgnoringEquivalents(newValue);
            } else {
                ref.add(newValue);
            }
        } else if (node instanceof PrimitiveXNodeImpl) {
            // empty
        } else if (node instanceof IncompleteMarkerXNodeImpl) {
            ref.setIncomplete(true);
        } else {
            String msg = "Cannot parse reference from %s";
            pc.validationLogger(false, ValidationLogType.ERROR,
                    node.getSourceLocation(), msg.formatted(node),
                    msg, "");
            throw new IllegalArgumentException("Cannot parse reference from " + node);
        }
        return ref;
    }

    @NotNull
    private PrismReferenceValue parseReferenceValueFromXNode(@NotNull XNodeImpl node,
            @NotNull PrismReferenceDefinition definition, @NotNull QName itemName, @NotNull ParsingContext pc) throws SchemaException {
        /*
         *  We distinguish between "real" references and composite objects by
         *  (1) looking at type QName of XNode passed (whether it's ObjectType or ObjectReferenceType)
         *  (2) comparing itemName and name from reference definition - e.g. linkRef vs. link
         */
        boolean isComposite;
        if (node.getTypeQName() != null) {
            QName typeName = node.getTypeQName();
            ItemDefinition<?> contentDefinition = schemaRegistry.findItemDefinitionByType(typeName);
            isComposite = contentDefinition instanceof PrismObjectDefinition;
        } else {
            // FIXME: MID-6818 This should be probably distinguished by type, instead of
            // name difference
            isComposite = !QNameUtil.match(itemName, definition.getItemName());
        }

        PrismReferenceValue rv;
        if (isComposite) {
            rv = parseReferenceValueAsCompositeObject(node, definition, pc);  // This is a composite object (complete object stored inside reference)
        } else {
            // TODO fix this hack: for delta values of ObjectReferenceType we will not
            //  insist on having reference type (because the target definition could be such that it won't require it)
            boolean allowMissingRefTypesOverride = node.isExplicitTypeDeclaration();
            rv = parseReferenceValueAsReference(itemName, node, definition, pc, allowMissingRefTypesOverride);   // This is "real" reference (oid,  and nothing more)
        }

        addMetadataIfPresent(rv, node, pc);
        return rv;
    }

    private PrismReferenceValue parseReferenceValueAsReference(QName name, @NotNull XNodeImpl xnode, @NotNull PrismReferenceDefinition definition,
            @NotNull ParsingContext pc, boolean allowMissingRefTypesOverride) throws SchemaException {
        checkArgument(xnode instanceof MapXNodeImpl, "Cannot parse reference from %s", xnode);
        MapXNodeImpl map = (MapXNodeImpl) xnode;
        TypeDefinition typeDefinition = schemaRegistry.findTypeDefinitionByType(definition.getTypeName());
        String oid = map.getParsedPrimitiveValue(XNodeImpl.KEY_REFERENCE_OID.getLocalPart(), DOMUtil.XSD_STRING);
        PrismReferenceValue refVal = new PrismReferenceValueImpl(oid);

        QName targetType = map.getParsedPrimitiveValue(XNodeImpl.KEY_REFERENCE_TYPE.getLocalPart(), DOMUtil.XSD_QNAME);
        if (targetType == null) {
            if (!pc.isAllowMissingRefTypes() && !allowMissingRefTypesOverride) {
                String msg = "Target type in reference %s not specified in reference nor in the schema";
                pc.validationLogger(false, ValidationLogType.ERROR,
                        map.getSourceLocation(), msg.formatted(definition.getItemName()),
                        msg, "");
                targetType = checkSchemaNotNull(definition.getTargetTypeName(),
                        msg, definition.getItemName());
            }
        } else {
            if (QNameUtil.noNamespace(targetType)) {
                targetType = ((SchemaRegistry) schemaRegistry).resolveUnqualifiedTypeName(targetType);
            }
            QName defTargetType = definition.getTargetTypeName();
            if (defTargetType != null) {
                String msg = "Target type specified in reference %s (%s) does not match target type in schema (%s)";
                pc.validationLogger(false, ValidationLogType.ERROR,
                        map.getSourceLocation(), msg.formatted(definition.getItemName(), targetType, defTargetType),
                        msg, "");
                checkSchema(prismContext.getSchemaRegistry().isAssignableFrom(defTargetType, targetType),
                        msg, definition.getItemName(), targetType, defTargetType);
            }
        }
        PrismObjectDefinition<Objectable> objectDefinition = null;
        if (targetType != null) {
            String msg = "No definition for type %s in reference";
            pc.validationLogger(false, ValidationLogType.ERROR,
                    map.getSourceLocation(), msg.formatted(targetType),
                    msg, targetType.getLocalPart());
            objectDefinition = checkSchemaNotNull(schemaRegistry.findObjectDefinitionByType(targetType),
                    msg, targetType);
            refVal.setTargetType(targetType);
        }

        QName relationAttribute = map.getParsedPrimitiveValue(XNodeImpl.KEY_REFERENCE_RELATION.getLocalPart(), DOMUtil.XSD_QNAME);
        refVal.setRelation(relationAttribute);

        refVal.setDescription(map.getParsedPrimitiveValue(XNodeImpl.KEY_REFERENCE_DESCRIPTION.getLocalPart(), DOMUtil.XSD_STRING));

        refVal.setFilter(parseFilter(map.get(XNodeImpl.KEY_REFERENCE_FILTER.withoutNamespace()), pc));

        String resolutionTimeString = map.getParsedPrimitiveValue(XNodeImpl.KEY_REFERENCE_RESOLUTION_TIME.getLocalPart(), DOMUtil.XSD_STRING);
        if (resolutionTimeString != null) {
            EvaluationTimeType resolutionTime = EvaluationTimeType.fromValue(resolutionTimeString);
            refVal.setResolutionTime(resolutionTime);
        }

        String referentialIntegrityString = map.getParsedPrimitiveValue(XNodeImpl.KEY_REFERENCE_REFERENTIAL_INTEGRITY.withoutNamespace(),
                DOMUtil.XSD_STRING);
        if (referentialIntegrityString != null) {
            refVal.setReferentialIntegrity(ReferentialIntegrityType.fromValue(referentialIntegrityString));
        }

        XNodeImpl xnodeForTargetName = map.get(XNodeImpl.KEY_REFERENCE_TARGET_NAME.withoutNamespace());
        if (xnodeForTargetName != null) {
            PolyStringType targetName = beanUnmarshaller.unmarshal(xnodeForTargetName, PolyStringType.class, pc);
            refVal.setTargetName(targetName);
        }

        XNodeImpl xrefObject = map.get(XNodeImpl.KEY_REFERENCE_OBJECT.withoutNamespace());
        if (xrefObject != null) {
            MapXNodeImpl objectMapNode = toObjectMapNode(xrefObject, pc);
            String msg = "Cannot parse object from %s without knowing its type";
            pc.validationLogger(false, ValidationLogType.ERROR,
                    xrefObject.getSourceLocation(), msg.formatted(xrefObject),
                    msg, "");
            checkSchemaNotNull(targetType, msg, xrefObject);
            PrismObject<Objectable> object = parseObject(objectMapNode, objectDefinition, pc);
            setReferenceObject(refVal, object, pc, xrefObject.getSourceLocation());
        }
        for (Entry<QName, XNodeImpl> entry : map.entrySet()) {
            QName itemName = entry.getKey();
            if (!isDefinedProperty(itemName, name.getNamespaceURI(), REFERENCE_PROPERTIES)) {
                handleMissingDefinition(itemName, definition, typeDefinition, pc, map, map.getSourceLocation());
            }
        }

        return refVal;
    }

    @SuppressWarnings({ "unused", "SameParameterValue" })
    private boolean isDefinedProperty(QName itemName, String parentNamespace, Set<String> properties) {
        // TODO: Namespace awarness is disabled, because some calls from parseItem use ItemDefinition.itemName
        // instead of used itemName
        /*
        String ns = itemName.getNamespaceURI();
        if(Strings.isNullOrEmpty(ns) || Objects.equals(parentNamespace,ns)) {
            return properties.contains(itemName.getLocalPart());
        }
        */
        return properties.contains(itemName.getLocalPart());
    }

    private MapXNodeImpl toObjectMapNode(XNodeImpl xNode, @NotNull ParsingContext pc) throws SchemaException {
        if (xNode instanceof MapXNodeImpl) {
            return (MapXNodeImpl) xNode;
        } else if (xNode instanceof PrimitiveXNode && xNode.isEmpty()) {
            return new MapXNodeImpl();
        } else {
            // TODO ...or warn
            String msg = "Cannot parse object from %s";
            pc.validationLogger(false, ValidationLogType.ERROR,
                    xNode.getSourceLocation(), msg.formatted(xNode),
                    msg, "");
            throw new SchemaException(msg.formatted(xNode));
        }
    }

    private void setReferenceObject(PrismReferenceValue refVal, PrismObject<Objectable> object, @NotNull ParsingContext pc, SourceLocation sourceLocation) throws SchemaException {
        refVal.setObject(object);
        if (object.getOid() != null) {
            if (refVal.getOid() == null) {
                refVal.setOid(object.getOid());
            } else {
                String msg = "OID in reference (%s) does not match OID in composite object (%s)";
                pc.validationLogger(false, ValidationLogType.ERROR,
                        sourceLocation, msg.formatted(refVal.getOid(), object.getOid()),
                        msg, "");
                checkSchema(refVal.getOid().equals(object.getOid()),
                        msg, refVal.getOid(), object.getOid());
            }
        }
        QName objectTypeName = object.getDefinition().getTypeName();
        if (refVal.getTargetType() == null) {
            refVal.setTargetType(objectTypeName);
        } else {
            String msg = "Target type in reference (%s) does not match type in composite object (%s)";
            pc.validationLogger(false, ValidationLogType.ERROR,
                    sourceLocation, msg.formatted(refVal.getTargetType(), objectTypeName),
                    msg,"");
            checkSchema(refVal.getTargetType().equals(objectTypeName),
                    msg, refVal.getTargetType(), objectTypeName);
        }
    }

    private PrismReferenceValue parseReferenceValueAsCompositeObject(XNodeImpl node,
            PrismReferenceDefinition definition, ParsingContext pc) throws SchemaException {
        checkArgument(node instanceof MapXNodeImpl, "Cannot parse reference composite object from %s", node);
        MapXNodeImpl map = (MapXNodeImpl) node;
        QName targetTypeName = definition.getTargetTypeName();
        PrismObjectDefinition<Objectable> objectDefinition = null;
        if (map.getTypeQName() != null) {
            objectDefinition = schemaRegistry.findObjectDefinitionByType(map.getTypeQName());
        }
        if (objectDefinition == null && targetTypeName != null) {
            objectDefinition = schemaRegistry.findObjectDefinitionByType(targetTypeName);
        }

        String msg = "No object definition for composite object in reference element %s";
        pc.validationLogger(false, ValidationLogType.ERROR,
                map.getSourceLocation(), msg.formatted(definition.getItemName()),
                msg, "");
        checkSchemaNotNull(objectDefinition, msg, definition.getItemName());
        PrismObject<Objectable> compositeObject;
        try {
            compositeObject = parseObject(map, objectDefinition, pc);
        } catch (SchemaException e) {
            msg = "%s while parsing composite object in reference element %s";
            pc.validationLogger(false, ValidationLogType.ERROR,
                    map.getSourceLocation(), msg.formatted(e.getMessage(), definition.getItemName()),
                    msg, "");
            throw new SchemaException(msg.formatted(e.getMessage(), definition.getItemName()), e);
        }

        PrismReferenceValue refVal = new PrismReferenceValueImpl();
        setReferenceObject(refVal, compositeObject, pc, map.getSourceLocation());
        return refVal;
    }

    private SearchFilterType parseFilter(XNodeImpl xnode, ParsingContext pc) throws SchemaException {
        if (xnode == null) {
            return null;
        }
        if (xnode.isEmpty()) {
            return null;
        }
        return SearchFilterType.createFromParsedXNode(xnode, pc);
    }

    private ItemDefinition<?> locateItemDefinition(
            @NotNull QName itemName,
            @Nullable ComplexTypeDefinition complexTypeDefinition,
            XNode xnode) {
        QName explicitItemType = xnode != null && xnode.isExplicitTypeDeclaration() ? xnode.getTypeQName() : null;

        if(xnode != null && xnode.getDefinition() != null) {
            // XNode provided definition
            return xnode.getDefinition();
        }


        return schemaRegistry.locateItemDefinition(
                itemName, explicitItemType, complexTypeDefinition, qName -> createDynamicItemDefinition(qName, xnode));
    }

    private ItemDefinition<?> createDynamicItemDefinition(QName itemName, XNode node) {
        if (node == null) {
            return null;
        }
        QName typeName = node.getTypeQName();
        if (typeName == null) {
            if (node instanceof ListXNodeImpl) {
                // there may be type definitions in individual list members
                for (XNodeImpl subNode : ((ListXNodeImpl) node)) {
                    ItemDefinition<?> subdef = createDynamicItemDefinition(itemName, subNode);
                    // TODO: make this smarter, e.g. detect conflicting type definitions
                    if (subdef != null) {
                        return subdef;
                    }
                }
            }
        }
        if (typeName == null) {
            return null;
        }

        ItemDefinition<?> itemDef;
        var typeDefinition = schemaRegistry.findTypeDefinitionByType(typeName);
        if (typeDefinition instanceof ComplexTypeDefinition ctd && (ctd.isContainerMarker() || ctd.isObjectMarker())) {
            itemDef = definitionFactory.newContainerDefinition(itemName, ctd);
        } else if (typeDefinition instanceof ComplexTypeDefinition ctd && ctd.isReferenceMarker()) {
            itemDef = definitionFactory.newReferenceDefinition(itemName, typeName);
        } else {
            // definition is unknown or it's a property definition
            itemDef = definitionFactory.newPropertyDefinition(itemName, typeName);
        }

        // Make this multivalue by default, this is more "open"
        itemDef.mutator().setMaxOccurs(Objects.requireNonNullElse(node.getMaxOccurs(), -1));
        itemDef.mutator().setDynamic(true);
        return itemDef;
    }

    //endregion

    //TODO
    public ItemDefinition<?> locateItemDefinition(
            @NotNull ItemDefinition<?> containerDefinition, @NotNull QName itemName, @Nullable XNode xnode) {
        if (containerDefinition instanceof PrismContainerDefinition) {
            return locateItemDefinition(itemName,
                    ((PrismContainerDefinition<?>) containerDefinition).getComplexTypeDefinition(), xnode);
        }
        throw new UnsupportedOperationException("Now what?"); // TODO finish this case... if needed
    }

    /**
     * Looks for the expected object definitions for children to perform a hint in validation response
     * @param itemName children item
     * @return list of parents object definition
     */
    private List<ComplexTypeDefinition> findParentsByChildrenElement(@NotNull QName itemName) {

        List<ComplexTypeDefinition> parentsDef = new ArrayList<>();

        for (PrismSchema schema : prismContext.getSchemaRegistry().getSchemas()) {
            if (schema == null) {
                continue;
            }

            for (ComplexTypeDefinition complexTypeDefinition : schema.getComplexTypeDefinitions()) {
                for (ItemDefinition<?> item : complexTypeDefinition.getDefinitions()) {
                    if (itemName.getLocalPart().equals(item.getItemName().getLocalPart())) {
                        parentsDef.add(complexTypeDefinition);
                    }
                }
            }
        }

        return parentsDef;
    }
}
