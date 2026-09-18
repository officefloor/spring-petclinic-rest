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
import java.util.regex.Pattern;

/**
 * Normalizes an owner's telephone number to canonical E.164 form. A leading {@code '+'} and its
 * country code are preserved when present; otherwise the default country code {@code +61} is
 * assumed and a single leading {@code '0'} is dropped from the national digits. Separators such as
 * spaces, dashes and brackets are stripped. The result must carry 8 to 15 digits after the
 * {@code '+'}, and for a recognised country code the national number must be exactly as long as
 * that country requires ({@code +61} => 9 national digits, {@code +1} => 10). A number that cannot
 * form a valid E.164 value is rejected.
 */
public final class TelephoneNormalizer {

    private static final Pattern NON_DIGIT = Pattern.compile("\\D");

    private static final String DEFAULT_COUNTRY_CODE = "61";

    private static final int MIN_DIGITS = 8;

    private static final int MAX_DIGITS = 15;

    /** Recognised country codes (the digits after {@code '+'}) mapped to their required national-number length. */
    private static final Map<String, Integer> NATIONAL_LENGTH_BY_COUNTRY = Map.of(
            "61", 9,
            "1", 10);

    private TelephoneNormalizer() {
    }

    /**
     * Converts the supplied telephone to its canonical E.164 representation.
     *
     * @param telephone the raw telephone as submitted
     * @return the telephone in E.164 form, e.g. {@code +61412345678}
     * @throws InvalidTelephoneException if the value cannot form a valid E.164 number
     */
    public static String normalize(String telephone) {
        String trimmed = telephone == null ? "" : telephone.trim();
        boolean hasCountryCode = trimmed.startsWith("+");
        String digits = NON_DIGIT.matcher(trimmed).replaceAll("");

        String countryCode;
        String national;
        if (hasCountryCode) {
            countryCode = recognisedCountryCode(digits);
            national = countryCode == null ? digits : digits.substring(countryCode.length());
        } else {
            countryCode = DEFAULT_COUNTRY_CODE;
            national = digits.startsWith("0") ? digits.substring(1) : digits;
        }

        String e164 = "+" + (countryCode == null ? digits : countryCode + national);

        int digitCount = e164.length() - 1;
        if (digitCount < MIN_DIGITS || digitCount > MAX_DIGITS) {
            throw new InvalidTelephoneException(telephone);
        }
        Integer requiredNationalLength = countryCode == null ? null : NATIONAL_LENGTH_BY_COUNTRY.get(countryCode);
        if (requiredNationalLength != null && national.length() != requiredNationalLength) {
            throw new InvalidTelephoneException(telephone);
        }
        return e164;
    }

    /**
     * Returns the recognised country code that the given digit string begins with, or {@code null}
     * when no known country code matches. Longer codes are preferred so a specific match wins over
     * a shorter prefix.
     */
    private static String recognisedCountryCode(String digits) {
        String match = null;
        for (String code : NATIONAL_LENGTH_BY_COUNTRY.keySet()) {
            if (digits.startsWith(code) && (match == null || code.length() > match.length())) {
                match = code;
            }
        }
        return match;
    }
}
