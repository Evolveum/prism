package com.evolveum.prism.codegen.binding;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.jetbrains.annotations.Nullable;

import com.evolveum.midpoint.prism.ComplexTypeDefinition;
import com.evolveum.midpoint.prism.ItemDefinition;
import com.evolveum.midpoint.prism.schema.PrismSchema;

public class ObjectFactoryContract extends Contract {

    public static final String OBJECT_FACTORY = "ObjectFactory";

    public static final String NAMESPACE_CONST = "NAMESPACE";

    private SchemaBinding binding;
    private List<ItemBinding> items;
    private List<QName> types;

    private String prefix;
    private String name;

    public ObjectFactoryContract(SchemaBinding binding, PrismSchema schema, @Nullable NamespaceConstantMapping constantMapping) {
        super(binding.getPackageName());
        this.binding = binding;

        if (constantMapping != null) {
            this.name = constantMapping.getName();
            this.prefix = constantMapping.getPrefix();
        }

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

    public String getPrefix() {
        return prefix;
    }

    public String getName() {
        return name;
    }

}
