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

package org.springframework.samples.petclinic.rest.controller.v1;

/**
 * Computes the American Soundex code of a last name. Kept apart from {@link OwnerRestControllerV1} so
 * the request handler stays focused on orchestration while the phonetic-encoding rule lives in one
 * place. Soundex groups names that sound alike onto a common four-character code (a retained first
 * letter followed by three digits), so it is the phonetic segment of an owner's identity key and the
 * basis of the soft-match duplicate check.
 *
 * <p>This is the standard US English Soundex: input is upper-cased and stripped of non-letters, the
 * first letter is kept, subsequent letters are mapped to their digit ({@code b,f,p,v -> 1};
 * {@code c,g,j,k,q,s,x,z -> 2}; {@code d,t -> 3}; {@code l -> 4}; {@code m,n -> 5}; {@code r -> 6}),
 * adjacent letters sharing a digit (including when separated only by {@code h} or {@code w}) collapse
 * to a single digit, vowels reset that adjacency, and the result is padded with zeros or truncated to
 * exactly four characters.
 */
final class Soundex {

    /**
     * Digit each letter {@code A..Z} maps to, indexed by {@code letter - 'A'}; {@code '0'} marks a
     * letter (the vowels plus {@code h} and {@code w}) that contributes no digit of its own.
     */
    private static final String MAPPING = "01230120022455012623010202";

    private Soundex() {
    }

    /**
     * Encodes {@code lastName} as its four-character Soundex code. A {@code null} or letterless value
     * yields the empty string; otherwise the result is always the retained first letter followed by
     * three digits.
     *
     * @param lastName the owner's last name, or {@code null}
     * @return the Soundex code, or the empty string when there is no letter to encode
     */
    static String encode(String lastName) {
        if (lastName == null) {
            return "";
        }
        String cleaned = lastName.toUpperCase().replaceAll("[^A-Z]", "");
        if (cleaned.isEmpty()) {
            return "";
        }
        char[] out = {'0', '0', '0', '0'};
        out[0] = cleaned.charAt(0);
        char last = mappingCode(cleaned, 0);
        int count = 1;
        for (int i = 1; i < cleaned.length() && count < out.length; i++) {
            char mapped = mappingCode(cleaned, i);
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
     * Returns the digit the letter at {@code index} contributes, or {@code 0} (not the character
     * {@code '0'}) when an intervening {@code h} or {@code w} makes it a continuation of a preceding
     * letter that shares the same digit, so such pairs collapse rather than repeat.
     */
    private static char mappingCode(String str, int index) {
        char mapped = map(str.charAt(index));
        if (index > 1 && mapped != '0') {
            char hw = str.charAt(index - 1);
            if (hw == 'H' || hw == 'W') {
                char preHw = str.charAt(index - 2);
                if (map(preHw) == mapped || preHw == 'H' || preHw == 'W') {
                    return 0;
                }
            }
        }
        return mapped;
    }

    private static char map(char c) {
        return MAPPING.charAt(c - 'A');
    }

}
