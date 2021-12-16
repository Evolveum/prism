package com.evolveum.midpoint.prism.deleg;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import javax.xml.namespace.QName;

import com.evolveum.midpoint.prism.*;

import org.jetbrains.annotations.NotNull;

import com.evolveum.midpoint.util.exception.SchemaException;

public interface ObjectDefinitionDelegator<O extends Objectable> extends ContainerDefinitionDelegator<O>, PrismObjectDefinition<O> {

    @Override
    PrismObjectDefinition<O> delegate();

    @Override
    default PrismContainerDefinition<?> getExtensionDefinition() {
        return delegate().getExtensionDefinition();
    }

    @Override
    default @NotNull PrismObject<O> instantiate() throws SchemaException {
        return delegate().instantiate();
    }

    @Override
    default @NotNull PrismObject<O> instantiate(QName name) throws SchemaException {
        return delegate().instantiate(name);
    }

    @Override
    default @NotNull List<? extends ItemDefinition<?>> getDefinitions() {
        return delegate().getDefinitions();
    }
    @Override
    default PrismObjectValue<O> createValue() {
        return delegate().createValue();
    }

    @Override
    @NotNull
    PrismObjectDefinition<O> cloneWithReplacedDefinition(QName itemName, ItemDefinition<?> newDefinition);

    @Override
    PrismObjectDefinition<O> deepClone(@NotNull DeepCloneOperation operation);
}
