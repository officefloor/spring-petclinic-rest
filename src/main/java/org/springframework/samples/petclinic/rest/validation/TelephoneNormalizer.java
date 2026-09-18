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

/**
 * Normalizes an owner's telephone number to canonical E.164 form. A leading {@code '+'} and its
 * country code are preserved when present; otherwise the default country code {@code +61} is
 * assumed and a single leading {@code '0'} is dropped from the national digits. Separators such as
 * spaces, dashes and brackets are stripped. The result must carry 8 to 15 digits after the
 * {@code '+'}, and a number that cannot form a valid E.164 value is rejected.
 */
public final class TelephoneNormalizer {

    private static final Pattern NON_DIGIT = Pattern.compile("\\D");

    private static final String DEFAULT_COUNTRY_CODE = "+61";

    private static final int MIN_DIGITS = 8;

    private static final int MAX_DIGITS = 15;

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

        String national = digits;
        if (!hasCountryCode && national.startsWith("0")) {
            national = national.substring(1);
        }
        String prefix = hasCountryCode ? "+" : DEFAULT_COUNTRY_CODE;
        String e164 = prefix + national;

        int digitCount = e164.length() - 1;
        if (digitCount < MIN_DIGITS || digitCount > MAX_DIGITS) {
            throw new InvalidTelephoneException(telephone);
        }
        return e164;
    }
}
