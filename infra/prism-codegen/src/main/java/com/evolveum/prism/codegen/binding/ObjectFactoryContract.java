package com.evolveum.prism.codegen.binding;

import java.util.ArrayList;
import java.util.List;

import com.evolveum.midpoint.prism.ItemDefinition;
import com.evolveum.midpoint.prism.schema.PrismSchema;

public class ObjectFactoryContract extends Contract {

    private SchemaBinding binding;
    private List<ItemBinding> items;

    public ObjectFactoryContract(SchemaBinding binding, PrismSchema schema) {
        super(binding.getPackageName());
        this.binding = binding;

        var itemDefinitions = schema.getDefinitions(ItemDefinition.class);
        this.items = new ArrayList<>(itemDefinitions.size());
        for (ItemDefinition def : itemDefinitions) {
            String name = StructuredContract.javaFromItemName(def.getItemName());
            items.add(new ItemBinding(name, def));
        }
    }

    @Override
    public String fullyQualifiedName() {
        return packageName + "." + SchemaBinding.OBJECT_FACTORY;
    }


    public List<ItemBinding> getItemNameToType() {
        return items;
    }

    public String getNamespace() {
        return binding.getNamespaceURI();
    }
}
