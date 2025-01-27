package com.evolveum.midpoint.prism.foo;

import com.evolveum.midpoint.prism.PrismContainer;
import com.evolveum.midpoint.prism.PrismContainerValue;
import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.impl.binding.AbstractMutableContainerable;
import com.evolveum.midpoint.prism.impl.xjc.PrismForJAXBUtil;
import com.evolveum.midpoint.util.Producer;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;

import javax.xml.namespace.QName;
import java.util.List;

/**
 * Created by Dominik.
 */
@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(name = "ExtensionType", propOrder = {
        "any"
})
public class ExtensionType extends AbstractMutableContainerable
{

    private static final long serialVersionUID = 201105211233L;
    public static final QName COMPLEX_TYPE = new QName(ObjectType.NS_FOO, "ExtensionType");
    public static final Producer<ExtensionType> FACTORY = new Producer<ExtensionType>() {

        private static final long serialVersionUID = 201105211233L;

        public ExtensionType run() {
            return new ExtensionType();
        }

    }
            ;

    public ExtensionType() {
        super();
    }

    @Deprecated
    public ExtensionType(PrismContext context) {
        super();
    }

    public boolean equals(Object other) {
        return super.equals(other);
    }

    public ExtensionType id(Long value) {
        setId(value);
        return this;
    }

    public List<Object> getAny() {
        return PrismForJAXBUtil.getAny(asPrismContainerValue(), Object.class);
    }

    public<X >X end() {
        return ((X)((PrismContainerValue)((PrismContainer) asPrismContainerValue().getParent()).getParent()).asContainerable());
    }

    @Override
    public ExtensionType clone() {
        return ((ExtensionType) super.clone());
    }

}
