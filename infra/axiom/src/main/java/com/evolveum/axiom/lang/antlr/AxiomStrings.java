package com.evolveum.axiom.lang.antlr;

import java.io.IOException;
import java.io.StringWriter;

import org.apache.commons.text.translate.LookupTranslator;

import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;

public class AxiomStrings {

    private static final String ESCAPE = "\\";
    private static final String SQUOTE = "'";
    private static final String BACKTICK = "`";
    private static final String DQUOTE = "\"";
    public static final String TRIPLE_BACKTICK = "```";
    public static final String TRIPLE_DQOUTE = "\"\"\"";

    private static final BiMap<CharSequence, CharSequence> SINGLE_QUOTE_MAP = ImmutableBiMap.<CharSequence, CharSequence>builder()
            .put(SQUOTE, ESCAPE + SQUOTE)
            .build();

    private static final BiMap<CharSequence, CharSequence> DOUBLE_QUOTE_MAP = ImmutableBiMap.<CharSequence, CharSequence>builder()
            .put(DQUOTE, ESCAPE + DQUOTE)
            .build();

    private static final BiMap<CharSequence, CharSequence> BACKTICK_MAP = ImmutableBiMap.<CharSequence, CharSequence>builder()
            .put(BACKTICK, ESCAPE + BACKTICK)
            .build();


    private static final LookupTranslator SINGLE_QUOTED_ESCAPE = new LookupTranslator(SINGLE_QUOTE_MAP);
    private static final LookupTranslator SINGLE_QUOTED_UNESCAPE = new LookupTranslator(SINGLE_QUOTE_MAP.inverse());

    private static final LookupTranslator DOUBLE_QUOTED_ESCAPE = new LookupTranslator(DOUBLE_QUOTE_MAP);
    private static final LookupTranslator DOUBLE_QUOTED_UNESCAPE = new LookupTranslator(DOUBLE_QUOTE_MAP.inverse());

    private static final LookupTranslator BACKTICK_ESCAPE = new LookupTranslator(BACKTICK_MAP);
    private static final LookupTranslator BACKTICK_UNESCAPE = new LookupTranslator(BACKTICK_MAP.inverse());


    public static String fromSingleQuoted(String input) {
        return unescape(SQUOTE, SINGLE_QUOTED_UNESCAPE, input);
    }


    public static String fromDoubleQuoted(String input) {
        return unescape(DQUOTE, DOUBLE_QUOTED_UNESCAPE, input);
    }

    public static String toSingleQuoted(String input) {
        return escape(SQUOTE, SINGLE_QUOTED_ESCAPE, input);
    }

    public static String toDoubleQuoted(String input) {
        return escape(DQUOTE, DOUBLE_QUOTED_ESCAPE, input);
    }

    public static String fromSingleBacktick(String input) {
        return unescape(BACKTICK, BACKTICK_UNESCAPE, input);
    }

    public static String toSingleBacktick(String input) {
        return escape(BACKTICK, BACKTICK_ESCAPE, input);
    }

    public static String removeQuotes(String quote, String withQuotes) {
        withQuotes = withQuotes.trim();
        Preconditions.checkArgument(isQuoted(withQuotes, quote), "String `%s` must be quoted with '%s' quotes", quote, withQuotes);
        return withQuotes.substring(quote.length(), withQuotes.length() - quote.length());
    }

    private static String unescape(String quote, LookupTranslator unescaper, String withQuotes) {
        var withoutQuotes = removeQuotes(quote, withQuotes);
        return unescaper.translate(withoutQuotes);
    }

    private static boolean isQuoted(String withQuotes, String quote) {
        return withQuotes.length() >= 2*quote.length() // Both quotes must be written
                && withQuotes.startsWith(quote) // Must starts with quote
                && withQuotes.endsWith(quote); // Must end with quote
    }

    private static String escape(String quotes, LookupTranslator escaper, String input) {
        var output = new StringWriter(input.length() + 2* quotes.length());
        try {
            output.append(quotes);
            escaper.translate(input, output);
            output.append(quotes);
            return output.toString();
        } catch (IOException e) {
            throw new IllegalStateException("Should not happen.",e);
        }
    }

}
