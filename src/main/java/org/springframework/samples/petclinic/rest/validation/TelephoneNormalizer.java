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

import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

/**
 * Normalizes a submitted telephone to its canonical stored form in E.164: spaces, dashes
 * and brackets are stripped; a leading '+' and country code are kept when present,
 * otherwise the default country code '+61' is assumed and a single leading '0' is dropped
 * from the national digits. The result must carry 8 to 15 digits after the '+'.
 */
@Component
public class TelephoneNormalizer {

    /** Formatting characters removed before interpreting the number. */
    private static final Pattern SEPARATORS = Pattern.compile("[\\s()\\[\\]-]");

    /** Country code assumed when the submitted number carries no leading '+'. */
    private static final String DEFAULT_COUNTRY_CODE = "61";

    /** The digits following the '+' must number between 8 and 15 inclusive. */
    private static final Pattern E164_DIGITS = Pattern.compile("[0-9]{8,15}");

    /**
     * @param telephone the raw submitted telephone (may be {@code null})
     * @return the telephone in E.164 form, i.e. a '+' followed by 8 to 15 digits
     * @throws InvalidTelephoneException if the value cannot form a valid E.164 number
     */
    public String normalize(String telephone) {
        String cleaned = telephone == null ? "" : SEPARATORS.matcher(telephone.trim()).replaceAll("");
        String digits = cleaned.startsWith("+") ? cleaned.substring(1) : toDefaultCountry(cleaned);
        if (!E164_DIGITS.matcher(digits).matches()) {
            throw new InvalidTelephoneException(telephone);
        }
        return "+" + digits;
    }

    /**
     * Applies the default country code to national digits, dropping a single leading '0'.
     */
    private String toDefaultCountry(String national) {
        String trunkless = national.startsWith("0") ? national.substring(1) : national;
        return DEFAULT_COUNTRY_CODE + trunkless;
    }
}
