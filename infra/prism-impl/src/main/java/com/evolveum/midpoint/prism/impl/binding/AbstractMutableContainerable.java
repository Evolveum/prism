package com.evolveum.midpoint.prism.impl.binding;

import com.evolveum.midpoint.prism.Containerable;
import com.evolveum.midpoint.prism.PrismContainerValue;
import com.evolveum.midpoint.prism.impl.PrismContainerValueImpl;

public abstract class AbstractMutableContainerable extends AbstractBoundContainerable implements Containerable {

    private PrismContainerValue value;

    @Override
    public void setupContainerValue(PrismContainerValue container) {
        this.value = container;
    }

    @Override
    public PrismContainerValue asPrismContainerValue() {
        if (value == null) {
            value = new PrismContainerValueImpl<>(this);
        }
        return value;
    }

    @Override
    public String toString() {
        return asPrismContainerValue().toString();
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Containerable)) {
            return false;
        }
        Containerable other = ((Containerable) object);
        return asPrismContainerValue().equivalent(other.asPrismContainerValue());
    }

    @Override
    public int hashCode() {
        return asPrismContainerValue().hashCode();
    }

}
