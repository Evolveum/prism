/*
 * Copyright (c) 2010-2017 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.prism;

import static javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI;

import javax.xml.namespace.QName;

import com.evolveum.midpoint.prism.path.InfraItemName;
import com.evolveum.midpoint.prism.path.ItemName;
import com.evolveum.midpoint.util.annotation.Experimental;

/**
 * @author semancik
 */
public class PrismConstants {

    public static final String EXTENSION_LOCAL_NAME = "extension";
    public static final ItemName EXTENSION_ITEM_NAME = ItemName.interned(null, EXTENSION_LOCAL_NAME);
    public static final String NAME_LOCAL_NAME = "name";

    public static final String ATTRIBUTE_ID_LOCAL_NAME = "id";
    public static final String ATTRIBUTE_OID_LOCAL_NAME = "oid";
    public static final String ATTRIBUTE_VERSION_LOCAL_NAME = "version";
    public static final String ATTRIBUTE_REF_TYPE_LOCAL_NAME = "type";
    public static final String ATTRIBUTE_RELATION_LOCAL_NAME = "relation";

    public static final String ELEMENT_DESCRIPTION_LOCAL_NAME = "description";
    public static final String ELEMENT_FILTER_LOCAL_NAME = "filter";

    public static final String NS_PREFIX = "http://prism.evolveum.com/xml/ns/public/";
    public static final String NS_ANNOTATION = NS_PREFIX + "annotation-3";
    public static final String PREFIX_NS_ANNOTATION = "a";
    public static final String NS_TYPES = NS_PREFIX + "types-3";
    public static final String PREFIX_NS_TYPES = "t";
    public static final String NS_QUERY = NS_PREFIX + "query-3";
    public static final String PREFIX_NS_QUERY = "q";
    public static final String NS_METADATA = NS_PREFIX + "metadata-3";

    public static final String NS_MATCHING_RULE = NS_PREFIX + "matching-rule-3";

    public static final QName DEFAULT_MATCHING_RULE_NAME = new QName(NS_MATCHING_RULE, "default");
    public static final QName POLY_STRING_ORIG_MATCHING_RULE_NAME = new QName(NS_MATCHING_RULE, "polyStringOrig");
    public static final QName POLY_STRING_NORM_MATCHING_RULE_NAME = new QName(NS_MATCHING_RULE, "polyStringNorm");
    public static final QName POLY_STRING_STRICT_MATCHING_RULE_NAME = new QName(NS_MATCHING_RULE, "polyStringStrict");
    public static final QName STRING_IGNORE_CASE_MATCHING_RULE_NAME = new QName(NS_MATCHING_RULE, "stringIgnoreCase");
    public static final QName UUID_MATCHING_RULE_NAME = new QName(NS_MATCHING_RULE, "uuid");
    public static final QName XML_MATCHING_RULE_NAME = new QName(NS_MATCHING_RULE, "xml");
    public static final QName EXCHANGE_EMAIL_ADDRESSES_MATCHING_RULE_NAME = new QName(NS_MATCHING_RULE, "exchangeEmailAddresses");
    public static final QName DISTINGUISHED_NAME_MATCHING_RULE_NAME = new QName(NS_MATCHING_RULE, "distinguishedName");

    public static final String NS_POLY_STRING_NORMALIZER = NS_PREFIX + "poly-string-normalizer-3";

    public static final QName NO_OP_NORMALIZER = new QName(NS_POLY_STRING_NORMALIZER, "noOp");
    public static final QName POLY_STRING_NORM_NORMALIZER = new QName(NS_POLY_STRING_NORMALIZER, "polyStringNorm");
    public static final QName POLY_STRING_ORIG_NORMALIZER = new QName(NS_POLY_STRING_NORMALIZER, "polyStringOrig");
    public static final QName ALPHANUMERIC_POLY_STRING_NORMALIZER = new QName(NS_POLY_STRING_NORMALIZER, "alphanumeric");
    public static final QName ASCII7_POLY_STRING_NORMALIZER = new QName(NS_POLY_STRING_NORMALIZER, "ascii7");
    public static final QName PASSTHROUGH_POLY_STRING_NORMALIZER = new QName(NS_POLY_STRING_NORMALIZER, "passthrough");
    public static final QName LOWERCASE_STRING_NORMALIZER = new QName(NS_POLY_STRING_NORMALIZER, "lowercase");

