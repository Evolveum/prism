package com.evolveum.prism.codegen.binding;

public class SchemaBinding extends Binding {

    public static final String OBJECT_FACTORY = "ObjectFactory";
    private final String namespace;
    private final String packageName;

    public SchemaBinding(String namespace, String packageName) {
        this.namespace = namespace;
        this.packageName = packageName;
    }

    @Override
    public String defaultBindingClass() {
        // TODO Auto-generated method stub
        return packageName + "." + OBJECT_FACTORY;
    }

    public String getPackageName() {
        return packageName;
    }

    @Override
    public String getNamespaceURI() {
        return namespace;
    }
}
