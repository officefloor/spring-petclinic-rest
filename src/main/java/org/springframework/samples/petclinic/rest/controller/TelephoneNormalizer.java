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

import java.util.Optional;
import java.util.regex.Pattern;

import org.springframework.samples.petclinic.model.CountryDialingCode;
import org.springframework.stereotype.Component;

/**
 * Canonicalises an owner telephone number to E.164 form.
 * <p>
 * Spaces, dashes and brackets are stripped. A leading {@code '+'} and its country code are
 * kept when present; otherwise the {@link #DEFAULT_COUNTRY_CODE default country code} is
 * assumed and a single leading {@code '0'} is dropped from the national digits. The result
 * is a {@code '+'} followed by {@link #MIN_DIGITS}-{@link #MAX_DIGITS} digits, so numbers
 * that differ only in formatting or in the way their country code is written are stored and
 * compared identically.
 * <p>
 * For {@link #NATIONAL_LENGTHS known country codes} the national number (the digits after
 * the country code) must additionally carry the exact number of digits that country uses
 * (e.g. {@code '+61'} requires 9 national digits, {@code '+1'} requires 10); a number whose
 * national part is the wrong length for its country is rejected.
 */
@Component
public class TelephoneNormalizer {

    /** Country code assumed when the number carries no explicit {@code '+'} prefix. */
    public static final String DEFAULT_COUNTRY_CODE = "61";

    /** The fewest digits a valid E.164 number carries after the {@code '+'}. */
    public static final int MIN_DIGITS = 8;

    /** The most digits a valid E.164 number carries after the {@code '+'}. */
    public static final int MAX_DIGITS = 15;

    /** Separators removed before interpreting the number: spaces, dashes and brackets. */
    private static final Pattern SEPARATORS = Pattern.compile("[\\s()\\[\\]-]");

    /**
     * Converts a raw telephone into its canonical E.164 representation.
     *
     * @param raw the telephone as supplied by the client, possibly {@code null}
     * @return the E.164 number ({@code '+'} then {@value #MIN_DIGITS}-{@value #MAX_DIGITS}
     *         digits), or empty when {@code raw} cannot form a valid E.164 number
     */
    public Optional<String> toE164(String raw) {
        if (raw == null) {
            return Optional.empty();
        }
        String stripped = SEPARATORS.matcher(raw).replaceAll("");
        String digits = stripped.startsWith("+")
            ? stripped.substring(1)
            : DEFAULT_COUNTRY_CODE + dropLeadingZero(stripped);
        return isValid(digits) ? Optional.of("+" + digits) : Optional.empty();
    }

    private String dropLeadingZero(String national) {
        return national.startsWith("0") ? national.substring(1) : national;
    }

    private boolean isValid(String digits) {
        return digits.chars().allMatch(c -> c >= '0' && c <= '9')
            && digits.length() >= MIN_DIGITS && digits.length() <= MAX_DIGITS
            && hasValidNationalLength(digits);
    }

    /**
     * Checks the national-number length for a known country code. Numbers whose leading
     * digits match no known country code pass this check, relying on the generic bounds.
     */
    private boolean hasValidNationalLength(String digits) {
        return CountryDialingCode.matching(digits)
            .flatMap(code -> CountryDialingCode.nationalLength(code)
                .map(length -> digits.length() - code.length() == length))
            .orElse(true);
    }
}
