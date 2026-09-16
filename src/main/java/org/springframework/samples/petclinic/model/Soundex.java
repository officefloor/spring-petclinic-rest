/*
 * Copyright 2002-2017 the original author or authors.
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
package org.springframework.samples.petclinic.model;

import java.util.Locale;

/**
 * The American Soundex phonetic algorithm.
 *
 * <p>Encodes a word as a letter followed by three digits (e.g. {@code "Robert"} and
 * {@code "Rupert"} both encode to {@code "R163"}), so that names that sound alike share
 * a code. This is the single source of truth for that computation, so callers never
 * repeat it.
 */
public final class Soundex {

    private static final String EMPTY_CODE = "0000";

    private static final int CODE_LENGTH = 4;

    private Soundex() {
    }

    /**
     * Compute the four-character Soundex code of {@code value}. Non-letters are ignored;
     * a value with no letters encodes to {@code "0000"}.
     *
     * @param value the word to encode, e.g. a last name
     * @return the Soundex code: an upper-case initial letter and three digits
     */
    public static String of(String value) {
        String letters = value == null ? "" : value.replaceAll("[^A-Za-z]", "").toUpperCase(Locale.ROOT);
        if (letters.isEmpty()) {
            return EMPTY_CODE;
        }
        StringBuilder code = new StringBuilder(CODE_LENGTH).append(letters.charAt(0));
        int previous = codeOf(letters.charAt(0));
        for (int i = 1; i < letters.length() && code.length() < CODE_LENGTH; i++) {
            char letter = letters.charAt(i);
            // 'H' and 'W' are transparent: they neither contribute a digit nor separate
            // two like-coded consonants, so the previous code carries across them.
            if (letter == 'H' || letter == 'W') {
                continue;
            }
            int digit = codeOf(letter);
            if (digit != 0 && digit != previous) {
                code.append((char) ('0' + digit));
            }
            // Vowels (code 0) reset the running code so a repeated consonant that is
            // separated by a vowel is encoded again; consonants set it to their own code.
            previous = digit;
        }
        while (code.length() < CODE_LENGTH) {
            code.append('0');
        }
        return code.toString();
    }

    /**
     * The Soundex digit for a single upper-case letter: 0 for vowels and the transparent
     * letters (A, E, I, O, U, Y, H, W), and 1-6 for the six consonant groups.
     */
    private static int codeOf(char letter) {
        switch (letter) {
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
