package org.springframework.samples.petclinic.model;

import java.util.Locale;

/**
 * Standard American Soundex encoding of a name: the first letter (upper-cased) followed by three
 * consonant-group digits (padded with zeros), so names that sound alike share a code. Used by the
 * owner {@link Owner#getIdentityKey() identity key} and the soft-match rule.
 *
 * <ul>
 *   <li>b, f, p, v &rarr; 1; c, g, j, k, q, s, x, z &rarr; 2; d, t &rarr; 3; l &rarr; 4;
 *       m, n &rarr; 5; r &rarr; 6.</li>
 *   <li>a, e, i, o, u, y, h, w are not coded. Adjacent letters with the same code (and letters with
 *       the same code separated only by h or w) collapse to a single digit; a vowel between them
 *       keeps both.</li>
 * </ul>
 */
public final class Soundex {

    private Soundex() {
    }

    /** The Soundex code of {@code value} (e.g. "Robert" &rarr; "R163"), or "" when it has no letters. */
    public static String encode(String value) {
        if (value == null) {
            return "";
        }
        StringBuilder letters = new StringBuilder();
        for (char c : value.toUpperCase(Locale.ROOT).toCharArray()) {
            if (c >= 'A' && c <= 'Z') {
                letters.append(c);
            }
        }
        if (letters.length() == 0) {
            return "";
        }
        StringBuilder code = new StringBuilder().append(letters.charAt(0));
        char prevCode = codeOf(letters.charAt(0));
        for (int i = 1; i < letters.length() && code.length() < 4; i++) {
            char c = letters.charAt(i);
            char cc = codeOf(c);
            if (cc != '0' && cc != prevCode) {
                code.append(cc);
            }
            // h and w are transparent: they do not reset the previous code, so same-coded letters
            // on either side still collapse. Every other letter (including vowels) resets it.
            if (c != 'H' && c != 'W') {
                prevCode = cc;
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
            default: // A, E, I, O, U, Y, H, W
                return '0';
        }
    }
}
