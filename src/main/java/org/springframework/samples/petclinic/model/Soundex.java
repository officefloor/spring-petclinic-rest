package org.springframework.samples.petclinic.model;

/**
 * The single definition of the American Soundex phonetic code of a name: an upper-case
 * letter followed by three digits (e.g. {@code Robert -> R163}). Names that sound alike map
 * to the same code, so it is the basis for household soft-matching on surname and forms the
 * surname component of an owner's {@link OwnerIdentity identity key}.
 */
public final class Soundex {

    private Soundex() {
    }

    /**
     * The four-character Soundex code of {@code value}: its first letter (upper-cased) followed
     * by three digits derived from the remaining consonants, right-padded with zeros. Non-letter
     * characters are ignored; a null or letter-free value yields the empty string.
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
        char previous = digit(letters.charAt(0));
        for (int i = 1; i < letters.length() && code.length() < 4; i++) {
            char letter = letters.charAt(i);
            char digit = digit(letter);
            if (digit == '0') {
                // 'h' and 'w' are transparent (do not reset the previous digit); a true vowel does.
                if (letter != 'H' && letter != 'W') {
                    previous = '0';
                }
                continue;
            }
            if (digit != previous) {
                code.append(digit);
            }
            previous = digit;
        }
        while (code.length() < 4) {
            code.append('0');
        }
        return code.toString();
    }

    /** The Soundex digit for a consonant, or {@code '0'} for a vowel, 'h', 'w' or 'y'. */
    private static char digit(char letter) {
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
