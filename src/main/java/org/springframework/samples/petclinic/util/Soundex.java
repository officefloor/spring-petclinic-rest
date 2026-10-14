package org.springframework.samples.petclinic.util;

import java.util.Locale;

/**
 * American Soundex phonetic codes, e.g. 'Robert' and 'Rupert' both become 'R163'.
 */
public final class Soundex {

    /** Soundex digit for each letter A-Z; '0' marks vowels and 'Y', '-' marks 'H' and 'W'. */
    private static final String CODES = "0123012-02245501262301-202";

    private Soundex() {
    }

    /**
     * The Soundex code of a name: its first letter followed by three digits for the following
     * consonants, padded with zeros. Non-letters are ignored; adjacent letters with the same digit,
     * including those separated only by 'H' or 'W', are coded once, while vowels separate them.
     *
     * @param name the name, may be {@code null}
     * @return the four-character code, or the empty string when the name contains no letter A-Z
     */
    public static String of(String name) {
        if (name == null) {
            return "";
        }
        String letters = name.toUpperCase(Locale.ROOT).replaceAll("[^A-Z]", "");
        if (letters.isEmpty()) {
            return "";
        }
        StringBuilder code = new StringBuilder().append(letters.charAt(0));
        char previous = codeOf(letters.charAt(0));
        for (int i = 1; i < letters.length() && code.length() < 4; i++) {
            char digit = codeOf(letters.charAt(i));
            if (digit == '-') {
                continue;
            }
            if (digit != '0' && digit != previous) {
                code.append(digit);
            }
            previous = digit;
        }
        while (code.length() < 4) {
            code.append('0');
        }
        return code.toString();
    }

    private static char codeOf(char letter) {
        return CODES.charAt(letter - 'A');
    }
}
