package org.springframework.samples.petclinic.rest.function.owner;

/**
 * American Soundex of a name: a self-contained, reusable name-similarity rule. The code is the
 * retained first letter followed by three digits (zero-padded), with vowels and {@code h}/{@code w}
 * not coded and adjacent letters sharing a code collapsed. Returns empty for a null or letterless
 * value.
 *
 * <p>Single-sources name-matching for both the identity key's last-name component (see
 * {@link OwnerIdentity}) and the soft-duplicate name match (see {@link FlagPossibleDuplicate}), so
 * the two can never disagree on when two surnames sound alike.
 */
final class Soundex {

    private Soundex() {
    }

    /** The American Soundex code of {@code value}. */
    static String of(String value) {
        if (value == null) {
            return "";
        }
        StringBuilder letters = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char c = Character.toUpperCase(value.charAt(i));
            if (c >= 'A' && c <= 'Z') {
                letters.append(c);
            }
        }
        if (letters.length() == 0) {
            return "";
        }
        StringBuilder code = new StringBuilder();
        code.append(letters.charAt(0));
        char prev = charCode(letters.charAt(0));
        for (int i = 1; i < letters.length() && code.length() < 4; i++) {
            char c = letters.charAt(i);
            if (c == 'H' || c == 'W') {
                // Transparent: letters separated by H or W are treated as adjacent.
                continue;
            }
            char cc = charCode(c);
            if (cc != '0' && cc != prev) {
                code.append(cc);
            }
            prev = cc;
        }
        while (code.length() < 4) {
            code.append('0');
        }
        return code.toString();
    }

    /** American Soundex digit for an upper-case letter; {@code '0'} for vowels and h/w. */
    private static char charCode(char c) {
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
