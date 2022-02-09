package com.evolveum.prism.codegen.binding;

import com.evolveum.midpoint.prism.EnumerationTypeDefinition;

public class EnumerationContract extends Contract {

    private String packageName;
    private EnumerationTypeDefinition typeDefinition;

    public EnumerationContract(EnumerationTypeDefinition typeDefinition, String packageName) {
        this.typeDefinition = typeDefinition;
        this.packageName = packageName;
    }

    @Override
    public String fullyQualifiedName() {
        return packageName + "." + typeDefinition.getTypeName().getLocalPart();
    }

}
