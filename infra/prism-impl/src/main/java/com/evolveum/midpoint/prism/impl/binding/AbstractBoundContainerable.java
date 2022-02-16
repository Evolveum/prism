package com.evolveum.midpoint.prism.impl.binding;

import java.util.List;

import javax.xml.namespace.QName;

import com.evolveum.midpoint.prism.Containerable;
import com.evolveum.midpoint.prism.PrismContainer;
import com.evolveum.midpoint.prism.PrismContainerValue;
import com.evolveum.midpoint.prism.impl.xjc.PrismContainerArrayList;
import com.evolveum.midpoint.prism.impl.xjc.PrismForJAXBUtil;
import com.evolveum.midpoint.prism.path.ItemName;
import com.evolveum.midpoint.util.Producer;

public abstract class AbstractBoundContainerable implements Containerable {


    protected <T> T prismGetPropertyValue(QName name, Class<T> clazz) {
        return PrismForJAXBUtil.getPropertyValue(asPrismContainerValue(), name, clazz);
    }

    protected <T> List<T> prismGetPropertyValues(QName name, Class<T> clazz) {
        return PrismForJAXBUtil.getPropertyValues(asPrismContainerValue(), name, clazz);
    }

    protected <T extends AbstractBoundContainerable> List<T> prismGetContainerableList(Producer<T> producer, QName name, Class<T> clazz) {
        PrismContainerValue<?> pcv = asPrismContainerValue();
        PrismContainer<T> container = PrismForJAXBUtil.getContainer(pcv, name);
        return new ContainerableList<>(container, pcv, producer);
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

    protected static class ContainerableList<T extends Containerable> extends PrismContainerArrayList<T> {

        private static final long serialVersionUID = -8244451828909384509L;

        private final Producer<T> producer;

        public ContainerableList(PrismContainer<T> container, PrismContainerValue<?> parent, Producer<T> factory) {
            super(container, parent);
            this.producer = factory;
        }

        public ContainerableList(PrismContainer<T> container, Producer<T> factory) {
            super(container);
            this.producer = factory;
        }

        @Override
        protected PrismContainerValue getValueFrom(T t) {
            return t.asPrismContainerValue();
        }

        @Override
        protected T createItem(PrismContainerValue value) {
            T ret = producer.run();
            ret.setupContainerValue(value);
            return ret;
        }

    }

}
