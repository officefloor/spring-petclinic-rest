package org.springframework.samples.petclinic.util;

/**
 * American Soundex encoding of a name - a four-character code (a retained first letter followed by
 * three digits) that collapses names that sound alike to the same value. Used by the owner
 * {@code identityKey} (which hashes {@code soundex(lastName)} into its key) and by the soft-match
 * rule, so two owners whose last names share a Soundex code and postcode are flagged as possible
 * duplicates even when their identity keys differ.
 *
 * <p>The standard rules are applied: keep the first letter; map the remaining consonants to digits
 * (b,f,p,v&rarr;1; c,g,j,k,q,s,x,z&rarr;2; d,t&rarr;3; l&rarr;4; m,n&rarr;5; r&rarr;6); drop
 * repeated codes (also across an intervening {@code h} or {@code w}); treat vowels and {@code y} as
 * separators that let equal codes on either side both count; then pad or truncate to length four.
 */
public final class Soundex {

    private Soundex() {
    }

    /**
     * @param value the name to encode (letters only are considered; other characters are ignored).
     * @return the four-character Soundex code, or {@code "0000"} when {@code value} has no letters.
     */
    public static String of(String value) {
        if (value == null) {
            return "0000";
        }
        String letters = value.toUpperCase().replaceAll("[^A-Z]", "");
        if (letters.isEmpty()) {
            return "0000";
        }
        StringBuilder out = new StringBuilder();
        out.append(letters.charAt(0));
        char prev = code(letters.charAt(0));
        for (int i = 1; i < letters.length() && out.length() < 4; i++) {
            char ch = letters.charAt(i);
            if (ch == 'H' || ch == 'W') {
                continue; // transparent: does not reset the previous code
            }
            char c = code(ch);
            if (c != '0') {
                if (c != prev) {
                    out.append(c);
                }
                prev = c;
            }
            else {
                prev = '0'; // a vowel (or y) separates equal codes so both are kept
            }
        }
        while (out.length() < 4) {
            out.append('0');
        }
        return out.toString();
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
                return '0'; // vowels a,e,i,o,u and y,h,w
        }
    }
}
