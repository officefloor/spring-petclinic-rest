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

package org.springframework.samples.petclinic.util;

import java.util.Locale;

/**
 * The American Soundex phonetic code of a name. Kept separate from the owner controller and the
 * {@code Owner} model so the single way this codebase reduces a name to its phonetic key lives in
 * one place, as a pure function with no web or persistence dependencies. Mirrors
 * {@link TelephoneNormalizer} and {@link EmailNormalizer}, whose comparison keys sit alongside it in
 * the owner {@linkplain OwnerIdentity identity key}.
 *
 * <p>The code is the name's first letter followed by three digits derived from its remaining
 * consonants, so names that sound alike collapse to the same value (e.g. {@code "Robert"} and
 * {@code "Rupert"} both code to {@code R163}). Vowels and the letters {@code h}, {@code w},
 * {@code y} carry no digit; consonants that share a code are coalesced when adjacent, including when
 * separated only by {@code h} or {@code w}. A name with no letters codes to the empty string.
 */
public abstract class Soundex {

    /** The fixed length of a Soundex code: one letter and three digits. */
    private static final int CODE_LENGTH = 4;

    /**
     * The American Soundex code of a name: its first letter (upper-cased) followed by three digits,
     * zero-padded when the name yields fewer. Non-letter characters are ignored before coding, so
     * spacing and punctuation do not affect the result.
     *
     * @param name the name to encode, or {@code null}
     * @return the four-character Soundex code, or the empty string when {@code name} is {@code null}
     *         or contains no letters
     */
    public static String soundex(String name) {
        if (name == null) {
            return "";
        }
        String letters = onlyLetters(name);
        if (letters.isEmpty()) {
            return "";
        }
        StringBuilder code = new StringBuilder().append(letters.charAt(0));
        char previousDigit = digitOf(letters.charAt(0));
        for (int i = 1; i < letters.length() && code.length() < CODE_LENGTH; i++) {
            char letter = letters.charAt(i);
            if (letter == 'H' || letter == 'W') {
                // 'h' and 'w' are transparent: they neither contribute a digit nor break the
                // run of a preceding consonant, so same-coded consonants they separate coalesce.
                continue;
            }
            char digit = digitOf(letter);
            if (digit != '0' && digit != previousDigit) {
                code.append(digit);
            }
            // Vowels (and 'y') carry no digit but do break a run, so two same-coded consonants
            // separated by a vowel are both coded; any other letter carries its own code forward.
            previousDigit = isVowel(letter) ? '0' : digit;
        }
        while (code.length() < CODE_LENGTH) {
            code.append('0');
        }
        return code.toString();
    }

    /**
     * The letters of {@code value}, upper-cased, with every non-letter character removed.
     */
    private static String onlyLetters(String value) {
        StringBuilder letters = new StringBuilder(value.length());
        for (char c : value.toUpperCase(Locale.ROOT).toCharArray()) {
            if (c >= 'A' && c <= 'Z') {
                letters.append(c);
            }
        }
        return letters.toString();
    }

    /**
     * The Soundex digit a letter contributes, or {@code '0'} for a letter that carries no digit
     * (the vowels and {@code h}, {@code w}, {@code y}).
     */
    private static char digitOf(char letter) {
        return switch (letter) {
            case 'B', 'F', 'P', 'V' -> '1';
            case 'C', 'G', 'J', 'K', 'Q', 'S', 'X', 'Z' -> '2';
            case 'D', 'T' -> '3';
            case 'L' -> '4';
            case 'M', 'N' -> '5';
            case 'R' -> '6';
            default -> '0';
        };
    }

    private static boolean isVowel(char letter) {
        return letter == 'A' || letter == 'E' || letter == 'I' || letter == 'O' || letter == 'U'
            || letter == 'Y';
    }

}
