/*
 * Copyright (c) 2021 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.prism.codegen.binding;

import javax.xml.namespace.QName;

import org.jetbrains.annotations.NotNull;

public abstract class TypeBinding extends Binding {

    private final QName name;


    public TypeBinding(QName name) {
        super();
        this.name = name;
    }

    public Static asStatic() {
        return null;
    }

    public static class Static extends TypeBinding {

        Class<?> implClass;

        public Static(QName name, Class<?> javaClass) {
            super(name);
            this.implClass = javaClass;
        }

        @Override
        public Static asStatic() {
            return this;
        }

        @Override
        public String defaultBindingClass() {
            return implClass.getName();
        }
    }

    static class Derived extends TypeBinding {

        public Derived(@NotNull QName typeName) {
            super(typeName);
        }

        @Override
        public String defaultBindingClass() {
            return defaultContract.fullyQualifiedName();
        }

    }


    public QName getName() {
        return name;
    }

    @Override
    public String getNamespaceURI() {
        return getName().getNamespaceURI();
    }

    @Override
    public String toString() {
        return "TypeBinding[name=" + name + "]";
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TypeBinding) {
            return this.name.equals(((TypeBinding) obj).getName());
        }
        return false;
    }
}
