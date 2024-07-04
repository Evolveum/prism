package com.evolveum.midpoint.prism.impl.query;

import com.evolveum.midpoint.prism.ItemDefinition;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.prism.query.FilterItemPathTransformer;
import com.evolveum.midpoint.prism.query.ItemFilter;
import com.evolveum.midpoint.prism.query.ObjectFilter;

import org.jetbrains.annotations.NotNull;

public abstract class AbstractItemFilter extends ObjectFilterImpl implements ItemFilter {

    protected ItemPath fullPath;

    public AbstractItemFilter(ItemPath fullPath) {
        this.fullPath = fullPath;
    }

    @NotNull
    @Override
    public final ItemPath getFullPath() {
        return fullPath;
    }

    @Override
    public void transformItemPaths(ItemPath parentPath, ItemDefinition<?> parentDef, FilterItemPathTransformer transformer) {
        var ret = transformer.transform(parentPath, parentDef, this);
        if (ret != null) {
            fullPath = ret;
        }
    }
}
