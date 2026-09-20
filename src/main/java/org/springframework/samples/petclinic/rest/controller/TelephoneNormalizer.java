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

import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

/**
 * Normalizes a raw telephone input into canonical E.164 form.
 * <p>
 * Spaces, dashes and brackets are stripped. When the remaining value carries a
 * leading {@code '+'} its country code is kept as supplied; otherwise the default
 * country code {@link #DEFAULT_COUNTRY_CODE} is assumed and a single leading
 * {@code '0'} is dropped from the national digits. The digits following the
 * {@code '+'} must number between {@link #MIN_DIGITS} and {@link #MAX_DIGITS};
 * anything else is rejected with an {@link InvalidTelephoneException} (surfaced as
 * {@code 400 Bad Request}).
 */
@Component
public class TelephoneNormalizer {

    private static final String DEFAULT_COUNTRY_CODE = "61";
    private static final int MIN_DIGITS = 8;
    private static final int MAX_DIGITS = 15;

    /** Matches the digits that must follow the '+' once the country code is resolved. */
    private static final Pattern E164_DIGITS =
        Pattern.compile("\\d{" + MIN_DIGITS + "," + MAX_DIGITS + "}");

    /**
     * Convert {@code rawTelephone} into its E.164 representation.
     *
     * @param rawTelephone the telephone as supplied by the client (may contain formatting)
     * @return the E.164 telephone (a leading {@code '+'} followed by 8-15 digits) to store and return
     * @throws InvalidTelephoneException if the value cannot form a valid E.164 number
     */
    public String normalize(String rawTelephone) {
        String cleaned = rawTelephone == null ? "" : rawTelephone.replaceAll("[\\s()-]", "");
        String digits;
        if (cleaned.startsWith("+")) {
            digits = cleaned.substring(1);
        } else {
            String national = cleaned.startsWith("0") ? cleaned.substring(1) : cleaned;
            digits = DEFAULT_COUNTRY_CODE + national;
        }
        if (!E164_DIGITS.matcher(digits).matches()) {
            throw new InvalidTelephoneException(rawTelephone);
        }
        return "+" + digits;
    }
}
