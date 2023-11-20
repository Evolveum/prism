/*
 * Copyright (c) 2010-2018 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.prism.impl.polystring;

import java.text.Normalizer;
import java.util.Objects;
import java.util.regex.Pattern;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.evolveum.midpoint.prism.polystring.PolyStringNormalizer;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.prism.xml.ns._public.types_3.PolyStringNormalizerConfigurationType;

public abstract class AbstractConfigurablePolyStringNormalizer
        extends BaseStringNormalizer
        implements PolyStringNormalizer, ConfigurableNormalizer {

    private static final String WHITESPACE_REGEX = "\\s+";
    private static final Pattern WHITESPACE_PATTERN = Pattern.compile(WHITESPACE_REGEX);

    private PolyStringNormalizerConfigurationType configuration;

    @Override
    public void configure(PolyStringNormalizerConfigurationType configuration) {
        this.configuration = configuration;
    }

    protected PolyStringNormalizerConfigurationType getConfiguration() {
        return configuration;
    }

    /** Unicode Normalization Form Compatibility Decomposition (NFKD) */
    private @NotNull String nfkd(@NotNull String s) {
        return Normalizer.normalize(s, Normalizer.Form.NFKD);
    }

    protected @NotNull String replaceAll(@NotNull String s, Pattern pattern, String replacement) {
        return pattern.matcher(s).replaceAll(replacement);
    }

    protected @NotNull String removeAll(@NotNull String s, Pattern pattern) {
        return pattern.matcher(s).replaceAll("");
    }

    @SuppressWarnings("SameParameterValue")
    @NotNull String keepOnly(@NotNull String s, int lowerCode, int upperCode) {
        StringBuilder out = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c >= lowerCode && c <= upperCode) {
                out.append(c);
            }
        }
        return out.toString();
    }

    private @NotNull String trimWhitespace(@NotNull String s) {
        return replaceAll(s, WHITESPACE_PATTERN, " ");
    }

    @Override
    public String normalize(String orig) {
        if (orig == null) {
            return null;
        }
        String s = preprocess(orig);

        s = normalizeCore(s);

        return postprocess(s);
    }

    @Override
    public boolean match(@Nullable String a, @Nullable String b) throws SchemaException {
        return Objects.equals(
                normalize(a),
                normalize(b));
    }

    @Override
    public boolean matchRegex(String a, String regex) throws SchemaException {
        if (a == null) {
            return false;
        }
        return Pattern.matches(regex, normalize(a));
    }

    protected abstract String normalizeCore(String s);

    private @NotNull String preprocess(@NotNull String s) {
        if (configuration == null || !Boolean.FALSE.equals(configuration.isTrim())) {
            s = s.trim();
        }

        if (configuration == null || !Boolean.FALSE.equals(configuration.isNfkd())) {
            s = nfkd(s);
        }
        return s;
    }

    protected @NotNull String postprocess(@NotNull String s) {
        if (configuration == null || !Boolean.FALSE.equals(configuration.isTrimWhitespace())) {
            s = trimWhitespace(s);
            if (s.isBlank()) {
                return "";
            }
        }

        if (configuration == null || !Boolean.FALSE.equals(configuration.isLowercase())) {
            s = s.toLowerCase();
        }

        return s;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getClass().getSimpleName()).append("(");
        if (configuration != null) {
            configuration.shortDump(sb);
        }
        sb.append(")");
        return sb.toString();
    }
}
