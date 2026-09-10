package org.springframework.samples.petclinic.rest.function.common;

import java.util.Locale;

/**
 * The standard American Soundex encoding of a name: the first letter followed by three digits
 * that phonetically encode the remaining consonants, so names that sound alike share a code
 * (e.g. {@code "Robert"} and {@code "Rupert"} both encode to {@code "R163"}).
 *
 * <p>Used by the owner identity key (see
 * {@code org.springframework.samples.petclinic.rest.function.owner.OwnerIdentityKey}) — the last
 * name is folded to its Soundex so phonetically equal surnames collapse — and by the soft-match
 * ("possible duplicate") detection, which fires when two owners share a Soundex last name and a
 * postcode but differ in their whole identity key.
 */
public final class Soundex {

    private Soundex() {
    }

    /**
     * The 4-character Soundex code (a letter and three digits) for {@code value}, using only its
     * letters. Returns {@code "0000"} when the value is null or carries no letters.
     */
    public static String of(String value) {
        if (value == null) {
            return "0000";
        }
        String letters = value.toUpperCase(Locale.ROOT).replaceAll("[^A-Z]", "");
        if (letters.isEmpty()) {
            return "0000";
        }
        StringBuilder code = new StringBuilder();
        code.append(letters.charAt(0));
        char previous = digit(letters.charAt(0));
        for (int i = 1; i < letters.length() && code.length() < 4; i++) {
            char c = letters.charAt(i);
            if (c == 'H' || c == 'W') {
                continue; // ignored, but do not reset the previous digit
            }
            char d = digit(c);
            if (d != '0' && d != previous) {
                code.append(d);
            }
            previous = d; // a vowel (0) resets, so a repeat consonant after it is coded again
        }
        while (code.length() < 4) {
            code.append('0');
        }
        return code.toString();
    }

    /** The Soundex digit for a letter; {@code '0'} for vowels and the ignorable H, W and Y. */
    private static char digit(char c) {
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
