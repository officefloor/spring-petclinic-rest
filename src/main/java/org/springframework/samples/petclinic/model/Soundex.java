package org.springframework.samples.petclinic.model;

/**
 * Minimal, dependency-free implementation of the classic American Soundex phonetic code: a surname is
 * reduced to its first letter followed by three digits, so that names which sound alike map to the same
 * code (e.g. {@code Robert}/{@code Rupert} both encode to {@code R163}).
 *
 * <p>Used to build an owner's {@code identityKey} (see {@link Owner#getIdentityKey()}) and to detect a
 * soft-match household duplicate, both of which key off how a surname <em>sounds</em> rather than its
 * exact spelling. Non-letters are ignored; a value with no letters encodes to the empty string.
 */
public final class Soundex {

    private Soundex() {
    }

    /** Encode {@code value} to its four-character Soundex code, or {@code ""} when it has no letters. */
    public static String encode(String value) {
        if (value == null) {
            return "";
        }
        StringBuilder letters = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (Character.isLetter(c)) {
                letters.append(Character.toUpperCase(c));
            }
        }
        if (letters.length() == 0) {
            return "";
        }
        StringBuilder code = new StringBuilder();
        code.append(letters.charAt(0));
        char prevDigit = digit(letters.charAt(0));
        for (int i = 1; i < letters.length() && code.length() < 4; i++) {
            char c = letters.charAt(i);
            // 'H' and 'W' are transparent: a same-coded letter on either side is still collapsed.
            if (c == 'H' || c == 'W') {
                continue;
            }
            char d = digit(c);
            if (d != '0' && d != prevDigit) {
                code.append(d);
            }
            // Vowels (digit '0') reset the run, so a repeat of a code across a vowel is emitted twice.
            prevDigit = d;
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
            default:
                return '0';
        }
    }
}
