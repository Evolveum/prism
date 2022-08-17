/*
 * Copyright (C) 2010-2022 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.prism.query.fuzzy;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.evolveum.midpoint.tools.testng.AbstractUnitTest;

public class TriGramSimilarityComputerTest extends AbstractUnitTest {

    @Test(dataProvider = "sample")
    public void test(String first, String second, double result) {
        assertThat(TriGramSimilarityComputer.getSimilarity(first, second)).isEqualTo(result);
    }

    @DataProvider(name = "sample")
    public Object[][] sampleData() {
        return new Object[][] {
                new Object[] {"Nejake meno", "ine meno", 0.3125},
        };
    }
}
