package org.springframework.samples.petclinic.rest.function.common;

/**
 * The American Soundex phonetic code of a name — the last-name component of an owner's
 * {@code identityKey} and the last-name match used for soft duplicate detection. Two names that
 * sound alike collapse to the same four-character code (an initial letter followed by three digits),
 * so {@code "Robert"} and {@code "Rupert"} both encode as {@code R163}.
 *
 * <p>The algorithm keeps the first letter, maps the remaining consonants to digits
 * (b,f,p,v→1; c,g,j,k,q,s,x,z→2; d,t→3; l→4; m,n→5; r→6), drops adjacent duplicates — treating
 * {@code h} and {@code w} as transparent so they do not separate a repeated digit while vowels do —
 * and pads or truncates to length four. A {@code null} or letter-free input yields the empty string.
 */
public final class Soundex {

    private Soundex() {
    }

    /** The four-character Soundex code of {@code value}, or {@code ""} when it has no letters. */
    public static String of(String value) {
        if (value == null) {
            return "";
        }
        StringBuilder letters = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char c = Character.toUpperCase(value.charAt(i));
            if (c >= 'A' && c <= 'Z') {
                letters.append(c);
            }
        }
        if (letters.length() == 0) {
            return "";
        }
        StringBuilder code = new StringBuilder();
        code.append(letters.charAt(0));
        char previous = digit(letters.charAt(0));
        for (int i = 1; i < letters.length() && code.length() < 4; i++) {
            char c = letters.charAt(i);
            if (c == 'H' || c == 'W') {
                // Transparent: does not break a run of the same digit, so previous is left unchanged.
                continue;
            }
            char d = digit(c);
            if (d != '0' && d != previous) {
                code.append(d);
            }
            // Vowels (digit '0') reset the run so a later same-digit consonant is coded again.
            previous = d;
        }
        while (code.length() < 4) {
            code.append('0');
        }
        return code.toString();
    }

    /** The Soundex digit for a letter; {@code '0'} for vowels and the transparent letters. */
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
