package com.evolveum.midpoint.prism.foo;

import com.evolveum.midpoint.prism.*;

import com.evolveum.prism.xml.ns._public.types_3.PolyStringType;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;

import javax.xml.namespace.QName;

/**
 * Created by Dominik.
 */
@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(name = "RoleType", propOrder = {

})
public class RoleType extends AbstractRoleType implements Objectable {

    public static final QName COMPLEX_TYPE = new QName(ObjectType.NS_FOO, "RoleType");

    public RoleType() {
        super();
    }

    @Deprecated
    public RoleType(PrismContext context) {
        super();
    }

    public boolean equals(Object other) {
        return super.equals(other);
    }

    public RoleType version(String value) {
        setVersion(value);
        return this;
    }

    public RoleType oid(String value) {
        setOid(value);
        return this;
    }

    public RoleType name(PolyStringType value) {
        setName(value);
        return this;
    }

    public RoleType name(String value) {
        return name(PolyStringType.fromOrig(value));
    }

    public RoleType description(String value) {
        setDescription(value);
        return this;
    }


    public<X >X end() {
        return ((X)((PrismContainerValue)((PrismContainer) asPrismContainerValue().getParent()).getParent()).asContainerable());
    }

    @Override
    public RoleType clone() {
        return ((RoleType) super.clone());
    }

    @Override
    protected QName prismGetContainerName() {
        return COMPLEX_TYPE;
    }

    @Override
    protected QName prismGetContainerType() {
        return COMPLEX_TYPE;
    }

    public RoleType authorization(AuthorizationType value) {
        getAuthorization().add(value);
        return this;
    }

    public AuthorizationType beginAuthorization() {
        AuthorizationType value = new AuthorizationType();
        authorization(value);
        return value;
    }
}
