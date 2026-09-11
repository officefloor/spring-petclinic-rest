package org.springframework.samples.petclinic.rest.function.owner;

/**
 * American Soundex encoding of a name — a phonetic code (a leading letter followed by three
 * digits) that folds names sounding alike onto the same value. It is the last-name component
 * of an owner's {@link IdentityKeys identity key} and the phonetic axis of the soft-match in
 * {@link AssignPossibleDuplicate}, so centralising it here keeps both in exact agreement.
 *
 * <p>Non-letters are ignored; a null or letterless value yields {@code "0000"}. Consonants
 * with the same code are collapsed when adjacent or separated only by {@code H}/{@code W},
 * but coded twice when separated by a vowel — the standard algorithm.
 */
public final class Soundex {

    private Soundex() {
    }

    /** The Soundex code (a letter followed by three digits) for {@code name}. */
    public static String encode(String name) {
        if (name == null) {
            return "0000";
        }
        StringBuilder letters = new StringBuilder();
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (Character.isLetter(c)) {
                letters.append(Character.toUpperCase(c));
            }
        }
        if (letters.length() == 0) {
            return "0000";
        }
        StringBuilder code = new StringBuilder();
        code.append(letters.charAt(0));
        char previous = codeOf(letters.charAt(0));
        for (int i = 1; i < letters.length() && code.length() < 4; i++) {
            char c = letters.charAt(i);
            if (c == 'H' || c == 'W') {
                // H and W do not separate: keep the previous code so a repeat stays collapsed.
                continue;
            }
            char digit = codeOf(c);
            if (digit != '0' && digit != previous) {
                code.append(digit);
            }
            // A vowel resets the previous code, so a later same-code consonant IS coded twice.
            previous = digit;
        }
        while (code.length() < 4) {
            code.append('0');
        }
        return code.toString();
    }

    /** The Soundex digit for a letter; {@code '0'} for vowels (A E I O U Y) and H/W. */
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
