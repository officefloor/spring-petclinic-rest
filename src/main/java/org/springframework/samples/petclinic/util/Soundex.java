/*
 * Copyright 2016 the original author or authors.
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
 * Encodes a name to its four-character American Soundex code, so names that sound alike collapse to
 * the same value (e.g. {@code "Smith"} and {@code "Smyth"} both yield {@code "S530"}). The code is
 * the retained first letter followed by three digits derived from the remaining consonants; adjacent
 * letters sharing a digit are coded once, vowels reset the run, and {@code 'H'}/{@code 'W'} are
 * transparent. Used to fold together like-sounding last names when deriving an owner's identity and
 * detecting soft duplicates.
 */
public final class Soundex {

    private static final int CODE_LENGTH = 4;

    private Soundex() {
    }

    /**
     * Produce the Soundex code of the supplied name.
     *
     * @param value the name to encode, may be {@code null}
     * @return the four-character Soundex code, or an empty string when the value is {@code null} or
     *         holds no letters
     */
    public static String encode(String value) {
        if (value == null) {
            return "";
        }
        StringBuilder letters = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (Character.isLetter(c)) {
                letters.append(Character.toUpperCase(c));
            }
        }
        if (letters.length() == 0) {
            return "";
        }
        StringBuilder code = new StringBuilder().append(letters.charAt(0));
        char previous = digitOf(letters.charAt(0));
        for (int i = 1; i < letters.length() && code.length() < CODE_LENGTH; i++) {
            char letter = letters.charAt(i);
            char digit = digitOf(letter);
            if (digit != '0' && digit != previous) {
                code.append(digit);
            }
            // 'H' and 'W' are transparent: they neither code nor separate a run of like-coded
            // consonants. Every other letter (including vowels, which code as '0') ends the run.
            if (letter != 'H' && letter != 'W') {
                previous = digit;
            }
        }
        while (code.length() < CODE_LENGTH) {
            code.append('0');
        }
        return code.toString();
    }

    private static char digitOf(char letter) {
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
