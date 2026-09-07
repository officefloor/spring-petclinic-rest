package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

/**
 * American Soundex encoding of a surname — the phonetic bucket used by the owner
 * {@link OwnerIdentity} key and by {@link FlagPossibleDuplicate} to detect a soft (possible)
 * duplicate. Two surnames that sound alike (e.g. {@code Robert}/{@code Rupert}) share a code, so an
 * owner registered under a near-spelling of an existing surname (same postcode) is flagged as a
 * possible duplicate even though the exact spelling — and therefore the identity key — differs.
 *
 * <p>The code is the retained first letter followed by three digits: consonants map to
 * {@code b,f,p,v->1}, {@code c,g,j,k,q,s,x,z->2}, {@code d,t->3}, {@code l->4}, {@code m,n->5},
 * {@code r->6}; vowels (and {@code y}) separate codes; {@code h}/{@code w} are transparent (two
 * same-coded consonants they separate collapse to one). Padded with zeros or truncated to four
 * characters.
 */
final class Soundex {

    private Soundex() {
    }

    /** The four-character Soundex code of {@code name}, or {@code ""} when it holds no letters. */
    static String of(String name) {
        if (name == null) {
            return "";
        }
        String letters = name.toUpperCase(Locale.ROOT).replaceAll("[^A-Z]", "");
        if (letters.isEmpty()) {
            return "";
        }
        StringBuilder code = new StringBuilder();
        code.append(letters.charAt(0));
        char previous = codeFor(letters.charAt(0));
        for (int i = 1; i < letters.length() && code.length() < 4; i++) {
            char c = letters.charAt(i);
            if (c == 'H' || c == 'W') {
                continue; // transparent: does not reset the previous code
            }
            char digit = codeFor(c);
            if (digit != '0' && digit != previous) {
                code.append(digit);
            }
            previous = digit; // vowels (code '0') reset, so a repeated code after one is kept
        }
        while (code.length() < 4) {
            code.append('0');
        }
        return code.toString();
    }

    private static char codeFor(char c) {
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
                return '0'; // vowels A E I O U and Y
        }
    }
}
