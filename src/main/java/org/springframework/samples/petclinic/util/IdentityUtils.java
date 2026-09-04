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

import java.util.Locale;

/**
 * Small stateless helpers for the string normalisation that backs the application's
 * identity comparisons.
 *
 * <p>Keeping the normalisation in one place means every identity comparison keys on the
 * same canonical form and they can never drift apart.
 */
public abstract class IdentityUtils {

    /**
     * Normalize a value for identity comparison: null becomes an empty string, surrounding
     * whitespace is trimmed, internal whitespace runs collapse to a single space, and the
     * result is lower-cased.
     *
     * @param value the raw value to normalize, possibly {@code null}
     * @return the canonical, lower-cased form, or the empty string if {@code value} is {@code null}
     */
    public static String normalizeIdentity(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    /**
     * The American Soundex code of {@code value}: its first letter followed by three digits that
     * encode the remaining consonants, so names that sound alike collapse to the same code (e.g.
     * 'Robert' and 'Rupert' both yield 'R163'). Letters are folded to their {@link #soundexDigit
     * digit class}; vowels (and 'y') separate equal-class letters while 'h' and 'w' do not, runs of
     * an equal class code once, and the result is padded with zeros or truncated to four characters.
     *
     * @param value the raw value to encode, possibly {@code null}
     * @return the four-character Soundex code, or the empty string when {@code value} is {@code null}
     *         or holds no letters
     */
    public static String soundex(String value) {
        if (value == null) {
            return "";
        }
        String letters = value.toUpperCase(Locale.ROOT).replaceAll("[^A-Z]", "");
        if (letters.isEmpty()) {
            return "";
        }
        StringBuilder code = new StringBuilder().append(letters.charAt(0));
        int previous = soundexDigit(letters.charAt(0));
        for (int i = 1; i < letters.length() && code.length() < 4; i++) {
            char c = letters.charAt(i);
            // 'h' and 'w' are transparent: they neither code nor break a run of an equal digit class.
            if (c == 'H' || c == 'W') {
                continue;
            }
            int digit = soundexDigit(c);
            if (digit != 0 && digit != previous) {
                code.append((char) ('0' + digit));
            }
            // A vowel (digit 0) resets the run, so an equal class either side of it codes twice.
            previous = digit;
        }
        while (code.length() < 4) {
            code.append('0');
        }
        return code.toString();
    }

    /**
     * The Soundex digit class (1-6) of an upper-case letter, or 0 for a vowel, 'h', 'w', 'y' or any
     * character that Soundex does not code: b/f/p/v -> 1, c/g/j/k/q/s/x/z -> 2, d/t -> 3, l -> 4,
     * m/n -> 5, r -> 6.
     *
     * @param letter the upper-case letter to classify
     * @return the letter's Soundex digit class, or 0 when it is not coded
     */
    private static int soundexDigit(char letter) {
        return switch (letter) {
            case 'B', 'F', 'P', 'V' -> 1;
            case 'C', 'G', 'J', 'K', 'Q', 'S', 'X', 'Z' -> 2;
            case 'D', 'T' -> 3;
            case 'L' -> 4;
            case 'M', 'N' -> 5;
            case 'R' -> 6;
            default -> 0;
        };
    }
}
