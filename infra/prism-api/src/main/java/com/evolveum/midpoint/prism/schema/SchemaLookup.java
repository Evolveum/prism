package com.evolveum.midpoint.prism.schema;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.schemaContext.SchemaContextDefinition;
import com.evolveum.midpoint.prism.schemaContext.resolver.SchemaContextResolver;

import javax.xml.namespace.QName;

public interface SchemaLookup extends SchemaRegistryState {

    SchemaContextResolver resolverFor (SchemaContextDefinition schemaContextDefinition);

    DefinitionFactory definitionFactory();

    interface Aware {
        default SchemaLookup schemaLookup() {
            return PrismContext.get().getDefaultSchemaLookup();
        }
    }

}
