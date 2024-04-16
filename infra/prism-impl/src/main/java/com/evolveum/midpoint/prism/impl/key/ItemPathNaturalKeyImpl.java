/*
 * Copyright (C) 2010-2024 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism.impl.key;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import com.evolveum.midpoint.prism.PrismContainerValue;
import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.PrismProperty;
import com.evolveum.midpoint.prism.key.NaturalKey;
import com.evolveum.midpoint.prism.path.ItemName;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.prism.path.UniformItemPath;
import com.evolveum.midpoint.util.QNameUtil;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.util.exception.SystemException;
import com.evolveum.prism.xml.ns._public.types_3.ItemPathType;

/**
 * Natural key consisting of an item path.
 *
 * Assumptions:
 *
 * 1. the type of the key item is {@link ItemPathType}
 * 2. each container value to be merged must have the key value specified
 */
public class ItemPathNaturalKeyImpl implements NaturalKey {

    @NotNull private final ItemName keyItemName;

    private ItemPathNaturalKeyImpl(@NotNull ItemName keyItemName) {
        this.keyItemName = keyItemName;
    }

    public static ItemPathNaturalKeyImpl of(@NotNull ItemName itemName) {
        return new ItemPathNaturalKeyImpl(itemName);
    }

    @Override
    public boolean valuesMatch(PrismContainerValue<?> targetValue, PrismContainerValue<?> sourceValue) {
        ItemPath targetPath = getItemPath(targetValue);
        ItemPath sourcePath = getItemPath(sourceValue);

        if (targetPath == null || sourcePath == null) {
            return false;
        }

        return targetPath.equivalent(sourcePath);
    }

    /**
     * Source and target paths have to be equivalent here.
     *
     * We assume the source and target paths are purely name-based.
     * We go through them and construct the resulting path from the "most qualified" segments of these.
     *
     * We intentionally ignore some exotic cases here. (We are not obliged to create a "ideal" merged key, anyway.)
     */
    @Override
    public void mergeMatchingKeys(PrismContainerValue<?> targetValue, PrismContainerValue<?> sourceValue) {
        ItemPath targetPath = getItemPath(targetValue);
        ItemPath sourcePath = getItemPath(sourceValue);

        if (targetPath == null || sourcePath == null) {
            return;
        }

        assert targetPath.equivalent(sourcePath);

        if (targetPath.size() != sourcePath.size()) {
            return; // Paths are equivalent but differ in size -> ignore this case
        }

        List<Object> combinedPathSegments = new ArrayList<>(targetPath.size());
        for (int i = 0; i < targetPath.size(); i++) {
            Object targetSegment = targetPath.getSegment(i);
            Object sourceSegment = sourcePath.getSegment(i);
            if (!ItemPath.isName(targetSegment) || !ItemPath.isName(sourceSegment)) {
                return; // Exotic case -> ignore
            }
            ItemName targetSegmentName = ItemPath.toName(targetSegment);
            ItemName sourceSegmentName = ItemPath.toName(sourceSegment);
            if (!QNameUtil.match(targetSegmentName, sourceSegmentName)) {
                return; // Shouldn't occur!
            }
            if (QNameUtil.isQualified(targetSegmentName)) {
                combinedPathSegments.add(targetSegment);
            } else {
                combinedPathSegments.add(sourceSegment); // qualified or not
            }
        }

        try {
            targetValue.findProperty(keyItemName)
                    .replace(
                            PrismContext.get().itemFactory().createPropertyValue(
                                    new ItemPathType(
                                            UniformItemPath.create(combinedPathSegments))));
        } catch (SchemaException e) {
            throw SystemException.unexpected(e, "when updating '" + keyItemName + "'");
        }
    }

    private ItemPath getItemPath(PrismContainerValue<?> containerValue) {
        PrismProperty<ItemPathType> property = containerValue.findProperty(keyItemName);
        if (property == null) {
            return null;
        }

        try {
            ItemPathType itemPathType = property.getRealValue(ItemPathType.class);

            return itemPathType != null ? itemPathType.getItemPath() : null;
        } catch (RuntimeException e) {
            throw new SystemException("Couldn't get '" + keyItemName + "' from " + containerValue, e);
        }
    }
}
