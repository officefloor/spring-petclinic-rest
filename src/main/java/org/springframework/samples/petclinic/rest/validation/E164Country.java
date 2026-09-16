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

package org.springframework.samples.petclinic.rest.validation;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;

/**
 * A country recognised by {@link TelephoneNormalizer}, identified by its E.164 calling code and the
 * exact number of national digits a valid subscriber number carries in that country.
 *
 * <p>Australia ({@code +61}) requires 9 national digits; the NANP ({@code +1}, e.g. the United States
 * and Canada) requires 10.
 */
public enum E164Country {

    /** Australia: {@code +61} followed by 9 national digits. */
    AUSTRALIA("61", 9),

    /** North American Numbering Plan: {@code +1} followed by 10 national digits. */
    NANP("1", 10);

    private final String callingCode;

    private final int nationalNumberLength;

    E164Country(String callingCode, int nationalNumberLength) {
        this.callingCode = callingCode;
        this.nationalNumberLength = nationalNumberLength;
    }

    /** The E.164 calling code without the leading {@code '+'} (e.g. {@code "61"}). */
    public String callingCode() {
        return this.callingCode;
    }

    /** The exact number of national digits a valid number in this country carries. */
    public int nationalNumberLength() {
        return this.nationalNumberLength;
    }

    /**
     * Finds the country whose calling code is a leading prefix of the given digit string. When two
     * calling codes would both match, the longest (most specific) one wins.
     *
     * @param digits the digits of an E.164 number, without the leading {@code '+'}
     * @return the matching country, or empty if none is recognised
     */
    public static Optional<E164Country> forDigits(String digits) {
        return Arrays.stream(values())
            .filter(country -> digits.startsWith(country.callingCode))
            .max(Comparator.comparingInt(country -> country.callingCode.length()));
    }
}
