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
package org.springframework.samples.petclinic.util;

/**
 * Standard American Soundex: a four-character phonetic code — a retained first letter followed by
 * three digits — that groups surnames that sound alike (e.g. {@code Robert} and {@code Rupert}
 * both encode to {@code R163}). Used as the surname component of an owner's identity key and by the
 * soft "possible duplicate" match, so that names differing only in spelling collapse together.
 *
 * <p>The letters are mapped to digits as {@code B,F,P,V -> 1}; {@code C,G,J,K,Q,S,X,Z -> 2};
 * {@code D,T -> 3}; {@code L -> 4}; {@code M,N -> 5}; {@code R -> 6}; the vowels
 * {@code A,E,I,O,U,Y} act as separators and {@code H,W} are skipped without separating. Adjacent
 * letters sharing a code (including the first letter and an immediately following same-coded
 * consonant) are coded once; the result is padded with zeros and truncated to four characters. A
 * value with no letters (including {@code null}) encodes to the empty string.
 */
public final class Soundex {

    private Soundex() {
    }

    /**
     * Encode {@code value} as its Soundex code, or the empty string when it contains no letters.
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
        StringBuilder code = new StringBuilder();
        code.append(letters.charAt(0));
        char previous = codeOf(letters.charAt(0));
        for (int i = 1; i < letters.length() && code.length() < 4; i++) {
            char c = letters.charAt(i);
            if (c == 'H' || c == 'W') {
                // Not coded and not a separator: a same-coded consonant across an H/W is coded once.
                continue;
            }
            char mapped = codeOf(c);
            if (mapped != '0' && mapped != previous) {
                code.append(mapped);
            }
            // Vowels (mapped == '0') reset the previous code so they act as a separator.
            previous = mapped;
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
            default:
                return '0';
        }
    }
}
