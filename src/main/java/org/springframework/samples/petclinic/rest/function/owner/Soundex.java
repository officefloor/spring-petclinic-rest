package org.springframework.samples.petclinic.rest.function.owner;

/**
 * The standard American Soundex code of a name: the first letter followed by three digits that encode
 * the remaining consonants by sound, so names that sound alike share a code (e.g. {@code "Robert"} and
 * {@code "Rupert"} both give {@code "R163"}). Used by {@link OwnerIdentityKey} as the last-name segment
 * of the identity key and by {@link AssignPossibleDuplicate} to detect a soft (sound-alike) match.
 *
 * <p>Vowels (and {@code y}) separate consonants of the same code; {@code h} and {@code w} do not. The
 * code is right-padded with zeros and truncated to four characters. A {@code null}, blank or
 * letter-free value yields an empty string.
 */
final class Soundex {

    private Soundex() {
    }

    /** The four-character Soundex code of {@code value}, or an empty string when it has no letters. */
    static String of(String value) {
        if (value == null) {
            return "";
        }
        String letters = value.toUpperCase().replaceAll("[^A-Z]", "");
        if (letters.isEmpty()) {
            return "";
        }
        StringBuilder code = new StringBuilder();
        code.append(letters.charAt(0));
        char previous = codeOf(letters.charAt(0));
        for (int i = 1; i < letters.length() && code.length() < 4; i++) {
            char c = letters.charAt(i);
            char digit = codeOf(c);
            if (digit != '0' && digit != previous) {
                code.append(digit);
            }
            // 'H' and 'W' are transparent: they do not reset the previous code, so two same-coded
            // consonants separated only by them collapse. Vowels (code '0') do reset it.
            if (c != 'H' && c != 'W') {
                previous = digit;
            }
        }
        while (code.length() < 4) {
            code.append('0');
        }
        return code.toString();
    }

    /** The Soundex digit for a letter, or {@code '0'} for vowels and the uncoded letters h, w, y. */
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
