/*
 * Copyright (C) 2010-2022 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.prism.query.fuzzy;

import com.evolveum.midpoint.tools.testng.AbstractUnitTest;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;
import org.assertj.core.api.Assertions;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class LevenshteinComputerTest extends AbstractUnitTest {

    protected static final String TEST_RESOURCES_PATH = "src/test/resources";
    protected static final File TEST_RESOURCES_DIR = new File(TEST_RESOURCES_PATH);
    protected static final File TEST_DIR = new File(TEST_RESOURCES_DIR, "fuzzy-match-data.csv");

    private static List<String[]> resourceData;

    @BeforeClass
    public void initialize() throws IOException, CsvException {
        resourceData = getResourceData();
    }

    @DataProvider
    public static Object[][] loadObject() {
        int resourceDataSize = resourceData.size();
        Object[][] csvDataObject = new Object[resourceDataSize][3];

        int lObjectIterator = 0;
        int rObjectIterator = 1;
        int distance = 3;

        for (int i = 0; i < resourceDataSize; i++) {
            csvDataObject[i][0] = resourceData.get(i)[lObjectIterator];
            csvDataObject[i][1] = resourceData.get(i)[rObjectIterator];
            csvDataObject[i][2] = Integer.parseInt(resourceData.get(i)[distance]);
        }
        return csvDataObject;
    }

    @Test(dataProvider = "loadObject")
    private void computeLevenshteinDistanceTest(String lObject, String rObject, int result) {
        int distanceSimilarity = LevenshteinComputer.computeLevenshteinDistance(lObject, rObject);
        Assertions.assertThat(distanceSimilarity).isEqualTo(result);
    }

    private static List<String[]> getResourceData() throws IOException, CsvException {
        List<String[]> csvDataList;
        CSVParser csvParser = new CSVParserBuilder().withSeparator(',').withQuoteChar('\"').build();
        try (CSVReader reader = new CSVReaderBuilder(
                new FileReader(TEST_DIR))
                .withCSVParser(csvParser)
                .withSkipLines(1)
                .build()) {
            csvDataList = reader.readAll();
            return csvDataList;
        }
    }
}
