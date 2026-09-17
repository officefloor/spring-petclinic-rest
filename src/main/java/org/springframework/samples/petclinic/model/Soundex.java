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
 * Soundex helper shared across the domain: the single place that reduces a name to its standard
 * American Soundex code (an initial letter followed by three digits, e.g. {@code "Robert"} and
 * {@code "Rupert"} both yield {@code "R163"}), so every phonetic name comparison codes the same way.
 */
public final class Soundex {

    private Soundex() {
    }

    /**
     * Compute the four-character Soundex code of {@code name}: its first letter followed by three
     * digits encoding the remaining consonants. Non-letters are ignored, case is irrelevant, and a
     * value with no letters (null, blank or all punctuation) yields the empty string.
     *
     * @param name the name to encode
     * @return the Soundex code, or {@code ""} when {@code name} contains no letters
     */
    public static String of(String name) {
        if (name == null) {
            return "";
        }
        String letters = name.toUpperCase(Locale.ROOT).replaceAll("[^A-Z]", "");
        if (letters.isEmpty()) {
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
            // 'H' and 'W' do not separate two like-coded consonants, so they leave the running code
            // untouched; every other letter (including vowels, which code to '0') resets it.
            if (letter != 'H' && letter != 'W') {
                previousCode = digit;
            }
        }
        while (code.length() < 4) {
            code.append('0');
        }
        return code.toString();
    }

    /** Map a letter to its Soundex digit, using {@code '0'} for the uncoded letters (vowels, y, h, w). */
    private static char codeOf(char letter) {
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
