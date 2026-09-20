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

package org.springframework.samples.petclinic.rest.controller;

import java.util.Locale;

import org.springframework.stereotype.Component;

/**
 * American Soundex: a phonetic code (a letter followed by three digits) that groups names that
 * sound alike. Used to compare last names for owner identity and soft-duplicate detection, so two
 * spellings that sound the same are treated as the same household name.
 */
@Component
public class Soundex {

    private static final int CODE_LENGTH = 4;

    /**
     * The Soundex code of {@code name} (e.g. {@code "Robert" -> "R163"}). Non-letters are ignored;
     * {@code null}/blank names yield an empty string.
     */
    public String of(String name) {
        String letters = name == null ? "" : name.toUpperCase(Locale.ROOT).replaceAll("[^A-Z]", "");
        if (letters.isEmpty()) {
            return "";
        }
        StringBuilder code = new StringBuilder().append(letters.charAt(0));
        char previous = digitOf(letters.charAt(0));
        for (int i = 1; i < letters.length() && code.length() < CODE_LENGTH; i++) {
            char letter = letters.charAt(i);
            // 'H' and 'W' are transparent: same-coded letters they separate still collapse.
            if (letter == 'H' || letter == 'W') {
                continue;
            }
            char digit = digitOf(letter);
            if (digit != '0' && digit != previous) {
                code.append(digit);
            }
            // A vowel resets adjacency so equal codes it separates are encoded twice.
            previous = isVowel(letter) ? '0' : digit;
        }
        while (code.length() < CODE_LENGTH) {
            code.append('0');
        }
        return code.toString();
    }

    private char digitOf(char letter) {
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

    private boolean isVowel(char letter) {
        return "AEIOUY".indexOf(letter) >= 0;
    }
}
