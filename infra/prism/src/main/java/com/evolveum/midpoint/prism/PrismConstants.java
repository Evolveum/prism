/**
 * Copyright (c) 2011 Evolveum
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * http://www.opensource.org/licenses/cddl1 or
 * CDDLv1.0.txt file in the source code distribution.
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * Portions Copyrighted 2011 [name of copyright owner]
 */
package com.evolveum.midpoint.prism;

import javax.xml.namespace.QName;


/**
 * @author semancik
 *
 */
public class PrismConstants {

	public static final String EXTENSION_LOCAL_NAME = "extension";
	public static final String NAME_LOCAL_NAME = "name";
	
	public static final String ATTRIBUTE_ID_LOCAL_NAME = "id";
	public static final String ATTRIBUTE_OID_LOCAL_NAME = "oid";
	public static final String ATTRIBUTE_VERSION_LOCAL_NAME = "version";
	public static final String ATTRIBUTE_REF_TYPE_LOCAL_NAME = "type";
	public static final String ATTRIBUTE_RELATION_LOCAL_NAME = "relation";
	
	public static final String ELEMENT_DESCRIPTION_LOCAL_NAME = "description";
	public static final String ELEMENT_FILTER_LOCAL_NAME = "filter";
	
	public static final String NS_PREFIX = "http://prism.evolveum.com/xml/ns/public/";
	public static final String NS_ANNOTATION = NS_PREFIX + "annotation-2";
	public static final String PREFIX_NS_ANNOTATION = "a";
	public static final String NS_TYPES = NS_PREFIX + "types-2";
	public static final String PREFIX_NS_TYPES = "t";
	public static final String NS_QUERY = NS_PREFIX + "query-2";
	public static final String PREFIX_NS_QUERY = "q";

	// Annotations

	public static final QName A_PROPERTY_CONTAINER = new QName(NS_ANNOTATION, "container");
	public static final QName A_OBJECT = new QName(NS_ANNOTATION, "object");

	
	public static final QName A_TYPE = new QName(NS_ANNOTATION, "type");
	public static final QName A_DISPLAY_NAME = new QName(NS_ANNOTATION, "displayName");
	public static final QName A_DISPLAY_ORDER = new QName(NS_ANNOTATION, "displayOrder");
	public static final QName A_HELP = new QName(NS_ANNOTATION, "help");	
	public static final QName A_ACCESS = new QName(NS_ANNOTATION, "access");
	public static final String A_ACCESS_CREATE = "create";
	public static final String A_ACCESS_UPDATE = "update";
	public static final String A_ACCESS_READ = "read";
	public static final QName A_INDEXED = new QName(NS_ANNOTATION, "indexed");
	public static final QName A_IGNORE = new QName(NS_ANNOTATION, "ignore");
	public static final QName A_EXTENSION = new QName(NS_ANNOTATION, "extension");
	public static final QName A_EXTENSION_REF = new QName(NS_ANNOTATION, "ref");
	public static final QName A_OBJECT_REFERENCE = new QName(NS_ANNOTATION, "objectReference");
	public static final QName A_OBJECT_REFERENCE_TARGET_TYPE = new QName(NS_ANNOTATION, "objectReferenceTargetType");
	
	public static final QName A_MAX_OCCURS = new QName(NS_ANNOTATION, "maxOccurs");
	public static final String MULTIPLICITY_UNBONUNDED = "unbounded";
	
	public static final QName A_NAMESPACE = new QName(NS_ANNOTATION, "namespace");
	public static final String A_NAMESPACE_PREFIX = "prefix";
	public static final String A_NAMESPACE_URL = "url";
	
	// Misc
	
	public static final Class DEFAULT_VALUE_CLASS = String.class;
	
	public static final QName POLYSTRING_TYPE_QNAME = new QName(NS_TYPES, "PolyStringType");
	public static final QName POLYSTRING_ELEMENT_ORIG_QNAME = new QName(NS_TYPES, "orig");
	public static final QName POLYSTRING_ELEMENT_NORM_QNAME = new QName(NS_TYPES, "norm");
		
}
