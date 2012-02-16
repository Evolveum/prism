/*
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
 *
 * Portions Copyrighted 2011 [name of copyright owner]
 */

package com.evolveum.midpoint.prism.schema;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.xml.namespace.QName;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.EntityResolver;

import com.evolveum.midpoint.prism.ComplexTypeDefinition;
import com.evolveum.midpoint.prism.Definition;
import com.evolveum.midpoint.prism.ItemDefinition;
import com.evolveum.midpoint.prism.Objectable;
import com.evolveum.midpoint.prism.PrismContainerDefinition;
import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.PrismObjectDefinition;
import com.evolveum.midpoint.prism.PrismPropertyDefinition;
import com.evolveum.midpoint.util.DebugDumpable;
import com.evolveum.midpoint.util.Dumpable;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;

/**
 * Schema as a collection of definitions. This is a midPoint-specific view of
 * schema definition. It is just a collection of definitions grouped under a
 * specific namespace.
 * 
 * The schema and all the public classes in this package define a schema
 * meta-model. It is supposed to be used for run-time schema interpretation. It
 * will not be a convenient tool to work with static data model objects such as
 * user or role. But it is needed for interpreting dynamic schemas for resource
 * objects, extensions and so on.
 * 
 * @author Radovan Semancik
 * 
 */
public class PrismSchema implements Dumpable, DebugDumpable, Serializable {

	private static final long serialVersionUID = 5068618465625931984L;

	private static final Trace LOGGER = TraceManager.getTrace(PrismSchema.class);
	
	protected String namespace;
	protected Set<Definition> definitions;
	protected PrismContext prismContext;

	public PrismSchema(String namespace, PrismContext prismContext) {
		if (StringUtils.isEmpty(namespace)) {
			throw new IllegalArgumentException("Namespace can't be null or empty.");
		}
		this.namespace = namespace;
		this.prismContext = prismContext;
		definitions = new HashSet<Definition>();
	}

	/**
	 * Returns schema namespace.
	 * 
	 * All schema definitions are placed in the returned namespace.
	 * 
	 * @return schema namespace
	 */
	public String getNamespace() {
		return namespace;
	}

	/**
	 * Returns set of definitions.
	 * 
	 * The set contains all definitions of all types that were parsed. Order of
	 * definitions is insignificant.
	 * 
	 * @return set of definitions
	 */
	public Collection<Definition> getDefinitions() {
		if (definitions == null) {
			definitions = new HashSet<Definition>();
		}
		return definitions;
	}

	public <T extends Definition> Collection<T> getDefinitions(Class<T> type) {
		Collection<T> defs = new HashSet<T>();
		for (Definition def: getDefinitions()) {
			if (type.isAssignableFrom(def.getClass())) {
				defs.add((T) def);
			}
		}
		return defs;
	}
	
	public static PrismSchema parse(Element element, PrismContext prismContext) throws SchemaException {
		return parse(element, null, prismContext);
	}
	
	public static PrismSchema parse(Element element, EntityResolver resolver, PrismContext prismContext) throws SchemaException {
		if (element == null) {
			throw new IllegalArgumentException("Schema DOM element must not be null.");
		}

		DomToSchemaProcessor processor = new DomToSchemaProcessor();
		processor.setEntityResolver(resolver);
		processor.setPrismContext(prismContext);
		return processor.parseDom(element);
	}

	public Document serializeToXsd() throws SchemaException {
		return serializeToXsd(this);
	}

	public static Document serializeToXsd(PrismSchema schema) throws SchemaException {
		if (schema == null) {
			throw new IllegalArgumentException("Schema can't be null.");
		}

		SchemaToDomProcessor processor = new SchemaToDomProcessor();
		return processor.parseSchema(schema);
	}

	// TODO: Methods for searching the schema, such as findDefinitionByName(),
	// etc.

