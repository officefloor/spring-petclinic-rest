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

import java.util.Locale;

/**
 * Standard (American) Soundex phonetic encoder. Produces a four-character code - the retained first
 * letter followed by three digits - so that surnames that sound alike share a code. Used to derive
 * the surname component of an owner's identity key and to detect soft surname matches.
 */
public final class Soundex {

    private Soundex() {
    }

    /**
     * Encodes a value with the standard Soundex algorithm: keep the first letter, map the remaining
     * consonants to digits ({@code BFPV->1}, {@code CGJKQSXZ->2}, {@code DT->3}, {@code L->4},
     * {@code MN->5}, {@code R->6}), collapse adjacent equal codes, drop vowels (which separate equal
     * codes) while {@code H} and {@code W} do not separate, then pad or truncate to four characters.
     * Non-letters are ignored; a {@code null} or letter-free value encodes to the empty string.
     *
     * @param value the value to encode, may be {@code null}
     * @return the four-character Soundex code, or the empty string when there is no letter to encode
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
        char previous = digit(first);
        for (int i = 1; i < letters.length() && code.length() < 4; i++) {
            char c = letters.charAt(i);
            if (c == 'H' || c == 'W') {
                // H and W neither encode nor separate: the previous code carries across them.
                continue;
            }
            char d = digit(c);
            if (d != '0' && d != previous) {
                code.append(d);
            }
            previous = d;
        }
        while (code.length() < 4) {
            code.append('0');
        }
        return code.toString();
    }

    private static char digit(char c) {
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
