package com.evolveum.midpoint.prism.schemaContext;

import java.io.Serializable;

import com.evolveum.midpoint.prism.ItemDefinition;

/**
 * The interface represents the schema context annotation, which provides semantic information about object.
 */
public interface SchemaContext extends Serializable {

    ItemDefinition<?> getItemDefinition();

}
