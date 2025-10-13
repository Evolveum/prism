/*
 * Copyright (c) 2010-2025 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.prism;


public interface ItemDefinitionTransformer {

    <I extends ItemDefinition<?>> I transformItem(ComplexTypeDefinition parentDef, I currentDef);

    <T extends TypeDefinition> T applyValue(ComplexTypeDefinition parentDef, ItemDefinition<?> itemDef, T valueDef);


    public interface TransformableItem {

        void transformDefinition(ComplexTypeDefinition parentDef, ItemDefinitionTransformer transformer);

    }

    public interface TransformableValue {

        void transformDefinition(ComplexTypeDefinition parentDef, ItemDefinition<?> itemDef, ItemDefinitionTransformer transformation);
    }

}
