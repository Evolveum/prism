/*
 * Copyright (c) 2010-2018 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0 
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.xml.namespace.QName;
import java.util.Collection;

/**
 * @author mederly
 */
public interface TypeDefinition extends Definition {

	/**
	 * Returns compile-time class, if this type has any. For example, UserType.class, ObjectType.class, ExtensionType.class.
	 */
	@Nullable
	Class<?> getCompileTimeClass();

	/**
	 * Name of super type of this complex type definition. E.g. c:ObjectType is a super type for
	 * c:FocusType which is a super type for c:UserType. Or (more complex example) ri:ShadowAttributesType
	 * is a super type of ri:AccountObjectClass. (TODO is this really true?)
	 */
	@Nullable
	QName getSuperType();

	/**
	 * Subtypes - but only these that are a part of the static schema. A little bit experimental. :)
	 */
	@NotNull
	Collection<TypeDefinition> getStaticSubTypes();

	Integer getInstantiationOrder();
	
	boolean canRepresent(QName typeName);
}
