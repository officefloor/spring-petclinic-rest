package org.springframework.samples.petclinic.model;

/**
 * American Soundex phonetic encoding: the retained first letter of a name followed by three
 * digits derived from its remaining consonants, so names that sound alike share a code (for
 * example {@code "Robert"} and {@code "Rupert"} both encode to {@code "R163"}).
 *
 * <p>The encoding maps consonants to digits ({@code b,f,p,v}&rarr;1; {@code c,g,j,k,q,s,x,z}&rarr;2;
 * {@code d,t}&rarr;3; {@code l}&rarr;4; {@code m,n}&rarr;5; {@code r}&rarr;6), drops vowels, and
 * collapses runs of the same code (adjacent, or separated only by {@code h}/{@code w}) to a single
 * digit; a vowel between two same-coded consonants keeps both. The result is padded with zeros or
 * truncated to exactly four characters. A {@code null}, blank or letterless value encodes to the
 * empty string.
 *
 * <p>Used by {@link Owner#getIdentityKey()} (the last-name component of the duplicate-detection
 * key) and by the create-owner soft-match, which flags a possible duplicate when the last names
 * share a Soundex code and the postcodes match.
 */
public final class Soundex {

    /** Soundex digit for each letter A-Z; {@code '0'} means the letter is not coded. */
    private static final String MAPPING = "01230120022455012623010202";

    private Soundex() {
    }

    /** The four-character American Soundex code of {@code value}, or {@code ""} when it has no letters. */
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
        char first = letters.charAt(0);
        code.append(first);
        char lastCode = codeOf(first);
        for (int i = 1; i < letters.length() && code.length() < 4; i++) {
            char c = letters.charAt(i);
            if (c == 'H' || c == 'W') {
                continue; // transparent: does not break a run of the same code
            }
            char digit = codeOf(c);
            if (digit != '0' && digit != lastCode) {
                code.append(digit);
            }
            // A vowel resets the run so a following same-coded consonant is coded again;
            // a coded consonant carries its digit forward to collapse an adjacent repeat.
            lastCode = digit;
        }
        while (code.length() < 4) {
            code.append('0');
        }
        return code.toString();
    }

    /** The Soundex digit for a single upper-case letter, or {@code '0'} when it is not coded. */
    private static char codeOf(char letter) {
        int index = letter - 'A';
        if (index < 0 || index >= MAPPING.length()) {
            return '0';
        }
        return MAPPING.charAt(index);
    }
}
