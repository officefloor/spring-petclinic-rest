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

import org.springframework.samples.petclinic.rest.advice.InvalidTelephoneException;
import org.springframework.stereotype.Component;

/**
 * Produces the canonical stored form of an owner's telephone number in E.164.
 * <p>
 * Centralising this here keeps the REST controllers thin: they simply delegate to
 * {@link #normalize(String)} both when canonicalising an incoming value before it is
 * persisted and when comparing values to detect duplicates, so the two always agree.
 * <p>
 * The canonical form is E.164: a leading {@code '+'} and country code are kept when
 * present, otherwise country code {@code '+61'} is assumed and a single leading
 * {@code '0'} is dropped from the national digits. Spaces, dashes and brackets are
 * stripped. The result must carry 8 to 15 digits after the {@code '+'}; anything that
 * cannot form valid E.164 is rejected with {@link InvalidTelephoneException} (a 400).
 * Because a value already in E.164 form re-normalizes to itself, comparing normalized
 * values reliably detects duplicate telephones.
 */
@Component
public class TelephoneNormalizer {

    private static final String DEFAULT_COUNTRY_CODE = "61";

    /**
     * Normalizes the supplied telephone value to its canonical E.164 stored form.
     * Spaces, dashes and brackets are stripped; a leading {@code '+'} and country code
     * are preserved when present, otherwise {@code '+61'} is assumed and a single leading
     * {@code '0'} is dropped from the national digits. Values that differ only in
     * formatting therefore normalize to the same result.
     *
     * @param telephone the raw telephone value (may be {@code null})
     * @return the normalized E.164 telephone, or {@code null} if the input was {@code null}
     * @throws InvalidTelephoneException if the value cannot form a valid E.164 number
     */
    public String normalize(String telephone) {
        if (telephone == null) {
            return null;
        }
        boolean hasCountryCode = telephone.trim().startsWith("+");
        String digits = telephone.replaceAll("\\D", "");
        String nationalOrFull;
        if (hasCountryCode) {
            nationalOrFull = digits;
        } else {
            if (digits.startsWith("0")) {
                digits = digits.substring(1);
            }
            nationalOrFull = DEFAULT_COUNTRY_CODE + digits;
        }
        if (!nationalOrFull.matches("\\d{8,15}")) {
            throw new InvalidTelephoneException(
                "Telephone '" + telephone + "' cannot be normalized to a valid E.164 number");
        }
        return "+" + nationalOrFull;
    }
}
