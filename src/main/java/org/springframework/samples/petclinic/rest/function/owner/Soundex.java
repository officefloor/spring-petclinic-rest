package org.springframework.samples.petclinic.rest.function.owner;

/**
 * The American Soundex phonetic encoding of a name: a letter followed by three digits that
 * collapses similar-sounding surnames onto the same code. Used to key identity and soft
 * duplicate matching on how a last name <em>sounds</em> rather than on its exact spelling, so
 * that spelling variants of one surname share a code.
 */
final class Soundex {

    private Soundex() {
    }

    /**
     * The four-character Soundex code of {@code name} (first letter, then three digits, zero
     * padded). Non-letters are ignored; {@code h} and {@code w} are transparent (they do not
     * separate two consonants sharing a code) while vowels and {@code y} do separate them.
     * Returns an empty string when {@code name} holds no letters.
     */
    static String encode(String name) {
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
        char previous = digitOf(letters.charAt(0));
        for (int i = 1; i < letters.length() && code.length() < 4; i++) {
            char c = letters.charAt(i);
            if (c == 'H' || c == 'W') {
                continue; // transparent: leave the previous code in place
            }
            char digit = digitOf(c);
            if (digit != '0' && digit != previous) {
                code.append(digit);
            }
            previous = digit;
        }
        while (code.length() < 4) {
            code.append('0');
        }
        return code.toString();
    }

    /**
     * The Soundex digit for {@code letter}: {@code '1'}-{@code '6'} for a coded consonant,
     * {@code '0'} for a vowel or any letter (including {@code y}) that carries no code.
     */
    private static char digitOf(char letter) {
        switch (letter) {
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
