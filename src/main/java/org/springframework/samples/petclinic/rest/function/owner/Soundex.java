package org.springframework.samples.petclinic.rest.function.owner;

/**
 * American Soundex code of a surname: the first letter followed by up to three digits,
 * zero-padded to four characters. Used as the name component of the owner identity key
 * and to group phonetically similar surnames for soft-duplicate detection.
 */
public final class Soundex {

    private Soundex() {
    }

    /** Soundex of {@code name} (e.g. "Robert" -> "R163"); "0000" when it has no letters. */
    public static String code(String name) {
        String s = name == null ? "" : name.toUpperCase().replaceAll("[^A-Z]", "");
        if (s.isEmpty()) {
            return "0000";
        }
        StringBuilder sb = new StringBuilder().append(s.charAt(0));
        int prev = digit(s.charAt(0));
        for (int i = 1; i < s.length() && sb.length() < 4; i++) {
            char c = s.charAt(i);
            int d = digit(c);
            if (d != 0 && d != prev) {
                sb.append(d);
            }
            if (c != 'H' && c != 'W') {
                prev = d; // H and W are transparent: they do not reset the previous code
            }
        }
        while (sb.length() < 4) {
            sb.append('0');
        }
        return sb.toString();
    }

    /** Soundex digit for a letter; 0 for vowels and the transparent H/W. */
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
                return 0;
        }
    }
}
