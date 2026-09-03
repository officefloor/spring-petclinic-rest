package org.springframework.samples.petclinic.rest.function.owner;

/**
 * American Soundex of a name: its first letter followed by up to three digits encoding the
 * remaining consonants, zero-padded to length four. Vowels and {@code H}/{@code W} do not
 * encode; adjacent letters with the same code collapse to one. Used to group phonetically
 * similar last names in the owner {@code identityKey} and in soft-duplicate detection. A
 * null/blank name yields the empty string.
 */
public final class Soundex {

    /** Code per letter A-Z; '0' means the letter is not encoded (vowels, H, W, Y). */
    private static final String CODES = "01230120022455012623010202";

    private Soundex() {
    }

    public static String of(String value) {
        String upper = value == null ? "" : value.toUpperCase().replaceAll("[^A-Z]", "");
        if (upper.isEmpty()) {
            return "";
        }
        StringBuilder out = new StringBuilder().append(upper.charAt(0));
        char prev = code(upper.charAt(0));
        for (int i = 1; i < upper.length() && out.length() < 4; i++) {
            char c = upper.charAt(i);
            char digit = code(c);
            if (digit != '0' && digit != prev) {
                out.append(digit);
            }
            if (c != 'H' && c != 'W') {
                prev = digit;
            }
        }
        while (out.length() < 4) {
            out.append('0');
        }
        return out.toString();
    }

    private static char code(char letter) {
        return CODES.charAt(letter - 'A');
    }
}
