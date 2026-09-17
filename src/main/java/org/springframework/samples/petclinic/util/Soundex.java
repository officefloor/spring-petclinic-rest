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
 * Utility for computing the American Soundex code of a name: a phonetic encoding that groups
 * names that sound alike under the same four-character code (the first letter followed by three
 * digits, zero-padded), so similarly-spelled surnames collide.
 */
public final class Soundex {

    private Soundex() {
    }

    /**
     * Compute the four-character American Soundex code of {@code value}: its first letter (upper
     * cased) followed by three consonant-group digits, with vowels acting as separators, {@code h}
     * and {@code w} ignored, adjacent equal codes collapsed, and the result zero-padded or truncated
     * to four characters. Non-letters are ignored; a value with no letters yields the empty string.
     *
     * @param value the name to encode
     * @return the four-character Soundex code, or the empty string when {@code value} has no letters
     */
    public static String encode(String value) {
        if (value == null) {
            return "";
        }
        String letters = value.toUpperCase(Locale.ROOT).replaceAll("[^A-Z]", "");
        if (letters.isEmpty()) {
            return "";
        }
        StringBuilder code = new StringBuilder();
        char first = letters.charAt(0);
        code.append(first);
        int previousCode = codeOf(first);
        for (int i = 1; i < letters.length() && code.length() < 4; i++) {
            char c = letters.charAt(i);
            // 'H' and 'W' are transparent: they neither contribute a digit nor separate two equal
            // codes, so a repeated code either side of them still collapses to one.
            if (c == 'H' || c == 'W') {
                continue;
            }
            int digit = codeOf(c);
            if (digit == 0) {
                // A vowel (or 'Y'): separates consonants, so an equal code after it is coded again.
                previousCode = 0;
                continue;
            }
            if (digit != previousCode) {
                code.append(digit);
            }
            previousCode = digit;
        }
        while (code.length() < 4) {
            code.append('0');
        }
        return code.substring(0, 4);
    }

    /** The Soundex digit for a letter, or {@code 0} for a vowel (A, E, I, O, U, Y) or H/W. */
    private static int codeOf(char c) {
        switch (c) {
            case 'B': case 'F': case 'P': case 'V':
                return 1;
            case 'C': case 'G': case 'J': case 'K': case 'Q': case 'S': case 'X': case 'Z':
                return 2;
            case 'D': case 'T':
                return 3;
            case 'L':
                return 4;
            case 'M': case 'N':
                return 5;
            case 'R':
                return 6;
            default:
                return 0;
        }
    }
}
