/*
 * Copyright (c) 2010-2020 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism.impl.marshaller;

import java.util.*;

import javax.xml.namespace.QName;

import org.jetbrains.annotations.NotNull;
import com.evolveum.midpoint.prism.PrismConstants;
import com.evolveum.midpoint.prism.PrismNamespaceContext;
import com.evolveum.midpoint.prism.PrismNamespaceContext.PrefixPreference;
import com.evolveum.midpoint.prism.path.*;
import com.google.common.base.Strings;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

/**
 * Holds internal (parsed) form of midPoint-style XPath-like expressions.
 * It is able to retrieve/export these expressions from/to various forms (text, text in XML document,
 * XPathSegment list, prism path specification).
 * <p>
 * Assumes relative XPath, but somehow can also work with absolute XPaths.
 * <p>
 * NOT to be used outside prism module (except for XPathTest in schema - but this is also to be resolved).
 *
 * @author semancik
 * @author mederly
 */
public final class ItemPathSerialization {

    private final PrismNamespaceContext context;
    private final String xpath;

    private final Map<String, String> undeclaredPrefixes;
    private final Map<String, String> usedPrefixes;

    private ItemPathSerialization(String xpath, PrismNamespaceContext context,
            Map<String, String> usedPrefixToNs, Map<String, String> undeclaredPrefixes) {
        this.xpath = xpath;
        this.context = context;
        this.usedPrefixes = usedPrefixToNs;
        this.undeclaredPrefixes = undeclaredPrefixes;

    }

    public PrismNamespaceContext context() {
        return context;
    }

    public Map<String, String> undeclaredPrefixes() {
        return undeclaredPrefixes;
    }

    public Map<String, String> usedPrefixes() {
        return usedPrefixes;
    }

    public String getXpath() {
        return xpath;
    }

    @Override
    public String toString() {
        return xpath;
    }

    public String getXPathWithoutDeclarations() {
        return xpath;
    }

    public String getXPathWithLocalDeclarations() {
        StringBuilder sb = new StringBuilder();
        ItemPathHolder.addDeclarations(sb, undeclaredPrefixes);
        sb.append(xpath);
        return sb.toString();
    }

    public String getXPathWithAllDeclarations() {
        StringBuilder sb = new StringBuilder();
        ItemPathHolder.addDeclarations(sb, usedPrefixes);
        sb.append(xpath);
        return sb.toString();
    }

    public static ItemPathSerialization serialize(@NotNull UniformItemPath itemPath, PrismNamespaceContext context) {
        return serialize(itemPath, context, false);
    }

    public static ItemPathSerialization serialize(@NotNull UniformItemPath itemPath, PrismNamespaceContext context, boolean overrideNs) {
        Map<String, String> usedPrefixToNs = new HashMap<String, String>();
        BiMap<String, String> undeclaredNsToPrefix = HashBiMap.create();

        List<PathHolderSegment> segments = new ArrayList<>();

        for (ItemPathSegment segment : itemPath.getSegments()) {
            PathHolderSegment xsegment;
            if (segment instanceof NameItemPathSegment) {
                QName name = ((NameItemPathSegment) segment).getName();
                xsegment = new PathHolderSegment(assignPrefix(name, context, undeclaredNsToPrefix, usedPrefixToNs, overrideNs));
            } else if (segment instanceof VariableItemPathSegment) {
                QName name = ((VariableItemPathSegment) segment).getName();
                xsegment = new PathHolderSegment(assignPrefix(name, context, undeclaredNsToPrefix, usedPrefixToNs, overrideNs), true);
            } else if (segment instanceof IdItemPathSegment) {
                xsegment = new PathHolderSegment(idToString(((IdItemPathSegment) segment).getId()));
            } else if (segment instanceof ObjectReferencePathSegment) {
                xsegment = new PathHolderSegment(PrismConstants.T_OBJECT_REFERENCE, false);
            } else if (segment instanceof ParentPathSegment) {
                xsegment = new PathHolderSegment(PrismConstants.T_PARENT, false);
            } else if (segment instanceof IdentifierPathSegment) {
                xsegment = new PathHolderSegment(PrismConstants.T_ID, false);
            } else {
                throw new IllegalStateException("Unknown segment: " + segment);
            }
            segments.add(xsegment);
        }

        StringBuilder xpath = new StringBuilder();
        ItemPathHolder.addPureXpath(false, segments, xpath);

        return new ItemPathSerialization(xpath.toString(), context, usedPrefixToNs, undeclaredNsToPrefix.inverse());
    }

