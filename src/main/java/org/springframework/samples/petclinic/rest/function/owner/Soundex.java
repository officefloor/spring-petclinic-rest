package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Standard American Soundex encoding of a surname, used as the phonetic component of an owner's
 * {@code identityKey} (see {@link IdentityKey}) and as the soft-duplicate match key (see
 * {@link FlagPossibleDuplicate}).
 *
 * <p>The result is the first letter of the name followed by three digits (e.g. {@code Robert} and
 * {@code Rupert} both encode to {@code R163}), so names that sound alike collapse to the same code.
 * A {@code null}, blank, or letter-free value has no phonetic code and returns the empty string.
 */
public final class Soundex {

    private Soundex() {
    }

    public static String of(String value) {
        if (value == null) {
            return "";
        }
        String s = value.trim().toUpperCase().replaceAll("[^A-Z]", "");
        if (s.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(s.charAt(0));
        char prevCode = code(s.charAt(0));
        for (int i = 1; i < s.length() && sb.length() < 4; i++) {
            char c = s.charAt(i);
            if (c == 'H' || c == 'W') {
                continue; // does not break adjacency: a same-coded letter across H/W stays merged
            }
            char cd = code(c);
            if (cd != '0' && cd != prevCode) {
                sb.append(cd);
            }
            prevCode = cd; // a vowel (code '0') separates two otherwise-adjacent same codes
        }
        while (sb.length() < 4) {
            sb.append('0');
        }
        return sb.toString();
    }

    private static char code(char c) {
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
