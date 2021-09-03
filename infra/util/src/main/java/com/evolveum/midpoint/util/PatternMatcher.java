/*
 * Copyright (C) 2010-2021 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.util;

import com.evolveum.midpoint.util.annotation.Experimental;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Matches a string against a set of patterns that can be either in "naive" format, using "*" as a wildcard,
 * or in regex format. The regex format is used when the string is in the form of ~/.../.
 */
@Experimental
public class PatternMatcher {

    @NotNull private final Collection<Pattern> compiledPatterns;

    private PatternMatcher(@NotNull Collection<Pattern> compiledPatterns) {
        this.compiledPatterns = compiledPatterns;
    }

    /**
     * Creates a matcher.
     */
    public static PatternMatcher create(Collection<String> patterns) {
        return new PatternMatcher(
                patterns.stream()
                        .map(PatternMatcher::compile)
                        .collect(Collectors.toList())
        );
    }

    private static Pattern compile(String pattern) {
        return Pattern.compile(toRegex(pattern));
    }

    private static String toRegex(String pattern) {
        if (pattern.startsWith("~/") && pattern.endsWith("/")) {
            return pattern.substring(2, pattern.length() - 1);
        } else {
            return pattern.replace(".", "\\.").replace("*", ".*");
        }
    }

    public boolean match(String text) {
        return compiledPatterns.stream()
                .anyMatch(p -> p.matcher(text).matches());
    }

    public boolean isEmpty() {
        return compiledPatterns.isEmpty();
    }
}
