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

import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Converts telephone numbers to canonical E.164 form. Shared by the create-time validation
 * constraint and the mapping that stores the value, so both agree on what a valid, stored
 * telephone looks like.
 *
 * <p>A leading {@code '+'} with its country code is kept as given; otherwise the Australian
 * country code {@code '+61'} is assumed and a single leading {@code '0'} is dropped from the
 * national digits. Spaces, dashes and brackets are ignored. The digits following the
 * {@code '+'} must number between 8 and 15 inclusive, and for a recognised country calling
 * code the national-number length must match what that country requires (see
 * {@link CountryCallingCode}).
 */
public final class TelephoneNormalizer {

    /** Country code assumed when the number carries no explicit {@code '+'} prefix. */
    private static final String DEFAULT_COUNTRY_CODE = "61";

    /** Separators that carry no meaning and are stripped before parsing. */
    private static final Pattern SEPARATORS = Pattern.compile("[\\s()\\-]");

    private static final int MIN_DIGITS = 8;
    private static final int MAX_DIGITS = 15;

    private TelephoneNormalizer() {
    }

    /**
     * Convert the given telephone value to E.164 form.
     *
     * @param telephone the raw telephone value (may be {@code null})
     * @return the E.164 string (a {@code '+'} followed by 8 to 15 digits), or
     * {@link Optional#empty()} when {@code telephone} is {@code null} or cannot form valid E.164
     */
    public static Optional<String> toE164(String telephone) {
        if (telephone == null) {
            return Optional.empty();
        }
        String cleaned = SEPARATORS.matcher(telephone).replaceAll("");
        String digits;
        if (cleaned.startsWith("+")) {
            digits = cleaned.substring(1);
        } else {
            String national = cleaned.startsWith("0") ? cleaned.substring(1) : cleaned;
            digits = DEFAULT_COUNTRY_CODE + national;
        }
        if (digits.length() < MIN_DIGITS || digits.length() > MAX_DIGITS || !isAllDigits(digits)) {
            return Optional.empty();
        }
        Optional<CountryCallingCode> country = CountryCallingCode.forDigits(digits);
        if (country.isPresent() && !country.get().hasValidNationalLength(digits)) {
            return Optional.empty();
        }
        return Optional.of("+" + digits);
    }

    private static boolean isAllDigits(String value) {
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return true;
    }
}
