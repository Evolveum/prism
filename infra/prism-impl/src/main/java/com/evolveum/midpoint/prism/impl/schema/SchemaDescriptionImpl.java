/*
 * Copyright (c) 2010-2018 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.prism.impl.schema;

import com.evolveum.midpoint.prism.AbstractFreezable;
import com.evolveum.midpoint.prism.schema.SchemaDescription;
import com.evolveum.midpoint.util.DOMUtil;
import com.evolveum.midpoint.util.DebugUtil;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import java.io.InputStream;

import static com.evolveum.midpoint.util.MiscUtil.stateCheck;

/**
 * Schema (prism or non-prism) with additional information.
 *
 * TODO Make this class "initializable at once" i.e. that it would not need to be in semi-finished state e.g. during parsing.
 */
public final class SchemaDescriptionImpl extends AbstractFreezable implements SchemaDescription {

    private final String path;
    private final String sourceDescription;
    private String usualPrefix;
    private String namespace;
    private InputStreamable streamable;
    private Node node;
    private boolean isPrismSchema = false;
    private boolean isDefault = false;
    private boolean isDeclaredByDefault = false;
    private Package compileTimeClassesPackage;
    private boolean registered;

    private String defaultPrefix;

    SchemaDescriptionImpl(String sourceDescription, String path) {
        this.sourceDescription = sourceDescription;
        this.path = path;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        checkMutable();
        this.namespace = namespace;
    }

    void setStreamable(InputStreamable streamable) {
        checkMutable();
        this.streamable = streamable;
    }

    public void setNode(Node node) {
        checkMutable();
        this.node = node;
    }

    @Override
    public String getUsualPrefix() {
        return usualPrefix;
    }

    void setUsualPrefix(String usualPrefix) {
        checkMutable();
        this.usualPrefix = usualPrefix;
    }

    @Override
    public String getSourceDescription() {
        return sourceDescription;
    }

    @Override
    public boolean isPrismSchema() {
        return isPrismSchema;
    }

    @SuppressWarnings("SameParameterValue")
    void setPrismSchema(boolean value) {
        checkMutable();
        this.isPrismSchema = value;
    }

    @Override
    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean isDefault) {
        checkMutable();
        this.isDefault = isDefault;
    }

    @Override
    public boolean isDeclaredByDefault() {
        return isDeclaredByDefault;
    }

    void setDeclaredByDefault(boolean isDeclaredByDefault) {
        checkMutable();
        this.isDeclaredByDefault = isDeclaredByDefault;
    }

    @Override
    public Package getCompileTimeClassesPackage() {
        return compileTimeClassesPackage;
    }

    void setCompileTimeClassesPackage(Package compileTimeClassesPackage) {
        checkMutable();
        stateCheck(!registered, "Not possible to set compile time classes package after registration: %s", this);
        this.compileTimeClassesPackage = compileTimeClassesPackage;
    }

    synchronized void setRegistered() {
        stateCheck(!registered, "Already registered: %s", this);
        registered = true;
    }

    @Override
    public boolean canInputStream() {
        return streamable != null;
    }

    @Override
    public InputStream openInputStream() {
        if (!canInputStream()) {
            throw new IllegalStateException("Schema "+sourceDescription+" cannot provide input stream");
        }
        return streamable.openInputStream();
    }

    @Override
    public Source getSource() {
        Source source;
        if (canInputStream()) {
            InputStream inputStream = openInputStream();
            // Return stream source as a first option. It is less efficient,
            // but it provides information about line numbers
            source = new StreamSource(inputStream);
        } else {
            source = new DOMSource(node);
        }
        source.setSystemId(path);
        return source;
    }

    @Override
    public Element getDomElement() {
        if (node instanceof Element) {
            return (Element)node;
        } else {
            return DOMUtil.getFirstChildElement(node);
        }
    }

    @FunctionalInterface
    interface InputStreamable {
        InputStream openInputStream();
    }

    @Override
    public String debugDump(int indent) {
        StringBuilder sb = new StringBuilder();
        DebugUtil.indentDebugDump(sb, indent);
        sb.append(path);
        return sb.toString();
    }

    @Override public String toString() {
        return "SchemaDescriptionImpl{" +
                "sourceDescription='" + sourceDescription + '\'' +
                ", usualPrefix='" + usualPrefix + '\'' +
                ", namespace='" + namespace + '\'' +
                '}';
    }

    public void setDefaultPrefix(String prefix) {
        this.defaultPrefix = prefix;
    }

    public String getDefaultPrefix() {
        return defaultPrefix;
    }
}
