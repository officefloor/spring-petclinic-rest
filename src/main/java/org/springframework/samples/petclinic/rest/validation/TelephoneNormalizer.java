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

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.samples.petclinic.rest.error.InvalidTelephoneException;
import org.springframework.stereotype.Component;

/**
 * Normalizes a submitted telephone number into E.164 form. A leading '+' and country code are kept
 * when present; otherwise the default country code '+61' is assumed and a single leading '0' is
 * dropped from the national digits. Spaces, dashes and brackets are stripped, and the result must
 * hold between 8 and 15 digits after the '+'. For known country codes the national number must also
 * be exactly the length that country requires ('+61' => 9 national digits, '+1' => 10).
 */
@Component
public class TelephoneNormalizer {

    private static final String DEFAULT_COUNTRY_CODE = "61";

    /** Characters used only for human-readable grouping and removed before parsing. */
    private static final String FORMATTING_CHARS = "[\\s\\-()\\[\\]]";

    private static final int MIN_DIGITS = 8;
    private static final int MAX_DIGITS = 15;

    /**
     * Required national-number length for each known country code, ordered longest prefix first so
     * the most specific country code is matched against a normalized number.
     */
    private static final Map<String, Integer> NATIONAL_LENGTH_BY_COUNTRY_CODE = new LinkedHashMap<>();

    static {
        NATIONAL_LENGTH_BY_COUNTRY_CODE.put("61", 9);
        NATIONAL_LENGTH_BY_COUNTRY_CODE.put("1", 10);
    }

    /**
     * @param telephone the raw, possibly formatted telephone number
     * @return the telephone in E.164 form (a '+' followed by 8 to 15 digits)
     * @throws InvalidTelephoneException if the value cannot form a valid E.164 number
     */
    public String normalize(String telephone) {
        if (telephone == null) {
            throw new InvalidTelephoneException(null);
        }
        String cleaned = telephone.trim().replaceAll(FORMATTING_CHARS, "");
        String digits;
        if (cleaned.startsWith("+")) {
            digits = cleaned.substring(1);
        } else {
            String national = cleaned.startsWith("0") ? cleaned.substring(1) : cleaned;
            digits = DEFAULT_COUNTRY_CODE + national;
        }
        if (!digits.matches("\\d{" + MIN_DIGITS + "," + MAX_DIGITS + "}")) {
            throw new InvalidTelephoneException(telephone);
        }
        requireValidNationalLength(digits, telephone);
        return "+" + digits;
    }

    /**
     * Rejects a normalized number whose national part is the wrong length for its country code. Only
     * known country codes are checked; a number with an unrecognised country code passes.
     */
    private void requireValidNationalLength(String digits, String telephone) {
        for (Map.Entry<String, Integer> entry : NATIONAL_LENGTH_BY_COUNTRY_CODE.entrySet()) {
            String countryCode = entry.getKey();
            if (digits.startsWith(countryCode)) {
                int nationalLength = digits.length() - countryCode.length();
                if (nationalLength != entry.getValue()) {
                    throw new InvalidTelephoneException(telephone);
                }
                return;
            }
        }
    }
}
