/*
 * Copyright (c) 2010-2020 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.prism.impl.xnode;

import java.io.File;
import java.util.List;
import java.util.Map.Entry;

import javax.xml.namespace.QName;

import com.evolveum.midpoint.prism.AbstractFreezable;
import com.evolveum.midpoint.prism.ItemDefinition;
import com.evolveum.midpoint.prism.PrismConstants;
import com.evolveum.midpoint.prism.PrismNamespaceContext;
import com.evolveum.midpoint.prism.path.ItemName;
import com.evolveum.midpoint.prism.util.CloneUtil;
import com.evolveum.midpoint.prism.xnode.MapXNode;
import com.evolveum.midpoint.prism.xnode.XNode;
import com.evolveum.midpoint.util.DebugUtil;
import com.evolveum.midpoint.util.MiscUtil;
import com.evolveum.midpoint.util.Transformer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * @author semancik
 *
 */
public abstract class XNodeImpl extends AbstractFreezable implements XNode {

    public static final ItemName KEY_OID = ItemName.interned(null, "oid");
    public static final ItemName KEY_VERSION = ItemName.interned(null ,"version");
    public static final ItemName KEY_CONTAINER_ID = PrismConstants.T_ID;
    public static final ItemName.WithoutPrefix KEY_REFERENCE_OID = PrismConstants.T_OBJECT_REFERENCE_OID;
    public static final ItemName.WithoutPrefix KEY_REFERENCE_TYPE = PrismConstants.T_OBJECT_REFERENCE_TYPE;
    public static final ItemName.WithoutPrefix KEY_REFERENCE_RELATION = PrismConstants.T_OBJECT_REFERENCE_RELATION;

    // FIXME: Versions with namespace break PrismUnmarshaller parsing of references
    public static final ItemName KEY_REFERENCE_DESCRIPTION = PrismConstants.T_OBJECT_REFERENCE_DESCRIPTION;
    public static final ItemName KEY_REFERENCE_FILTER = PrismConstants.T_OBJECT_REFERENCE_FILTER;
    public static final ItemName KEY_REFERENCE_RESOLUTION_TIME = PrismConstants.T_OBJECT_REFERENCE_RESOLUTION_TIME;
    public static final ItemName KEY_REFERENCE_REFERENTIAL_INTEGRITY = PrismConstants.T_OBJECT_REFERENCE_REFERENTIAL_INTEGRITY;
    public static final ItemName KEY_REFERENCE_TARGET_NAME = PrismConstants.T_OBJECT_REFERENCE_TARGET_NAME;
    public static final ItemName KEY_REFERENCE_OBJECT = PrismConstants.T_OBJECT_REFERENCE_OBJECT;

    private static final QName DUMMY_NAME = new QName(null, "dummy");

    // Common fields
    protected XNodeImpl parent;         // currently unused

    /**
     * If set to true that the element came from the explicit type definition
     * (e.g. xsi:type in XML) on the parsing side; or that it the explicit type
     * definition should be included on the serialization side.
     */
    private boolean explicitTypeDeclaration = false;

    // These are set when parsing a file
    // FIXME: Consider using SourceLocation

    private File originFile;
    private String originDescription;
    private int lineNumber;

    // These may be detected in parsed file and are also used for serialization
    protected QName typeQName;
    protected QName elementName;                            // Filled if and only if this is a member of heterogeneous list.
    protected Integer maxOccurs;

    // a comment that could be stored into formats that support these (e.g. XML or YAML)
    private String comment;

    /**
     * Attached definition if it was discovered during parsing.
     */
    private ItemDefinition<?> definition;

    /**
     * Custom data to be used during parsing process. TODO reconsider
     */
    private transient Object parserData;

    private final PrismNamespaceContext namespaceContext;

    public XNodeImpl() {
        this(PrismNamespaceContext.EMPTY);
    }

    public XNodeImpl(PrismNamespaceContext local) {
        this.namespaceContext = local;
    }

    public XNodeImpl getParent() {
        return parent;
    }

    public void setParent(XNodeImpl parent) {
        checkMutable();
        this.parent = parent;
    }

    public File getOriginFile() {
        return originFile;
    }

    public void setOriginFile(File originFile) {
        checkMutable();
        this.originFile = originFile;
    }

    public String getOriginDescription() {
        return originDescription;
    }

    public void setOriginDescription(String originDescription) {
        checkMutable();
        this.originDescription = originDescription;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        checkMutable();
        this.lineNumber = lineNumber;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        checkMutable();
        this.comment = comment;
    }

    @Override
    public QName getTypeQName() {
        return typeQName;
    }

    public void setTypeQName(QName typeQName) {
        checkMutable();
        this.typeQName = typeQName;
    }

    public QName getElementName() {
        return elementName;
    }

    public void setElementName(QName elementName) {
        checkMutable();
        this.elementName = elementName;
    }

    @Override
    public Integer getMaxOccurs() {
        return maxOccurs;
    }

    public void setMaxOccurs(Integer maxOccurs) {
        checkMutable();
        this.maxOccurs = maxOccurs;
    }

    @Override
    public abstract boolean isEmpty();

    @Override
    public boolean isExplicitTypeDeclaration() {
        return explicitTypeDeclaration;
    }

    public void setExplicitTypeDeclaration(boolean explicitTypeDeclaration) {
        checkMutable();
        this.explicitTypeDeclaration = explicitTypeDeclaration;
    }

