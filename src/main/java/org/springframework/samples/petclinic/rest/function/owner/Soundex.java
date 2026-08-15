package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Standard American (Russell/NARA) Soundex encoding of a surname: the first letter followed by three
 * digits summarising the remaining consonant sounds, so that names that sound alike share a code
 * (e.g. {@code Smith} and {@code Smyth} both encode to {@code S530}).
 *
 * <p>Used by {@link OwnerIdentityKey} (as the last-name component of an owner's identity key) and by
 * {@link AssignPossibleDuplicate} (which flags a soft match when two owners share a Soundex code and
 * postcode but differ in their identity key).
 *
 * <p>Rules: retain the first letter; map the rest to digits — {@code b,f,p,v}→1;
 * {@code c,g,j,k,q,s,x,z}→2; {@code d,t}→3; {@code l}→4; {@code m,n}→5; {@code r}→6; vowels and
 * {@code y} do not code and separate consonants (same-coded consonants split by a vowel are coded
 * twice), while {@code h} and {@code w} are transparent (same-coded consonants split by them are
 * coded once). Adjacent identical codes collapse to one. The result is padded with zeros or truncated
 * to exactly four characters.
 */
final class Soundex {

    private Soundex() {
    }

    /** The four-character Soundex code of {@code name}, or empty when it contains no letters. */
    static String soundex(String name) {
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
        StringBuilder code = new StringBuilder(4);
        code.append(letters.charAt(0));
        char prev = codeOf(letters.charAt(0));
        for (int i = 1; i < letters.length() && code.length() < 4; i++) {
            char c = letters.charAt(i);
            char digit = codeOf(c);
            if (digit != '0' && digit != prev) {
                code.append(digit);
            }
            // 'h' and 'w' are transparent: keep the previous code so consonants they separate merge.
            if (c != 'H' && c != 'W') {
                prev = digit;
            }
        }
        while (code.length() < 4) {
            code.append('0');
        }
        return code.toString();
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
            default: // A, E, I, O, U, Y, H, W
                return '0';
        }
    }
}
