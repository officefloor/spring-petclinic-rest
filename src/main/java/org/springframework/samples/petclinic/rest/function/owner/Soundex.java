package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Standard American Soundex encoding of a name, used to group phonetically-similar surnames.
 * A soundex code is the surname's first letter followed by three digits (padded with zeros or
 * truncated to length four), so names that sound alike share a code.
 *
 * <p>It is a component of the consolidated {@link OwnerIdentityKey} (the {@code lastName} part of
 * the key is its soundex, not the raw surname) and the phonetic half of the soft-duplicate match in
 * {@link AssignPossibleDuplicate} (same soundex + same postcode, but a different identityKey).
 */
public final class Soundex {

    private Soundex() {
    }

    /** The 4-character soundex code of {@code value}; empty when it contains no letters. */
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
        char prev = mapCode(letters.charAt(0));
        for (int i = 1; i < letters.length() && code.length() < 4; i++) {
            char c = letters.charAt(i);
            // 'H' and 'W' do not separate two like-coded consonants: skip without resetting.
            if (c == 'H' || c == 'W') {
                continue;
            }
            char digit = mapCode(c);
            if (digit != '0' && digit != prev) {
                code.append(digit);
            }
            prev = digit; // a vowel (code '0') resets, so a later like-coded letter is recoded
        }
        while (code.length() < 4) {
            code.append('0');
        }
        return code.toString();
    }

    private static char mapCode(char c) {
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
            default: // vowels, Y, H, W
                return '0';
        }
    }
}
