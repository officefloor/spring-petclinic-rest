/*
 * Copyright 2016-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.samples.petclinic.util;

/**
 * Encodes a name to its Soundex code, a fixed four-character phonetic key (a letter followed by
 * three digits, e.g. {@code "Robert"} and {@code "Rupert"} both encode to {@code "R163"}). Names
 * that sound alike map to the same code, so comparing Soundex codes groups spelling variants of the
 * same surname together.
 * <p>
 * Centralising this here keeps the code that matches owners by surname thin: it delegates to
 * {@link #encode(String)} so every "same-sounding surname" comparison in the app is computed one
 * way, in the same spirit as the other hash- and normalization-derived keys in this package (see
 * {@link Sha256Hex} and {@link HouseholdNormalizer}).
 * <p>
 * The standard (American) Soundex algorithm is used: the first letter is retained, the remaining
 * letters are mapped to their digit codes with vowels and the letters {@code H}, {@code W} and
 * {@code Y} not coded, adjacent letters sharing a code are collapsed to one (letters separated only
 * by {@code H} or {@code W} still collapse, while letters separated by a vowel are coded twice), and
 * the result is truncated to three digits or right-padded with zeros to that length.
 */
public final class Soundex {

    private Soundex() {
    }

    /**
     * Returns the four-character Soundex code of {@code value}: the retained first letter followed by
     * three digits. Non-letter characters are ignored and letters are treated case-insensitively, so
     * incidental punctuation, spacing and letter case do not affect the code. A {@code null} value, or
     * one that contains no letters, encodes to the empty string.
     *
     * @param value the name to encode (may be {@code null})
     * @return the four-character Soundex code, or the empty string when {@code value} has no letters
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
        StringBuilder code = new StringBuilder(4).append(letters.charAt(0));
        // Seed the "previous digit" with the first letter's code so a following letter sharing that
        // code is collapsed into it rather than repeated.
        char previousDigit = mapDigit(letters.charAt(0));
        for (int i = 1; i < letters.length() && code.length() < 4; i++) {
            char c = letters.charAt(i);
            char digit = mapDigit(c);
            if (digit != '0' && digit != previousDigit) {
                code.append(digit);
            }
            // H and W do not separate two same-coded letters, so they leave the previous digit
            // unchanged; a vowel (which maps to '0') resets it, letting a later same-coded letter be
            // coded again.
            if (c != 'H' && c != 'W') {
                previousDigit = digit;
            }
        }
        while (code.length() < 4) {
            code.append('0');
        }
        return code.toString();
    }

    /**
     * Maps a single upper-case letter to its Soundex digit, returning {@code '0'} for the letters
     * that are not coded (the vowels and {@code H}, {@code W}, {@code Y}) and for any non-letter.
     *
     * @param c the upper-case letter to map
     * @return the letter's Soundex digit, or {@code '0'} when the letter is not coded
     */
    private static char mapDigit(char c) {
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
