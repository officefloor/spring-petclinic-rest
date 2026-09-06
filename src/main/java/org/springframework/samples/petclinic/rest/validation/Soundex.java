package org.springframework.samples.petclinic.rest.validation;

import java.util.Locale;

import org.springframework.stereotype.Component;

/**
 * Computes the standard American Soundex code of an owner's last name. Soundex reduces a surname to a
 * phonetic key - a leading letter followed by three digits - so that surnames that sound alike share a
 * code regardless of spelling. Keeping the encoding in one place lets the create endpoint treat
 * "phonetic last name" as an opaque value it can fold into the owner {@code identityKey} and compare
 * across owners for the soft {@code possibleDuplicate} match.
 *
 * <p>The algorithm retains the first letter, maps the remaining consonants to their Soundex digit
 * ({@code b,f,p,v => 1}; {@code c,g,j,k,q,s,x,z => 2}; {@code d,t => 3}; {@code l => 4}; {@code m,n => 5};
 * {@code r => 6}), drops adjacent letters that share a digit, and treats {@code h}/{@code w} as
 * transparent (they do not separate two same-coded consonants) while vowels do separate them. The
 * result is padded with zeros and truncated to four characters.
 */
@Component
public class Soundex {

    /** Number of characters in a Soundex code: the retained first letter plus three digits. */
    private static final int CODE_LENGTH = 4;

    /**
     * Encodes a last name to its four-character Soundex code. Non-letter characters are ignored and the
     * value is treated case-insensitively (via {@link Locale#ROOT}). A {@code null}, empty or
     * letter-free value has no phonetic code and yields the empty string.
     *
     * @param value the last name to encode, or {@code null}
     * @return the four-character Soundex code, or the empty string when there is nothing to encode
     */
    public String encode(String value) {
        if (value == null) {
            return "";
        }
        String letters = value.toUpperCase(Locale.ROOT).replaceAll("[^A-Z]", "");
        if (letters.isEmpty()) {
            return "";
        }
        StringBuilder code = new StringBuilder();
        code.append(letters.charAt(0));
        char previousDigit = digitFor(letters.charAt(0));
        for (int i = 1; i < letters.length() && code.length() < CODE_LENGTH; i++) {
            char letter = letters.charAt(i);
            char digit = digitFor(letter);
            if (digit != '0' && digit != previousDigit) {
                code.append(digit);
            }
            // 'h' and 'w' are transparent: they do not reset adjacency, so two same-coded consonants
            // separated only by them still collapse; every other letter (vowels included) does reset it.
            if (letter != 'H' && letter != 'W') {
                previousDigit = digit;
            }
        }
        while (code.length() < CODE_LENGTH) {
            code.append('0');
        }
        return code.toString();
    }

    /**
     * The Soundex digit for a single upper-case letter, or {@code '0'} for a vowel or {@code h}/{@code w}
     * (letters that carry no digit).
     *
     * @param letter an upper-case letter
     * @return the letter's Soundex digit, or {@code '0'} when it has none
     */
    private char digitFor(char letter) {
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
