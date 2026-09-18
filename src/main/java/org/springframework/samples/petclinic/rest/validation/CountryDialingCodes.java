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

package org.springframework.samples.petclinic.rest.validation;

import java.util.Map;

/**
 * The country dialing codes the clinic recognises, mapped to the national-number length each one
 * requires. This is the single source of truth shared by the telephone {@link TelephoneNormalizer
 * normalizer} and {@link TelephoneFormatter formatter}.
 */
final class CountryDialingCodes {

    /** Recognised country codes (the digits after {@code '+'}) mapped to their required national-number length. */
    private static final Map<String, Integer> NATIONAL_LENGTH_BY_COUNTRY = Map.of(
            "61", 9,
            "1", 10);

    private CountryDialingCodes() {
    }

    /**
     * Returns the recognised country code that the given digit string begins with, or {@code null}
     * when no known country code matches. Longer codes are preferred so a specific match wins over
     * a shorter prefix.
     */
    static String matching(String digits) {
        String match = null;
        for (String code : NATIONAL_LENGTH_BY_COUNTRY.keySet()) {
            if (digits.startsWith(code) && (match == null || code.length() > match.length())) {
                match = code;
            }
        }
        return match;
    }

    /**
     * Returns the national-number length required for the given recognised country code, or
     * {@code null} when the code is unrecognised.
     */
    static Integer nationalLength(String countryCode) {
        return countryCode == null ? null : NATIONAL_LENGTH_BY_COUNTRY.get(countryCode);
    }
}
