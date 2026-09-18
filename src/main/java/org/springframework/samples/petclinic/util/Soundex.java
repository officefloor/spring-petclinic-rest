package org.springframework.samples.petclinic.util;

/**
 * American Soundex phonetic encoding of a name: the first letter followed by three digits that
 * fold together consonants sharing a sound, so surnames that sound alike encode alike
 * (e.g. {@code Robert} and {@code Rupert} both encode {@code R163}). Used by the owner pipeline
 * so its identity and soft-match logic compare last names phonetically rather than literally.
 */
public final class Soundex {

    private Soundex() {
    }

    /**
     * The four-character Soundex code of {@code value} (retained first letter plus three digits,
     * zero-padded and truncated to length four). Non-letters are ignored; a value with no letters
     * (including {@code null}) encodes to the empty string. Letters separated by {@code h}/{@code w}
     * keep a shared code, letters separated by a vowel are coded twice.
     */
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
        StringBuilder code = new StringBuilder().append(letters.charAt(0));
        char previous = codeOf(letters.charAt(0));
        for (int i = 1; i < letters.length() && code.length() < 4; i++) {
            char c = letters.charAt(i);
            if (c == 'H' || c == 'W') {
                continue; // does not break a run of same-coded consonants, nor reset the run
            }
            char digit = codeOf(c);
            if (digit != '0' && digit != previous) {
                code.append(digit);
            }
            previous = digit; // a vowel (digit '0') resets the run so a repeat is coded again
        }
        while (code.length() < 4) {
            code.append('0');
        }
        return code.toString();
    }

    /** The Soundex digit for an upper-case letter, {@code '0'} for a vowel or uncoded letter. */
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
                return '0';
        }
    }
}
