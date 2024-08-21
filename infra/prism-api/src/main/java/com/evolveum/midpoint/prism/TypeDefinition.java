/*
 * Copyright (c) 2010-2018 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism;

import com.evolveum.midpoint.prism.schema.SchemaLookup;
import com.evolveum.midpoint.prism.schema.SchemaRegistry;
import com.evolveum.midpoint.util.DOMUtil;
import com.evolveum.midpoint.util.QNameUtil;
import com.evolveum.midpoint.util.annotation.Experimental;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.xml.namespace.QName;
import java.util.Collection;

/**
 * Definition of a type (as opposed to definition of an item).
 *
 * @see ItemDefinition
 */
public interface TypeDefinition extends Definition, AbstractTypeDefinition, SchemaLookup.Aware {

    /**
     * Returns compile-time class, if this type has any. For example, UserType.class, ObjectType.class, ExtensionType.class.
     */
    @Nullable
    Class<?> getCompileTimeClass();

    /**
     * Name of super type of this complex type definition.
     *
     * For example, `c:ObjectType` is a super type for `c:FocusType` which is a super type for `c:UserType`.
     *
     * Extension types have `c:ExtensionType` as their supertype, if no explicit supertype is specified in XSD.
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

    /**
     * @return True if variables of this type can be assigned value of specified other type, i.e. if
     * this type is equal or supertype of the other type.
     */
    @Experimental
    default boolean isAssignableFrom(TypeDefinition other, SchemaRegistry registry) {
        if (QNameUtil.match(this.getTypeName(), DOMUtil.XSD_ANYTYPE)) {
            return true;
        }
        while (other != null) {
            if (QNameUtil.match(this.getTypeName(), other.getTypeName())) {
                return true;
            }
            if (other.getSuperType() == null) {
                return false;
            }
            other = PrismContext.get().getSchemaRegistry().findTypeDefinitionByType(other.getSuperType());
        }
        return false;
    }

    interface TypeDefinitionMutator extends DefinitionMutator {
    }

    interface TypeDefinitionLikeBuilder
            extends
            PrismPresentationDefinition.Mutable,
            DefinitionBuilder {

        void setInstantiationOrder(Integer value);
        void setSuperType(QName value);
    }
}
