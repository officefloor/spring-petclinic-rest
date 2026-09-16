package org.springframework.samples.petclinic.util;

import java.util.Locale;

/**
 * American Soundex phonetic encoding of a name: a letter followed by three digits (e.g.
 * "Robert" and "Rupert" both encode to {@code "R163"}), so names that sound alike collapse
 * to the same code. Used as the surname component of the owner {@link IdentityKey} and to
 * group phonetically-equal surnames when flagging soft duplicates.
 *
 * <p>Implements the standard algorithm: keep the first letter, map each following letter to
 * its consonant digit (dropping vowels and {@code h}/{@code w}), collapse adjacent equal
 * digits, and pad or truncate to four characters. A blank name encodes to the empty string.
 */
public final class Soundex {

    /**
     * The digit each letter A-Z maps to ({@code '0'} for the non-coded letters
     * A E I O U Y H W): the standard US-English Soundex table.
     */
    private static final char[] MAPPING = "01230120022455012623010202".toCharArray();

    private Soundex() {
    }

    /** The Soundex code of {@code name}, or the empty string when it has no letters. */
    public static String of(String name) {
        String cleaned = clean(name);
        if (cleaned.isEmpty()) {
            return "";
        }
        char[] out = { '0', '0', '0', '0' };
        out[0] = cleaned.charAt(0);
        int count = 1;
        int index = 1;
        char last = mappingCode(cleaned, 0);
        while (index < cleaned.length() && count < out.length) {
            char mapped = mappingCode(cleaned, index++);
            if (mapped != 0) {
                if (mapped != '0' && mapped != last) {
                    out[count++] = mapped;
                }
                last = mapped;
            }
        }
        return new String(out);
    }

    /**
     * The mapped digit for the letter at {@code index}, or {@code 0} when it is transparent:
     * a consonant separated from an equal-coded consonant only by an {@code h} or {@code w}
     * is coded once, so the second occurrence is skipped.
     */
    private static char mappingCode(String str, int index) {
        char mapped = map(str.charAt(index));
        if (index > 1 && mapped != '0') {
            char prev = str.charAt(index - 1);
            if (prev == 'H' || prev == 'W') {
                char prePrev = str.charAt(index - 2);
                if (map(prePrev) == mapped || prePrev == 'H' || prePrev == 'W') {
                    return 0;
                }
            }
        }
        return mapped;
    }

    private static char map(char letter) {
        return MAPPING[letter - 'A'];
    }

    /** Upper-case the name and drop everything but the letters A-Z. */
    private static String clean(String value) {
        if (value == null) {
            return "";
        }
        StringBuilder cleaned = new StringBuilder(value.length());
        for (char ch : value.toUpperCase(Locale.ROOT).toCharArray()) {
            if (ch >= 'A' && ch <= 'Z') {
                cleaned.append(ch);
            }
        }
        return cleaned.toString();
    }
}
