package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

/**
 * The standard American Soundex phonetic code of a name: the retained first letter followed by three
 * digits derived from the remaining consonants. Names that sound alike collapse to the same code
 * (e.g. {@code Robert}/{@code Rupert} → {@code R163}), which is exactly what the identity key and the
 * soft-match need to treat spelling variants of a surname as one.
 *
 * <p>Implemented in-house (rather than pulling in a codec dependency) so the algorithm is fixed and
 * self-contained. Non-letters are ignored; a blank or letter-free value yields the empty string.
 */
final class Soundex {

    private Soundex() {
    }

    /** The Soundex code of the value: {@code letter + three digits}, or {@code ""} when it has no letters. */
    static String of(String value) {
        if (value == null) {
            return "";
        }
        String letters = value.toUpperCase(Locale.ROOT).replaceAll("[^A-Z]", "");
        if (letters.isEmpty()) {
            return "";
        }
        StringBuilder code = new StringBuilder(4);
        char first = letters.charAt(0);
        code.append(first);
        char previous = code(first); // guards against coding a second letter equal to the first
        for (int i = 1; i < letters.length() && code.length() < 4; i++) {
            char c = letters.charAt(i);
            if (c == 'H' || c == 'W') {
                continue; // transparent: they neither code nor separate two equal codes
            }
            char digit = code(c);
            if (digit != '0' && digit != previous) {
                code.append(digit);
            }
            previous = digit; // a vowel (digit '0') resets, so a later equal code is emitted again
        }
        while (code.length() < 4) {
            code.append('0');
        }
        return code.toString();
    }

    private static char code(char c) {
        return switch (c) {
            case 'B', 'F', 'P', 'V' -> '1';
            case 'C', 'G', 'J', 'K', 'Q', 'S', 'X', 'Z' -> '2';
            case 'D', 'T' -> '3';
            case 'L' -> '4';
            case 'M', 'N' -> '5';
            case 'R' -> '6';
            default -> '0'; // vowels A, E, I, O, U and Y
        };
    }
}
