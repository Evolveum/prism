package com.evolveum.midpoint.prism.deleg;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.delta.ChangeType;
import com.evolveum.midpoint.prism.delta.ItemDelta;
import com.evolveum.midpoint.prism.delta.ObjectDelta;
import com.evolveum.midpoint.prism.equivalence.ParameterizedEquivalenceStrategy;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.prism.polystring.PolyString;
import com.evolveum.midpoint.util.exception.SchemaException;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.xml.namespace.QName;
import java.util.Collection;

public interface PrismObjectDelegator<O extends Objectable> extends PrismContainerDelegator<O>, PrismObject<O> {


    PrismObject<O> delegate();

    @Override
    default PrismObjectValue<O> createNewValue() {
        return delegate().createNewValue();
    }

    @Override
    default @NotNull PrismObjectValue<O> getValue() {
        return delegate().getValue();
    }

    @Override
    default void setValue(@NotNull PrismContainerValue<O> value) throws SchemaException {
        delegate().setValue(value);
    }

    @Override
    default String getOid() {
        return delegate().getOid();
    }

    @Override
    default void setOid(String oid) {
        delegate().setOid(oid);
    }

    @Override
    default String getVersion() {
        return delegate().getVersion();
    }

    @Override
    default void setVersion(String version) {
        delegate().setVersion(version);
    }

    @Override
    default PrismObjectDefinition<O> getDefinition() {
        return delegate().getDefinition();
    }

    @Override
    default @NotNull O asObjectable() {
        return delegate().asObjectable();
    }

    @Override
    default PolyString getName() {
        return delegate().getName();
    }

    @Override
    default PrismContainer<?> getExtension() {
        return delegate().getExtension();
    }

    @Override
    default PrismContainer<?> getOrCreateExtension() throws SchemaException {
        return delegate().getOrCreateExtension();
    }

    @Override
    default PrismContainerValue<?> getExtensionContainerValue() {
        return delegate().getExtensionContainerValue();
    }

    @Override
    default <I extends Item<?, ?>> I findExtensionItem(String elementLocalName) {
        return delegate().findExtensionItem(elementLocalName);
    }

    @Override
    default <I extends Item<?, ?>> I findExtensionItem(QName elementName) {
        return delegate().findExtensionItem(elementName);
    }

    @Override
    default <I extends Item<?, ?>> void addExtensionItem(I item) throws SchemaException {
        delegate().addExtensionItem(item);
    }

    @Override
    default PrismContainer<?> createExtension() throws SchemaException {
        return delegate().createExtension();
    }

    @Override
    default <IV extends PrismValue, ID extends ItemDefinition<?>, I extends Item<IV, ID>> void removeItem(ItemPath path, Class<I> itemType) {
        delegate().removeItem(path, itemType);
    }

    @Override
    default void addReplaceExisting(Item<?, ?> item) throws SchemaException {
        delegate().addReplaceExisting(item);
    }

    @Override
    default PrismObject<O> clone() {
        return delegate().clone();
    }

    @Override
    default PrismObject<O> cloneComplex(CloneStrategy strategy) {
        return delegate().cloneComplex(strategy);
    }

    @Override
    default PrismObjectDefinition<O> deepCloneDefinition(@NotNull DeepCloneOperation operation) {
        return delegate().deepCloneDefinition(operation);
    }

    @Override
    default @NotNull ObjectDelta<O> diff(PrismObject<O> other) {
        return delegate().diff(other);
    }

    @Override
    default @NotNull ObjectDelta<O> diff(PrismObject<O> other, ParameterizedEquivalenceStrategy strategy) {
        return delegate().diff(other, strategy);
    }

    @Override
    default Collection<? extends ItemDelta<?, ?>> narrowModifications(Collection<? extends ItemDelta<?, ?>> modifications, @NotNull ParameterizedEquivalenceStrategy plusStrategy, @NotNull ParameterizedEquivalenceStrategy minusStrategy, boolean assumeMissingItems) {
        return delegate().narrowModifications(modifications, plusStrategy, minusStrategy, assumeMissingItems);
    }

    @Override
    default ObjectDelta<O> createDelta(ChangeType changeType) {
        return delegate().createDelta(changeType);
    }

    @Override
    default ObjectDelta<O> createAddDelta() {
        return delegate().createAddDelta();
    }

    @Override
    default ObjectDelta<O> createModifyDelta() {
        return delegate().createModifyDelta();
    }

    @Override
    default ObjectDelta<O> createDeleteDelta() {
        return delegate().createDeleteDelta();
    }

    @Override
    default void setParent(PrismContainerValue<?> parentValue) {
        delegate().setParent(parentValue);
    }

    @Override
    default @Nullable PrismContainerValue<?> getParent() {
        return delegate().getParent();
    }

    @Override
    default boolean equivalent(Object obj) {
        return delegate().equivalent(obj);
    }

    @Override
    default String toDebugName() {
        return delegate().toDebugName();
    }

    @Override
    default String toDebugType() {
        return delegate().toDebugType();
    }

    @Override
    default String getBusinessDisplayName() {
        return delegate().getBusinessDisplayName();
    }

    @Override
    default PrismObject<O> cloneIfImmutable() {
        return delegate().cloneIfImmutable();
    }

    @Override
    default PrismObject<O> createImmutableClone() {
        return delegate().createImmutableClone();
    }

    @Override
    default boolean isOfType(@NotNull Class<?> type) {
        return delegate().isOfType(type);
    }
}
