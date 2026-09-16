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

/**
 * Normalizes an owner's telephone to its canonical E.164 storage form. Spaces, dashes and
 * brackets (and any other formatting) are stripped. A leading {@code '+'} and its country code are
 * kept as given; otherwise the default country code {@code '+61'} is assumed and a single leading
 * {@code '0'} is dropped from the national digits. The result is {@code '+'} followed by 8 to 15
 * digits.
 */
public final class TelephoneNormalizer {

    private static final String DEFAULT_COUNTRY_CODE = "61";

    private static final int MIN_DIGITS = 8;

    private static final int MAX_DIGITS = 15;

    private TelephoneNormalizer() {
    }

    /**
     * Converts the submitted telephone to its canonical E.164 storage form.
     *
     * @param telephone the submitted telephone, possibly containing formatting characters
     * @return the normalized E.164 telephone: {@code '+'} followed by 8 to 15 digits
     * @throws InvalidTelephoneException if the value cannot form a valid E.164 number
     */
    public static String normalize(String telephone) {
        String trimmed = telephone == null ? "" : telephone.trim();
        boolean hasCountryCode = trimmed.startsWith("+");
        String digits = trimmed.replaceAll("\\D", "");
        String national;
        if (hasCountryCode) {
            national = digits;
        } else {
            if (digits.startsWith("0")) {
                digits = digits.substring(1);
            }
            national = DEFAULT_COUNTRY_CODE + digits;
        }
        if (national.length() < MIN_DIGITS || national.length() > MAX_DIGITS) {
            throw new InvalidTelephoneException(
                "Telephone must form a valid E.164 number with " + MIN_DIGITS + " to " + MAX_DIGITS
                    + " digits after the '+'");
        }
        return "+" + national;
    }
}
