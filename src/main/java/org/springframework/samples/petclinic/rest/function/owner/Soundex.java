package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

/**
 * American Soundex encoding of a surname: the first letter followed by three digits
 * derived from the remaining consonants (vowels, {@code H} and {@code W} are not coded;
 * adjacent duplicate codes collapse). Empty when the name has no letters. Used so
 * similar-sounding last names share a phonetic key.
 */
final class Soundex {

    /** Soundex code per letter A-Z; '0' marks a letter that is not coded. */
    private static final String MAP = "01230120022455012623010202";

    private Soundex() {
    }

    static String of(String name) {
        String letters = name == null ? "" : name.toUpperCase(Locale.ROOT).replaceAll("[^A-Z]", "");
        if (letters.isEmpty()) {
            return "";
        }
        StringBuilder code = new StringBuilder().append(letters.charAt(0));
        char prev = MAP.charAt(letters.charAt(0) - 'A');
        for (int i = 1; i < letters.length() && code.length() < 4; i++) {
            char c = letters.charAt(i);
            char d = MAP.charAt(c - 'A');
            if (d != '0' && d != prev) {
                code.append(d);
            }
            if (c != 'H' && c != 'W') {
                prev = d;
            }
        }
        while (code.length() < 4) {
            code.append('0');
        }
        return code.toString();
    }
}
