package org.springframework.samples.petclinic.rest.function.owner;

/**
 * American Soundex encoder used to fold a last name to its phonetic key. The key is the
 * first letter followed by three digits (zero-padded, truncated to four characters), with
 * the usual coding rules: adjacent letters sharing a code collapse to one, {@code H} and
 * {@code W} are transparent (letters either side are treated as adjacent), and the vowels
 * {@code A E I O U Y} act as separators that let an otherwise-repeated code count twice.
 *
 * <p>Used both in the owner {@code identityKey} (see {@link OwnerIdentity}) and in the
 * soft possible-duplicate match (see {@link AssignOwnerPossibleDuplicate}) so both key off
 * the same phonetic surname.
 */
public final class Soundex {

    private Soundex() {
    }

    /** The Soundex code of {@code value}, or {@code ""} when it has no letters (or is null). */
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
        StringBuilder code = new StringBuilder();
        char first = letters.charAt(0);
        code.append(first);
        char prev = codeOf(first);
        for (int i = 1; i < letters.length() && code.length() < 4; i++) {
            char c = letters.charAt(i);
            if (c == 'H' || c == 'W') {
                continue; // transparent: does not change the previous code
            }
            char d = codeOf(c);
            if (d != '0' && d != prev) {
                code.append(d);
            }
            prev = d; // vowels (code '0') reset, so a later matching code counts again
        }
        while (code.length() < 4) {
            code.append('0');
        }
        return code.toString();
    }

    /** Soundex digit for an upper-case letter; {@code '0'} for A E I O U Y (and H, W). */
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
