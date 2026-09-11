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

import java.util.Map;

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
 * <p>
 * {@link #normalizeAndValidate(String)} additionally enforces the national-number length
 * expected for the country code ({@code '+61'} requires 9 national digits, {@code '+1'}
 * requires 10) and is used to validate a client-supplied value on create; plain
 * {@link #normalize(String)} keeps tolerating legacy stored numbers so it can still be used
 * to canonicalise existing values for duplicate comparison.
 */
@Component
public class TelephoneNormalizer {

    private static final String DEFAULT_COUNTRY_CODE = "61";

    /**
     * Required national-number length (the digits after the {@code '+'} and country code) for each
     * country code we know how to size. E.164 country codes form a prefix-free set, so at most one
     * of these is a prefix of any given number. Country codes not listed here are only subject to
     * the generic 8-to-15 total-length rule.
     */
    private static final Map<String, Integer> NATIONAL_DIGITS_BY_COUNTRY_CODE = Map.of(
        "1", 10,
        DEFAULT_COUNTRY_CODE, 9);

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

    /**
     * Normalizes the supplied telephone to canonical E.164 form (see {@link #normalize(String)})
     * and additionally validates that its national-number length matches its country code:
     * {@code '+61'} requires 9 national digits and {@code '+1'} requires 10. This is the method to
     * use for a value supplied by a client on create/update; comparison of values already stored
     * should use {@link #normalize(String)}, which tolerates legacy numbers whose national length
     * predates this rule.
     *
     * @param telephone the raw telephone value (may be {@code null})
     * @return the normalized E.164 telephone, or {@code null} if the input was {@code null}
     * @throws InvalidTelephoneException if the value cannot form a valid E.164 number, or its
     *                                   national-number length is wrong for its country code
     */
    public String normalizeAndValidate(String telephone) {
        String e164 = normalize(telephone);
        if (e164 != null) {
            validateNationalNumberLength(telephone, e164.substring(1));
        }
        return e164;
    }

    /**
     * Rejects a normalized number whose national-number length does not match the requirement for
     * its country code. Numbers whose country code is not in {@link #NATIONAL_DIGITS_BY_COUNTRY_CODE}
     * are left to the generic total-length rule enforced by {@link #normalize(String)}.
     *
     * @param telephone  the original raw value, for the error message
     * @param fullDigits the normalized digits (country code followed by the national number, no {@code '+'})
     */
    private void validateNationalNumberLength(String telephone, String fullDigits) {
        for (Map.Entry<String, Integer> entry : NATIONAL_DIGITS_BY_COUNTRY_CODE.entrySet()) {
            String countryCode = entry.getKey();
            if (fullDigits.startsWith(countryCode)) {
                int nationalDigits = fullDigits.length() - countryCode.length();
                if (nationalDigits != entry.getValue()) {
                    throw new InvalidTelephoneException(
                        "Telephone '" + telephone + "' has " + nationalDigits
                            + " national digits, but country code '+" + countryCode + "' requires "
                            + entry.getValue());
                }
                return;
            }
        }
    }
}
