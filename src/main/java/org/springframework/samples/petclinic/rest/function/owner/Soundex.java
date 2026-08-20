package org.springframework.samples.petclinic.rest.function.owner;

/**
 * The American Soundex encoding of a surname: a four-character phonetic code (an initial letter
 * followed by three digits) that maps similarly-sounding names to the same value. Used by the owner
 * {@link OwnerIdentityKey identity key} and by the {@link AssignPossibleDuplicate soft-match} so that
 * spelling variants of one surname are treated as the same household name.
 *
 * <p>A null, blank or letter-free name encodes to the empty string.
 */
public final class Soundex {

    private Soundex() {
    }

    /** The four-character American Soundex code for {@code name}, or {@code ""} when it has no letters. */
    public static String of(String name) {
        if (name == null) {
            return "";
        }
        StringBuilder letters = new StringBuilder();
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (Character.isLetter(c)) {
                letters.append(Character.toUpperCase(c));
            }
        }
        if (letters.length() == 0) {
            return "";
        }
        StringBuilder code = new StringBuilder();
        char first = letters.charAt(0);
        code.append(first);
        char prev = digit(first);
        for (int i = 1; i < letters.length() && code.length() < 4; i++) {
            char c = letters.charAt(i);
            char d = digit(c);
            if (d != '0' && d != prev) {
                code.append(d);
            }
            // 'H' and 'W' are transparent: they do not reset the previous code, so two like-coded
            // consonants they separate still collapse to one. A vowel resets it, so they code twice.
            if (c != 'H' && c != 'W') {
                prev = d;
            }
        }
        while (code.length() < 4) {
            code.append('0');
        }
        return code.toString();
    }

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
            default: // A, E, I, O, U, Y, H, W
                return '0';
        }
    }
}
