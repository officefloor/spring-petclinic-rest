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
package org.springframework.samples.petclinic.model;

/**
 * Encodes a name with the <a href="https://en.wikipedia.org/wiki/Soundex">American Soundex</a>
 * phonetic algorithm, so names that sound alike share a code. The result is the retained first
 * letter followed by three digits (e.g. {@code "Robert"} and {@code "Rupert"} both encode to
 * {@code "R163"}). Used to group owners by how their last name sounds.
 */
public final class Soundex {

    private Soundex() {
    }

    /**
     * Encode {@code name} to its four-character Soundex code: the upper-cased first letter of the
     * name followed by three digits derived from the remaining consonants. Non-letter characters
     * are ignored; a value with no letters (or {@code null}) encodes to the empty string.
     *
     * @param name the name to encode
     * @return the four-character Soundex code, or the empty string when {@code name} has no letters
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
        StringBuilder code = new StringBuilder();
        code.append(letters.charAt(0));
        char previousCode = codeOf(letters.charAt(0));
        for (int i = 1; i < letters.length() && code.length() < 4; i++) {
            char c = letters.charAt(i);
            // 'h' and 'w' are transparent: they neither contribute a digit nor break the run of a
            // preceding consonant, so a same-coded consonant on either side still counts once.
            if (c == 'H' || c == 'W') {
                continue;
            }
            char digit = codeOf(c);
            if (digit != NO_CODE && digit != previousCode) {
                code.append(digit);
            }
            previousCode = digit;
        }
        while (code.length() < 4) {
            code.append('0');
        }
        return code.toString();
    }

    /** The marker for letters (vowels, plus 'h' and 'w') that contribute no Soundex digit. */
    private static final char NO_CODE = '0';

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
                return NO_CODE;
        }
    }
}
