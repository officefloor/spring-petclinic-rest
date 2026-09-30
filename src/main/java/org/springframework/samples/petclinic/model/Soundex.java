package org.springframework.samples.petclinic.model;

import java.util.Locale;

/**
 * Single definition of the American Soundex code of a surname, used to group names that
 * sound alike. The identityKey (see {@link OwnerIdentity}) folds the lastName through this
 * so two spellings that share a Soundex code collide on the surname component, and the
 * soft-duplicate rule uses the same code to decide when two owners are a phonetic match on
 * lastName. Lives beside {@link Owner} so both the mapping and request-handling layers can
 * depend on it without a package cycle.
 *
 * <p>The algorithm is the standard American Soundex: keep the first letter, map the
 * remaining letters to the digits {@code b,f,p,v->1 · c,g,j,k,q,s,x,z->2 · d,t->3 · l->4 ·
 * m,n->5 · r->6}, drop vowels and {@code h/w/y}, collapse adjacent letters that share a
 * digit (including across an {@code h} or {@code w}, but not across a vowel), then pad or
 * truncate to a letter plus three digits (e.g. "Robert" -> "R163"). A value with no letters
 * yields the empty string.
 */
public final class Soundex {

    private Soundex() {
    }

    /** The American Soundex code of {@code value} (letter + 3 digits), or "" when it has no letters. */
    public static String of(String value) {
        if (value == null) {
            return "";
        }
        String letters = value.toUpperCase(Locale.ROOT).replaceAll("[^A-Z]", "");
        if (letters.isEmpty()) {
            return "";
        }
        StringBuilder code = new StringBuilder();
        code.append(letters.charAt(0));
        char prevDigit = digit(letters.charAt(0));
        for (int i = 1; i < letters.length() && code.length() < 4; i++) {
            char c = letters.charAt(i);
            char d = digit(c);
            if (d != '0' && d != prevDigit) {
                code.append(d);
            }
            // 'h' and 'w' are transparent: they do not reset the previous digit, so two coded
            // letters on either side still collapse. Every other letter (vowels included) resets
            // it, so like-coded letters split by a vowel are both kept.
            if (c != 'H' && c != 'W') {
                prevDigit = d;
            }
        }
        while (code.length() < 4) {
            code.append('0');
        }
        return code.toString();
    }

    /** The Soundex digit for a letter, or '0' for letters that are not coded (vowels, h, w, y). */
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
