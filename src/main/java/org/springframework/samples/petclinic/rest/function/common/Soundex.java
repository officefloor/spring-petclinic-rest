package org.springframework.samples.petclinic.rest.function.common;

/**
 * Computes the American Soundex code of a name — a phonetic key that folds together surnames that
 * sound alike (e.g. {@code Robert}/{@code Rupert} both code to {@code R163}). Used to derive an
 * owner's {@code identityKey} (folding in the last name's Soundex) and to soft-match owners whose
 * last names sound alike. The algorithm is the standard American Soundex; pinned so both the Spring
 * and OfficeFloor arms produce identical values.
 *
 * <p>The classic algorithm: keep the first letter, map each following consonant to a digit
 * ({@code b,f,p,v}→1; {@code c,g,j,k,q,s,x,z}→2; {@code d,t}→3; {@code l}→4; {@code m,n}→5;
 * {@code r}→6), drop vowels and {@code h}/{@code w}, collapse adjacent letters that share a digit
 * (letters separated only by {@code h}/{@code w} still collapse; those separated by a vowel do not),
 * then pad or truncate to a letter and three digits. A name with no letters yields the empty string.
 */
public final class Soundex {

    /** Length of a Soundex code: the retained first letter plus three digits. */
    private static final int CODE_LENGTH = 4;

    private Soundex() {
    }

    /** The American Soundex code of {@code name}, or the empty string when it holds no letters. */
    public static String of(String name) {
        if (name == null) {
            return "";
        }
        String letters = name.toUpperCase().replaceAll("[^A-Z]", "");
        if (letters.isEmpty()) {
            return "";
        }
        StringBuilder code = new StringBuilder().append(letters.charAt(0));
        char previous = digit(letters.charAt(0));
        for (int i = 1; i < letters.length() && code.length() < CODE_LENGTH; i++) {
            char c = letters.charAt(i);
            if (c == 'H' || c == 'W') {
                // 'h'/'w' are transparent: they neither code nor break a run of the same digit.
                continue;
            }
            char digit = digit(c);
            if (digit != '0' && digit != previous) {
                code.append(digit);
            }
            // A vowel (digit '0') resets the run so a later same-digit consonant codes again.
            previous = digit;
        }
        while (code.length() < CODE_LENGTH) {
            code.append('0');
        }
        return code.toString();
    }

    /** The Soundex digit of a single upper-case letter; {@code '0'} for a vowel (or {@code h}/{@code w}/{@code y}). */
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
