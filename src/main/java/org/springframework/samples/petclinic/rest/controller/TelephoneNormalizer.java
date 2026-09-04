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
}