    @Override
    @NotNull
    public XNodeImpl clone() {
        return cloneTransformKeys(null);
    }

    public XNodeImpl cloneTransformKeys(Transformer<QName,QName> keyTransformer) {
        return cloneTransformKeys(keyTransformer, this);
    }

    @Contract("_, null -> null; _, !null -> !null")
    private static <X extends XNodeImpl> X cloneTransformKeys(Transformer<QName,QName> keyTransformer, X xnode) {
        XNodeImpl xclone;
        if (xnode == null) {
            return null;
        } else if (xnode instanceof PrimitiveXNodeImpl<?>) {
            return (X) ((PrimitiveXNodeImpl) xnode).cloneInternal();
        } else if (xnode instanceof MapXNodeImpl) {
            MapXNodeImpl xmap = (MapXNodeImpl)xnode;
            xclone = new MapXNodeImpl(xnode.namespaceContext());
            for (Entry<QName, XNodeImpl> entry: xmap.entrySet()) {
                QName key = entry.getKey();
                QName newKey = keyTransformer != null ? keyTransformer.transform(key) : key;
                if (newKey != null) {
                    XNodeImpl value = entry.getValue();
                    XNodeImpl newValue = cloneTransformKeys(keyTransformer, value);
                    ((MapXNodeImpl) xclone).put(newKey, newValue);
                }
            }
        } else if (xnode instanceof ListXNodeImpl) {
            xclone = new ListXNodeImpl(xnode.namespaceContext());
            for (XNodeImpl xsubnode: ((ListXNodeImpl)xnode)) {
                ((ListXNodeImpl) xclone).add(cloneTransformKeys(keyTransformer, xsubnode));
            }
        } else if (xnode instanceof RootXNodeImpl) {
            xclone = new RootXNodeImpl(((RootXNodeImpl) xnode).getRootElementName(),
                    cloneTransformKeys(keyTransformer, ((RootXNodeImpl) xnode).getSubnode()), xnode.namespaceContext());
        } else if (xnode instanceof SchemaXNodeImpl) {
            xclone = new SchemaXNodeImpl(xnode.namespaceContext());
            ((SchemaXNodeImpl) xclone).setSchemaElement(((SchemaXNodeImpl) xnode).getSchemaElement());
        } else {
            throw new IllegalArgumentException("Unknown xnode "+xnode);
        }

        QName name = xnode.getElementName();
        if (name != null) {
            QName newName = keyTransformer != null ? keyTransformer.transform(name) : name;
            xclone.setElementName(newName);
        }

        xclone.copyCommonAttributesFrom(xnode);
        return (X) xclone;
    }

    // filling-in other properties (we skip parent and origin-related things)
    protected void copyCommonAttributesFrom(XNodeImpl xnode) {
        checkMutable();
        explicitTypeDeclaration = xnode.explicitTypeDeclaration;
        setTypeQName(xnode.getTypeQName());
        setComment(xnode.getComment());
        setMaxOccurs(xnode.getMaxOccurs());
    }

    public abstract String getDesc();

    protected String dumpSuffix() {
        StringBuilder sb = new StringBuilder();
        if (elementName != null) {
            sb.append(" element=").append(elementName);
        }
        if (typeQName != null) {
            sb.append(" type=").append(typeQName);
        }
        if (maxOccurs != null) {
            sb.append(" maxOccurs=").append(maxOccurs);
        }
        return sb.toString();
    }

    // overridden in RootXNode
    @Override
    public RootXNodeImpl toRootXNode() {
        return new RootXNodeImpl(XNodeImpl.DUMMY_NAME, this, namespaceContext());
    }

    public boolean isHeterogeneousList() {
        return false;
    }

    public final boolean isSingleEntryMap() {
        return this instanceof MapXNodeImpl && ((MapXNodeImpl) this).size() == 1;
    }

    public Object getParserData() {
        return parserData;
    }

    public void setParserData(Object parserData) {
        this.parserData = parserData;
    }

    void appendMetadata(StringBuilder sb, int indent, List<MapXNode> metadata) {
        if (metadata != null && !metadata.isEmpty()) {
            sb.append("\n");
            DebugUtil.debugDumpWithLabel(sb, "metadata", metadata, indent);
        }
    }

    boolean metadataEquals(@NotNull List<MapXNode> metadata1, @NotNull List<MapXNode> metadata2) {
        return MiscUtil.unorderedCollectionEquals(metadata1, metadata2);
    }

    @Override
    public PrismNamespaceContext namespaceContext() {
        return namespaceContext;
    }

    protected <T extends XNodeImpl> T copyCommonTo(T target) {
        target.setComment(getComment());
        target.setElementName(getElementName());
        target.setExplicitTypeDeclaration(isExplicitTypeDeclaration());
        target.setLineNumber(getLineNumber());
        target.setMaxOccurs(getMaxOccurs());
        target.setOriginDescription(getOriginDescription());
        target.setOriginFile(getOriginFile());
        target.setParserData(CloneUtil.clone(getParserData()));
        target.setTypeQName(getTypeQName());
        return target;
    }

    @Override
    public void setDefinition(ItemDefinition<?> definition) {
        this.definition = definition;
    }

    @Override
    public ItemDefinition<?> getDefinition() {
        return definition;
    }
}
