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

package org.springframework.samples.petclinic.rest.advice;

import java.util.Locale;

/**
 * Computes the standard 4-character Soundex code of a name (first letter followed by three
 * digits). Used by the owner identity key and the soft-match rule to group phonetically similar
 * last names. Kept as its own small, self-contained unit.
 */
final class OwnerSoundex {

    /** Soundex digit for each letter A-Z ('0' means the letter is dropped). */
    private static final String CODES = "01230120022455012623010202";

    private OwnerSoundex() {
    }

    static String soundex(String name) {
        String letters = (name == null) ? "" : name.toUpperCase(Locale.ROOT).replaceAll("[^A-Z]", "");
        if (letters.isEmpty()) {
            return "0000";
        }
        StringBuilder code = new StringBuilder().append(letters.charAt(0));
        char previous = codeOf(letters.charAt(0));
        for (int i = 1; i < letters.length() && code.length() < 4; i++) {
            char digit = codeOf(letters.charAt(i));
            if (digit != '0' && digit != previous) {
                code.append(digit);
            }
            if (letters.charAt(i) != 'H' && letters.charAt(i) != 'W') {
                previous = digit;
            }
        }
        while (code.length() < 4) {
            code.append('0');
        }
        return code.toString();
    }

    private static char codeOf(char letter) {
        return CODES.charAt(letter - 'A');
    }
}
