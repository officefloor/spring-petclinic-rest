package org.springframework.samples.petclinic.util;

/**
 * American Soundex phonetic encoding of a name: a letter followed by three digits (e.g.
 * {@code "Robert"} and {@code "Rupert"} both encode to {@code "R163"}). Shared so every
 * derivation that groups owners by how a surname sounds — the identity key and the
 * soft-duplicate match — encodes it identically.
 */
public final class Soundex {

    private Soundex() {
    }

    /**
     * Soundex code for {@code value}: its first letter (upper-cased) followed by three digits
     * derived from the remaining consonants, zero-padded or truncated to length four. Non-letters
     * are ignored; a {@code null} or letter-free value yields an empty string.
     *
     * <p>Adjacent letters mapping to the same digit contribute it once; {@code 'h'} and {@code 'w'}
     * do not break that adjacency, while a vowel does, so a code repeated across a vowel is emitted
     * twice.
     */
    public static String encode(String value) {
        if (value == null) {
            return "";
        }
        StringBuilder letters = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (Character.isLetter(c)) {
                letters.append(Character.toUpperCase(c));
            }
        }
        if (letters.length() == 0) {
            return "";
        }
        StringBuilder code = new StringBuilder().append(letters.charAt(0));
        char previous = codeOf(letters.charAt(0));
        for (int i = 1; i < letters.length() && code.length() < 4; i++) {
            char c = letters.charAt(i);
            if (c == 'H' || c == 'W') {
                continue; // keeps the surrounding consonants adjacent, leaves previous unchanged
            }
            char digit = codeOf(c);
            if (digit != '0' && digit != previous) {
                code.append(digit);
            }
            previous = digit; // a vowel (digit '0') resets, so a repeated code across it counts twice
        }
        while (code.length() < 4) {
            code.append('0');
        }
        return code.toString();
    }

    /** The Soundex digit for a letter, or {@code '0'} for vowels and other uncoded letters. */
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
