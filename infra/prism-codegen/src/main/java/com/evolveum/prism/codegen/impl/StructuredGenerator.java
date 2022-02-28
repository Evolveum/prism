/*
 * Copyright (c) 2021 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.prism.codegen.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;

import org.jetbrains.annotations.NotNull;

import com.evolveum.midpoint.prism.ComplexTypeDefinition;
import com.evolveum.midpoint.prism.PrismConstants;
import com.evolveum.midpoint.prism.PrismReferenceValue;
import com.evolveum.midpoint.prism.TypeDefinition;
import com.evolveum.midpoint.prism.impl.PrismReferenceValueImpl;
import com.evolveum.midpoint.prism.xml.XmlTypeConverter;
import com.evolveum.midpoint.util.Producer;
import com.evolveum.prism.codegen.binding.BindingContext;
import com.evolveum.prism.codegen.binding.ContainerableContract;
import com.evolveum.prism.codegen.binding.ItemBinding;
import com.evolveum.prism.codegen.binding.ObjectFactoryContract;
import com.evolveum.prism.codegen.binding.ReferenceContract;
import com.evolveum.prism.codegen.binding.StructuredContract;
import com.evolveum.prism.codegen.binding.TypeBinding;
import com.sun.codemodel.JAnnotationArrayMember;
import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JPrimitiveType;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;

public abstract class StructuredGenerator<T extends StructuredContract> extends ContractGenerator<T> {

    private static final String XML_ELEMENT_NAME = "name";
    private static final String VALUE_PARAM = "value";

    protected static final String FACTORY = "FACTORY";
    private static final String FROM_ORIG = "fromOrig";
    private static final String CREATE_GREGORIAN = "createXMLGregorianCalendar";


    public StructuredGenerator(CodeGenerator codeGenerator) {
        super(codeGenerator);
    }

    protected void declareFactory(JDefinedClass clazz) {
        JClass producerType = clazz(Producer.class).narrow(clazz);
        JDefinedClass annonFactory = codeModel().anonymousClass(producerType);
        declareSerialVersionUid(annonFactory);
        annonFactory.method(JMod.PUBLIC, clazz, "run").body()._return(JExpr._new(clazz));
        clazz.field(JMod.PUBLIC | JMod.STATIC | JMod.FINAL, producerType, FACTORY, JExpr._new(annonFactory));
    }

    protected void declareSerialVersionUid(JDefinedClass clazz) {
        clazz.field(JMod.PRIVATE | JMod.FINAL | JMod.STATIC, long.class, "serialVersionUID",
                JExpr.lit(BindingContext.SERIAL_VERSION_UID));
    }

    protected void declareConstants(JDefinedClass clazz, StructuredContract contract, Collection<ItemBinding> definitions) {
        declareSerialVersionUid(clazz);

        var namespaceField = codeModel().ref(clazz.getPackage().name() + "." + ObjectFactoryContract.OBJECT_FACTORY).staticRef(ObjectFactoryContract.NAMESPACE_CONST);
        createQNameConstant(clazz, BindingContext.TYPE_CONSTANT, contract.getTypeDefinition().getTypeName(),  namespaceField, false, false);
        for(ItemBinding def : definitions) {
            createQNameConstant(clazz, "F_"  + def.constantName(), def.itemName(), namespaceField, true, true);
        }
    }

    @Override
    public void implement(T contract, JDefinedClass clazz) {
        for (ItemBinding definition : itemDefinitions(contract)) {

            // Getter
            JClass bindingType = asBindingType(definition, contract);
            JType  maybeBoxedType = asBoxedTypeIfPossible(bindingType, definition);

            JMethod getter = clazz.method(JMod.PUBLIC,  maybeBoxedType, definition.getterName());

            // @XmlElement annotation
            if (shouldAnnotateGetter(definition, contract)) {
                JAnnotationUse jaxbAnn = getter.annotate(XmlElement.class);
                jaxbAnn.param(XML_ELEMENT_NAME, definition.itemName().getLocalPart());
            }
            if (shouldImplementGetter(clazz, contract, definition)) {
                implementGetter(clazz, getter, definition, bindingType);
            }
            // Setter
            if (shouldImplementSetter(clazz, contract, definition)) {
                JMethod setter = clazz.method(JMod.PUBLIC, void.class, definition.setterName());
                JVar valueParam = setter.param(bindingType, VALUE_PARAM);
                implementSetter(clazz, setter, definition, valueParam);
            }
            implementAdditionalFieldMethod(clazz, definition, bindingType);
        }

        // Fluent API

        implementHashCode(clazz, contract);
        implementEquals(clazz, contract);


        implementFluentApi(clazz, contract);
        implementationAfterFluentApi(contract,clazz);


        var clone = clazz.method(JMod.PUBLIC, clazz, "clone");
        clone.annotate(Override.class);
        implementClone(clazz, contract, clone);
    }

    private void implementFluentApi(JDefinedClass clazz, T contract) {
        StructuredContract current = contract;
        // Generate fluent api from local definitions of current contract and
        // then from parent type contracts.
        Set<String> alreadyGenerated = new HashSet<>();
        while (current != null) {
            generateFluentApi(clazz, contract, current.getAttributeDefinitions(), alreadyGenerated);
            generateFluentApi(clazz, contract, current.getLocalDefinitions(), alreadyGenerated);
            QName superType = current.getSuperType();
            current = null;
            if (superType != null) {
                var superContract = bindingFor(superType).getDefaultContract();
                if (superContract instanceof StructuredContract) {
                    current = (StructuredContract) superContract;
                }
            }
        }

    }

    protected void implementEquals(JDefinedClass clazz, T contract) {
        // NOOP
    }

    protected void implementHashCode(JDefinedClass clazz, T contract) {
        // NOOP
    }

    protected boolean shouldAnnotateGetter(ItemBinding definition, T contract) {
        return true;
    }

    protected JType asBoxedTypeIfPossible(JClass bindingType, ItemBinding definition) {
        JPrimitiveType primitiveType = bindingType.getPrimitiveType();
        // If item is required and it is primitive
        if (primitiveType != null && definition.getDefinition().getMinOccurs() == 1 && definition.getDefinition().getMaxOccurs() == 1) {
            return primitiveType;
        }
        return bindingType;
    }

    protected boolean shouldImplementSetter(JDefinedClass clazz, T contract, ItemBinding definition) {
        return true;
    }

    protected boolean shouldImplementGetter(JDefinedClass clazz, T contract, ItemBinding definition) {
        return true;
    }

    protected Iterable<ItemBinding> itemDefinitions(T contract) {
        return contract.getLocalDefinitions();
    }

    protected void implementClone(JDefinedClass clazz, T contract, JMethod clone) {
        clone.body()._throw(JExpr._new(clazz(UnsupportedOperationException.class)));
    }

    protected void implementationAfterFluentApi(T contract, JDefinedClass clazz) {
        // Intentional NOOP
    }

    private void generateFluentApi(JDefinedClass clazz, T contract, Iterable<ItemBinding> localDefs, Set<String> alreadyGenerated) {
        for (ItemBinding definition : localDefs) {
            if (alreadyGenerated.contains(definition.fieldName())) {
                continue;
            }
            alreadyGenerated.add(definition.fieldName());
            JMethod fluentSetter = clazz.method(JMod.PUBLIC, clazz, definition.fieldName());
            JClass type = asBindingTypeOrJaxbElement(definition, contract);
            JVar value = fluentSetter.param(type, "value");
            if (definition.isList()) {
                fluentSetter.body().invoke(JExpr.invoke(definition.getterName()), "add").arg(value);
            } else {
                fluentSetter.body().invoke(definition.setterName()).arg(value);
            }

            fluentSetter.body()._return(JExpr._this());

            // If binding is structured, generate begin / end method

            var targetContract = bindingFor(definition.getDefinition().getTypeName()).getDefaultContract();
            if (targetContract instanceof ReferenceContract) {
                var refClazz = codeModel().ref(((ReferenceContract) targetContract).fullyQualifiedName());
                declareReferenceMethod(clazz, definition.fieldName(), refClazz, (m,r) -> {});
                declareReferenceMethod(clazz, definition.fieldName(), refClazz, (method,refVal) -> {
                    var relation = method.param(QName.class, "relation");
                    method.body().invoke(refVal, "setRelation").arg(relation);
                });

            }
            if (PrismConstants.POLYSTRING_TYPE_QNAME.equals(definition.getDefinition().getTypeName())) {
                JMethod stringSetter = clazz.method(JMod.PUBLIC, clazz, definition.fieldName());
                var stringValue = stringSetter.param(String.class, "value");
                stringSetter.body()._return(
                        JExpr.invoke(definition.fieldName()).arg(type.staticInvoke(FROM_ORIG).arg(stringValue)));
            }

            if (clazz(XMLGregorianCalendar.class).equals(type)) {
                JMethod stringSetter = clazz.method(JMod.PUBLIC, clazz, definition.fieldName());
                var stringValue = stringSetter.param(String.class, "value");
                stringSetter.body()._return(
                        JExpr.invoke(definition.fieldName())
                        .arg(clazz(XmlTypeConverter.class).staticInvoke(CREATE_GREGORIAN).arg(stringValue)));

            }

            // Generate begin only for structured, non substituble, non abstract complex types
            if (targetContract instanceof StructuredContract
                    && !((StructuredContract) targetContract).getTypeDefinition().isAbstract()
                    && !type.erasure().equals(clazz(JAXBElement.class))) {
                var beginMethod = clazz.method(JMod.PUBLIC, type, "begin" + definition.getJavaName());
                value = beginMethod.body().decl(type, "value", JExpr._new(type));
                beginMethod.body().invoke(definition.fieldName()).arg(value);
                beginMethod.body()._return(value);
            }
        }
    }

    private void declareReferenceMethod(JDefinedClass clazz, String name, JClass refClazz, BiConsumer<JMethod, JVar> refValCustomizer) {
        var method = clazz.method(JMod.PUBLIC, clazz, name);
        var oid = method.param(String.class, "oid");
        var type = method.param(QName.class, "type");
        var body = method.body();
        var refVal = body.decl(clazz(PrismReferenceValue.class), "refVal",
                JExpr._new(clazz(PrismReferenceValueImpl.class)).arg(oid).arg(type));
        refValCustomizer.accept(method, refVal);
        // ObjectReferenceType instance
        var ort = body.decl(refClazz, "ort", JExpr._new(refClazz));
        body.invoke(ort, "setupReferenceValue").arg(refVal);
        body._return(JExpr.invoke(name).arg(ort));
    }

    protected abstract void implementGetter(JDefinedClass clazz, JMethod method, ItemBinding definition, JType returnType);

    protected abstract void implementSetter(JDefinedClass clazz, JMethod method, ItemBinding definition, JVar valueParam);

    protected void implementAdditionalFieldMethod(JDefinedClass clazz, ItemBinding definition, JType returnType) {
        // Intentional NOOP
    }


    protected JClass asBindingType(ItemBinding definition, T contract) {
        JClass valueType = asBindingTypeOrJaxbElement(definition, contract);
        if (definition.isList()) {
            // Wrap as list
            valueType = codeModel().ref(List.class).narrow(valueType);
        }
        return valueType;
    }

    protected JClass asBindingTypeOrJaxbElement(ItemBinding definition, T contract) {
        ComplexTypeDefinition parentDef = contract.getTypeDefinition();
        JClass valueType = asBindingTypeUnwrapped(definition.getDefinition().getTypeName());
        // we have multiple object elements, which are not target for substitutions
        if (shouldUseJaxbElement(definition, contract)) {
            valueType = clazz(JAXBElement.class).narrow(valueType.wildcard());
        }
        return valueType;
    }

    protected boolean shouldUseJaxbElement(ItemBinding definition, T contract) {
        ComplexTypeDefinition parentDef = contract.getTypeDefinition();
        if (!parentDef.hasSubstitutions(definition.getQName())) {
            return false;
        }
        if (definition.getJavaName().equals("Object")) {
            TypeBinding type = bindingFor(definition.getDefinition().getTypeName());
            if (type.getDefaultContract() instanceof ContainerableContract) {
                ComplexTypeDefinition typeDef = ((ContainerableContract) type.getDefaultContract()).getTypeDefinition();
                return typeDef.isAbstract();
            }
            return false;
        }
        return true;
    }

    public void annotateType(JDefinedClass clazz, StructuredContract contract, XmlAccessType type) {
        // XML Accessor Type
        clazz.annotate(XmlAccessorType.class).param("value", clazz(XmlAccessType.class).staticRef(type.name()));

        // XML Type annotation
        JAnnotationUse typeAnnon = clazz.annotate(XmlType.class);
        typeAnnon.param("name", contract.getTypeDefinition().getTypeName().getLocalPart());
        // Property order
        if (contract != null) {
            JAnnotationArrayMember propOrder = typeAnnon.paramArray("propOrder");
            for(ItemBinding def : contract.getLocalDefinitions()) {
                propOrder.param(def.fieldName());
            }

            @NotNull
            Collection<TypeDefinition> subtypes = contract.getTypeDefinition().getStaticSubTypes();
            if (!subtypes.isEmpty()) {
                var seeAlso = clazz.annotate(XmlSeeAlso.class).paramArray("value");
                for(TypeDefinition subtype : subtypes) {
                    seeAlso.param(codeModel().ref(bindingFor(subtype.getTypeName()).defaultBindingClass()));
                }


            }
        }
    }
}

