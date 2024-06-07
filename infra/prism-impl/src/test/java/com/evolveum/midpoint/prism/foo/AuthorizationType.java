package com.evolveum.midpoint.prism.foo;

import com.evolveum.midpoint.prism.PrismContainer;
import com.evolveum.midpoint.prism.PrismContainerValue;
import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.impl.binding.AbstractMutableContainerable;
import com.evolveum.midpoint.prism.path.ItemName;
import com.evolveum.midpoint.util.Producer;
import com.evolveum.prism.xml.ns._public.types_3.ItemPathType;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

import javax.xml.namespace.QName;
import java.util.List;

/**
 * Created by Dominik.
 */
@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(name = "AuthorizationType", propOrder = {
        "name",
        "description",
        "documentation",
        "decision",
        "action",
        "phase",
        "enforcementStrategy",
        "zoneOfControl",
        "object",
        "item",
        "exceptItem",
        "target",
        "relation",
        "orderConstraints",
        "limitations"
})
public class AuthorizationType extends AbstractMutableContainerable {

    public static final QName COMPLEX_TYPE = new QName(ObjectType.NS_FOO, "AuthorizationType");
    public static final ItemName F_NAME = new ItemName(ObjectType.NS_FOO, "name");
    public static final ItemName F_DESCRIPTION = new ItemName(ObjectType.NS_FOO, "description");
    public static final ItemName F_DOCUMENTATION = new ItemName(ObjectType.NS_FOO, "documentation");
    public static final ItemName F_ACTION = new ItemName(ObjectType.NS_FOO, "action");
    public static final ItemName F_ITEM = new ItemName(ObjectType.NS_FOO, "item");
    public static final ItemName F_EXCEPT_ITEM = new ItemName(ObjectType.NS_FOO, "exceptItem");
    public static final ItemName F_RELATION = new ItemName(ObjectType.NS_FOO, "relation");
    public static final ItemName F_OBJECT = new ItemName(ObjectType.NS_FOO, "object");
    public static final Producer<AuthorizationType> FACTORY = new Producer<AuthorizationType>() {

        private static final long serialVersionUID = 201105211233L;

        public AuthorizationType run() {
            return new AuthorizationType();
        }

    };

    public AuthorizationType() {
        super();
    }

    @Deprecated
    public AuthorizationType(PrismContext context) {
        super();
    }

    @XmlElement(name = "name")
    public String getName() {
        return this.prismGetPropertyValue(F_NAME, String.class);
    }

    public void setName(String value) {
        this.prismSetPropertyValue(F_NAME, value);
    }

    @XmlElement(name = "description")
    public String getDescription() {
        return this.prismGetPropertyValue(F_DESCRIPTION, String.class);
    }

    public void setDescription(String value) {
        this.prismSetPropertyValue(F_DESCRIPTION, value);
    }

    @XmlElement(name = "documentation")
    public String getDocumentation() {
        return this.prismGetPropertyValue(F_DOCUMENTATION, String.class);
    }

    public void setDocumentation(String value) {
        this.prismSetPropertyValue(F_DOCUMENTATION, value);
    }

    @XmlElement(name = "action")
    public List<String> getAction() {
        return this.prismGetPropertyValues(F_ACTION, String.class);
    }

    @XmlElement(name = "item")
    public List<ItemPathType> getItem() {
        return this.prismGetPropertyValues(F_ITEM, ItemPathType.class);
    }

    @XmlElement(name = "exceptItem")
    public List<ItemPathType> getExceptItem() {
        return this.prismGetPropertyValues(F_EXCEPT_ITEM, ItemPathType.class);
    }

    @XmlElement(name = "relation")
    public List<QName> getRelation() {
        return this.prismGetPropertyValues(F_RELATION, QName.class);
    }


    @XmlElement(name = "object")
    public List<OwnedObjectSelectorType> getObject() {
        return this.prismGetContainerableList(OwnedObjectSelectorType.FACTORY, F_OBJECT, OwnedObjectSelectorType.class);
    }

    public boolean equals(Object other) {
        return super.equals(other);
    }

    public AuthorizationType id(Long value) {
        setId(value);
        return this;
    }

    public AuthorizationType name(String value) {
        setName(value);
        return this;
    }

    public AuthorizationType description(String value) {
        setDescription(value);
        return this;
    }

    public AuthorizationType documentation(String value) {
        setDocumentation(value);
        return this;
    }


    public AuthorizationType action(String value) {
        getAction().add(value);
        return this;
    }



    public AuthorizationType item(ItemPathType value) {
        getItem().add(value);
        return this;
    }

    public AuthorizationType exceptItem(ItemPathType value) {
        getExceptItem().add(value);
        return this;
    }

    public AuthorizationType relation(QName value) {
        getRelation().add(value);
        return this;
    }

    public<X >X end() {
        return ((X)((PrismContainerValue)((PrismContainer) asPrismContainerValue().getParent()).getParent()).asContainerable());
    }

    @Override
    public AuthorizationType clone() {
        return ((AuthorizationType) super.clone());
    }
}
