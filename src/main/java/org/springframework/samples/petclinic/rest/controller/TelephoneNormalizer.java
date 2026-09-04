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

package org.springframework.samples.petclinic.rest.controller;

import java.util.Map;

import org.springframework.stereotype.Component;

/**
 * Normalises a raw telephone number to the single canonical form under which it is both
 * stored and compared for duplicates.
 *
 * <p>Keeping this in one place means the stored form and the form used to detect duplicate
 * telephones can never drift apart: an owner is a duplicate of another exactly when their
 * telephones normalise to the same value.
 *
 * <p>The canonical form is E.164: a leading '+' followed by 8 to 15 digits. A number that
 * already carries a leading '+' keeps its explicit country code; otherwise country code
 * '+61' is assumed and a single leading '0' is dropped from the national digits. Spaces,
 * dashes and brackets are stripped. Anything that cannot reduce to a '+' plus 8 to 15 digits
 * has no canonical form.
 */
@Component
public class TelephoneNormalizer {

    private static final String DEFAULT_COUNTRY_CODE = "61";

    private static final int MIN_DIGITS = 8;

    private static final int MAX_DIGITS = 15;

    /**
     * Country code (the digits after the '+') to the exact number of national digits an
     * E.164 telephone for that country must carry. Australia ('+61') requires 9 national
     * digits and the North American Numbering Plan ('+1') requires 10. A country code not
     * listed here carries no per-country length constraint beyond the generic E.164 range.
     */
    private static final Map<String, Integer> NATIONAL_NUMBER_LENGTHS = Map.of("61", 9, "1", 10);

    /**
     * Reduce a raw telephone to its canonical E.164 stored form.
     *
     * @param rawTelephone the telephone as supplied by the client, possibly {@code null} or
     *                     containing spaces, dashes or brackets
     * @return the canonical E.164 telephone (a '+' followed by 8 to 15 digits), or
     *         {@code null} if {@code rawTelephone} cannot form a valid E.164 number
     */
    public String normalize(String rawTelephone) {
        if (rawTelephone == null) {
            return null;
        }
        String trimmed = rawTelephone.trim();
        boolean hasCountryCode = trimmed.startsWith("+");
        String digits = trimmed.replaceAll("\\D", "");
        if (!hasCountryCode) {
            if (digits.startsWith("0")) {
                digits = digits.substring(1);
            }
            digits = DEFAULT_COUNTRY_CODE + digits;
        }
        if (digits.length() < MIN_DIGITS || digits.length() > MAX_DIGITS) {
            return null;
        }
        return "+" + digits;
    }

    /**
     * Whether a canonical E.164 telephone carries the national-number length its country
     * code requires. Country code '+61' requires 9 national digits and '+1' requires 10;
     * the national number is the digits that follow the country code. A number whose
     * country code has no pinned length is not constrained beyond the generic E.164 range.
     *
     * @param e164Telephone a canonical E.164 telephone (a '+' followed by digits), as
     *                      produced by {@link #normalize(String)}
     * @return {@code true} if the national-number length matches the country code (or the
     *         country code has no pinned length), {@code false} otherwise
     */
    public boolean hasValidNationalNumberLength(String e164Telephone) {
        if (e164Telephone == null || !e164Telephone.startsWith("+")) {
            return false;
        }
        String countryCode = countryCodeOf(e164Telephone);
        if (countryCode == null) {
            return true;
        }
        return nationalNumberOf(e164Telephone, countryCode).length() == NATIONAL_NUMBER_LENGTHS.get(countryCode);
    }

    /**
     * Format a canonical E.164 telephone for humans: a '+' and the country code, a space, then the
     * national digits grouped in threes from the left (e.g. '+61412345678' becomes
     * '+61 412 345 678'). A number whose country code is not listed carries no known split into
     * country code and national number and is returned unchanged.
     *
     * @param e164Telephone a canonical E.164 telephone (a '+' followed by digits), as produced by
     *                      {@link #normalize(String)}
     * @return the human-readable telephone, or {@code null} when {@code e164Telephone} is
     *         {@code null} or not a '+'-prefixed number
     */
    public String toDisplayForm(String e164Telephone) {
        if (e164Telephone == null || !e164Telephone.startsWith("+")) {
            return null;
        }
        String countryCode = countryCodeOf(e164Telephone);
        if (countryCode == null) {
            return e164Telephone;
        }
        return "+" + countryCode + " " + groupInThrees(nationalNumberOf(e164Telephone, countryCode));
    }

    /**
     * Group a run of digits into space-separated groups of three, from the left, with any final
     * short group left as-is (e.g. '412345678' becomes '412 345 678').
     */
    private static String groupInThrees(String digits) {
        StringBuilder grouped = new StringBuilder(digits.length() + digits.length() / 3);
        for (int i = 0; i < digits.length(); i++) {
            if (i > 0 && i % 3 == 0) {
                grouped.append(' ');
            }
            grouped.append(digits.charAt(i));
        }
        return grouped.toString();
    }

    /**
     * The listed country code (a key of {@link #NATIONAL_NUMBER_LENGTHS}) that a canonical E.164
     * telephone carries, i.e. the one its digits begin with, or {@code null} when the number's
     * digits begin with no listed country code.
     *
     * @param e164Telephone a canonical E.164 telephone (a '+' followed by digits), as produced by
     *                      {@link #normalize(String)}
     * @return the listed country code the number begins with, or {@code null} when none is listed
     */
    private static String countryCodeOf(String e164Telephone) {
        String digits = e164Telephone.substring(1);
        for (String countryCode : NATIONAL_NUMBER_LENGTHS.keySet()) {
            if (digits.startsWith(countryCode)) {
                return countryCode;
            }
        }
        return null;
    }

    /**
     * The national number of a canonical E.164 telephone: the digits that follow the '+' and the
     * given country code.
     *
     * @param e164Telephone a canonical E.164 telephone (a '+' followed by digits)
     * @param countryCode   the country code the number carries, as returned by
     *                      {@link #countryCodeOf(String)}
     * @return the national-number digits (the digits after the country code)
     */
    private static String nationalNumberOf(String e164Telephone, String countryCode) {
        return e164Telephone.substring(1 + countryCode.length());
    }
}
