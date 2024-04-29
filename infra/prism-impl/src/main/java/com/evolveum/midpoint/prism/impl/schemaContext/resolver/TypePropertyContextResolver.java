package com.evolveum.midpoint.prism.impl.schemaContext.resolver;

import com.evolveum.midpoint.prism.PrismValue;
import com.evolveum.midpoint.prism.impl.schemaContext.ContextResolverFactory;
import com.evolveum.midpoint.prism.schema.SchemaContextDefinition;
//import com.evolveum.midpoint.prism.impl.schemaContext.SchemaContext;

import javax.xml.namespace.QName;

/**
 * Created by Dominik.
 */
public class TypePropertyContextResolver implements SchemaContextResolver {

    @Override
    public SchemaContextDefinition computeContext(PrismValue prismValue) {
        // return object type from xml
        return null;
    }

    static class Factory implements ContextResolverFactory {

        @Override
        public QName getAlgorithmName() {
            return null;
        }

//        @Override
//        public SchemaContextResolver createResolver(SchemaContext schemaContext) {
//            return null;
//        }
    }
}