    // TODO move (some of these) outside prism?
    public static final QName DISTINGUISHED_NAME_NORMALIZER = new QName(NS_POLY_STRING_NORMALIZER, "distinguishedName");
    public static final QName UUID_NORMALIZER = new QName(NS_POLY_STRING_NORMALIZER, "uuid");
    public static final QName EXCHANGE_EMAIL_ADDRESS_NORMALIZER = new QName(NS_POLY_STRING_NORMALIZER, "exchangeEmailAddress");
    public static final QName XML_NORMALIZER = new QName(NS_POLY_STRING_NORMALIZER, "xml");

    public static final String NS_PREFIX_CRYPTO = NS_PREFIX + "crypto/";
    public static final String NS_PREFIX_CRYPTO_ALGORITHM = NS_PREFIX_CRYPTO + "algorithm/";
    public static final String NS_CRYPTO_ALGORITHM_PBKD = NS_PREFIX_CRYPTO_ALGORITHM + "pbkd-3";

    // Annotations

    public static final QName A_CONTAINER = new QName(NS_ANNOTATION, "container");
    public static final QName A_OBJECT = new QName(NS_ANNOTATION, "object");
    public static final QName A_INSTANTIATION_ORDER = new QName(NS_ANNOTATION, "instantiationOrder");

    public static final QName A_DEFAULT_ITEM_TYPE_NAME = new QName(NS_ANNOTATION, "defaultItemTypeName");
    public static final QName A_DEFAULT_NAMESPACE = new QName(NS_ANNOTATION, "defaultNamespace");
    public static final QName A_IGNORED_NAMESPACE = new QName(NS_ANNOTATION, "ignoredNamespace");

