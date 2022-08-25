/*
 * Copyright (C) 2010-2022 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.prism.query.fuzzy;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.*;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;
import org.assertj.core.api.Assertions;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.evolveum.midpoint.tools.testng.AbstractUnitTest;

public class TriGramSimilarityComputerTest extends AbstractUnitTest {

    private static final DecimalFormat df = new DecimalFormat("0.000");

    protected static final String TEST_RESOURCES_PATH = "src/test/resources";
    protected static final File TEST_RESOURCES_DIR = new File(TEST_RESOURCES_PATH);
    protected static final File TEST_DIR = new File(TEST_RESOURCES_DIR, "fuzzy-match-data.csv");
    private static final int NUMBER_OF_OBJECTS = 1000;
    private static final String stringSplitter = ",";

    private static List<String[]> resourceData;

    @BeforeClass
    public void initialize() throws IOException, CsvException {
        resourceData = getResourceData();
    }

    @Test(dataProvider = "data-provider")
    private void computeTrigramSimilarityTest(String lObject, String rObject, String result) {
        String trigramSimilarity = df.format(TriGramSimilarityComputer.getSimilarity(lObject, rObject));
        Assertions.assertThat(trigramSimilarity).isEqualTo(result);
    }

    @Test(dataProvider = "data-provider")
    private void trigramGenerateTest(String lObject, String rObject, String result, String lTriGrams, String rTriGrams) {
        List<String> sampleTriGramL = new ArrayList<>(Arrays.asList(lTriGrams.split(stringSplitter)));
        List<String> sampleTriGramR = new ArrayList<>(Arrays.asList(rTriGrams.split(stringSplitter)));

        List<String> generatedTriGramL = TriGramSimilarityComputer.generateTriGram(lObject);
        List<String> generatedTriGramR = TriGramSimilarityComputer.generateTriGram(rObject);

        boolean isEqualL = isEqual(generatedTriGramL, sampleTriGramL);
        boolean isEqualR = isEqual(generatedTriGramR, sampleTriGramR);

        Assert.assertTrue(isEqualL);
        Assert.assertTrue(isEqualR);
    }

    @Test(dataProvider = "data-provider")
    private void computeSimilarityFromTrigramTest(
            String lObject, String rObject, String result, String lTriGrams, String rTriGrams) {

        String trigramSimilarity = df.format(TriGramSimilarityComputer.getSimilarity(lObject, rObject));

        List<String> sampleTriGramL = new ArrayList<>(Arrays.asList(lTriGrams.split(stringSplitter)));
        List<String> sampleTriGramR = new ArrayList<>(Arrays.asList(rTriGrams.split(stringSplitter)));

        double intersectionListSize = intersection(sampleTriGramL, sampleTriGramR).size();
        double unionListSize = union(sampleTriGramL, sampleTriGramR).size();

        String similarity = df.format(intersectionListSize / unionListSize);

        Assertions.assertThat(similarity).isEqualTo(result);
        Assertions.assertThat(similarity).isEqualTo(trigramSimilarity);
    }

    @DataProvider(name = "data-provider")
    private Object[][] dpMethod(Method method) {

        int lObjectIterator = 0;
        int rObjectIterator = 1;
        int similarity = 2;
        int lTriGramIterator = 4;
        int rTriGramIterator = 5;

        Object[][] csvDataObject;

        switch (method.getName()) {
            case "trigramGenerateTest":
            case "computeSimilarityFromTrigramTest":
                csvDataObject = new Object[NUMBER_OF_OBJECTS][5];
                for (int i = 0; i < NUMBER_OF_OBJECTS; i++) {
                    int iterator = i + 1;
                    csvDataObject[i][0] = resourceData.get(iterator)[lObjectIterator];
                    csvDataObject[i][1] = resourceData.get(iterator)[rObjectIterator];
                    csvDataObject[i][2] = resourceData.get(iterator)[similarity];
                    csvDataObject[i][3] = resourceData.get(iterator)[lTriGramIterator];
                    csvDataObject[i][4] = resourceData.get(iterator)[rTriGramIterator];
                }
                return csvDataObject;
            case "computeTrigramSimilarityTest":
                csvDataObject = new Object[NUMBER_OF_OBJECTS][3];

                for (int i = 0; i < NUMBER_OF_OBJECTS; i++) {
                    int iterator = i + 1;
                    csvDataObject[i][0] = resourceData.get(iterator)[lObjectIterator];
                    csvDataObject[i][1] = resourceData.get(iterator)[rObjectIterator];
                    csvDataObject[i][2] = resourceData.get(iterator)[similarity];
                }
                return csvDataObject;
        }
        return null;
    }

    private static boolean isEqual(List<String> x, List<String> y) {
        if (x == null || y == null) {
            return false;
        }
        return x.containsAll(y) && y.containsAll(x);
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

    private static <T> List<T> union(List<T> list1, List<T> list2) {
        Set<T> set = new HashSet<>();
        set.addAll(list1);
        set.addAll(list2);
        return new ArrayList<>(set);
    }

    private static <T> List<T> intersection(List<T> list1, List<T> list2) {
        List<T> list = new ArrayList<>(list1);
        list.retainAll(list2);
        return list;
    }

}
