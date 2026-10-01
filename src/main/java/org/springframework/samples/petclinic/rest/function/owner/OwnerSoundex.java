package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

/**
 * Single source of truth for the owner last-name Soundex code — the phonetic key that feeds both the
 * {@link OwnerIdentity identity key} and the {@link FlagOwnerPossibleDuplicate soft-duplicate} rule,
 * so two surnames that sound alike collapse to the same code in both places.
 *
 * <p>Implements the standard American Soundex: keep the first letter, then encode the remaining
 * letters to digits (b,f,p,v→1; c,g,j,k,q,s,x,z→2; d,t→3; l→4; m,n→5; r→6), dropping the vowels
 * (a,e,i,o,u) and y. Adjacent letters mapping to the same digit — and same-digit letters separated
 * only by h or w — collapse to a single digit, while a vowel between them keeps both. The result is
 * a letter followed by exactly three digits, zero-padded or truncated. A {@code null} or
 * letter-free value yields an empty string.
 */
final class OwnerSoundex {

    private OwnerSoundex() {
    }

    static String soundex(String value) {
        if (value == null) {
            return "";
        }
        String letters = value.toUpperCase(Locale.ROOT).replaceAll("[^A-Z]", "");
        if (letters.isEmpty()) {
            return "";
        }
        StringBuilder code = new StringBuilder();
        char first = letters.charAt(0);
        code.append(first);
        // The first letter's own code suppresses an immediately following same-coded consonant.
        char previous = codeOf(first);
        for (int i = 1; i < letters.length() && code.length() < 4; i++) {
            char c = letters.charAt(i);
            char digit = codeOf(c);
            if (digit != '0' && digit != previous) {
                code.append(digit);
            }
            // h and w are transparent: they neither code nor reset the preceding digit; any other
            // letter (coded consonant or vowel) becomes the new "previous" so a vowel separates.
            if (c != 'H' && c != 'W') {
                previous = digit;
            }
        }
        while (code.length() < 4) {
            code.append('0');
        }
        return code.toString();
    }

    private static char codeOf(char c) {
        switch (c) {
            case 'B':
            case 'F':
            case 'P':
            case 'V':
                return '1';
            case 'C':
            case 'G':
            case 'J':
            case 'K':
            case 'Q':
            case 'S':
            case 'X':
            case 'Z':
                return '2';
            case 'D':
            case 'T':
                return '3';
            case 'L':
                return '4';
            case 'M':
            case 'N':
                return '5';
            case 'R':
                return '6';
            default:
                // A, E, I, O, U, Y and the transparent H, W contribute no digit.
                return '0';
        }
    }
}
