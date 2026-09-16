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

package org.springframework.samples.petclinic.rest.validation;

import java.util.Locale;

/**
 * Encodes a name to its American Soundex code: the retained first letter followed by three digits
 * that fingerprint the remaining consonant sounds, so names that sound alike share a code. This is
 * the shared phonetic key used to group owners by last name — for the {@link Owner#getIdentityKey()
 * identity key} and for {@link PossibleDuplicateOwnerDetector possible-duplicate} matching — so both
 * agree on when two surnames sound the same.
 */
public final class Soundex {

    private Soundex() {
    }

    /**
     * Return the four-character Soundex code of {@code value} (a letter and three digits, zero
     * padded). Non-letters are ignored; a {@code null} or letterless value encodes to the empty
     * string.
     *
     * @param value the name to encode
     * @return the Soundex code, or the empty string when there is no letter to encode
     */
    public static String encode(String value) {
        if (value == null) {
            return "";
        }
        String letters = value.replaceAll("[^A-Za-z]", "").toUpperCase(Locale.ROOT);
        if (letters.isEmpty()) {
            return "";
        }
        StringBuilder code = new StringBuilder();
        code.append(letters.charAt(0));
        char previousDigit = digit(letters.charAt(0));
        for (int i = 1; i < letters.length() && code.length() < 4; i++) {
            char c = letters.charAt(i);
            // 'H' and 'W' are transparent: they neither code nor break the run of the letters
            // around them, so two same-coded consonants they separate still collapse to one digit.
            if (c == 'H' || c == 'W') {
                continue;
            }
            char d = digit(c);
            if (d != '0' && d != previousDigit) {
                code.append(d);
            }
            previousDigit = d;
        }
        while (code.length() < 4) {
            code.append('0');
        }
        return code.toString();
    }

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
                // vowels A, E, I, O, U and Y (and any other letter) do not code
                return '0';
        }
    }
}
