package com.evolveum.midpoint.prism.impl;

import java.io.Serializable;

import javax.xml.namespace.QName;

import org.jetbrains.annotations.NotNull;

import com.evolveum.midpoint.prism.ComplexTypeDefinition;
import com.evolveum.midpoint.prism.PrismConstants;
import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.path.ItemName;
import com.evolveum.midpoint.prism.schema.SchemaRegistry;

public abstract class SerializationProxy implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    protected Object readResolve() {
        return resolve(PrismContext.get().getSchemaRegistry());
    }

    protected abstract Object resolve(SchemaRegistry registry);

    static class TypeDef extends SerializationProxy {

        /**
         *
         */
        private static final long serialVersionUID = 1L;
        private final QName typeName;



        public TypeDef(QName typeName) {
            this.typeName = typeName;
        }

        @Override
        protected Object resolve(SchemaRegistry registry) {
            // Is type name enough?
            return registry.findTypeDefinitionByType(typeName);
        }

    }

    static class ItemDef extends SerializationProxy {

        /**
         *
         */
        private static final long serialVersionUID = 1L;
        private final QName complexType;
        private final QName itemName;

        public ItemDef(QName complexType, QName itemName) {
            this.complexType = complexType;
            this.itemName = itemName;
        }

        @Override
        protected Object resolve(SchemaRegistry registry) {
            if (PrismConstants.VIRTUAL_SCHEMA_ROOT.equals(complexType)) {
                // Top level item definition
                return registry.findItemDefinitionByElementName(itemName);
            }
            // Item definition from container
            ComplexTypeDefinition typeDef = registry.findComplexTypeDefinitionByType(complexType);
            if (typeDef == null) {
                throw new NullPointerException("No complex type definition for " + typeDef);
            }
            return typeDef.findLocalItemDefinition(itemName);
        }

    }

    public static SerializationProxy forTypeDef(@NotNull QName typeName) {
        return new TypeDef(typeName);
    }

    public static SerializationProxy forItemDef(QName definedInType, @NotNull ItemName itemName) {
        return new ItemDef(definedInType, itemName);
    }
}
