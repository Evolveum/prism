package com.evolveum.midpoint.prism.impl.binding;

import java.util.List;

import javax.xml.namespace.QName;

import com.evolveum.midpoint.prism.Containerable;
import com.evolveum.midpoint.prism.PrismContainerValue;
import com.evolveum.midpoint.prism.impl.xjc.PrismForJAXBUtil;
import com.evolveum.midpoint.prism.path.ItemName;

public abstract class AbstractBoundContainerable implements Containerable {


    protected <T> T prismGetPropertyValue(QName name, Class<T> clazz) {
        return PrismForJAXBUtil.getPropertyValue(asPrismContainerValue(), name, clazz);
    }

    protected <T> List<T> prismGetPropertyValues(QName name, Class<T> clazz) {
        return PrismForJAXBUtil.getPropertyValues(asPrismContainerValue(), name, clazz);
    }

    protected <T> void prismSetPropertyValue(ItemName name, T value) {
        PrismForJAXBUtil.setPropertyValue(asPrismContainerValue(), name, value);
    }

    protected <T extends Containerable> T prismGetSingleContainerable(QName name, Class<T> clazz) {
        return PrismForJAXBUtil.getFieldSingleContainerable(asPrismContainerValue(), name, clazz);
    }

    protected <T extends Containerable> void prismSetSingleContainerable(ItemName name, T mappedValue) {
        PrismContainerValue<?> value = mappedValue != null ? mappedValue.asPrismContainerValue() : null;
        PrismForJAXBUtil.setFieldContainerValue(asPrismContainerValue(), name, value);
    }

}
