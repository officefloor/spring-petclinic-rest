package org.springframework.samples.petclinic.model;

/**
 * The standard American Soundex encoding of a name — a phonetic code (a retained first
 * letter followed by three digits) that maps similarly-sounding surnames to the same value.
 * It is the {@code soundex(lastName)} segment of an owner's {@code identityKey} (see
 * {@link IdentityKey}) and the phonetic key the soft-match compares on.
 *
 * <p>Letters are folded to digits — B/F/P/V→1, C/G/J/K/Q/S/X/Z→2, D/T→3, L→4, M/N→5, R→6 —
 * while vowels and Y/H/W code to 0. Adjacent letters sharing a digit collapse to one, and a
 * pair split only by H or W is likewise treated as adjacent. A null or letter-free value
 * yields {@code "0000"}.
 */
public final class Soundex {

    private Soundex() {
    }

    /** The 4-character Soundex code of {@code name}, or {@code "0000"} when it has no letters. */
    public static String of(String name) {
        if (name == null) {
            return "0000";
        }
        String letters = name.toUpperCase().replaceAll("[^A-Z]", "");
        if (letters.isEmpty()) {
            return "0000";
        }
        StringBuilder code = new StringBuilder();
        code.append(letters.charAt(0));
        char previous = digit(letters.charAt(0));
        for (int i = 1; i < letters.length() && code.length() < 4; i++) {
            char c = letters.charAt(i);
            char d = digit(c);
            if (d != '0' && d != previous) {
                code.append(d);
            }
            // H and W are transparent: they neither code nor reset the previous digit, so a
            // consonant pair split only by H/W still collapses. Every other letter (including
            // vowels) resets it.
            if (c != 'H' && c != 'W') {
                previous = d;
            }
        }
        while (code.length() < 4) {
            code.append('0');
        }
        return code.toString();
    }

    private static char digit(char c) {
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
            default: // A E I O U Y H W
                return '0';
        }
    }
}
