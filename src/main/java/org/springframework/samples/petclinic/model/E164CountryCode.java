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

package org.springframework.samples.petclinic.model;

import java.util.Optional;

/**
 * The E.164 country calling codes this application recognizes, each paired with the exact
 * number of national (subscriber) digits a valid number for that country must carry.
 *
 * <p>For example {@code +61} (Australia) requires 9 national digits while {@code +1} (the
 * North American Numbering Plan) requires 10.
 */
public enum E164CountryCode {

    AUSTRALIA("61", 9),
    NORTH_AMERICA("1", 10);

    private final String callingCode;

    private final int nationalNumberLength;

    E164CountryCode(String callingCode, int nationalNumberLength) {
        this.callingCode = callingCode;
        this.nationalNumberLength = nationalNumberLength;
    }

    public String callingCode() {
        return this.callingCode;
    }

    /** Whether {@code nationalDigits} carries exactly the number of digits this country requires. */
    public boolean acceptsNationalNumber(String nationalDigits) {
        return nationalDigits.length() == this.nationalNumberLength;
    }

    /**
     * Finds the recognized country whose calling code prefixes {@code e164Digits} (the digits of
     * an E.164 number, without the leading {@code +}). When several codes match, the longest wins,
     * so the most specific country is chosen.
     *
     * @param e164Digits the E.164 digits without the leading {@code +}
     * @return the matching country, or empty if none is recognized
     */
    public static Optional<E164CountryCode> forE164Digits(String e164Digits) {
        E164CountryCode match = null;
        for (E164CountryCode candidate : values()) {
            if (e164Digits.startsWith(candidate.callingCode)
                    && (match == null || candidate.callingCode.length() > match.callingCode.length())) {
                match = candidate;
            }
        }
        return Optional.ofNullable(match);
    }
}
