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

package org.springframework.samples.petclinic.rest.validation;

import java.util.Optional;
import java.util.regex.Pattern;

import org.springframework.samples.petclinic.model.CountryCallingCode;
import org.springframework.stereotype.Component;

/**
 * Normalizes and validates an owner's telephone number into E.164 form.
 *
 * <p>A leading {@code '+'} and country code are kept when present; otherwise the
 * default country code {@link #DEFAULT_COUNTRY_CODE} is assumed and a single
 * leading {@code '0'} is dropped from the national digits. Spaces, dashes and
 * brackets are stripped. The result is only accepted when it carries between
 * {@link #MIN_DIGITS} and {@link #MAX_DIGITS} digits after the {@code '+'} and,
 * for a recognised country code, exactly as many national digits as that
 * country requires (see {@link CountryCallingCode}).
 */
@Component
public class TelephoneNormalizer {

    /** Country code assumed when the raw number has no explicit {@code '+'} prefix. */
    public static final String DEFAULT_COUNTRY_CODE = "61";

    /** The fewest digits a valid E.164 number may have after the {@code '+'}. */
    public static final int MIN_DIGITS = 8;

    /** The most digits a valid E.164 number may have after the {@code '+'}. */
    public static final int MAX_DIGITS = 15;

    /** Separators that carry no numeric meaning and are stripped before parsing. */
    private static final Pattern SEPARATORS = Pattern.compile("[\\s()\\-]");

    private static final Pattern DIGITS = Pattern.compile("\\d+");

    /**
     * Convert a raw telephone value into its E.164 representation.
     *
     * @param telephone the raw telephone value (may be {@code null})
     * @return the E.164 number (e.g. {@code "+61412345678"}), or
     *         {@link Optional#empty()} if it cannot form a valid E.164 number
     */
    public Optional<String> toE164(String telephone) {
        if (telephone == null) {
            return Optional.empty();
        }
        String trimmed = telephone.trim();
        boolean explicitCountryCode = trimmed.startsWith("+");
        String body = explicitCountryCode ? trimmed.substring(1) : trimmed;
        String digits = SEPARATORS.matcher(body).replaceAll("");
        if (!DIGITS.matcher(digits).matches()) {
            return Optional.empty();
        }
        if (!explicitCountryCode) {
            String national = digits.startsWith("0") ? digits.substring(1) : digits;
            digits = DEFAULT_COUNTRY_CODE + national;
        }
        if (digits.length() < MIN_DIGITS || digits.length() > MAX_DIGITS) {
            return Optional.empty();
        }
        if (!hasValidNationalLength(digits)) {
            return Optional.empty();
        }
        return Optional.of("+" + digits);
    }

    /**
     * Check that the national number has the exact length its country code
     * requires. Numbers whose country code is not recognised are accepted here;
     * only the generic length bounds constrain them.
     *
     * @param digits the E.164 digits (country code followed by the national number)
     * @return {@code true} if the national-number length matches the country code
     */
    private boolean hasValidNationalLength(String digits) {
        return CountryCallingCode.of(digits)
            .map(code -> digits.length() - code.length() == CountryCallingCode.nationalDigits(code).orElseThrow())
            .orElse(true);
    }
}
