/*
 * Copyright (c) 2022 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism.impl.binding;

import java.util.List;

import javax.xml.namespace.QName;

import com.evolveum.midpoint.prism.Containerable;
import com.evolveum.midpoint.prism.Objectable;
import com.evolveum.midpoint.prism.PrismContainer;
import com.evolveum.midpoint.prism.PrismContainerValue;
import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.prism.PrismReference;
import com.evolveum.midpoint.prism.PrismReferenceValue;
import com.evolveum.midpoint.prism.Referencable;
import com.evolveum.midpoint.prism.impl.xjc.PrismContainerArrayList;
import com.evolveum.midpoint.prism.impl.xjc.PrismForJAXBUtil;
import com.evolveum.midpoint.prism.impl.xjc.PrismReferenceArrayList;
import com.evolveum.midpoint.prism.path.ItemName;
import com.evolveum.midpoint.util.Producer;

public interface ContainerablePrismBinding extends Containerable {


    default <T> T prismGetPropertyValue(QName name, Class<T> clazz) {
        return PrismForJAXBUtil.getPropertyValue(asPrismContainerValue(), name, clazz);
    }

    default <T> List<T> prismGetPropertyValues(QName name, Class<T> clazz) {
        return PrismForJAXBUtil.getPropertyValues(asPrismContainerValue(), name, clazz);
    }

    default <T extends ContainerablePrismBinding> List<T> prismGetContainerableList(Producer<T> producer, QName name, Class<T> clazz) {
        PrismContainerValue<?> pcv = asPrismContainerValue();
        PrismContainer<T> container = PrismForJAXBUtil.getContainer(pcv, name);
        return new ContainerableList<>(container, pcv, producer);
    }

    default <T extends Referencable> List<T> prismGetReferencableList(Producer<T> producer, QName name, Class<?> clazz) {
        PrismContainerValue<?> pcv = asPrismContainerValue();
        PrismReference reference = PrismForJAXBUtil.getReference(pcv, name);
        return new ReferencableList<>(reference, pcv, producer);
    }

    default <T> void prismSetPropertyValue(ItemName name, T value) {
        PrismForJAXBUtil.setPropertyValue(asPrismContainerValue(), name, value);
    }

    default <T extends Containerable> T prismGetSingleContainerable(QName name, Class<T> clazz) {
        return PrismForJAXBUtil.getFieldSingleContainerable(asPrismContainerValue(), name, clazz);
    }

    default <T extends Containerable> void prismSetSingleContainerable(ItemName name, T mappedValue) {
        PrismContainerValue<?> value = mappedValue != null ? mappedValue.asPrismContainerValue() : null;
        PrismForJAXBUtil.setFieldContainerValue(asPrismContainerValue(), name, value);
    }

    default <T extends Referencable> T prismGetReferencable(ItemName name, Class<T> type, Producer<T> factory) {
        var value = PrismForJAXBUtil.getReferenceValue(asPrismContainerValue(), name);
        if (value == null) {
            return null;
        }
        T ret = factory.run();
        ret.setupReferenceValue(value);
        return ret;
    }

    default void prismSetReferencable(ItemName name, Referencable value) {
        PrismReferenceValue referenceValue = (value!= null) ? value.asReferenceValue() : null;
        PrismForJAXBUtil.setReferenceValueAsRef(asPrismContainerValue(), name, referenceValue);
    }

    default <T extends Objectable> T prismGetReferenceObjectable(ItemName refName, Class<T> objType) {
        PrismReferenceValue reference = PrismForJAXBUtil.getReferenceValue(asPrismContainerValue(), refName);
        if ((reference == null) || (reference.getObject() == null)) {
            return null;
        }
        return objType.cast(reference.getObject().asObjectable());

    }


    default <T extends Objectable> void prismSetReferenceObjectable(ItemName refName, Objectable value) {
        PrismObject object = (value!= null) ? value.asPrismObject() : null;
        PrismForJAXBUtil.setReferenceValueAsObject(asPrismContainerValue(), refName, object);
    }


    static class ContainerableList<T extends Containerable> extends PrismContainerArrayList<T> {

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

    static class ReferencableList<T extends Referencable> extends PrismReferenceArrayList<T> {

        private static final long serialVersionUID = 1L;
        private final Producer<T> factory;

        public ReferencableList(PrismReference reference, PrismContainerValue parent, Producer<T> producer) {
            super(reference, parent);
            this.factory= producer;
        }

        @Override
        protected T createItem(PrismReferenceValue value) {
            T approverRef = factory.run();
            approverRef.setupReferenceValue(value);
            return approverRef;
        }

        @Override
        protected PrismReferenceValue getValueFrom(T value) {
            return value.asReferenceValue();
        }

        @Override
        protected boolean willClear(PrismReferenceValue value) {
            return (value.getObject() == null);
        }

    }

}
