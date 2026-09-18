package org.springframework.samples.petclinic.rest.function.owner;

/**
 * The single definition of the American Soundex code of a name: its first letter followed by
 * three digits that encode the remaining consonant sounds, so names that sound alike share a
 * code. Used by the owner {@link IdentityKey} (which hashes the code, not the raw last name)
 * and by the {@link PossibleDuplicate} soft match, so both agree on when two surnames are
 * phonetically the same.
 *
 * <p>Pure function of its input; no state. Letters are the only characters considered; a value
 * with no letters yields the all-zero code {@code "0000"}.
 */
final class Soundex {

    /** Length of a Soundex code: an initial letter plus three digits. */
    private static final int CODE_LENGTH = 4;

    private Soundex() {
    }

    /** The Soundex code of {@code name}: an upper-case initial letter plus three digits. */
    static String of(String name) {
        if (name == null) {
            return "0000";
        }
        StringBuilder code = new StringBuilder(CODE_LENGTH);
        int previous = 0;
        for (int i = 0; i < name.length() && code.length() < CODE_LENGTH; i++) {
            char c = Character.toUpperCase(name.charAt(i));
            if (c < 'A' || c > 'Z') {
                continue; // only letters are encoded
            }
            if (code.length() == 0) {
                code.append(c); // retain the first letter verbatim
                previous = digit(c);
                continue;
            }
            if (c == 'H' || c == 'W') {
                continue; // spanned by H/W: leave the previous digit in force
            }
            int digit = digit(c);
            if (digit != 0 && digit != previous) {
                code.append(digit);
            }
            previous = digit; // a vowel (digit 0) resets, so a repeat across it is coded twice
        }
        if (code.length() == 0) {
            return "0000"; // no letters at all
        }
        while (code.length() < CODE_LENGTH) {
            code.append('0');
        }
        return code.toString();
    }

    /** The Soundex digit of an upper-case letter, or 0 for a letter that is not coded. */
    private static int digit(char c) {
        switch (c) {
            case 'B': case 'F': case 'P': case 'V':
                return 1;
            case 'C': case 'G': case 'J': case 'K': case 'Q': case 'S': case 'X': case 'Z':
                return 2;
            case 'D': case 'T':
                return 3;
            case 'L':
                return 4;
            case 'M': case 'N':
                return 5;
            case 'R':
                return 6;
            default:
                return 0; // vowels, H, W, Y
        }
    }
}
