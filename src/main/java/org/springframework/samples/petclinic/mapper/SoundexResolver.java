package org.springframework.samples.petclinic.mapper;

/**
 * Derives the American Soundex code of a name. Soundex reduces a surname to a phonetic key — a retained first letter
 * followed by three digits encoding its remaining consonant sounds — so names that sound alike share a code. It is used
 * both in the owner {@code identityKey} (see {@link IdentityKeyResolver#deriveIdentityKey}) and in the soft-duplicate
 * match, which fires when two owners share a last-name Soundex and a postcode but differ in identity key. Kept a static
 * utility like {@link HexDigest} and {@link LocalityResolver}, since it derives purely from its argument.
 */
public final class SoundexResolver {

    private SoundexResolver() {
    }

    /**
     * Returns the four-character American Soundex code of {@code name}: its first letter followed by three digits, or
     * an empty string when the name carries no letters. Non-letter characters are ignored; the coding is
     * case-insensitive. Consecutive letters mapping to the same digit collapse to one, letters separated only by
     * {@code 'h'} or {@code 'w'} are treated as adjacent, and the code is right-padded with zeros and truncated to
     * four characters.
     *
     * @param name the name to encode, may be {@code null}
     * @return the four-character Soundex code, or an empty string when {@code name} has no letters
     */
    public static String soundex(String name) {
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
        StringBuilder code = new StringBuilder();
        code.append(letters.charAt(0));
        char previousCode = codeOf(letters.charAt(0));
        for (int i = 1; i < letters.length() && code.length() < 4; i++) {
            char c = letters.charAt(i);
            char digit = codeOf(c);
            if (digit != '0' && digit != previousCode) {
                code.append(digit);
            }
            if (c != 'H' && c != 'W') {
                previousCode = digit;
            }
        }
        while (code.length() < 4) {
            code.append('0');
        }
        return code.toString();
    }

    /**
     * Maps a single upper-case letter to its Soundex digit, or {@code '0'} for the letters that carry no digit
     * ({@code a, e, i, o, u, y, h, w}).
     */
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
