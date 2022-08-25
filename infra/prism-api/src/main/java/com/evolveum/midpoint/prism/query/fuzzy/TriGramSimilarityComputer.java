/*
 * Copyright (C) 2010-2022 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.prism.query.fuzzy;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * TODO move to a better place?
 *
 * TODO document a little?
 */
public class TriGramSimilarityComputer {

    private static final int N_GRAM_VALUE = 3;

    public static List<String> generateTriGram(String object) {
        String[] normalizedInput = normalization(object);
        List<String> triGrams = new ArrayList<>();

        for (String preparedString : normalizedInput) {
            for (int j = 0; j < preparedString.length() - N_GRAM_VALUE + 1; j++) {
                String triGramSubstring = preparedString.substring(j, j + N_GRAM_VALUE);

                if (!triGrams.contains(triGramSubstring)) {
                    triGrams.add(triGramSubstring);
                }

            }
        }
        return triGrams;
    }

    private static String[] normalization(String object) {
        String removeNonAlpha = object.replaceAll("[^\\p{Alnum}]", " ");
        String normalizeWhiteSpaces = removeNonAlpha.replaceAll("\\s{2,}", " ").trim();
        String[] strArray = normalizeWhiteSpaces.split(" ");

        for (int i = 0; i < strArray.length; i++) {
            String normalizedString = "  " + strArray[i] + " ";
            strArray[i] = normalizedString.toLowerCase();
        }

        return strArray;
    }

    public static double getSimilarity(String lObject, String rObject) {
        List<String> firstTriGrams = generateTriGram(lObject);
        List<String> secondTriGrams = generateTriGram(rObject);

        List<String> intersectionList = intersection(firstTriGrams, secondTriGrams);
        List<String> unionList = union(firstTriGrams, secondTriGrams);

        double intersectionListSize = intersectionList.size();
        double unionListSize = unionList.size();

        return (intersectionListSize / unionListSize);
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