    public static final QName A_TYPE = new QName(NS_ANNOTATION, "type");
    public static final QName A_DISPLAY_NAME = new QName(NS_ANNOTATION, "displayName");
    public static final QName A_DISPLAY_ORDER = new QName(NS_ANNOTATION, "displayOrder");
    public static final QName A_HELP = new QName(NS_ANNOTATION, "help");
    public static final QName A_ACCESS = new QName(NS_ANNOTATION, "access");
    public static final String A_ACCESS_CREATE = "create";
    public static final String A_ACCESS_UPDATE = "update";
    public static final String A_ACCESS_READ = "read";
    public static final String A_ACCESS_NONE = "none";
    public static final QName A_INDEX_ONLY = new QName(NS_ANNOTATION, "indexOnly");
    public static final QName A_INDEXED = new QName(NS_ANNOTATION, "indexed");
    public static final QName A_IGNORE = new QName(NS_ANNOTATION, "ignore");
    public static final QName A_PROCESSING = new QName(NS_ANNOTATION, "processing");
    public static final QName A_OPERATIONAL = new QName(NS_ANNOTATION, "operational");
    public static final QName A_EXTENSION = new QName(NS_ANNOTATION, "extension");
    public static final QName A_REF = new QName(NS_ANNOTATION, "ref");
    public static final QName A_OBJECT_REFERENCE = new QName(NS_ANNOTATION, "objectReference");
    public static final QName A_OBJECT_REFERENCE_TARGET_TYPE = new QName(NS_ANNOTATION, "objectReferenceTargetType");
    public static final QName A_COMPOSITE = new QName(NS_ANNOTATION, "composite");
    public static final QName A_EMBEDDED_OBJECT = new QName(NS_ANNOTATION, "embeddedObject");
    public static final QName A_DEPRECATED = new QName(NS_ANNOTATION, "deprecated");
    public static final QName A_DEPRECATED_SINCE = new QName(NS_ANNOTATION, "deprecatedSince");
    public static final QName A_REMOVED = new QName(NS_ANNOTATION, "removed");
    public static final QName A_REMOVED_SINCE = new QName(NS_ANNOTATION, "removedSince");
    public static final QName A_EXPERIMENTAL = new QName(NS_ANNOTATION, "experimental");
    public static final QName A_PLANNED_REMOVAL = new QName(NS_ANNOTATION, "plannedRemoval");
    public static final QName A_ELABORATE = new QName(NS_ANNOTATION, "elaborate");
    public static final QName A_LABEL = new QName(NS_ANNOTATION, "label");
    public static final QName A_MATCHING_RULE = new QName(NS_ANNOTATION, "matchingRule");
    public static final QName A_EMPHASIZED = new QName(NS_ANNOTATION, "emphasized");
    public static final QName A_DISPLAY_HINT = new QName(NS_ANNOTATION, "displayHint");
    public static final QName A_VALUE_ENUMERATION_REF = new QName(NS_ANNOTATION, "valueEnumerationRef");
    public static final QName A_HETEROGENEOUS_LIST_ITEM = new QName(NS_ANNOTATION, "heterogeneousListItem");
    public static final QName A_SCHEMA_MIGRATION = new QName(NS_ANNOTATION, "schemaMigration");
    public static final QName A_SCHEMA_MIGRATION_ELEMENT = new QName(NS_ANNOTATION, "element");
    public static final QName A_SCHEMA_MIGRATION_VERSION = new QName(NS_ANNOTATION, "version");
    public static final QName A_SCHEMA_MIGRATION_OPERATION = new QName(NS_ANNOTATION, "operation");
    public static final QName A_SCHEMA_MIGRATION_REPLACEMENT = new QName(NS_ANNOTATION, "replacement");
    public static final QName A_OPTIONAL_CLEANUP = new QName(NS_ANNOTATION, "optionalCleanup");
    public static final QName A_DEFAULT_PREFIX = new QName(NS_ANNOTATION, "defaultPrefix");
    public static final QName A_DIAGRAM = new QName(NS_ANNOTATION, "diagram");
    public static final QName A_DIAGRAM_NAME = new QName(NS_ANNOTATION, "name");
    public static final QName A_DIAGRAM_FORM = new QName(NS_ANNOTATION, "form");
    public static final QName A_DIAGRAM_INCLUSION = new QName(NS_ANNOTATION, "inclusion");
    public static final QName A_DIAGRAM_SUBITEM_INCLUSION = new QName(NS_ANNOTATION, "subitemInclusion");
    public static final QName A_MERGER = new QName(NS_ANNOTATION, "merger");
    public static final QName A_NATURAL_KEY = new QName(NS_ANNOTATION, "naturalKey");

    public static final QName SCHEMA_DOCUMENTATION = new QName(W3C_XML_SCHEMA_NS_URI, "documentation");
    public static final QName SCHEMA_ANNOTATION = new QName(W3C_XML_SCHEMA_NS_URI, "annotation");
    public static final QName SCHEMA_APP_INFO = new QName(W3C_XML_SCHEMA_NS_URI, "appinfo");

    public static final QName A_SEARCHABLE = new QName(NS_ANNOTATION, "searchable");
    public static final QName A_MAX_OCCURS = new QName(NS_ANNOTATION, "maxOccurs");
    public static final String MULTIPLICITY_UNBOUNDED = "unbounded";

    public static final QName A_ALWAYS_USE_FOR_EQUALS = new QName(NS_ANNOTATION, "alwaysUseForEquals");

    public static final QName A_NAMESPACE = new QName(NS_ANNOTATION, "namespace");
    public static final String A_NAMESPACE_PREFIX = "prefix";
    public static final String A_NAMESPACE_URL = "url";

    //Query constants
    public static final QName Q_OID = new QName(NS_QUERY, "oid");
    public static final QName Q_TYPE = new QName(NS_QUERY, "type");
    public static final QName Q_RELATION = new QName(NS_QUERY, "relation");
    public static final QName Q_VALUE = new QName(NS_QUERY, "value");
    public static final QName Q_ORDER_BY = new QName(NS_QUERY, "orderBy");
    public static final ItemName Q_ANY = ItemName.interned(NS_QUERY, "any");

