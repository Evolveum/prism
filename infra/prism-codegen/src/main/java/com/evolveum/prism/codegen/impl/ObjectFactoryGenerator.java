/*
 * Copyright (c) 2022 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.prism.codegen.impl;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlNsForm;
import javax.xml.bind.annotation.XmlSchema;
import javax.xml.namespace.QName;

import com.evolveum.midpoint.prism.Raw;
import com.evolveum.prism.codegen.binding.ItemBinding;
import com.evolveum.prism.codegen.binding.ObjectFactoryContract;
import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JMod;

public class ObjectFactoryGenerator extends ContractGenerator<ObjectFactoryContract> {

    static final String NAMESPACE = "NAMESPACE";

    public ObjectFactoryGenerator(CodeGenerator codeGenerator) {
        super(codeGenerator);
    }

    @Override
    public JDefinedClass declare(ObjectFactoryContract contract) throws JClassAlreadyExistsException {

        JDefinedClass clazz = codeModel()._class(contract.fullyQualifiedName());

        JAnnotationUse packageAnn = clazz._package().annotate(XmlSchema.class);
        packageAnn.param("namespace", contract.getNamespace());
        packageAnn.param("elementFormDefault", clazz(XmlNsForm.class).staticRef("QUALIFIED"));


        var namespaceVar =  clazz.field(JMod.PUBLIC | JMod.STATIC | JMod.FINAL, String.class, NAMESPACE, JExpr.lit(contract.getNamespace()));

        for (ItemBinding item : contract.getItemNameToType()) {
            createQNameConstant(clazz, constant(item), item.itemName(), namespaceVar, true, false);
        }
        return clazz;
    }

    private String constant(ItemBinding item) {
        return "F_" + item.constantName();
    }

    /*
     *
     *     <pre>
     *     @XmlElementDecl(namespace = "http://midpoint.evolveum.com/xml/ns/public/connector/icf-1/resource-schema-3", name = "enableDate")
     *      public JAXBElement<Long> createEnableDate(Long value) {
     *        return new JAXBElement<Long>(_EnableDate_QNAME, Long.class, null, value);
          }
     */
    @Override
    public void implement(ObjectFactoryContract contract, JDefinedClass clazz) {
        for (ItemBinding item : contract.getItemNameToType()) {
            var itemType = asBindingTypeUnwrapped(item.getDefinition().getTypeName());
            var retType = clazz(JAXBElement.class).narrow(itemType);
            var method = clazz.method(JMod.PUBLIC, retType, "create" + item.getJavaName());
            var value = method.param(itemType, "value");
            method.body()._return(JExpr._new(retType)
                    .arg(JExpr.ref(constant(item)))
                    .arg(JExpr.dotclass(itemType))
                    .arg(JExpr._null())
                    .arg(value));

            JAnnotationUse decl = method.annotate(XmlElementDecl.class) //
                .param("namespace", JExpr.ref(NAMESPACE))
                .param("name", item.getQName().getLocalPart());

            QName subst = item.getDefinition().getSubstitutionHead();
            if (subst != null) {
                decl.param("substitutionHeadNamespace", subst.getNamespaceURI());
                decl.param("substitutionHeadName", subst.getLocalPart());
            }
            if (item.isRaw()) {
                method.annotate(Raw.class);
            }
        }

        for (QName typeName : contract.getTypes()) {
            var type = asBindingTypeUnwrapped(typeName);
            var method = clazz.method(JMod.PUBLIC, type, "create" + type.name());
            method.body()._return(JExpr._new(type));
        }
    }
}
