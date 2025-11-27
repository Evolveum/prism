/*
 * Copyright (c) 2010-2025 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.prism.impl.xnode;

import java.util.Objects;
import java.util.Optional;

import javax.xml.namespace.QName;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.path.ItemName;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.evolveum.midpoint.prism.impl.lex.json.JsonInfraItems;
import com.evolveum.midpoint.prism.schema.SchemaRegistry;
import com.evolveum.midpoint.util.DOMUtil;
import com.evolveum.midpoint.util.QNameUtil;
import com.evolveum.midpoint.util.QNameUtil.PrefixedName;
import com.evolveum.midpoint.util.QNameUtil.QNameInfo;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.google.common.base.Strings;

public abstract class XNodeDefinition {

    private static final SchemaIgnorant EMPTY = new SchemaIgnorant(ItemName.interned(null, ""));

    private static final @NotNull QName FILTER_CLAUSE = new QName(PrismConstants.NS_QUERY, "filterClause");

    private final @NotNull ItemName name;

    protected XNodeDefinition(ItemName name) {
        this.name = name;
    }

    public static Root root(@NotNull SchemaRegistry schemaRegistry) {
        return new SchemaRoot(schemaRegistry);
    }

    public static Root rootWithDefinition(@NotNull SchemaRegistry schemaRegistry, ItemDefinition<?> topLevelItem) {
        return new SchemaRootExpectingItem(schemaRegistry, topLevelItem);
    }

    public static Root empty() {
        return EMPTY;
    }

    protected abstract XNodeDefinition unawareFrom(ItemName name);

    public static QName resolveQName(String name, PrismNamespaceContext context) throws SchemaException {
        // taken from empty.resolve(context.withoutDefault) so it can be simplified to this.
        if (!QNameUtil.isUriQName(name)) {
            PrefixedName prefixed = QNameUtil.parsePrefixedName(name);
            if (!prefixed.prefix().isEmpty()) {
                Optional<String> ns = context.namespaceFor(prefixed.prefix());
                if (ns.isPresent()) {
                    return new QName(ns.get(), prefixed.localName(), prefixed.prefix());
                }
            }
        }
        QNameInfo result = QNameUtil.uriToQNameInfo(name, true);
        return result.name;
    }


    public @NotNull ItemName getName() {
        return name;
    }

    public abstract Optional<QName> getType();

    public boolean isXmlAttribute() {
        return false;
    }

    public @NotNull XNodeDefinition resolve(@NotNull String name, @NotNull PrismNamespaceContext namespaceContext) throws SchemaException {
        if (isInfra(name)) {
            return infra(name);
        }
        if (!QNameUtil.isUriQName(name)) {
            PrefixedName prefixed = QNameUtil.parsePrefixedName(name);
            if (prefixed.prefix().isEmpty()) {
                XNodeDefinition resolved = resolveLocally(name, namespaceContext.defaultNamespace().orElse(null));
                if (resolved != null) {
                    return resolved;
                }
            }
            Optional<String> ns = namespaceContext.namespaceFor(prefixed.prefix());

            if (ns.isPresent()) {
                return toContext(ItemName.from(ns.get(), prefixed.localName()));
            } else if (!prefixed.prefix().isEmpty()) {
                warnOrThrow("Undeclared prefix '%s' , name: %s", prefixed.prefix(), name);
            } else {
                return toContext(new ItemName(prefixed.localName()));
            }
        }
        QNameInfo result = QNameUtil.uriToQNameInfo(name, true);
        // FIXME: Explicit empty namespace is workaround for cases, where we somehow lost namespace
        // eg. parsing json with filters without namespaces
        if (Strings.isNullOrEmpty(result.name.getNamespaceURI()) && !result.explicitEmptyNamespace) {
            Optional<String> defaultNs = namespaceContext.defaultNamespace();
            if(defaultNs.isPresent()) {
                result = QNameUtil.qnameToQnameInfo(new QName(defaultNs.get(), result.name.getLocalPart()));
            }
        }
        return toContext(ItemName.fromQName(result.name));
    }

    private @NotNull XNodeDefinition infra(@NotNull String name) {
        if (JsonInfraItems.PROP_VALUE.equals(name)) {
            return valueContext();
        }
        if (JsonInfraItems.PROP_METADATA.equals(name)) {
            return metadataDef();
        }
        // Infra properties are unqualified for now
        // TODO: We could return definition for infra properties later
        return unawareFrom(new ItemName(name));
    }

    public @NotNull XNodeDefinition unaware() {
        return unawareFrom(getName());
    }

    public @NotNull XNodeDefinition moreSpecific(@NotNull XNodeDefinition other) {
        // Prefer type aware
        if(other instanceof ComplexTypeAware) {
            return other;
        }
        return this;
    }

    public XNodeDefinition child(QName name) {
        return child(ItemName.fromQName(name));
    }

    public XNodeDefinition child(ItemName name) {
        XNodeDefinition maybe = resolveLocally(ItemName.from(name.getNamespaceURI(), name.getLocalPart()));

        if(maybe != null) {
            return maybe;
        }
        return unawareFrom(name);
    }

    private @NotNull XNodeDefinition valueContext() {
        return new Value(this);
    }

    protected boolean isInfra(@NotNull String name) {
        return name.startsWith("@");
    }

    private void warnOrThrow(String string, Object... prefix) throws SchemaException {
        throw new SchemaException(Strings.lenientFormat(string, prefix));
    }

    protected @Nullable XNodeDefinition resolveLocally(@NotNull String localName, String defaultNs) {
        return null;
    }

    protected @Nullable XNodeDefinition resolveLocally(@NotNull ItemName name) {
        return null;
    }

    private @NotNull XNodeDefinition toContext(ItemName name) {
        XNodeDefinition ret = resolveLocally(name);
        if(ret != null) {
            return ret;
        }
        return unawareFrom(name);
    }

    public abstract ItemDefinition<?> itemDefinition();

    private abstract static class SchemaAware extends XNodeDefinition {

        protected final SchemaRoot root;
        protected final ItemDefinition<?> itemDefinition;
        private final boolean inherited;

        public SchemaAware(ItemName name, ItemDefinition<?> itemDefinition, SchemaRoot root, boolean inherited) {
            super(name);
            this.itemDefinition = itemDefinition;
            this.inherited = inherited;
            this.root = root;
        }

        public SchemaAware(ItemDefinition<?> itemDefinition, SchemaRoot root, boolean inherited) {
            super(itemDefinition.getItemName());
            this.itemDefinition = itemDefinition;
            this.inherited = inherited;
            this.root = root;
        }

        @Override
        public boolean definedInParent() {
            return inherited;
        }

        protected XNodeDefinition awareFrom(ItemName name, ItemDefinition<?> definition, boolean inherited) {
            return root.awareFrom(name, definition, inherited);
        }

        @Override
        public @NotNull XNodeDefinition withType(QName typeName) {
            return root.fromType(itemDefinition, typeName, inherited);
        }

        @Override
        protected XNodeDefinition unawareFrom(ItemName name) {
            return root.unawareFrom(name);
        }

        @Override
        public XNodeDefinition metadataDef() {
            return root.metadataDef();
        }

        @Override
        public ItemDefinition<?> itemDefinition() {
            return itemDefinition;
        }
    }

    public abstract static class Root extends XNodeDefinition {

        protected Root(ItemName name) {
            super(name);
        }

        @Override
        public abstract XNodeDefinition metadataDef();

        public abstract @NotNull PrismNamespaceContext staticNamespaceContext();

    }

    private static class SchemaRoot extends Root {

        private SchemaRegistry registry;

        public SchemaRoot(SchemaRegistry reg) {
            super(new ItemName(""));
            registry = reg;
        }

        @Override
        public @NotNull PrismNamespaceContext staticNamespaceContext() {
            return registry.staticNamespaceContext();
        }

        public @NotNull XNodeDefinition fromType(@NotNull ItemDefinition<?> itemDef, QName typeName, boolean inherited) {
            var typeDef = Optional.ofNullable(registry.findComplexTypeDefinitionByType(typeName));
            return awareFrom(itemDef, typeDef, inherited);
        }


        XNodeDefinition awareFrom(ItemName name, ItemDefinition<?> definition, boolean inherited) {
//            if(definition instanceof PrismReferenceDefinition) {
//                var refDef = ((PrismReferenceDefinition) definition);
//                QName compositeName = refDef.getCompositeObjectElementName();
//                // FIXME: MID-6818 We use lastName in xmaps, because references returns accountRef even for account
//                // where these two are really different types - accountRef is strict reference
//                // account is composite reference (probably should be different types altogether)
//                if(QNameUtil.match(name, compositeName)) {
//                    // Return composite item definition
//                    return fromType(compositeName, refDef.getTargetTypeName(), inherited);
//                }
//                // TODO: This could allow special handling of object reference attributes
//                //return new ObjectReference(name, definition.structuredType().get(), this, inherited);
//            }

            if(definition != null) {
                if (definition.isDynamic()) {
                    inherited = false;
                }
                return awareFrom(definition, definition.structuredType(), inherited);
            }
            return unawareFrom(name);
        }

        private XNodeDefinition awareFrom(@NotNull ItemDefinition<?> definition, Optional<ComplexTypeDefinition> structuredType, boolean inherited) {
            if(structuredType.isPresent()) {
                var complex = structuredType.get();
                if(complex.isReferenceMarker()) {
                    return new ObjectReference(definition, structuredType.get(), this, inherited);
                }
                if(complex.hasSubstitutions()) {
                    return new ComplexTypeWithSubstitutions(definition, structuredType.get(), this, inherited);
                }
                if (allowsStrictAny(complex)) {
                    return new ComplexTypeWithStrictAny(definition, structuredType.get(),  this, inherited);
                }
                return new ComplexTypeAware(definition, structuredType.get(),  this, inherited);
            }
            return new SimpleType(definition, definition.getTypeName(), inherited, this);

        }

        @Override
        public @NotNull XNodeDefinition withType(QName typeName) {
            return fromType(itemDefinition(), typeName, false);
        }


        @Override
        protected XNodeDefinition resolveLocally(String localName, String defaultNs) {
            var baseNs = registry.staticNamespaceContext().defaultNamespace();
            if (Strings.isNullOrEmpty(defaultNs) && baseNs.isPresent()) {
                var maybe = registry.findItemDefinitionByElementName(ItemName.from(baseNs.get(), localName));
                if (maybe != null) {
                    return awareFrom(maybe.getItemName(), maybe, true);
                }
            }
            return null;
        }

        @Override
        protected XNodeDefinition resolveLocally(ItemName name) {
            ItemDefinition<?> def = registry.findObjectDefinitionByElementName(name);
            if(def == null) {
                try {
                    def = registry.findItemDefinitionByElementName(name);
                } catch (IllegalStateException e) {
                    return unawareFrom(name);
                }
            }
            return awareFrom(name, def, false);
        }

        @Override
        public ItemDefinition<?> itemDefinition() {
            return null;
        }

        @Override
        public Optional<QName> getType() {
            return Optional.empty();
        }

        @Override
        protected XNodeDefinition unawareFrom(ItemName name) {
            return new SimpleType(name, null, false, this, false);
        }

        @Override
        public XNodeDefinition metadataDef() {
            var def = registry.getValueMetadataDefinition();
            return new ComplexTypeAware(JsonInfraItems.PROP_METADATA_QNAME, def, def.structuredType().get(), this, true);
        }

    }

    private static class SchemaRootExpectingItem extends SchemaRoot {

        private final ItemDefinition<?> expectedItem;

        public SchemaRootExpectingItem(SchemaRegistry reg, ItemDefinition<?> expectedItem) {
            super(reg);
            this.expectedItem = expectedItem;
        }

        @Override
        protected XNodeDefinition resolveLocally(String localName, String defaultNs) {
            if (expectedItem.getItemName().getLocalPart().equals(localName)) {
                return awareFrom(expectedItem.getItemName(), expectedItem, true);
            }
            return super.resolveLocally(localName, defaultNs);
        }

        @Override
        protected XNodeDefinition resolveLocally(ItemName name) {
            if (expectedItem.getItemName().equals(name)) {
                return awareFrom(expectedItem.getItemName(), expectedItem, true);
            }
            return super.resolveLocally(name);
        }
    }

    private static class ComplexTypeAware extends SchemaAware {

        protected final ComplexTypeDefinition typeDefinition;

        public ComplexTypeAware(ItemDefinition<?> itemDef, ComplexTypeDefinition typeDef, SchemaRoot root, boolean inherited) {
            super(itemDef, root, inherited);
            this.typeDefinition = typeDef;
        }

        public ComplexTypeAware(ItemName name, ItemDefinition<?> itemDef, ComplexTypeDefinition typeDef, SchemaRoot root, boolean inherited) {
            super(name, itemDef, root, inherited);
            this.typeDefinition = typeDef;
        }

        @Override
        public Optional<QName> getType() {
            return Optional.of(typeDefinition.getTypeName());
        }

        @Override
        protected XNodeDefinition resolveLocally(ItemName name) {
            return awareFrom(name, findDefinition(name), true);
        }

        /**
         * Looks up definition by provided QName
         *
         * Searches for container-local item definition by provided QName,
         * if definition is not present, looks for schema migration for provided QName
         * and tries to find definition of replacement.
         *
         * Migration search is not version aware, and is triggered only
         * if original definition is removed from schema.
         *
         * @param name
         * @return
         */
        protected ItemDefinition<?> findDefinition(ItemName name) {
            ItemDefinition ret = typeDefinition.findLocalItemDefinition(name);
            if (ret != null) {
                return ret;
            }
            // Definition may be renamed, lets look schema migrations;
            if ( typeDefinition.getSchemaMigrations() == null) {
                return null;
            }
            for(SchemaMigration migration : typeDefinition.getSchemaMigrations()) {
                if (migration.getOperation() == SchemaMigrationOperation.MOVED
                        && QNameUtil.match(name, migration.getElementQName())
                        && migration.getReplacement() != null) {
                    QName replacement = migration.getReplacement();
                    return typeDefinition.findLocalItemDefinition(replacement);
                }
            }
            return null;
        }

        @Override
        protected XNodeDefinition resolveLocally(String localName, String defaultNs) {
            var proposed = ItemName.from(typeDefinition.getTypeName().getNamespaceURI(),localName);
            ItemDefinition<?> childDef = findDefinition(proposed);

            // If child definition is dynamic and default namespace is specified and parent definition generates
            // it with constant type - use default namespace (do not assume definition exists in parent).
            // for example shadow/associations
            var defaultNsName = ItemName.from(defaultNs, localName);
            if (childDef != null && childDef.isDynamic() && typeDefinition.getDefaultItemTypeName() != null && defaultNs != null) {
                var maybeDef = findDefinition(defaultNsName);
                if (maybeDef != null) {
                    childDef = maybeDef;
                }
            }
            if (childDef == null) {
                childDef = findDefinition(defaultNsName);
            }
            if (childDef == null) {
                childDef = findDefinition(new ItemName(localName));
            }
            if (childDef != null) {
                return awareFrom(proposed, childDef, true);
            }
            // Fallback to infra properties such as id, version serialized without @ prefix
            if (PrismConstants.T_ID.getLocalPart().equals(localName)) {
                return unawareFrom(PrismConstants.T_ID);
            }

            return null;
        }

        @Override
        public @NotNull XNodeDefinition moreSpecific(@NotNull XNodeDefinition other) {
            if(other instanceof ComplexTypeAware) {
                ComplexTypeDefinition localType = this.typeDefinition;
                ComplexTypeDefinition otherType = ((ComplexTypeAware) other).typeDefinition;
                if(localType == otherType) {
                    return other;
                }
                if (localType.getTypeName().equals(otherType.getSuperType())) {
                    return other;
                }
            }
            return this;
        }
    }

    private static class ComplexTypeWithSubstitutions extends ComplexTypeAware {

        public ComplexTypeWithSubstitutions(ItemDefinition<?> definition, ComplexTypeDefinition typeDef, SchemaRoot root, boolean inherited) {
            super(definition, typeDef,  root, inherited);
        }

        @Override
        protected ItemDefinition<?> findDefinition(ItemName name) {
            // TODO: Add schemaMigrations lookup
            return typeDefinition.itemOrSubstitution(name).orElse(null);
        }
    }

    private static class ComplexTypeWithStrictAny extends ComplexTypeAware {

        public ComplexTypeWithStrictAny(ItemDefinition<?> definition, ComplexTypeDefinition typeDef,  SchemaRoot root, boolean inherited) {
            super(definition, typeDef, root, inherited);
        }

        @Override
        protected XNodeDefinition resolveLocally(String localName, String defaultNs) {
            var proposed = ItemName.from(typeDefinition.getTypeName().getNamespaceURI(),localName);
            ItemDefinition<?> def = findDefinition(proposed);
            if(def == null && !Strings.isNullOrEmpty(defaultNs)) {
                def = findDefinition(ItemName.from(typeDefinition.getTypeName().getNamespaceURI(),localName));
            }
            if(def == null) {
                def = findDefinition(new ItemName(localName));
            }
            if(def != null) {
                return awareFrom(proposed, def, true);
            }
            return null;
        }

        @Override
        protected ItemDefinition<?> findDefinition(ItemName name) {
            var maybe = super.findDefinition(name);
            if (maybe == null) {
                maybe = root.registry.findItemDefinitionByElementName(name);
            }
            return maybe;
        }
    }

    private static class SchemaIgnorant extends Root {

        public SchemaIgnorant(ItemName name) {
            super(name);
        }

        @Override
        public @NotNull XNodeDefinition unaware() {
            return this;
        }

        @Override
        public Optional<QName> getType() {
            return Optional.empty();
        }

        @Override
        public @NotNull XNodeDefinition withType(QName typeName) {
            return this;
        }

        @Override
        protected XNodeDefinition unawareFrom(ItemName name) {
            return new SchemaIgnorant(name);
        }

        @Override
        public XNodeDefinition metadataDef() {
            return new SchemaIgnorant(JsonInfraItems.PROP_METADATA_QNAME);
        }

        @Override
        public @NotNull PrismNamespaceContext staticNamespaceContext() {
            return PrismNamespaceContext.EMPTY;
        }

        @Override
        public ItemDefinition<?> itemDefinition() {
            return null;
        }
    }

    private static class Value extends XNodeDefinition {

        XNodeDefinition delegate;

        public Value(XNodeDefinition delegate) {
            super(JsonInfraItems.PROP_VALUE_QNAME);
            this.delegate = delegate;
        }

        @Override
        protected @Nullable XNodeDefinition resolveLocally(@NotNull String localName, String defaultNs) {
            return delegate.resolveLocally(localName, defaultNs);
        }

        @Override
        protected @Nullable XNodeDefinition resolveLocally(@NotNull ItemName name) {
            return delegate.resolveLocally(name);
        }

        @Override
        public Optional<QName> getType() {
            return delegate.getType();
        }

        @Override
        public @NotNull XNodeDefinition withType(QName typeName) {
            return new Value(delegate.withType(typeName));
        }

        @Override
        protected XNodeDefinition unawareFrom(ItemName name) {
            return delegate.unawareFrom(name);
        }

        @Override
        public XNodeDefinition metadataDef() {
            return delegate.metadataDef();
        }

        @Override
        public ItemDefinition<?> itemDefinition() {
            return delegate.itemDefinition();
        }
    }

    private static class SimpleType extends SchemaAware {

        private boolean xmlAttribute;

        public SimpleType(ItemName name, QName type, boolean inherited, SchemaRoot root, boolean xmlAttribute) {
            super(name, null, root, inherited);
            this.type = type;
            this.xmlAttribute = false;
        }

        public SimpleType(ItemDefinition<?> def, QName type, boolean inherited, SchemaRoot root) {
            this(def, type, inherited, root, false);
        }

        public SimpleType(ItemDefinition<?> definition, QName type, boolean inherited, SchemaRoot root, boolean xmlAttribute) {
            super(definition, root, inherited);
            this.type = type;
            this.xmlAttribute = xmlAttribute;
        }

        private final QName type;

        @Override
        public Optional<QName> getType() {
            return Optional.ofNullable(type);
        }

        @Override
        public boolean isXmlAttribute() {
            return xmlAttribute;
        }

        @Override
        public @NotNull XNodeDefinition withType(QName typeName) {
            if (itemDefinition != null) {
                return super.withType(typeName);
            }
            return new SimpleType(getName(), typeName, definedInParent(), root, isXmlAttribute());
        }
    }

    private static class ObjectReference extends ComplexTypeAware {

        public ObjectReference(ItemDefinition<?> def, ComplexTypeDefinition typeDef, SchemaRoot root, boolean inherited) {
            super(def, typeDef, root, inherited);
        }

        @Override
        protected XNodeDefinition resolveLocally(ItemName name) {
            // TODO: Since CTD now contains attributes section this could be reworked
            // into ComplexTypeAware as search in attributes section
            if (PrismConstants.ATTRIBUTE_OID_LOCAL_NAME.equals(name.getLocalPart())) {
                return new SimpleType(XNodeImpl.KEY_REFERENCE_OID, DOMUtil.XSD_STRING, true, root, true);
            }
            if (PrismConstants.ATTRIBUTE_REF_TYPE_LOCAL_NAME.equals(name.getLocalPart())) {
                return new SimpleType(XNodeImpl.KEY_REFERENCE_TYPE, DOMUtil.XSD_QNAME, true, root, true);
            }
            if (PrismConstants.ATTRIBUTE_RELATION_LOCAL_NAME.equals(name.getLocalPart())) {
                return new SimpleType(XNodeImpl.KEY_REFERENCE_RELATION, DOMUtil.XSD_QNAME, true, root, true);
            }
            return super.resolveLocally(name);
        }
    }

    @Override
    public String toString() {
        return Objects.toString(getName());
    }

    static boolean allowsStrictAny(ComplexTypeDefinition complex) {
        return complex.isStrictAnyMarker();
    }

    public boolean definedInParent() {
        return false;
    }

    public abstract @NotNull XNodeDefinition withType(QName typeName);

    public abstract XNodeDefinition metadataDef();

    public XNodeDefinition valueDef() {
        return valueContext();
    }

}
