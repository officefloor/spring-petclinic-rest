package org.springframework.samples.petclinic.model;

/**
 * American Soundex phonetic encoding of a name. Two spellings that sound alike (e.g. {@code "Smith"}
 * and {@code "Smyth"}) share a code, which is why the owner identity key hashes the soundex of the
 * last name rather than its exact text. A code is the first letter followed by three digits, e.g.
 * {@code "Robert"} → {@code "R163"}; short names are padded with trailing zeros.
 */
public final class Soundex {

    private Soundex() {
    }

    /**
     * The four-character Soundex code of {@code name}: its first letter followed by up to three
     * digits derived from the remaining consonants. Non-letters are ignored; letters with the same
     * code are collapsed unless separated by a vowel (H and W do not separate). Returns the empty
     * string when {@code name} contains no letters.
     */
    public static String encode(String name) {
        if (name == null) {
            return "";
        }
        StringBuilder letters = new StringBuilder();
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
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
            char letter = letters.charAt(i);
            char digit = codeOf(letter);
            if (digit == '0') {
                // Vowels separate consonants (reset), but H and W do not.
                if (letter != 'H' && letter != 'W') {
                    previous = '0';
                }
            } else {
                if (digit != previous) {
                    code.append(digit);
                }
                previous = digit;
            }
        }
        while (code.length() < 4) {
            code.append('0');
        }
        return code.toString();
    }

    /** The Soundex digit for a letter; {@code '0'} for vowels and the non-coding letters H, W, Y. */
    private static char codeOf(char letter) {
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
