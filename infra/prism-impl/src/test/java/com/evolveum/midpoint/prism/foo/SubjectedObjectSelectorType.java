package com.evolveum.midpoint.prism.foo;

import com.evolveum.midpoint.prism.PrismContainer;
import com.evolveum.midpoint.prism.PrismContainerValue;
import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.path.ItemName;
import com.evolveum.midpoint.util.Producer;
import com.evolveum.prism.xml.ns._public.query_3.SearchFilterType;

import jakarta.xml.bind.annotation.*;

import javax.xml.namespace.QName;

/**
 * Created by Dominik.
 */
@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(name = "SubjectedObjectSelectorType", propOrder = {
        "allowInactive"
})
@XmlSeeAlso({
        OwnedObjectSelectorType.class
})
public class SubjectedObjectSelectorType extends ObjectSelectorType {
    private static final long serialVersionUID = 201105211233L;
    public static final QName COMPLEX_TYPE = new QName(ObjectType.NS_FOO, "SubjectedObjectSelectorType");
    public static final ItemName F_ALLOW_INACTIVE = new ItemName(ObjectType.NS_FOO, "allowInactive");
    public static final Producer<SubjectedObjectSelectorType> FACTORY = new Producer<SubjectedObjectSelectorType>() {

        private static final long serialVersionUID = 201105211233L;

        public SubjectedObjectSelectorType run() {
            return new SubjectedObjectSelectorType();
        }

    }
            ;

    public SubjectedObjectSelectorType() {
        super();
    }

    @Deprecated
    public SubjectedObjectSelectorType(PrismContext context) {
        super();
    }

    @XmlElement(name = "allowInactive")
    public Boolean isAllowInactive() {
        return this.prismGetPropertyValue(F_ALLOW_INACTIVE, Boolean.class);
    }

    public void setAllowInactive(Boolean value) {
        this.prismSetPropertyValue(F_ALLOW_INACTIVE, value);
    }

    public boolean equals(Object other) {
        return super.equals(other);
    }

    public SubjectedObjectSelectorType id(Long value) {
        setId(value);
        return this;
    }

    public SubjectedObjectSelectorType allowInactive(Boolean value) {
        setAllowInactive(value);
        return this;
    }

    public SubjectedObjectSelectorType name(String value) {
        setName(value);
        return this;
    }

    public SubjectedObjectSelectorType description(String value) {
        setDescription(value);
        return this;
    }

    public SubjectedObjectSelectorType documentation(String value) {
        setDocumentation(value);
        return this;
    }

    public SubjectedObjectSelectorType parent(ObjectParentSelectorType value) {
        setParent(value);
        return this;
    }

    public ObjectParentSelectorType beginParent() {
        ObjectParentSelectorType value = new ObjectParentSelectorType();
        parent(value);
        return value;
    }

    public SubjectedObjectSelectorType type(QName value) {
        setType(value);
        return this;
    }

    public SubjectedObjectSelectorType subtype(String value) {
        setSubtype(value);
        return this;
    }

    public SubjectedObjectSelectorType filter(SearchFilterType value) {
        setFilter(value);
        return this;
    }

    public<X >X end() {
        return ((X)((PrismContainerValue)((PrismContainer) asPrismContainerValue().getParent()).getParent()).asContainerable());
    }

    @Override
    public SubjectedObjectSelectorType clone() {
        return ((SubjectedObjectSelectorType) super.clone());
    }

}
