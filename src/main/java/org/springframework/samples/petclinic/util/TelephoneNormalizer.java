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

import java.util.Optional;

/**
 * Normalization and comparison of owner telephone numbers. Kept separate from the owner controller
 * and the {@code Owner} model so the rules for shaping and comparing a telephone live in one place,
 * as pure functions with no web or persistence dependencies.
 *
 * <p>A submitted telephone is reduced to its canonical <a href="https://en.wikipedia.org/wiki/E.164">E.164</a>
 * form, which is the value stored and returned. Spaces, dashes and brackets are stripped; a leading
 * {@code '+'} and its country code are kept when present, otherwise the country code {@code '+61'} is
 * assumed and a single leading {@code '0'} is dropped from the national digits. The result is accepted
 * only when 8 to 15 digits follow the {@code '+'}. Two telephones denote the same number when their
 * {@linkplain #toComparisonKey(String) comparison keys} — their E.164 forms — are equal.
 */
public abstract class TelephoneNormalizer {

    /** Characters stripped from a submitted telephone: spaces, dashes and brackets. */
    private static final String SEPARATORS = "[\\s()\\[\\]-]";

    private static final int MIN_E164_DIGITS = 8;

    private static final int MAX_E164_DIGITS = 15;

    /**
     * Reduce a submitted telephone to its canonical E.164 form, which is the value to be stored and
     * returned. Spaces, dashes and brackets are removed; when the number carries a leading {@code '+'}
     * its country code is kept, otherwise country code {@code 61} is assumed and a single leading
     * {@code '0'} is dropped from the national digits. The result is accepted only when 8 to 15 digits
     * follow the {@code '+'}.
     *
     * @param rawTelephone the submitted telephone (non-null)
     * @return the canonical E.164 telephone (e.g. {@code +61412345678}), or {@link Optional#empty()}
     *         if {@code rawTelephone} cannot form a valid E.164 number
     */
    public static Optional<String> normalize(String rawTelephone) {
        String cleaned = rawTelephone.replaceAll(SEPARATORS, "");
        String digits;
        if (cleaned.startsWith("+")) {
            digits = cleaned.substring(1);
        }
        else {
            String national = cleaned.startsWith("0") ? cleaned.substring(1) : cleaned;
            digits = "61" + national;
        }
        if (!digits.matches("\\d+") || digits.length() < MIN_E164_DIGITS || digits.length() > MAX_E164_DIGITS) {
            return Optional.empty();
        }
        return Optional.of("+" + digits);
    }

    /**
     * The key used to decide whether two telephones denote the same number: the telephone's canonical
     * E.164 form, so telephones stored in different textual shapes still compare equal. A value that
     * does not form a valid E.164 number (e.g. legacy data) falls back to its separator-stripped form.
     *
     * @param telephone a stored or already-normalized telephone (non-null)
     * @return the comparison key
     */
    public static String toComparisonKey(String telephone) {
        return normalize(telephone).orElseGet(() -> telephone.replaceAll(SEPARATORS, ""));
    }

}
