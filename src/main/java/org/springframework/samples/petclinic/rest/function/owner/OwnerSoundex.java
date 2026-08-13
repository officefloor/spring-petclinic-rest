package org.springframework.samples.petclinic.rest.function.owner;

/**
 * The American Soundex code of a name, used as the phonetic component of the owner
 * {@link OwnerIdentityKey} and as the soft-match key in {@link AssignOwnerPossibleDuplicate}. Two
 * surnames that sound alike share a Soundex code, so owners at the same postcode with phonetically
 * similar last names are flagged as possible duplicates even when their identity keys differ.
 *
 * <p>The code is the retained first letter followed by three digits: consonants map to a digit
 * ({@code b,f,p,v}->1; {@code c,g,j,k,q,s,x,z}->2; {@code d,t}->3; {@code l}->4; {@code m,n}->5;
 * {@code r}->6), vowels are dropped and separate repeated digits, and {@code h}/{@code w} are
 * dropped without separating. The result is padded with zeros or truncated to length four (e.g.
 * {@code "Robert" -> "R163"}). A name with no letters yields the empty string.
 */
final class OwnerSoundex {

    private OwnerSoundex() {
    }

    /** The Soundex code of the given name, or {@code ""} when it contains no letters. */
    static String of(String name) {
        if (name == null) {
            return "";
        }
        StringBuilder letters = new StringBuilder();
        for (int i = 0; i < name.length(); i++) {
            char c = Character.toUpperCase(name.charAt(i));
            if (c >= 'A' && c <= 'Z') {
                letters.append(c);
            }
        }
        if (letters.length() == 0) {
            return "";
        }
        char first = letters.charAt(0);
        StringBuilder code = new StringBuilder().append(first);
        char previousDigit = digit(first);
        for (int i = 1; i < letters.length() && code.length() < 4; i++) {
            char c = letters.charAt(i);
            char d = digit(c);
            if (d != '0' && d != previousDigit) {
                code.append(d);
            }
            if (c != 'H' && c != 'W') {
                previousDigit = d; // vowels reset (they separate repeats); h/w do not
            }
        }
        while (code.length() < 4) {
            code.append('0');
        }
        return code.toString();
    }

    /** The Soundex digit for a consonant, or {@code '0'} for a vowel, {@code h}, {@code w} or {@code y}. */
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
