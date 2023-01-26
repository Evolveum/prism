/*
 * Copyright (C) 2010-2023 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.prism.query;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;

import com.evolveum.midpoint.prism.AbstractPrismTest;
import com.evolveum.midpoint.prism.Referencable;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.prism.xml.ns._public.query_3.SearchFilterType;

public class QueryConverterTest extends AbstractPrismTest {

    @Test
    public void test900ReferenceSearchFilter() throws SchemaException {
        given("reference search object filter");
        ObjectFilter origFilter = queryParser().parseFilter(Referencable.class,
                ". ownedBy (@type = UserType and @path = accountRef)");

        when("converting it to search filter and back");
        QueryConverter queryConverter = getPrismContext().getQueryConverter();
        SearchFilterType searchFilter = queryConverter.createSearchFilterType(origFilter);
        ObjectFilter finalFilter = queryConverter.createObjectFilter(Referencable.class, searchFilter);

        then("the final filter is still the same like the original one");
        assertThat(origFilter).isEqualTo(finalFilter);
    }

}