    // Path constants
    public static final String T_PARENT_LOCAL_PART = "parent";
    public static final ItemName T_PARENT = ItemName.interned(NS_TYPES, T_PARENT_LOCAL_PART);
    public static final ItemName T_OBJECT_REFERENCE = ItemName.interned(NS_TYPES, "objectReference");
    public static final String T_ID_LOCAL_PART = "id";
    public static final ItemName T_ID = ItemName.interned(NS_TYPES, T_ID_LOCAL_PART);




    public static final InfraItemName I_ID = InfraItemName.of(NS_TYPES, T_ID_LOCAL_PART);
    public static final InfraItemName I_TYPE = InfraItemName.of(NS_TYPES, "type");

    public static final InfraItemName I_PATH = InfraItemName.of(NS_TYPES, "path");

    public static final InfraItemName I_METADATA = InfraItemName.of(NS_METADATA,"metadata").intern();

    public static final ItemName T_OBJECT_REFERENCE_OID = ItemName.interned(NS_TYPES, "oid");

    public static final ItemName T_OBJECT_REFERENCE_TYPE = ItemName.interned(NS_TYPES, "type");

    public static final ItemName T_OBJECT_REFERENCE_RELATION = ItemName.interned(NS_TYPES, "relation");

    public static final QName T_OBJECT_REFERENCE_DESCRIPTION = ItemName.interned(NS_TYPES,  "description");
    public static final QName T_OBJECT_REFERENCE_FILTER = ItemName.interned(NS_TYPES,  "filter");
    public static final QName T_OBJECT_REFERENCE_RESOLUTION_TIME = ItemName.interned(NS_TYPES,  "resolutionTime");
    public static final QName T_OBJECT_REFERENCE_REFERENTIAL_INTEGRITY = ItemName.interned(NS_TYPES,  "referentialIntegrity");
    public static final QName T_OBJECT_REFERENCE_TARGET_NAME = ItemName.interned(NS_TYPES,  "targetName");
    public static final QName T_OBJECT_REFERENCE_OBJECT = ItemName.interned(NS_TYPES,  "object");


    /**
     * Self is a bit special, because the item path for self (.) is technically an empty path.
     * So it's not a single-segment path with this QName in it and must be treated on some places.
     * Use {@link com.evolveum.midpoint.prism.path.ItemPath#SELF_PATH} where applicable instead of this constant.
     */
    public static final QName T_SELF = new QName(NS_TYPES, "");

    // Misc

    public static final Class DEFAULT_VALUE_CLASS = String.class;

    public static final QName POLYSTRING_TYPE_QNAME = new QName(NS_TYPES, "PolyStringType");
    public static final QName RAW_TYPE_QNAME = new QName(NS_TYPES, "RawType");
    public static final QName T_OBJECT_TYPE = new QName(NS_TYPES, "ObjectType");
    public static final QName T_RAW_OBJECT_TYPE = new QName(NS_TYPES, "RawObjectType");
    public static final QName POLYSTRING_ELEMENT_ORIG_QNAME = new QName(NS_TYPES, "orig");
    public static final QName POLYSTRING_ELEMENT_NORM_QNAME = new QName(NS_TYPES, "norm");

    // a bit of hack: by this local name we know if a object is a reference (c:ObjectReferenceType)
    public static final String REFERENCE_TYPE_NAME = "ObjectReferenceType";

    public static final String EXPRESSION_LOCAL_PART = "expression";

    /**
     * This is just an internal name for value metadata container.
     * It is _NOT_ used for serialization purposes.
     */
    @Experimental
    public static final QName VALUE_METADATA_CONTAINER_NAME = new QName(NS_METADATA, "valueMetadata");

    public static final QName VIRTUAL_SCHEMA_ROOT = new QName(NS_TYPES, "virtualSchemaRoot");
    public static final QName Q_FUZZY_STRING_MATCH = new QName(NS_QUERY, "fuzzyStringMatch");
    public static final QName Q_METHOD = new QName(NS_QUERY, "method");

}
