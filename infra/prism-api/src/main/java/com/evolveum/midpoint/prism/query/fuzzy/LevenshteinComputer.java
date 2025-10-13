/*
 * Copyright (C) 2010-2022 Evolveum and contributors
 *
 * Licensed under the EUPL-1.2 or later.
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
