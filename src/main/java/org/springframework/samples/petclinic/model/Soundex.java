package org.springframework.samples.petclinic.model;

import java.util.Locale;

/**
 * American Soundex encoding of a surname: a letter followed by three digits (e.g. {@code "Robert"} and
 * {@code "Rupert"} both encode to {@code "R163"}). It is the surname-similarity key used by the owner
 * {@link OwnerIdentity} (a component of the {@code identityKey}) and by the create pipeline's
 * soft-duplicate match, so two surnames that sound alike collide on the same code.
 *
 * <p>The standard algorithm: keep the first letter; map the remaining letters to digits
 * ({@code b,f,p,v}&rarr;1; {@code c,g,j,k,q,s,x,z}&rarr;2; {@code d,t}&rarr;3; {@code l}&rarr;4;
 * {@code m,n}&rarr;5; {@code r}&rarr;6); drop {@code a,e,i,o,u,y,h,w}; collapse adjacent equal digits
 * (digits separated only by {@code h} or {@code w} are also collapsed, while a vowel between them is
 * not); then pad with zeros or truncate to exactly four characters. A value with no letters encodes to
 * the empty string.
 */
public final class Soundex {

    private Soundex() {
    }

    /** Soundex code (letter plus three digits) of {@code value}, or {@code ""} when it has no letters. */
    public static String encode(String value) {
        if (value == null) {
            return "";
        }
        String letters = value.toUpperCase(Locale.ROOT).replaceAll("[^A-Z]", "");
        if (letters.isEmpty()) {
            return "";
        }
        StringBuilder code = new StringBuilder();
        code.append(letters.charAt(0));
        char previousDigit = digit(letters.charAt(0));
        for (int i = 1; i < letters.length() && code.length() < 4; i++) {
            char c = letters.charAt(i);
            char d = digit(c);
            if (d != '0' && d != previousDigit) {
                code.append(d);
            }
            // 'h' and 'w' are transparent: they do not reset the previous digit for collapsing;
            // every other letter (digit or vowel) does.
            if (c != 'H' && c != 'W') {
                previousDigit = d;
            }
        }
        while (code.length() < 4) {
            code.append('0');
        }
        return code.toString();
    }

    /** Soundex digit for a letter, {@code '0'} for the uncoded letters {@code a,e,i,o,u,y,h,w}. */
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
