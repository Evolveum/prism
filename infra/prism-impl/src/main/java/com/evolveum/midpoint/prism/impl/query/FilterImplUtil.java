/*
 * Copyright (c) 2010-2018 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.prism.impl.query;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.path.ItemPath;

public class FilterImplUtil {

    public static ItemDefinition findItemDefinition(ItemPath itemPath, PrismContainerDefinition<? extends Containerable> containerDef) {
        ItemDefinition itemDef = containerDef.findItemDefinition(itemPath);
        if (itemDef == null) {
            throw new IllegalStateException("No definition for item " + itemPath + " in container definition "
                    + containerDef);
        }

        return itemDef;
    }

    public static ItemDefinition findItemDefinition(ItemPath parentPath, ComplexTypeDefinition complexTypeDefinition) {
        ItemDefinition itemDef = complexTypeDefinition.findItemDefinition(parentPath);
        if (itemDef == null) {
            throw new IllegalStateException("No definition for item " + parentPath + " in complex type definition "
                    + complexTypeDefinition);
        }
        return itemDef;
    }

    public static ItemDefinition findItemDefinition(ItemPath parentPath, Class type) {
        ComplexTypeDefinition complexTypeDefinition = PrismContext.get().getSchemaRegistry().findComplexTypeDefinitionByCompileTimeClass(type);
        if (complexTypeDefinition == null) {
            // TODO SchemaException instead?
            throw new IllegalStateException("Definition of complex type " + type + " couldn't be not found");
        }
        return findItemDefinition(parentPath, complexTypeDefinition);
    }

}
