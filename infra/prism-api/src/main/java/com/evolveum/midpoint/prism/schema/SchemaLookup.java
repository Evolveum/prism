/*
 * Copyright (c) 2010-2025 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.prism.schema;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.schemaContext.SchemaContextDefinition;
import com.evolveum.midpoint.prism.schemaContext.resolver.SchemaContextResolver;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.xml.namespace.QName;
import java.util.function.Function;

public interface SchemaLookup extends SchemaRegistryState {

    SchemaContextResolver resolverFor (SchemaContextDefinition schemaContextDefinition);

    DefinitionFactory definitionFactory();

    /**
     * Returns schema-specific service
     * @param type
     * @return Requested service or null, if service is not available.
     * @param <T> Type of schema specific service
     */
    @Nullable
    <T extends Based> T schemaSpecific(@NotNull  Class<T> type);


    interface Aware {
        default SchemaLookup schemaLookup() {
            return PrismContext.get().getDefaultSchemaLookup();
        }
    }

    interface Mutable extends SchemaLookup {

        <T extends Based> void registerSchemaSpecific(Class<T> serviceClass, Function<SchemaLookup,T> factory);

    }

    interface Based {

    }
}
