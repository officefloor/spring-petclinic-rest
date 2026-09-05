package org.springframework.samples.petclinic.rest.function.owner;

/**
 * American Soundex of a name — the phonetic code the owner identity now hashes over (see
 * {@link IdentityKey}) and the soft-match ({@link DetectPossibleDuplicate}) groups by. The code is the
 * first letter followed by three digits derived from the remaining consonants (b,f,p,v→1;
 * c,g,j,k,q,s,x,z→2; d,t→3; l→4; m,n→5; r→6), where adjacent letters with the same digit — including a
 * pair separated only by {@code h}/{@code w} — collapse to one, vowels (and {@code y}) reset the run,
 * and the result is zero-padded or truncated to four characters. Non-letters are dropped first; a
 * value with no letters yields an empty string. Deterministic, so both the create-time duplicate check
 * and the mapped response derive the identical value.
 */
public final class Soundex {

    /** A–Z → Soundex digit; '0' marks vowels and the ignored {@code h}/{@code w}. */
    private static final char[] MAP = "01230120022455012623010202".toCharArray();

    private Soundex() {
    }

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
        char first = letters.charAt(0);
        StringBuilder code = new StringBuilder();
        code.append(first);
        char last = MAP[first - 'A'];
        for (int i = 1; i < letters.length() && code.length() < 4; i++) {
            char ch = letters.charAt(i);
            if (ch == 'H' || ch == 'W') {
                continue; // ignored: does not break a run of same-coded consonants
            }
            char digit = MAP[ch - 'A'];
            if (digit != '0' && digit != last) {
                code.append(digit);
            }
            last = digit; // vowels reset the run so later same-coded consonants still count
        }
        while (code.length() < 4) {
            code.append('0');
        }
        return code.toString();
    }
}
