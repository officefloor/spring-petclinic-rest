package org.springframework.samples.petclinic.util;

/**
 * American Soundex phonetic coding of a surname: the first letter followed by three
 * digits, zero-padded and truncated to length four (e.g. {@code Robert -> R163}).
 * A null or letter-free value codes to the empty string.
 */
public final class Soundex {

    /** Per-letter code for A..Z; '0' marks a letter that is not coded. */
    private static final String MAP = "01230120022455012623010202";

    private Soundex() {
    }

    /** The four-character Soundex code of {@code value}, or "" when it has no letters. */
    public static String of(String value) {
        if (value == null) {
            return "";
        }
        StringBuilder letters = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char c = Character.toUpperCase(value.charAt(i));
            if (c >= 'A' && c <= 'Z') {
                letters.append(c);
            }
        }
        if (letters.length() == 0) {
            return "";
        }
        StringBuilder out = new StringBuilder().append(letters.charAt(0));
        char prev = MAP.charAt(letters.charAt(0) - 'A');
        for (int i = 1; i < letters.length() && out.length() < 4; i++) {
            char c = letters.charAt(i);
            char code = MAP.charAt(c - 'A');
            if (code != '0' && code != prev) {
                out.append(code);
            }
            if (c != 'H' && c != 'W') {
                prev = code;
            }
        }
        while (out.length() < 4) {
            out.append('0');
        }
        return out.toString();
    }
}
