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
import java.text.DecimalFormatSymbols;
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

    private static final String TEST_RESOURCES_PATH = "src/test/resources";
    private static final File TEST_RESOURCES_DIR = new File(TEST_RESOURCES_PATH);
    private static final File TEST_DIR = new File(TEST_RESOURCES_DIR, "fuzzy-match-data.csv");
    private static final String stringSplitter = ",";
    private static final DecimalFormat DECIMAL_FORMAT;

    private static List<String[]> resourceData;

    static {
        // This must be explicit, so it doesn't depend on the locale on the machine.
        DecimalFormatSymbols decimalFormatSymbols = DecimalFormatSymbols.getInstance();
        decimalFormatSymbols.setDecimalSeparator(',');
        DECIMAL_FORMAT = new DecimalFormat("0.000", decimalFormatSymbols);
    }

    @BeforeClass
    public void initialize() throws IOException, CsvException {
        resourceData = getResourceData();
    }

    @Test(dataProvider = "data-provider")
    private void computeTrigramSimilarityTest(String lObject, String rObject, String result) {
        String trigramSimilarity = DECIMAL_FORMAT.format(TriGramSimilarityComputer.getSimilarity(lObject, rObject));
        Assertions.assertThat(trigramSimilarity).isEqualTo(result);
    }

    @Test(dataProvider = "data-provider")
    public void trigramGenerateTest(String lObject, String rObject,
            String ignoredResult, String lTriGrams, String rTriGrams) {
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
    public void computeSimilarityFromTrigramTest(
            String lObject, String rObject, String result, String lTriGrams, String rTriGrams) {

        String trigramSimilarity = DECIMAL_FORMAT.format(TriGramSimilarityComputer.getSimilarity(lObject, rObject));

        List<String> sampleTriGramL = new ArrayList<>(Arrays.asList(lTriGrams.split(stringSplitter)));
        List<String> sampleTriGramR = new ArrayList<>(Arrays.asList(rTriGrams.split(stringSplitter)));

        double intersectionListSize = intersection(sampleTriGramL, sampleTriGramR).size();
        double unionListSize = union(sampleTriGramL, sampleTriGramR).size();

        String similarity = DECIMAL_FORMAT.format(intersectionListSize / unionListSize);

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
        int resourceDataSize = resourceData.size();

        switch (method.getName()) {
            case "trigramGenerateTest":
            case "computeSimilarityFromTrigramTest":
                csvDataObject = new Object[resourceDataSize][5];
                for (int i = 0; i < resourceDataSize; i++) {
                    csvDataObject[i][0] = resourceData.get(i)[lObjectIterator];
                    csvDataObject[i][1] = resourceData.get(i)[rObjectIterator];
                    csvDataObject[i][2] = resourceData.get(i)[similarity];
                    csvDataObject[i][3] = resourceData.get(i)[lTriGramIterator];
                    csvDataObject[i][4] = resourceData.get(i)[rTriGramIterator];
                }
                return csvDataObject;
            case "computeTrigramSimilarityTest":
                csvDataObject = new Object[resourceDataSize][3];

                for (int i = 0; i < resourceDataSize; i++) {
                    csvDataObject[i][0] = resourceData.get(i)[lObjectIterator];
                    csvDataObject[i][1] = resourceData.get(i)[rObjectIterator];
                    csvDataObject[i][2] = resourceData.get(i)[similarity];
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
