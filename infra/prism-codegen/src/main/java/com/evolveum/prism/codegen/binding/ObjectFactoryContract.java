package com.evolveum.prism.codegen.binding;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import com.evolveum.midpoint.prism.ComplexTypeDefinition;
import com.evolveum.midpoint.prism.ItemDefinition;
import com.evolveum.midpoint.prism.schema.PrismSchema;

public class ObjectFactoryContract extends Contract {

    public static final String OBJECT_FACTORY = "ObjectFactory";

    public static final String NAMESPACE_CONST = "NAMESPACE";

    private SchemaBinding binding;
    private List<ItemBinding> items;
    private List<QName> types;

    public ObjectFactoryContract(SchemaBinding binding, PrismSchema schema) {
        super(binding.getPackageName());
        this.binding = binding;

        var itemDefinitions = schema.getDefinitions(ItemDefinition.class);
        this.items = new ArrayList<>(itemDefinitions.size());
        for (ItemDefinition def : itemDefinitions) {
            String name = StructuredContract.javaFromItemName(def.getItemName());
            items.add(new ItemBinding(name, def, false));
        }

        var typeDefinitions = schema.getComplexTypeDefinitions();
        this.types = new ArrayList<>();
        for(ComplexTypeDefinition type : typeDefinitions) {
            if (!type.isAbstract()) {
                types.add(type.getTypeName());
            }
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

    public List<QName> getTypes() {
        return types;
    }
}
