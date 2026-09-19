package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

/**
 * Standard American Soundex encoding of a name: its first letter followed by three digits that
 * phonetically classify the remaining consonants, so names that sound alike share a code
 * (e.g. {@code Robert} and {@code Rupert} both encode to {@code R163}). Used to group owners by
 * how their last name sounds, both as the last component of an owner's
 * {@link OwnerIdentity identity key} and to detect a {@link FlagPossibleDuplicate soft-match}
 * against an existing owner sharing a sound-alike last name and postcode.
 *
 * <p>Non-letters are ignored and case is irrelevant. A name with no letters (or {@code null})
 * encodes to the empty string.
 */
final class Soundex {

    /** Fixed width of a Soundex code: the retained first letter plus three digits. */
    private static final int CODE_LENGTH = 4;

    private Soundex() {
    }

    static String encode(String name) {
        if (name == null) {
            return "";
        }
        String letters = lettersOnly(name);
        if (letters.isEmpty()) {
            return "";
        }
        StringBuilder code = new StringBuilder(CODE_LENGTH);
        code.append(letters.charAt(0));
        // The first letter's own code seeds the run so a second letter sharing it is dropped.
        char previous = digitOf(letters.charAt(0));
        for (int i = 1; i < letters.length() && code.length() < CODE_LENGTH; i++) {
            char letter = letters.charAt(i);
            char digit = digitOf(letter);
            if (digit != '0' && digit != previous) {
                code.append(digit);
            }
            // 'h' and 'w' are transparent: they neither code nor break a run of one sound, so a
            // same-coded consonant across them stays merged. A vowel resets the run so a repeated
            // sound after it is coded again.
            if (letter != 'H' && letter != 'W') {
                previous = digit;
            }
        }
        while (code.length() < CODE_LENGTH) {
            code.append('0');
        }
        return code.toString();
    }

    private static String lettersOnly(String name) {
        StringBuilder sb = new StringBuilder(name.length());
        for (char c : name.toUpperCase(Locale.ROOT).toCharArray()) {
            if (c >= 'A' && c <= 'Z') {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /** The Soundex digit for an upper-case letter, or {@code '0'} for vowels, 'h', 'w' and 'y'. */
    private static char digitOf(char letter) {
        switch (letter) {
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
