/*
 * Copyright (c) 2023-2024 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */

package com.evolveum.midpoint.prism.path;

import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.PrismReferenceDefinition;
import com.evolveum.midpoint.prism.schema.SchemaRegistry;
import com.evolveum.midpoint.util.DebugUtil;

import org.jetbrains.annotations.NotNull;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Item Path rooted in specified type
 */
public class TypedItemPath {

    @NotNull private final QName rootType;
    @NotNull private final ItemPath path;

    private TypedItemPath(@NotNull QName rootType, @NotNull ItemPath path) {
        this.rootType = rootType;
        this.path = path;
    }

    public static TypedItemPath of(@NotNull QName root, @NotNull ItemPath path) {
        return new TypedItemPath(root, path);
    }

    public static TypedItemPath of(@NotNull QName typeName) {
        return new TypedItemPath(typeName, ItemPath.EMPTY_PATH);
    }

    public @NotNull QName getRootType() {
        return rootType;
    }

    public @NotNull ItemPath getPath() {
        return path;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        TypedItemPath that = (TypedItemPath) o;
        return Objects.equals(rootType, that.rootType) && path.equivalent(that.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rootType, path);
    }

    @Override
    public String toString() {
        return DebugUtil.formatElementName(rootType) + "/" + path;
    }

    public @NotNull TypedItemPath append(@NotNull ItemPath descendant) {
        return of(rootType, this.path.append(descendant));
    }

    /**
     * Emits this path to the consumer or multiple type paths (without references) if this path consists
     * of derences.
     *
     * Emiting References
     * Path: UserType/assignment/targetRef - only one emmit
     *     - UserType/assignment/targetRef
     * Path: UserType/assignment/target/@
     *    - UserType/assignment/targetRef
     *    - AssignmentHolderType/
     * Path: UserType/assignment/targetRef/@/name
     *   - UserType/assignment/target
     *   - AbstractRole/name
     * Path: UserType/assignment/targetRef/@/archetypeRef/@/name
     *    - UserType/assignment/targetRef
     *    - AbstractRole/archetypeRef
     *    - ArchetypeRef/name
     *
     * @param consumer Consumer of paths
     * @param expandReferences If object references should be emitted separately
     * @return If references are splited, last path, else this path.
     */
    public TypedItemPath emitTo(Consumer<TypedItemPath> consumer, boolean expandReferences) {
        if (expandReferences && path.containsSpecialSymbols()) {
            var builder = new Builder(getRootType());
            for (var segment : path.getSegments()) {

                if (ItemPath.isObjectReference(segment)) {
                    var upToRef = builder.build();
                    consumer.accept(upToRef);

                    QName type = findReferencedType(upToRef);
                    if (segment instanceof ObjectReferencePathSegment refSeg) {
                        // If we have type hint, use type hint
                        type = refSeg.typeHint().orElse(type);
                    }
                    builder = new Builder(type);
                } else {
                    builder.append(segment);
                }
            }
            var last = builder.build();
            consumer.accept(last);
            return last;
        } else {
            consumer.accept(this);
        }
        return this;
    }

    private static QName findReferencedType(TypedItemPath path) {
        var rootType = SchemaRegistry.get().findComplexTypeDefinitionByType(path.getRootType());
        var def = rootType.findItemDefinition(path.getPath());
        if (def instanceof PrismReferenceDefinition refDef) {
            if (refDef.getTargetTypeName() != null) {
                return refDef.getTargetTypeName();
            }
        }
        return PrismContext.get().getDefaultReferenceTargetType();

    }

    private static class Builder {
        @NotNull private final QName typeName;
        @NotNull private final List<Object> segments = new ArrayList<>();

        public Builder(@NotNull QName typeName) {
            this.typeName = typeName;
        }

        void append(Object segment) {
            segments.add(segment);
        }

        TypedItemPath build() {
            return of(typeName, ItemPath.create(segments));
        };
    }
}
