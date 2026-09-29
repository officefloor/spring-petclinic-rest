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
 * Computes the <a href="https://en.wikipedia.org/wiki/Soundex">American Soundex</a> code of a name:
 * the retained (upper-cased) first letter followed by three digits derived from the remaining
 * consonants, so names that sound alike share a code. Non-letters are ignored; {@code h} and
 * {@code w} do not separate same-coded consonants; vowels (and {@code y}) act as separators. The
 * result is padded with trailing zeros / truncated to exactly four characters. Returns the empty
 * string when the input is {@code null} or contains no letters.
 */
public final class Soundex {

    private Soundex() {
    }

    /** Digit each letter A-Z maps to ({@code '0'} for vowels, {@code h}, {@code w} and {@code y}). */
    private static final char[] CODES = new char[26];

    static {
        String[] groups = {"BFPV", "CGJKQSXZ", "DT", "L", "MN", "R"};
        for (int i = 0; i < groups.length; i++) {
            for (char c : groups[i].toCharArray()) {
                CODES[c - 'A'] = (char) ('1' + i);
            }
        }
    }

    /**
     * The four-character Soundex code of {@code name}, or the empty string when {@code name} is
     * {@code null} or holds no letters.
     */
    public static String encode(String name) {
        if (name == null) {
            return "";
        }
        String letters = name.toUpperCase(Locale.ROOT).replaceAll("[^A-Z]", "");
        if (letters.isEmpty()) {
            return "";
        }
        StringBuilder code = new StringBuilder(4);
        char first = letters.charAt(0);
        code.append(first);
        char prev = codeOf(first);
        for (int i = 1; i < letters.length() && code.length() < 4; i++) {
            char c = letters.charAt(i);
            if (c == 'H' || c == 'W') {
                continue;
            }
            char digit = codeOf(c);
            if (digit != '0' && digit != prev) {
                code.append(digit);
            }
            prev = digit;
        }
        while (code.length() < 4) {
            code.append('0');
        }
        return code.toString();
    }

    private static char codeOf(char c) {
        if (c < 'A' || c > 'Z') {
            return '0';
        }
        char digit = CODES[c - 'A'];
        return digit == 0 ? '0' : digit;
    }
}
