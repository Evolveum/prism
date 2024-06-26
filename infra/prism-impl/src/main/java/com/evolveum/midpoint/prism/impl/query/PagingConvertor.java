/*
 * Copyright (C) 2010-2022 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.prism.impl.query;

import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.query.ObjectPaging;
import com.evolveum.midpoint.prism.query.OrderDirection;
import com.evolveum.prism.xml.ns._public.query_3.OrderDirectionType;
import com.evolveum.prism.xml.ns._public.query_3.PagingType;
import com.evolveum.prism.xml.ns._public.types_3.ItemPathType;

public class PagingConvertor {

    public static ObjectPaging createObjectPaging(PagingType pagingType) {
        if (pagingType == null) {
            return null;
        }
        if (pagingType.getOrderBy() != null) {
            return PrismContext.get().queryFactory().createPaging(pagingType.getOffset(), pagingType.getMaxSize(),
                    pagingType.getOrderBy().getItemPath(), toOrderDirection(pagingType.getOrderDirection()));
        } else {
            return PrismContext.get().queryFactory().createPaging(pagingType.getOffset(), pagingType.getMaxSize());
        }
    }

    private static OrderDirection toOrderDirection(OrderDirectionType directionType) {
        if (directionType == null) {
            return null;
        }

        if (OrderDirectionType.ASCENDING == directionType) {
            return OrderDirection.ASCENDING;
        }
        if (OrderDirectionType.DESCENDING == directionType) {
            return OrderDirection.DESCENDING;
        }
        return null;
    }

    public static PagingType createPagingType(ObjectPaging paging) {
        if (paging == null) {
            return null;
        }
        PagingType pagingType = new PagingType();
        pagingType
                .setOrderDirection(toOrderDirectionType(paging.getPrimaryOrderingDirection()));
        pagingType.setMaxSize(paging.getMaxSize());
        pagingType.setOffset(paging.getOffset());
        if (paging.getPrimaryOrderingPath() != null) {
            pagingType.setOrderBy(new ItemPathType(paging.getPrimaryOrderingPath()));
        }
        return pagingType;
    }

    private static OrderDirectionType toOrderDirectionType(OrderDirection direction) {
        if (direction == null) {
            return null;
        }

        if (OrderDirection.ASCENDING == direction) {
            return OrderDirectionType.ASCENDING;
        }
        if (OrderDirection.DESCENDING == direction) {
            return OrderDirectionType.DESCENDING;
        }
        return null;
    }
}
