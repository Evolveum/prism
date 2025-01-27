package com.evolveum.midpoint.prism.foo;

import com.evolveum.midpoint.prism.*;

import com.evolveum.midpoint.prism.impl.binding.AbstractMutableObjectable;
import com.evolveum.midpoint.prism.impl.xjc.PrismForJAXBUtil;
import com.evolveum.midpoint.prism.path.ItemName;

import jakarta.xml.bind.annotation.*;

import javax.xml.namespace.QName;
import java.util.List;

/**
 * Created by Dominik.
 */
@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(name = "AbstractRoleType", propOrder = {
        "displayName",
        "identifier",
        "inducement",
        "authorization",
        "requestable",
        "delegable",
        "idempotence",
        "riskLevel",
        "condition",
        "adminGuiConfiguration",
        "dataProtection",
        "autoassign"
})
@XmlSeeAlso({
        RoleType.class,
})
public abstract class AbstractRoleType extends FocusType
        implements Objectable {
    private static final long serialVersionUID = 201105211233L;
    public static final QName COMPLEX_TYPE = new QName(ObjectType.NS_FOO, "AbstractRoleType");
    public static final ItemName F_AUTHORIZATION = new ItemName(ObjectType.NS_FOO, "authorization");

    public AbstractRoleType() {
        super();
    }

    @Deprecated
    public AbstractRoleType(PrismContext context) {
        super();
    }


    @XmlElement(name = "authorization")
    public List<AuthorizationType> getAuthorization() {
        return this.prismGetContainerableList(AuthorizationType.FACTORY, F_AUTHORIZATION, AuthorizationType.class);
    }

    public List<AuthorizationType> createAuthorizationList() {
        PrismForJAXBUtil.createContainer(asPrismContainerValue(), F_AUTHORIZATION);
        return getAuthorization();
    }

    public AbstractRoleType authorization(AuthorizationType value) {
        getAuthorization().add(value);
        return this;
    }

    public AuthorizationType beginAuthorization() {
        AuthorizationType value = new AuthorizationType();
        authorization(value);
        return value;
    }

    public<X >X end() {
        return ((X)((PrismContainerValue)((PrismContainer) asPrismContainerValue().getParent()).getParent()).asContainerable());
    }

    @Override
    public AbstractRoleType clone() {
        return ((AbstractRoleType) super.clone());
    }

    public AbstractRoleType metadata(MetadataType value) {
        setMetadata(value);
        return this;
    }

    public MetadataType beginMetadata() {
        MetadataType value = new MetadataType();
        metadata(value);
        return value;
    }
}
