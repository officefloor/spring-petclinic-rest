package org.springframework.samples.petclinic.util;

import java.util.Locale;

/**
 * The American Soundex phonetic encoding of a name: a letter followed by three digits (e.g.
 * {@code "Robert"} and {@code "Rupert"} both encode to {@code "R163"}). Names that sound alike
 * share a code, so it groups likely spelling variants of the same surname. Kept separate from
 * any one caller so the algorithm is defined once and reused wherever a phonetic key of a name
 * is needed (the household soft-match and the owner identity key).
 */
public final class Soundex {

    private Soundex() {
    }

    /**
     * The four-character Soundex code of {@code value}: its first letter, upper-cased, followed
     * by three digits derived from the remaining consonants, right-padded with zeros. Returns
     * {@code "0000"} when {@code value} contains no letters (null, blank or punctuation only).
     *
     * @param value the name to encode
     * @return a four-character Soundex code
     */
    public static String of(String value) {
        if (value == null) {
            return "0000";
        }
        String letters = value.toUpperCase(Locale.ROOT).replaceAll("[^A-Z]", "");
        if (letters.isEmpty()) {
            return "0000";
        }
        StringBuilder code = new StringBuilder().append(letters.charAt(0));
        char previousCode = codeOf(letters.charAt(0));
        for (int i = 1; i < letters.length() && code.length() < 4; i++) {
            char c = letters.charAt(i);
            char digit = codeOf(c);
            if (digit != '0' && digit != previousCode) {
                code.append(digit);
            }
            // 'H' and 'W' are transparent: they do not reset the "previous code" used to
            // collapse adjacent equal codes; every other letter does.
            if (c != 'H' && c != 'W') {
                previousCode = digit;
            }
        }
        while (code.length() < 4) {
            code.append('0');
        }
        return code.toString();
    }

    private static char codeOf(char c) {
        switch (c) {
            case 'B': case 'F': case 'P': case 'V':
                return '1';
            case 'C': case 'G': case 'J': case 'K': case 'Q': case 'S': case 'X': case 'Z':
                return '2';
            case 'D': case 'T':
                return '3';
            case 'L':
                return '4';
            case 'M': case 'N':
                return '5';
            case 'R':
                return '6';
            default:
                return '0';
        }
    }
}