	/**
	 * Finds a PropertyContainerDefinition by the type name.
	 * 
	 * @param typeName
	 *            property container type name
	 * @return found property container definition
	 * @throws IllegalStateException
	 *             if more than one definition is found
	 */
	public PrismContainerDefinition findContainerDefinitionByType(QName typeName) {
		return findContainerDefinitionByType(typeName,PrismContainerDefinition.class);
	}
	
	public PrismObjectDefinition findObjectDefinitionByType(QName typeName) {
		return findContainerDefinitionByType(typeName,PrismObjectDefinition.class);
	}
	
	public PrismObjectDefinition findObjectDefinitionByElementName(QName elementName) {
		return findContainerDefinitionByElementName(elementName, PrismObjectDefinition.class);
	}

	public <T extends Objectable> PrismObjectDefinition<T> findObjectDefinitionByType(QName typeName, Class<T> type) {
		return findContainerDefinitionByType(typeName,PrismObjectDefinition.class);
	}
	
	public <T extends Objectable> PrismObjectDefinition<T> findObjectDefinitionByClass(Class<T> type) {
		// TODO
		throw new UnsupportedOperationException();
	}

	private <T extends PrismContainerDefinition> T findContainerDefinitionByType(QName typeName, Class<T> type) {
		if (typeName == null) {
			throw new IllegalArgumentException("typeName must be supplied");
		}
		// TODO: check for multiple definition with the same type
		for (Definition definition : definitions) {
			if (type.isAssignableFrom(definition.getClass())
					&& typeName.equals(definition.getTypeName())) {
				return (T) definition;
			}
		}
		return null;
	}

	private <T extends PrismContainerDefinition> T findContainerDefinitionByElementName(QName elementName, Class<T> type) {
		if (elementName == null) {
			throw new IllegalArgumentException("elementName must be supplied");
		}
		// TODO: check for multiple definition with the same type
		for (Definition definition : definitions) {
			if (type.isAssignableFrom(definition.getClass())
					&& elementName.equals(definition.getDefaultName())) {
				return (T) definition;
			}
		}
		return null;
	}


	/**
	 * Finds complex type definition by type name.
	 */
	public ComplexTypeDefinition findComplexTypeDefinition(QName typeName) {
		if (typeName == null) {
			throw new IllegalArgumentException("typeName must be supplied");
		}
		// TODO: check for multiple definition with the same type
		for (Definition definition : definitions) {
			if (definition instanceof ComplexTypeDefinition && typeName.equals(definition.getTypeName())) {
				return (ComplexTypeDefinition) definition;
			}
		}
		return null;
	}

	/**
	 * Finds item definition by name.
	 * 
	 */
	@SuppressWarnings("unchecked")
	public <T extends ItemDefinition> T findItemDefinition(QName definitionName, Class<T> definitionType) {
		if (definitionName == null) {
			throw new IllegalArgumentException("definitionName must be supplied");
		}
		// TODO: check for multiple definition with the same type
		for (Definition definition : definitions) {
			if (definitionType.isAssignableFrom(definition.getClass())) {
				ItemDefinition idef = (ItemDefinition) definition;
				if (definitionName.equals(idef.getName())) {
					return (T) idef;
				}
			}
		}
		return null;
	}

	/**
	 * Finds item definition by local name
	 */
	public <T extends ItemDefinition> T findItemDefinition(String localName, Class<T> definitionType) {
		if (localName == null) {
			throw new IllegalArgumentException("localName must be supplied");
		}
		// TODO: check for multiple definition with the same type
		for (Definition definition : definitions) {
			if (definitionType.isAssignableFrom(definition.getClass())) {
				ItemDefinition idef = (ItemDefinition) definition;
				if (localName.equals(idef.getName().getLocalPart())) {
					return (T) idef;
				}
			}
		}
		return null;
	}

