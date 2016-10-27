/*
 * Copyright (c) 2010-2014 Evolveum
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.evolveum.midpoint.prism.schema;

import java.util.*;
import java.util.stream.Collectors;

import javax.xml.namespace.QName;

import com.evolveum.midpoint.prism.*;

import com.evolveum.midpoint.prism.xml.XsdTypeMapper;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.EntityResolver;

import com.evolveum.midpoint.util.QNameUtil;
import com.evolveum.midpoint.util.exception.SchemaException;

/**
 *
 * @author Radovan Semancik
 * 
 */
public class PrismSchemaImpl implements PrismSchema {

	//private static final long serialVersionUID = 5068618465625931984L;

	//private static final Trace LOGGER = TraceManager.getTrace(PrismSchema.class);
	
	@NotNull protected final Collection<Definition> definitions = new ArrayList<>();
	protected String namespace;
	protected PrismContext prismContext;

	protected PrismSchemaImpl(PrismContext prismContext) {
		this.prismContext = prismContext;
	}
	
	public PrismSchemaImpl(String namespace, PrismContext prismContext) {
		if (StringUtils.isEmpty(namespace)) {
			throw new IllegalArgumentException("Namespace can't be null or empty.");
		}
		this.namespace = namespace;
		this.prismContext = prismContext;
	}

	//region Trivia
	@Override
	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	@NotNull
	@Override
	public Collection<Definition> getDefinitions() {
		return definitions;
	}

	@SuppressWarnings("unchecked")
	@NotNull
	@Override
	public <T extends Definition> List<T> getDefinitions(@NotNull Class<T> type) {
		return definitions.stream()
				.filter(def -> type.isAssignableFrom(def.getClass()))
				.map(def -> (T) def)
				.collect(Collectors.toList());
	}

	@Override
	public boolean isEmpty() {
		return definitions.isEmpty();
	}

	public void add(@NotNull Definition def) {
		definitions.add(def);
	}
	
	@Override
	public PrismContext getPrismContext() {
		return prismContext;
	}
	//endregion

	//region XSD parsing and serialization
	// TODO: cleanup this chaos
	public static PrismSchema parse(Element element, boolean isRuntime, String shortDescription, PrismContext prismContext) throws SchemaException {
		return parse(element, prismContext.getEntityResolver(), new PrismSchemaImpl(prismContext), isRuntime, shortDescription, prismContext);
	}
	
	public static PrismSchema parse(Element element, EntityResolver resolver, boolean isRuntime, String shortDescription, PrismContext prismContext) throws SchemaException {
		return parse(element, resolver, new PrismSchemaImpl(prismContext), isRuntime, shortDescription, prismContext);
	}
	
	protected static PrismSchema parse(Element element, PrismSchemaImpl schema, boolean isRuntime, String shortDescription, PrismContext prismContext) throws SchemaException {
		return parse(element, prismContext.getEntityResolver(), schema, isRuntime, shortDescription, prismContext);
	}
	
	protected static PrismSchema parse(Element element, EntityResolver resolver, PrismSchemaImpl schema, boolean isRuntime, String shortDescription, PrismContext prismContext) throws SchemaException {
		if (element == null) {
			throw new IllegalArgumentException("Schema element must not be null in "+shortDescription);
		}

		DomToSchemaProcessor processor = new DomToSchemaProcessor();
		processor.setEntityResolver(resolver);
		processor.setPrismContext(prismContext);
		processor.setShortDescription(shortDescription);
		processor.setRuntime(isRuntime);
		processor.parseDom(schema, element);
		return schema;
	}

	@NotNull
	@Override
	public Document serializeToXsd() throws SchemaException {
		SchemaToDomProcessor processor = new SchemaToDomProcessor();
		processor.setPrismContext(prismContext);
		return processor.parseSchema(this);
	}
	//endregion

	//region Creating definitions
	/**
	 * Creates a new property container definition and adds it to the schema.
	 * 
	 * This is a preferred way how to create definition in the schema.
	 * 
	 * @param localTypeName
	 *            type name "relative" to schema namespace
	 * @return new property container definition
	 */
	public PrismContainerDefinitionImpl createPropertyContainerDefinition(String localTypeName) {
		QName typeName = new QName(getNamespace(), localTypeName);
		QName name = new QName(getNamespace(), toElementName(localTypeName));
		ComplexTypeDefinition cTypeDef = new ComplexTypeDefinitionImpl(typeName, prismContext);
		PrismContainerDefinitionImpl def = new PrismContainerDefinitionImpl(name, cTypeDef, prismContext);
		definitions.add(cTypeDef);
		definitions.add(def);
		return def;
	}
	
