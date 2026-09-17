package org.springframework.samples.petclinic.util;

/**
 * American Soundex: a phonetic index that maps a name to a letter plus three digits, so that names
 * that sound alike share a code. A pure function of its input with no dependency on other owners,
 * used as the surname component of the owner identity key (see
 * {@link org.springframework.samples.petclinic.rest.function.owner.IdentityKey}) and to group
 * like-sounding surnames for the possible-duplicate soft match
 * ({@link org.springframework.samples.petclinic.rest.function.owner.FlagPossibleDuplicate}).
 */
public final class Soundex {

    private Soundex() {
    }

    /**
     * The 4-character Soundex code of {@code value} (first letter plus three digits, zero padded), or
     * the empty string when {@code value} holds no letters.
     */
    public static String of(String value) {
        if (value == null) {
            return "";
        }
        String letters = value.toUpperCase().replaceAll("[^A-Z]", "");
        if (letters.isEmpty()) {
            return "";
        }
        StringBuilder code = new StringBuilder().append(letters.charAt(0));
        char previous = codeOf(letters.charAt(0));
        for (int i = 1; i < letters.length() && code.length() < 4; i++) {
            char letter = letters.charAt(i);
            // H and W are transparent: they neither code nor separate, so consonants they sit between
            // still merge if they share a digit.
            if (letter == 'H' || letter == 'W') {
                continue;
            }
            char digit = codeOf(letter);
            if (digit != '0' && digit != previous) {
                code.append(digit);
            }
            // Vowels (digit '0') separate, so an equal-digit consonant after a vowel is coded again.
            previous = digit;
        }
        while (code.length() < 4) {
            code.append('0');
        }
        return code.toString();
    }

    /** The Soundex digit for {@code letter}; '0' for vowels and other non-coded letters. */
    private static char codeOf(char letter) {
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