	/**
	 * Finds item definition by type.
	 * 
	 */
	@SuppressWarnings("unchecked")
	public <T extends ItemDefinition> T findItemDefinitionByType(QName typeName, Class<T> definitionType) {
		if (typeName == null) {
			throw new IllegalArgumentException("typeName must be supplied");
		}
		// TODO: check for multiple definition with the same type
		for (Definition definition : definitions) {
			if (definitionType.isAssignableFrom(definition.getClass())) {
				ItemDefinition idef = (ItemDefinition) definition;
				if (typeName.equals(idef.getTypeName())) {
					return (T) idef;
				}
			}
		}
		return null;
	}

	public boolean isEmpty() {
		return definitions.isEmpty();
	}

	/**
	 * Creates a new property container definition and adds it to the schema.
	 * 
	 * This is a preferred way how to create definition in the schema.
	 * 
	 * @param localTypeName
	 *            type name "relative" to schema namespace
	 * @return new property container definition
	 */
	public PrismContainerDefinition createPropertyContainerDefinition(String localTypeName) {
		QName typeName = new QName(getNamespace(), localTypeName);
		QName name = new QName(getNamespace(), toElementName(localTypeName));
		ComplexTypeDefinition cTypeDef = new ComplexTypeDefinition(name, typeName, prismContext);
		PrismContainerDefinition def = new PrismContainerDefinition(name, cTypeDef, prismContext);
		definitions.add(cTypeDef);
		definitions.add(def);
		return def;
	}

	/**
	 * Creates a top-level property definition and adds it to the schema.
	 * 
	 * This is a preferred way how to create definition in the schema.
	 * 
	 * @param localName
	 *            element name "relative" to schema namespace
	 * @param typeName
	 *            XSD type name of the element
	 * @return new property definition
	 */
	public PrismPropertyDefinition createPropertyDefinition(String localName, QName typeName) {
		QName name = new QName(getNamespace(), localName);
		return createPropertyDefinition(name, typeName);
	}

	/**
	 * Creates a top-level property definition and adds it to the schema.
	 * 
	 * This is a preferred way how to create definition in the schema.
	 * 
	 * @param localName
	 *            element name "relative" to schema namespace
	 * @param localTypeName
	 *            XSD type name "relative" to schema namespace
	 * @return new property definition
	 */
	public PrismPropertyDefinition createPropertyDefinition(String localName, String localTypeName) {
		QName name = new QName(getNamespace(), localName);
		QName typeName = new QName(getNamespace(), localTypeName);
		return createPropertyDefinition(name, typeName);
	}

	/**
	 * Creates a top-level property definition and adds it to the schema.
	 * 
	 * This is a preferred way how to create definition in the schema.
	 * 
	 * @param localName
	 *            element name
	 * @param typeName
	 *            XSD type name of the element
	 * @return new property definition
	 */
	public PrismPropertyDefinition createPropertyDefinition(QName name, QName typeName) {
		PrismPropertyDefinition def = new PrismPropertyDefinition(name, name, typeName, prismContext);
		definitions.add(def);
		return def;
	}

	/**
	 * Internal method to create a "nice" element name from the type name.
	 */
	String toElementName(String localTypeName) {
		String elementName = StringUtils.uncapitalize(localTypeName);
		if (elementName.endsWith("Type")) {
			return elementName.substring(0, elementName.length() - 4);
		}
		return elementName;
	}
	
	@Override
	public String debugDump() {
		return debugDump(0);
	}

	@Override
	public String debugDump(int indent) {
		StringBuilder sb = new StringBuilder();
		for (int i=0;i<indent;i++) {
			sb.append(INDENT_STRING);
		}
		sb.append(toString()).append("\n");
		Iterator<Definition> i = definitions.iterator();
		while (i.hasNext()) {
			Definition def = i.next();
			sb.append(def.debugDump(indent+1));
			if (i.hasNext()) {
				sb.append("\n");
			}
		}
		return sb.toString();
	}

	@Override
	public String dump() {
		return debugDump(0);
	}

	@Override
	public String toString() {
		return "Schema(ns=" + namespace + ")";
	}
	
}
