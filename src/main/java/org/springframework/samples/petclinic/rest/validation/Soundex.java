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

/**
 * Utility computing the standard American Soundex code of a name: the retained (upper-cased)
 * first letter followed by up to three digits derived from the remaining consonants, zero-padded
 * to a four-character code. Two names that sound alike therefore share a code, which is how the
 * owner {@code identityKey} and the soft-duplicate rule group owners by last name.
 *
 * <p>Non-letters are ignored, vowels (and {@code y}) separate otherwise-adjacent equal codes, and
 * {@code h}/{@code w} are transparent (they do not separate). A {@code null} or letter-free value
 * encodes to an empty string.
 */
public final class Soundex {

    private Soundex() {
    }

    /**
     * Encode the supplied name as its four-character Soundex code.
     *
     * @param name the raw name (may be {@code null})
     * @return the Soundex code, or an empty string when {@code name} is {@code null} or has no letters
     */
    public static String encode(String name) {
        if (name == null) {
            return "";
        }
        StringBuilder letters = new StringBuilder();
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (Character.isLetter(c)) {
                letters.append(Character.toUpperCase(c));
            }
        }
        if (letters.length() == 0) {
            return "";
        }
        char first = letters.charAt(0);
        StringBuilder code = new StringBuilder().append(first);
        char prev = codeOf(first);
        for (int i = 1; i < letters.length() && code.length() < 4; i++) {
            char c = letters.charAt(i);
            if (c == 'H' || c == 'W') {
                // Transparent: two same-coded letters split by H/W stay a single code.
                continue;
            }
            char digit = codeOf(c);
            if (digit != '0' && digit != prev) {
                code.append(digit);
            }
            // A vowel (or Y) resets the running code so a repeat after it is coded again.
            prev = isVowel(c) ? '0' : digit;
        }
        while (code.length() < 4) {
            code.append('0');
        }
        return code.substring(0, 4);
    }

    private static boolean isVowel(char c) {
        return c == 'A' || c == 'E' || c == 'I' || c == 'O' || c == 'U' || c == 'Y';
    }

    private static char codeOf(char c) {
        return switch (c) {
            case 'B', 'F', 'P', 'V' -> '1';
            case 'C', 'G', 'J', 'K', 'Q', 'S', 'X', 'Z' -> '2';
            case 'D', 'T' -> '3';
            case 'L' -> '4';
            case 'M', 'N' -> '5';
            case 'R' -> '6';
            default -> '0';
        };
    }
}
