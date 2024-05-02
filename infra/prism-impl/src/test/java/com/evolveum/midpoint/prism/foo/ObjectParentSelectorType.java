package com.evolveum.midpoint.prism.foo;

import com.evolveum.midpoint.prism.PrismContainer;
import com.evolveum.midpoint.prism.PrismContainerValue;
import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.path.ItemName;
import com.evolveum.midpoint.util.Producer;
import com.evolveum.prism.xml.ns._public.types_3.ItemPathType;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;


/**
 * Created by Dominik.
 */
@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(name = "ObjectParentSelectorType", propOrder = {
        "path"
})
public class ObjectParentSelectorType extends OwnedObjectSelectorType {

    private static final long serialVersionUID = 201105211233L;
    public static final ItemName F_PATH = new ItemName(ObjectType.NS_FOO, "path");
    public static final Producer<ObjectParentSelectorType> FACTORY = new Producer<ObjectParentSelectorType>() {

        private static final long serialVersionUID = 201105211233L;

        public ObjectParentSelectorType run() {
            return new ObjectParentSelectorType();
        }

    }
            ;

    public ObjectParentSelectorType() {
        super();
    }

    @Deprecated
    public ObjectParentSelectorType(PrismContext context) {
        super();
    }

    @XmlElement(name = "path")
    public ItemPathType getPath() {
        return this.prismGetPropertyValue(F_PATH, ItemPathType.class);
    }

    public void setPath(ItemPathType value) {
        this.prismSetPropertyValue(F_PATH, value);
    }

    public boolean equals(Object other) {
        return super.equals(other);
    }

    public ObjectParentSelectorType id(Long value) {
        setId(value);
        return this;
    }

    public ObjectParentSelectorType path(ItemPathType value) {
        setPath(value);
        return this;
    }

    public<X >X end() {
        return ((X)((PrismContainerValue)((PrismContainer) asPrismContainerValue().getParent()).getParent()).asContainerable());
    }

    @Override
    public ObjectParentSelectorType clone() {
        return ((ObjectParentSelectorType) super.clone());
    }

}