	public PrismContainerDefinitionImpl createPropertyContainerDefinition(String localElementName, String localTypeName) {
		QName typeName = new QName(getNamespace(), localTypeName);
		QName name = new QName(getNamespace(), localElementName);
		ComplexTypeDefinition cTypeDef = findComplexTypeDefinitionByType(typeName);
		if (cTypeDef == null) {
			cTypeDef = new ComplexTypeDefinitionImpl(typeName, prismContext);
			definitions.add(cTypeDef);
		}
		PrismContainerDefinitionImpl def = new PrismContainerDefinitionImpl(name, cTypeDef, prismContext);
		definitions.add(def);
		return def;
	}
	
	public ComplexTypeDefinition createComplexTypeDefinition(QName typeName) {
		ComplexTypeDefinition cTypeDef = new ComplexTypeDefinitionImpl(typeName, prismContext);
		definitions.add(cTypeDef);
		return cTypeDef;
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

	/*
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
//	public PrismPropertyDefinition createPropertyDefinition(String localName, String localTypeName) {
//		QName name = new QName(getNamespace(), localName);
//		QName typeName = new QName(getNamespace(), localTypeName);
//		return createPropertyDefinition(name, typeName);
//	}

	/**
	 * Creates a top-level property definition and adds it to the schema.
	 * 
	 * This is a preferred way how to create definition in the schema.
	 * 
	 * @param name
	 *            element name
	 * @param typeName
	 *            XSD type name of the element
	 * @return new property definition
	 */
	public PrismPropertyDefinition createPropertyDefinition(QName name, QName typeName) {
		PrismPropertyDefinition def = new PrismPropertyDefinitionImpl(name, typeName, prismContext);
		definitions.add(def);
		return def;
	}

	/**
	 * Internal method to create a "nice" element name from the type name.
	 */
	private String toElementName(String localTypeName) {
		String elementName = StringUtils.uncapitalize(localTypeName);
		if (elementName.endsWith("Type")) {
			return elementName.substring(0, elementName.length() - 4);
		}
		return elementName;
	}
	//endregion

	//region Pretty printing
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
	public String toString() {
		return "Schema(ns=" + namespace + ")";
	}
	//endregion

	//region Implementation of DefinitionsStore methods (commented out)
//	private final SearchContexts searchContexts = new SearchContexts(this);			// pre-existing object to avoid creating them each time
//
//	@Override
//	public GlobalDefinitionSearchContext<PrismObjectDefinition<? extends Objectable>> findObjectDefinition() {
//		return searchContexts.pod;
//	}
//
//	@Override
//	public GlobalDefinitionSearchContext<PrismContainerDefinition<? extends Containerable>> findContainerDefinition() {
//		return searchContexts.pcd;
//	}
//
//	@Override
//	public GlobalDefinitionSearchContext<ItemDefinition<?>> findItemDefinition() {
//		return searchContexts.id;
//	}
//
//	@Override
//	public GlobalDefinitionSearchContext<ComplexTypeDefinition> findComplexTypeDefinition() {
//		return searchContexts.ctd;
//	}
//
//	@Override
//	public GlobalDefinitionSearchContext<PrismPropertyDefinition> findPropertyDefinition() {
//		return searchContexts.ppd;
//	}
//
//	@Override
//	public GlobalDefinitionSearchContext<PrismReferenceDefinition> findReferenceDefinition() {
//		return searchContexts.prd;
//	}
	//endregion

	//region Finding definitions

	// items

	@NotNull
	public <ID extends ItemDefinition> List<ID> findItemDefinitionsByCompileTimeClass(
			@NotNull Class<?> compileTimeClass, @NotNull Class<ID> definitionClass) {
		List<ID> found = new ArrayList<>();
		for (Definition def: definitions) {
			if (definitionClass.isAssignableFrom(def.getClass())) {
				if (def instanceof PrismContainerDefinition) {
					@SuppressWarnings("unchecked")
					ID contDef = (ID) def;
					if (compileTimeClass.equals(((PrismContainerDefinition) contDef).getCompileTimeClass())) {
						found.add(contDef);
					}
				} else if (def instanceof PrismPropertyDefinition) {
					if (compileTimeClass.equals(prismContext.getSchemaRegistry().determineClassForType(def.getTypeName()))) {
						@SuppressWarnings("unchecked")
						ID itemDef = (ID) def;
						found.add(itemDef);
					}
				} else {
					// skipping the definition (PRD is not supported yet)
				}
			}
		}
		return found;
	}

	@Override
	public <ID extends ItemDefinition> ID findItemDefinitionByType(@NotNull QName typeName, @NotNull Class<ID> definitionClass) {
		// TODO: check for multiple definition with the same type
		for (Definition definition : definitions) {
			if (definitionClass.isAssignableFrom(definition.getClass())) {
				@SuppressWarnings("unchecked")
				ID itemDef = (ID) definition;
				if (QNameUtil.match(typeName, itemDef.getTypeName())) {
					return itemDef;
				}
			}
		}
		return null;
	}

	@NotNull
	@Override
	public <ID extends ItemDefinition> List<ID> findItemDefinitionsByElementName(@NotNull QName elementName,
			@NotNull Class<ID> definitionClass) {
		List<ID> rv = new ArrayList<ID>();
		for (Definition definition : definitions) {
			if (definitionClass.isAssignableFrom(definition.getClass())) {
				@SuppressWarnings("unchecked")
				ID itemDef = (ID) definition;
				if (QNameUtil.match(elementName, itemDef.getName())) {
					rv.add(itemDef);
				}
			}
		}
		return rv;
	}


	//	private Map<Class<? extends Objectable>, PrismObjectDefinition> classToDefCache = Collections.synchronizedMap(new HashMap<>());
//	@Override
//	public <O extends Objectable> PrismObjectDefinition<O> findObjectDefinitionByCompileTimeClass(@NotNull Class<O> type) {
//		if (classToDefCache.containsKey(type)) {		// there may be null values
//			return classToDefCache.get(type);
//		}
//		PrismObjectDefinition<O> definition = scanForPrismObjectDefinition(type);
//		classToDefCache.put(type, definition);
//		return definition;
//	}
//	private <T extends Objectable> PrismObjectDefinition<T> scanForPrismObjectDefinition(Class<T> type) {
//		for (Definition def: getDefinitions()) {
//			if (def instanceof PrismObjectDefinition<?>) {
//				PrismObjectDefinition<?> objDef = (PrismObjectDefinition<?>)def;
//				if (type.equals(objDef.getCompileTimeClass())) {
//					classToDefCache.put(type, objDef);
//					return (PrismObjectDefinition<T>) objDef;
//				}
//			}
//		}
//		return null;
//	}



	@Override
	public <C extends Containerable> ComplexTypeDefinition findComplexTypeDefinitionByCompileTimeClass(@NotNull Class<C> compileTimeClass) {
		for (Definition def: definitions) {
			if (def instanceof ComplexTypeDefinition) {
				ComplexTypeDefinition ctd = (ComplexTypeDefinition) def;
				if (compileTimeClass.equals(ctd.getCompileTimeClass())) {
					return ctd;
				}
			}
		}
		return null;
	}

	@Nullable
	@Override
	public <TD extends TypeDefinition> TD findTypeDefinitionByType(@NotNull QName typeName, @NotNull Class<TD> definitionClass) {
		// TODO: check for multiple definition with the same type
		for (Definition definition : definitions) {
			if (definitionClass.isAssignableFrom(definition.getClass()) && QNameUtil.match(typeName, definition.getTypeName())) {
				return (TD) definition;
			}
		}
		return null;
	}

	@Nullable
	@Override
	public <TD extends TypeDefinition> TD findTypeDefinitionByCompileTimeClass(@NotNull Class<?> compileTimeClass, @NotNull Class<TD> definitionClass) {
		// TODO: check for multiple definition with the same type
		for (Definition definition : definitions) {
			if (definitionClass.isAssignableFrom(definition.getClass()) && compileTimeClass.equals(((TD) definition).getCompileTimeClass())) {
				return (TD) definition;
			}
		}
		return null;
	}


	//endregion
}
