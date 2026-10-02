package org.springframework.samples.petclinic.util;

/**
 * Computes the American Soundex code of a string: the first letter followed by three digits that
 * phonetically encode the remaining consonants, upper-cased and right-padded with zeros to exactly
 * four characters. Used to group surnames that sound alike when flagging possible-duplicate owners.
 *
 * <p>The encoding follows the classic algorithm: letters are mapped to digits
 * (B,F,P,V -&gt; 1; C,G,J,K,Q,S,X,Z -&gt; 2; D,T -&gt; 3; L -&gt; 4; M,N -&gt; 5; R -&gt; 6),
 * the vowels (A,E,I,O,U,Y) and H,W are not coded, adjacent letters sharing the same digit collapse
 * to one, and two same-digit letters separated by H or W are likewise treated as one (while a
 * vowel separator keeps them distinct).
 */
public final class Soundex {

    private Soundex() {
    }

    /**
     * Encode the given value as its four-character Soundex code, or the empty string when the value
     * is {@code null} or contains no letters.
     *
     * @param value the string to encode (typically a surname)
     * @return the four-character upper-case Soundex code, or {@code ""} when there is nothing to encode
     */
    public static String encode(String value) {
        if (value == null) {
            return "";
        }
        StringBuilder letters = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char c = Character.toUpperCase(value.charAt(i));
            if (c >= 'A' && c <= 'Z') {
                letters.append(c);
            }
        }
        if (letters.length() == 0) {
            return "";
        }
        StringBuilder code = new StringBuilder();
        code.append(letters.charAt(0));
        char previousDigit = digitOf(letters.charAt(0));
        for (int i = 1; i < letters.length() && code.length() < 4; i++) {
            char c = letters.charAt(i);
            if (c == 'H' || c == 'W') {
                // A letter separated from an earlier same-digit letter by H or W stays merged:
                // keep the previous digit so a following same-digit letter is dropped.
                continue;
            }
            char digit = digitOf(c);
            if (digit != '0' && digit != previousDigit) {
                code.append(digit);
            }
            previousDigit = digit;
        }
        while (code.length() < 4) {
            code.append('0');
        }
        return code.toString();
    }

    /** Map an upper-case letter to its Soundex digit, or {@code '0'} when it is not coded. */
    private static char digitOf(char c) {
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
