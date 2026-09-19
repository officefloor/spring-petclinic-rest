/*
 * Copyright 2002-2013 the original author or authors.
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

import java.util.Locale;

/**
 * Computes the standard (American) Soundex code of a name: an upper-case letter followed by three
 * digits that group names sharing a similar pronunciation. Non-letters are ignored, the first
 * letter is kept verbatim, and the remaining consonants are mapped to their Soundex digit;
 * adjacent letters mapping to the same digit (including when separated only by {@code h} or
 * {@code w}) collapse to a single digit, while letters separated by a vowel are coded separately.
 * The code is right-padded with zeros or truncated to exactly four characters.
 */
public final class Soundex {

    /** Length of a Soundex code: the retained first letter plus three digits. */
    private static final int LENGTH = 4;

    private Soundex() {
    }

    /**
     * Compute the Soundex code of the given name.
     *
     * @param name the name to encode (may be {@code null})
     * @return the four-character Soundex code, or an empty string when {@code name} is {@code null}
     * or contains no letters
     */
    public static String encode(String name) {
        if (name == null) {
            return "";
        }
        String letters = lettersOnly(name);
        if (letters.isEmpty()) {
            return "";
        }
        StringBuilder code = new StringBuilder(LENGTH);
        code.append(letters.charAt(0));
        char previous = digit(letters.charAt(0));
        for (int i = 1; i < letters.length() && code.length() < LENGTH; i++) {
            char letter = letters.charAt(i);
            if (letter == 'H' || letter == 'W') {
                // h and w never contribute a digit and do not separate equal digits.
                continue;
            }
            char digit = digit(letter);
            if (digit != '0' && digit != previous) {
                code.append(digit);
            }
            previous = digit;
        }
        while (code.length() < LENGTH) {
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

    /**
     * Map an upper-case letter to its Soundex digit, returning {@code '0'} for the vowels and for
     * {@code y}, {@code h} and {@code w}, which are not coded.
     */
    private static char digit(char letter) {
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
