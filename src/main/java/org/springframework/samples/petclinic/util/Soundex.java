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
 * American Soundex phonetic encoder: a self-contained, stateless {@code String -> String} algorithm
 * for comparing names by sound. It is used both to derive an owner's identity key and to match
 * similar-sounding last names when flagging soft-duplicate owners, so it lives here in the shared
 * util package rather than on any one entity or controller.
 */
public abstract class Soundex {

    /**
     * Maps each letter {@code A}-{@code Z} to its American Soundex code digit, where {@code '0'}
     * marks the non-coded letters (the vowels plus {@code H}, {@code W} and {@code Y}). Indexed by
     * {@code letter - 'A'}.
     */
    private static final char[] MAPPING = "01230120022455012623010202".toCharArray();

    /**
     * The American Soundex code of a name: its first letter followed by three code digits derived
     * from the remaining letters (B/F/P/V=1, C/G/J/K/Q/S/X/Z=2, D/T=3, L=4, M/N=5, R=6; the vowels
     * plus H, W and Y are not coded), right-padded with zeros to exactly four characters. Adjacent
     * letters sharing a code (including when separated only by H or W) contribute a single digit,
     * while letters separated by a vowel are coded twice. Non-letters are ignored and the comparison
     * is case-insensitive. A {@code null} or letter-free value yields an empty string.
     *
     * @param value the name to encode, possibly {@code null}
     * @return the four-character Soundex code, or an empty string when there are no letters
     */
    public static String encode(String value) {
        if (value == null) {
            return "";
        }
        String clean = value.toUpperCase(Locale.ROOT).replaceAll("[^A-Z]", "");
        if (clean.isEmpty()) {
            return "";
        }
        char[] out = {'0', '0', '0', '0'};
        out[0] = clean.charAt(0);
        char last = codeAt(clean, 0);
        int count = 1;
        for (int i = 1; i < clean.length() && count < out.length; i++) {
            char mapped = codeAt(clean, i);
            if (mapped != 0) {
                if (mapped != '0' && mapped != last) {
                    out[count++] = mapped;
                }
                last = mapped;
            }
        }
        return new String(out);
    }

    /**
     * The Soundex code digit of the letter at {@code index}, applying the H/W rule: a letter whose
     * code matches the letter two positions back is suppressed (returns {@code (char) 0}) when the
     * intervening letter is {@code H} or {@code W}, so such a pair contributes a single digit.
     *
     * @param str a cleaned, upper-case, letters-only name
     * @param index the position of the letter to code
     * @return the mapped code digit, or {@code (char) 0} when the letter is suppressed by the H/W rule
     */
    private static char codeAt(String str, int index) {
        char mapped = MAPPING[str.charAt(index) - 'A'];
        if (index > 1 && mapped != '0') {
            char hw = str.charAt(index - 1);
            if (hw == 'H' || hw == 'W') {
                char preHW = str.charAt(index - 2);
                if (MAPPING[preHW - 'A'] == mapped || preHW == 'H' || preHW == 'W') {
                    return 0;
                }
            }
        }
        return mapped;
    }

}
