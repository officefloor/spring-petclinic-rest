package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Standard American Soundex encoding of a name: the first letter followed by three digits derived
 * from the remaining consonants, so surnames that sound alike share a code (e.g. {@code Robert} and
 * {@code Rupert} both encode to {@code R163}).
 *
 * <p>Used by {@link OwnerIdentity} (the last-name segment of the identity key) and by
 * {@link AssignPossibleDuplicate} (the soft-match on last name), so both key off the same
 * phonetic code.
 *
 * <ul>
 *   <li>only letters are considered; the code keeps the (upper-cased) first letter;</li>
 *   <li>subsequent letters map to digits — b,f,p,v→1; c,g,j,k,q,s,x,z→2; d,t→3; l→4; m,n→5;
 *       r→6; vowels and h,w,y are not coded;</li>
 *   <li>adjacent letters with the same digit collapse to one; letters separated by h or w are
 *       still treated as adjacent, while a vowel between them breaks the run;</li>
 *   <li>the result is padded with zeros / truncated to exactly four characters.</li>
 * </ul>
 *
 * <p>A null, blank or letter-free value encodes to the empty string.
 */
public final class Soundex {

    private Soundex() {
    }

    /** The four-character Soundex code of {@code name}, or {@code ""} when it has no letters. */
    public static String of(String name) {
        if (name == null) {
            return "";
        }
        String letters = name.toUpperCase().replaceAll("[^A-Z]", "");
        if (letters.isEmpty()) {
            return "";
        }
        StringBuilder code = new StringBuilder().append(letters.charAt(0));
        char prevDigit = digit(letters.charAt(0));
        for (int i = 1; i < letters.length() && code.length() < 4; i++) {
            char c = letters.charAt(i);
            char d = digit(c);
            if (d != '0' && d != prevDigit) {
                code.append(d);
            }
            // h and w are transparent (do not break a run); vowels reset it.
            if (c != 'H' && c != 'W') {
                prevDigit = d;
            }
        }
        while (code.length() < 4) {
            code.append('0');
        }
        return code.toString();
    }

    /** Soundex digit for a letter; {@code '0'} for vowels and h, w, y (not coded). */
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
