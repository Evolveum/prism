/*
 * Copyright (c) 2010-2019 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.util;

/**
 *
 */
@FunctionalInterface
public interface TreeNodeVisitor<T> {
    void visit(TreeNode<T> node);
}
