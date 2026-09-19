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

import org.springframework.stereotype.Component;

/**
 * Normalizes an owner's telephone number into E.164 form on create.
 *
 * <p>Spaces, dashes and brackets are stripped. A leading {@code +} and its country code
 * are kept as-is; without one, the default country code {@code +61} is assumed and a
 * single leading {@code 0} is dropped from the national digits. The result must carry
 * between 8 and 15 digits after the {@code +}. This E.164 string is what gets stored,
 * returned, and compared when detecting duplicate telephones.
 */
@Component
public class TelephoneNormalizer {

    private static final String DEFAULT_COUNTRY_CODE = "61";

    private static final int MIN_DIGITS = 8;

    private static final int MAX_DIGITS = 15;

    /**
     * Converts {@code raw} into its E.164 representation.
     *
     * @param raw the submitted telephone value
     * @return the normalized E.164 telephone number (a leading {@code +} followed by digits)
     * @throws InvalidTelephoneException if the value cannot form a valid E.164 number
     */
    public String normalize(String raw) {
        String trimmed = raw == null ? "" : raw.strip();
        boolean explicitCountryCode = trimmed.startsWith("+");
        String stripped = trimmed.replaceAll("[\\s()\\-]", "");

        String digits;
        if (explicitCountryCode) {
            digits = stripped.substring(1);
        } else {
            String national = stripped.startsWith("0") ? stripped.substring(1) : stripped;
            digits = DEFAULT_COUNTRY_CODE + national;
        }

        if (!digits.matches("\\d{" + MIN_DIGITS + "," + MAX_DIGITS + "}")) {
            throw new InvalidTelephoneException(raw);
        }
        return "+" + digits;
    }
}
