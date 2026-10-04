package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

/**
 * Single source of truth for the Soundex encoding of an owner's last name used by the
 * {@code POST /owners} pipeline. The last name's Soundex code is folded into the owner
 * {@link OwnerIdentity identityKey} (so two owners whose surnames sound alike share that component
 * of the key) and is the basis for the soft-duplicate match in {@link AssignPossibleDuplicate}
 * (a request is a <em>possible</em> duplicate when its surname's Soundex and its postcode match an
 * existing owner's while its whole identity key differs).
 *
 * <p>Centralised here so the identity key and the soft-match cannot disagree about when two
 * surnames "sound the same".
 *
 * <p>Implements the standard American Soundex: keep the (upper-cased) first letter, encode the
 * remaining letters to the digits {@code BFPV=1 CGJKQSXZ=2 DT=3 L=4 MN=5 R=6} (vowels and
 * {@code HWY} map to no digit), collapse adjacent letters that share a code, treat {@code H}/{@code W}
 * as transparent (they do not separate two same-coded consonants) while a vowel does separate them,
 * then pad or truncate to a letter plus three digits. A value with no letters encodes to the empty
 * string.
 */
final class OwnerSoundex {

    private OwnerSoundex() {
    }

    /** The Soundex code of {@code value}, or the empty string when it contains no letters. */
    static String of(String value) {
        if (value == null) {
            return "";
        }
        StringBuilder letters = new StringBuilder();
        for (char c : value.toUpperCase(Locale.ROOT).toCharArray()) {
            if (c >= 'A' && c <= 'Z') {
                letters.append(c);
            }
        }
        if (letters.length() == 0) {
            return "";
        }
        StringBuilder code = new StringBuilder().append(letters.charAt(0));
        char previousCode = codeOf(letters.charAt(0));
        for (int i = 1; i < letters.length() && code.length() < 4; i++) {
            char letter = letters.charAt(i);
            char digit = codeOf(letter);
            if (digit != '0' && digit != previousCode) {
                code.append(digit);
            }
            // H and W are transparent: they do not reset the running code, so two same-coded
            // consonants separated only by H/W still collapse. A vowel does reset it.
            if (letter != 'H' && letter != 'W') {
                previousCode = digit;
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
            default: // A E I O U H W Y
                return '0';
        }
    }
}
