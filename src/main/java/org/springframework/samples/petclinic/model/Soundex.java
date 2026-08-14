package org.springframework.samples.petclinic.model;

/**
 * The American Soundex phonetic encoding of a surname, used as the phonetic component of an owner's
 * {@link Owner#getIdentityKey() identityKey} and as the soft-match key in duplicate detection. Names
 * that sound alike encode to the same four-character code (a letter followed by three digits), so
 * two owners whose surnames are phonetically equal share a Soundex value.
 */
public final class Soundex {

    private Soundex() {
    }

    /**
     * The American Soundex code (an upper-case letter followed by three digits) for {@code value}.
     * A {@code null}, blank or letter-free value yields an empty string.
     */
    public static String encode(String value) {
        if (value == null) {
            return "";
        }
        String letters = value.trim().toUpperCase().replaceAll("[^A-Z]", "");
        if (letters.isEmpty()) {
            return "";
        }
        char first = letters.charAt(0);
        StringBuilder code = new StringBuilder().append(first);
        char previousDigit = digit(first); // a following same-code letter is dropped
        for (int i = 1; i < letters.length() && code.length() < 4; i++) {
            char c = letters.charAt(i);
            char d = digit(c);
            if (d != '0' && d != previousDigit) {
                code.append(d);
            }
            // 'H' and 'W' are transparent (adjacency survives them); vowels reset it.
            if (c != 'H' && c != 'W') {
                previousDigit = d;
            }
        }
        while (code.length() < 4) {
            code.append('0');
        }
        return code.toString();
    }

    /** The Soundex digit for a letter; {@code '0'} for vowels, 'Y', 'H' and 'W'. */
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
            default:
                return '0';
        }
    }
}
