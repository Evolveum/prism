/*
 * Copyright (C) 2010-2022 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.prism.query.fuzzy;

import org.apache.commons.text.similarity.LevenshteinDistance;

/**
 * TODO move to a better place?
 *
 * TODO replace by a library method?
 */
public class LevenshteinComputer {

    public static double computeLevenshteinSimilarity(String lObject, String rObject, int levenshteinDistance) {
        return 1 - (((double) levenshteinDistance) / (Math.max(lObject.length(), rObject.length())));
    }

    public static int computeLevenshteinDistance(String lObject, String rObject) {
        return new LevenshteinDistance().apply(lObject, rObject);
    }

}