    private static QName assignPrefix(@NotNull QName name, PrismNamespaceContext global,
            Map<String, String> localNamespaceToPrefix, Map<String, String> prefixToNs, boolean overrideNs) {
        String namespace = name.getNamespaceURI();
        String explicitPrefix = name.getPrefix();
        if(Strings.isNullOrEmpty(namespace)) {
            if(Strings.isNullOrEmpty(explicitPrefix)) {
                /*
                 * COMPAT: QName has prefix, but no namespace, fallback to default namespace
                 *   since we do not know how to interpret it
                 */
                return new ItemName(name.getLocalPart());
            }
            return name;
        }

        String proposedPrefix = assignPrefix(namespace, explicitPrefix, global, localNamespaceToPrefix, prefixToNs, overrideNs);
        if(explicitPrefix.equals(proposedPrefix)) {
            return name;
        }
        return new ItemName(namespace, name.getLocalPart(), proposedPrefix);

    }

    private static String assignPrefix(String namespace, String explicitPrefix, PrismNamespaceContext global,
            Map<String, String> localNamespaceToPrefix, Map<String, String> prefixToNs, boolean overrideNs) {

        // First we try to use existing prefix
        if(!Strings.isNullOrEmpty(explicitPrefix)) {
            String localNs = prefixToNs.get(explicitPrefix);
            if(namespace.equals(localNs)) {
                return explicitPrefix;
            }
            Optional<String> globalNs = global.namespaceFor(explicitPrefix);
            if(globalNs.isPresent() && namespace.equals(globalNs.get())) {
                prefixToNs.put(explicitPrefix, namespace);
                return explicitPrefix;
            }
            if(overrideNs && localNs == null) {
                localNamespaceToPrefix.putIfAbsent(namespace, explicitPrefix);
                prefixToNs.put(explicitPrefix, namespace);
                return explicitPrefix;
            }
        }
        // Renaming item/prefix
        String localPrefix = localNamespaceToPrefix.get(namespace);
        if(localPrefix != null) {
            // We already created local prefix for specified namespace
            return localPrefix;
        }
        Optional<String> globalPrefix = global.prefixFor(namespace, PrefixPreference.GLOBAL_FIRST_SKIP_DEFAULTS);
        if(globalPrefix.isPresent()) {
            // We are reusing inherited prefix
            prefixToNs.put(globalPrefix.get(), namespace);
            return globalPrefix.get();
        }

        // We Try to compute new prefix
        localPrefix = explicitPrefix;
        while(isPrefixConflicting(localPrefix, prefixToNs, global)) {
            localPrefix = proposeNewPrefix(namespace, explicitPrefix);
        }

        prefixToNs.put(localPrefix, namespace);
        localNamespaceToPrefix.put(namespace, localPrefix);
        return localPrefix;
    }

    private static String proposeNewPrefix(String ns, String candidate) {
        return (Strings.isNullOrEmpty(candidate) ? "gen" : candidate) + new Random().nextInt(999);
    }

    private static boolean isPrefixConflicting(String candidate, Map<String, String> prefixToNs,
            PrismNamespaceContext context) {
        return Strings.isNullOrEmpty(candidate) || prefixToNs.containsKey(candidate) || context.namespaceFor(candidate).isPresent();
    }

    private static String idToString(Long longVal) {
        if (longVal == null) {
            return null;
        }
        return longVal.toString();
    }

}
