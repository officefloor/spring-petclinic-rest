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
 * Computes the American Soundex code of a name: a phonetic index consisting of the (upper-cased)
 * first letter followed by three digits, e.g. {@code "Robert" -> "R163"}. Names that sound alike
 * share a code, which lets duplicate detection group likely-same surnames that are spelled
 * differently. Non-letters are ignored; {@code H} and {@code W} are transparent (they do not break
 * a run of same-coded consonants) while vowels do. A value with no letters yields the empty string.
 */
public final class Soundex {

    private Soundex() {
    }

    /**
     * @param value the name to encode (may be {@code null})
     * @return the four-character Soundex code, or {@code ""} when {@code value} carries no letters
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
        StringBuilder code = new StringBuilder(4).append(letters.charAt(0));
        char previous = digit(letters.charAt(0));
        for (int i = 1; i < letters.length() && code.length() < 4; i++) {
            char c = letters.charAt(i);
            if (c == 'H' || c == 'W') {
                continue;
            }
            char d = digit(c);
            if (d != '0' && d != previous) {
                code.append(d);
            }
            previous = isVowel(c) ? '0' : d;
        }
        while (code.length() < 4) {
            code.append('0');
        }
        return code.toString();
    }

    /** @return the Soundex digit for an upper-case letter, {@code '0'} for vowels, H, W and Y. */
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

    private static boolean isVowel(char c) {
        return c == 'A' || c == 'E' || c == 'I' || c == 'O' || c == 'U' || c == 'Y';
    }
}
