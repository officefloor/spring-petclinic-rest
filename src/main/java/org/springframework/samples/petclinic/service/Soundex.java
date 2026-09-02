package org.springframework.samples.petclinic.service;

/**
 * Standard American Soundex encoding of a surname: the first letter is kept, the remaining letters
 * are mapped to consonant digit codes with vowels and adjacent duplicate codes dropped, and the
 * result is padded with zeros or truncated to four characters (e.g. {@code "Robert" -> "R163"}).
 *
 * <p>Extracted as a small, self-contained collaborator so name-similarity rules can key on a
 * surname's phonetic code from one shared implementation.
 */
final class Soundex {

    private Soundex() {
    }

    /** The four-character Soundex code of {@code name}, or {@code ""} when it holds no letters. */
    static String of(String name) {
        if (name == null) {
            return "";
        }
        String letters = name.toUpperCase().replaceAll("[^A-Z]", "");
        if (letters.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder().append(letters.charAt(0));
        char previous = code(letters.charAt(0));
        for (int i = 1; i < letters.length() && sb.length() < 4; i++) {
            char letter = letters.charAt(i);
            char digit = code(letter);
            if (digit != '0' && digit != previous) {
                sb.append(digit);
            }
            if (letter != 'H' && letter != 'W') {
                previous = digit;
            }
        }
        while (sb.length() < 4) {
            sb.append('0');
        }
        return sb.toString();
    }

    /** The Soundex digit for a single upper-case letter ({@code '0'} for vowels and {@code H/W/Y}). */
    private static char code(char letter) {
        return switch (letter) {
            case 'B', 'F', 'P', 'V' -> '1';
            case 'C', 'G', 'J', 'K', 'Q', 'S', 'X', 'Z' -> '2';
            case 'D', 'T' -> '3';
            case 'L' -> '4';
            case 'M', 'N' -> '5';
            case 'R' -> '6';
            default -> '0';
        };
    }
}
