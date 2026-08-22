package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Standard American Soundex encoding of a surname: the retained first letter followed by three
 * digits derived from the following consonants, so names that sound alike share a code (e.g.
 * {@code Robert} and {@code Rupert} both encode to {@code R163}).
 *
 * <p>Used by the owner identity key (see {@link OwnerIdentity}) and the soft-match check
 * ({@link AssignPossibleDuplicate}): duplicate detection keys on {@code soundex(lastName)} rather
 * than the exact surname, so a phonetic re-spelling of the same household name still matches.
 *
 * <p>The encoding: keep only letters and upper-case them; the first letter is retained verbatim;
 * each subsequent letter maps to a digit (b,f,p,v→1; c,g,j,k,q,s,x,z→2; d,t→3; l→4; m,n→5; r→6),
 * with vowels (a,e,i,o,u,y) coding to 0. Adjacent letters with the same digit collapse to one; a
 * separating vowel breaks that collapse, while {@code h} and {@code w} are transparent (they do not
 * break it). Zeros are dropped and the result is right-padded with zeros or truncated to length 4.
 * An input with no letters encodes to {@code 0000}.
 */
public final class Soundex {

    private Soundex() {
    }

    public static String of(String value) {
        if (value == null) {
            return "0000";
        }
        StringBuilder letters = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (Character.isLetter(c)) {
                letters.append(Character.toUpperCase(c));
            }
        }
        if (letters.length() == 0) {
            return "0000";
        }
        StringBuilder code = new StringBuilder();
        code.append(letters.charAt(0));
        char previous = codeOf(letters.charAt(0));
        for (int i = 1; i < letters.length() && code.length() < 4; i++) {
            char c = letters.charAt(i);
            if (c == 'H' || c == 'W') {
                continue; // transparent: does not break a same-digit collapse
            }
            char digit = codeOf(c);
            if (digit != '0' && digit != previous) {
                code.append(digit);
            }
            previous = digit; // a vowel resets, so a same-digit consonant after it counts again
        }
        while (code.length() < 4) {
            code.append('0');
        }
        return code.substring(0, 4);
    }

    private static char codeOf(char c) {
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
                return '0'; // vowels a,e,i,o,u,y (h and w are handled before this)
        }
    }
}
