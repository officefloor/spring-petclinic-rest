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

import java.util.LinkedHashMap;
import java.util.Map;
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
 * only when 8 to 15 digits follow the {@code '+'} and, for a recognised country code, exactly as many
 * national digits follow the code as that country requires ({@code +61} requires 9, {@code +1} requires
 * 10). Two telephones denote the same number when their
 * {@linkplain #toComparisonKey(String) comparison keys} — their E.164 forms — are equal.
 */
public abstract class TelephoneNormalizer {

    /** Characters stripped from a submitted telephone: spaces, dashes and brackets. */
    private static final String SEPARATORS = "[\\s()\\[\\]-]";

    private static final int MIN_E164_DIGITS = 8;

    private static final int MAX_E164_DIGITS = 15;

    /**
     * The exact number of national (subscriber) digits required after each known country code.
     * A telephone whose country code appears here is accepted only when precisely this many
     * digits follow the code (e.g. {@code +61} requires 9 national digits, {@code +1} requires 10).
     * Ordered longest-prefix-first so the country code is matched greedily.
     */
    private static final Map<String, Integer> NATIONAL_LENGTH_BY_COUNTRY_CODE = new LinkedHashMap<>();

    static {
        NATIONAL_LENGTH_BY_COUNTRY_CODE.put("61", 9);
        NATIONAL_LENGTH_BY_COUNTRY_CODE.put("1", 10);
    }

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
        if (!hasValidNationalLength(digits)) {
            return Optional.empty();
        }
        return Optional.of("+" + digits);
    }

    /**
     * Checks the national (subscriber) portion of an all-digit E.164 number against the length its
     * country code requires. When the leading country code is one we recognise (see
     * {@link #NATIONAL_LENGTH_BY_COUNTRY_CODE}), the remaining digits must number exactly the value
     * mapped for that code; an unrecognised country code is left to the generic 8-to-15-digit bound.
     *
     * @param digits the E.164 number without its leading {@code '+'} (digits only)
     * @return {@code true} if the national-number length is valid for the country code
     */
    private static boolean hasValidNationalLength(String digits) {
        for (Map.Entry<String, Integer> entry : NATIONAL_LENGTH_BY_COUNTRY_CODE.entrySet()) {
            String countryCode = entry.getKey();
            if (digits.startsWith(countryCode)) {
                int nationalLength = digits.length() - countryCode.length();
                return nationalLength == entry.getValue();
            }
        }
        return true;
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

    /**
     * Format a stored E.164 telephone for human display: the country code, a single space, then the
     * national (subscriber) digits grouped in threes (e.g. {@code +61412345678} becomes
     * {@code +61 412 345 678}). The country code is taken from the recognised prefixes in
     * {@link #NATIONAL_LENGTH_BY_COUNTRY_CODE}; a value whose country code is not recognised, or that
     * is not a valid E.164 number, is returned unchanged so the raw form is never lost.
     *
     * @param telephone a stored, canonical E.164 telephone (non-null)
     * @return the human-readable telephone
     */
    public static String toDisplay(String telephone) {
        Optional<String> normalized = normalize(telephone);
        if (normalized.isEmpty()) {
            return telephone;
        }
        String digits = normalized.get().substring(1);
        for (String countryCode : NATIONAL_LENGTH_BY_COUNTRY_CODE.keySet()) {
            if (digits.startsWith(countryCode)) {
                String national = digits.substring(countryCode.length());
                return "+" + countryCode + " " + groupInThrees(national);
            }
        }
        return telephone;
    }

    /**
     * Group a run of digits into space-separated groups of three, counting from the left; a trailing
     * group of one or two digits is kept as-is (e.g. {@code 412345678} becomes {@code 412 345 678}).
     */
    private static String groupInThrees(String digits) {
        StringBuilder grouped = new StringBuilder();
        for (int i = 0; i < digits.length(); i++) {
            if (i > 0 && i % 3 == 0) {
                grouped.append(' ');
            }
            grouped.append(digits.charAt(i));
        }
        return grouped.toString();
    }

}
