/*
 * Copyright (C) 2010-2024 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
 */


package com.evolveum.midpoint.prism.testing;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.path.ItemPath;

import java.util.Objects;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class PrismAsserts2 {

    /** Ignores any non-expression-based values. */
    public static void assertPropertyValueExpressions(PrismContainer<?> container, ItemPath propPath, ExpressionWrapper... expressions) {
        var property = container.getValue().findProperty(propPath);
        var desc = "Property " + propPath + " in " + container;
        assertThat(property).as(desc).isNotNull();
        assertPropertyValueExpressions(property, desc, expressions);
    }

    /** Ignores any non-expression-based values. */
    public static void assertPropertyValueExpressions(PrismProperty<?> property, String desc, ExpressionWrapper... expressions) {
        var realExpressions = property.getValues().stream()
                .map(PrismPropertyValue::getExpression)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        assertThat(realExpressions)
                .as(() -> "Expressions in " + desc)
                .containsExactlyInAnyOrder(expressions);
    }
}
