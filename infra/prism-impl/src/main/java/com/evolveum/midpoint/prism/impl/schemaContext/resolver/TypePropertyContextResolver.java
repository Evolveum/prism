package com.evolveum.midpoint.prism.impl.schemaContext.resolver;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.impl.schemaContext.ContextResolverFactory;
import com.evolveum.midpoint.prism.impl.schemaContext.SchemaContextImpl;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.prism.schemaContext.SchemaContext;
import com.evolveum.midpoint.prism.schemaContext.SchemaContextDefinition;

import javax.xml.namespace.QName;

/**
 * Created by Dominik.
 */
public class TypePropertyContextResolver implements SchemaContextResolver {

    SchemaContextDefinition schemaContextDefinition;

    public TypePropertyContextResolver(SchemaContextDefinition schemaContextDefinition) {
        this.schemaContextDefinition = schemaContextDefinition;
    }

    @Override
    public SchemaContext computeContext(PrismValue prismValue) {
        if (prismValue instanceof PrismContainerValue<?> container) {
            var typeProp = container.findItem(ItemPath.create(schemaContextDefinition.getTypePath()), PrismProperty.class);

            if (typeProp != null) {
                if (typeProp.getAnyValue() != null) {
                    if (typeProp.getAnyValue().getRealValue() instanceof QName typeName) {
                        var objectDefinition = PrismContext.get().getSchemaRegistry().findObjectDefinitionByType(typeName);
                        return new SchemaContextImpl(objectDefinition);
                    }
                }
            }
        }

        return null;
    }
}
