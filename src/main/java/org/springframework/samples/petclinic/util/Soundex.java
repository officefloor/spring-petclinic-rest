package org.springframework.samples.petclinic.util;

import java.util.Locale;

/**
 * American Soundex encoding of a name: its first letter followed by up to three digits
 * that phonetically code the remaining consonants. Deterministic, so names that sound
 * alike resolve to the same code. Blank or letterless input yields {@code "0000"}.
 */
public final class Soundex {

    /** Soundex digit for each letter A..Z; '0' marks vowels and non-coded letters. */
    private static final String CODES = "01230120022455012623010202";

    private Soundex() {
    }

    public static String of(String name) {
        String letters = (name == null ? "" : name).toUpperCase(Locale.ROOT).replaceAll("[^A-Z]", "");
        if (letters.isEmpty()) {
            return "0000";
        }
        StringBuilder sb = new StringBuilder().append(letters.charAt(0));
        char prev = codeOf(letters.charAt(0));
        for (int i = 1; i < letters.length() && sb.length() < 4; i++) {
            char code = codeOf(letters.charAt(i));
            if (code != '0' && code != prev) {
                sb.append(code);
            }
            prev = code;
        }
        while (sb.length() < 4) {
            sb.append('0');
        }
        return sb.toString();
    }

    private static char codeOf(char letter) {
        return CODES.charAt(letter - 'A');
    }
}
