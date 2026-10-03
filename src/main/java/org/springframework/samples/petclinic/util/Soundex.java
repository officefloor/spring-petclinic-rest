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
 * Computes the American <a href="https://en.wikipedia.org/wiki/Soundex">Soundex</a> code of a name:
 * its first letter followed by three digits that phonetically encode the remaining consonants, so
 * similarly-sounding surnames (e.g. {@code 'Smith'} and {@code 'Smyth'}) share a code.
 *
 * <p>The algorithm: keep only letters and upper-case them; retain the first letter; map each
 * subsequent letter to a digit ({@code B,F,P,V->1}; {@code C,G,J,K,Q,S,X,Z->2}; {@code D,T->3};
 * {@code L->4}; {@code M,N->5}; {@code R->6}; vowels {@code A,E,I,O,U,Y->0}); drop a digit equal to
 * the previous letter's digit (letters separated by {@code H} or {@code W} count as adjacent, so
 * their shared digit collapses, but letters separated by a vowel are coded twice); drop the vowel
 * zeros; then truncate or zero-pad to exactly four characters. A value with no letters yields the
 * empty string.
 */
public final class Soundex {

    /** Digit for each letter A-Z; {@code '0'} marks a vowel (A, E, I, O, U, Y) that resets runs. */
    private static final String MAPPING = "01230120022455012623010202";

    private Soundex() {
    }

    /**
     * Encode {@code value} as its four-character Soundex code, or the empty string when the value is
     * {@code null} or contains no letters.
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
        char first = letters.charAt(0);
        code.append(first);
        char last = codeOf(first);
        for (int i = 1; i < letters.length() && code.length() < 4; i++) {
            char c = letters.charAt(i);
            if (c == 'H' || c == 'W') {
                // Transparent: does not reset the previous digit, so letters it separates collapse.
                continue;
            }
            char digit = codeOf(c);
            if (digit != '0' && digit != last) {
                code.append(digit);
            }
            last = digit;
        }
        while (code.length() < 4) {
            code.append('0');
        }
        return code.toString();
    }

    /** The Soundex digit for an upper-case letter A-Z. */
    private static char codeOf(char letter) {
        return MAPPING.charAt(letter - 'A');
    }
}
