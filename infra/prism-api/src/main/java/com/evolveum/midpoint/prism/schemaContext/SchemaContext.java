package com.evolveum.midpoint.prism.schemaContext;

import java.io.Serializable;

import com.evolveum.midpoint.prism.ItemDefinition;

public interface SchemaContext extends Serializable {

    ItemDefinition<?> getItemDefinition();
}
