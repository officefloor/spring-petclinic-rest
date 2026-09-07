package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

/**
 * Single home for the American Soundex encoding used by owner duplicate detection. The
 * {@link OwnerIdentity identity key} folds {@code soundex(lastName)} into its hash and
 * {@link CheckOwnerPossibleDuplicate} soft-matches on it, so the phonetic encoding stays defined in
 * one place.
 *
 * <p>Standard US Soundex: the (upper-cased) first letter followed by three digits derived from the
 * remaining consonants (b,f,p,v&rarr;1; c,g,j,k,q,s,x,z&rarr;2; d,t&rarr;3; l&rarr;4; m,n&rarr;5;
 * r&rarr;6). Vowels and h,w,y are not coded; adjacent letters mapping to the same digit (including
 * across an intervening h or w) collapse to one; the result is right-padded with zeros and truncated
 * to four characters. A value with no letters (or {@code null}) encodes to the empty string.
 */
final class Soundex {

    private Soundex() {
    }

    /** Digit for each letter A..Z, or '0' for a letter that is not coded (vowels and h,w,y). */
    private static final char[] MAP = "01230120022455012623010202".toCharArray();

    static String of(String value) {
        String cleaned = clean(value);
        if (cleaned.isEmpty()) {
            return "";
        }
        char[] out = {cleaned.charAt(0), '0', '0', '0'};
        char last = mappingCode(cleaned, 0);
        int count = 1;
        for (int i = 1; i < cleaned.length() && count < out.length; i++) {
            char mapped = mappingCode(cleaned, i);
            if (mapped != 0) {
                if (mapped != '0' && mapped != last) {
                    out[count++] = mapped;
                }
                last = mapped;
            }
        }
        return new String(out);
    }

    /** Upper-cased, letters only. */
    private static String clean(String value) {
        if (value == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(value.length());
        for (char c : value.toUpperCase(Locale.ROOT).toCharArray()) {
            if (c >= 'A' && c <= 'Z') {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * The Soundex digit for the letter at {@code index}, or {@code (char) 0} when the letter is
     * suppressed by the h/w rule (a coded letter separated from an equally-coded predecessor only by
     * an h or w does not restart the run).
     */
    private static char mappingCode(String str, int index) {
        char mapped = MAP[str.charAt(index) - 'A'];
        if (index > 1 && mapped != '0') {
            char hw = str.charAt(index - 1);
            if (hw == 'H' || hw == 'W') {
                char prev = str.charAt(index - 2);
                if (MAP[prev - 'A'] == mapped || prev == 'H' || prev == 'W') {
                    return 0;
                }
            }
        }
        return mapped;
    }
}
