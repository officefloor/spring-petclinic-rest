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

package org.springframework.samples.petclinic.util;

/**
 * Normalises a telephone number to E.164 form ({@code '+'} country code and national digits).
 *
 * <p>A leading {@code '+'} is treated as an explicit country code and kept verbatim; otherwise the
 * Australian country code {@code '+61'} is assumed and a single leading {@code '0'} is dropped from
 * the national digits. Spaces, dashes and brackets are stripped. The result must carry 8 to 15
 * digits after the {@code '+'}, else the input cannot form a valid E.164 number.
 */
public abstract class E164 {

    /**
     * @param raw the raw telephone input, may be {@code null}
     * @return the E.164 string, or {@code null} when {@code raw} is {@code null}
     * @throws IllegalArgumentException when {@code raw} cannot form a valid E.164 number
     */
    public static String toE164(String raw) {
        if (raw == null) {
            return null;
        }
        boolean explicitCountryCode = raw.trim().startsWith("+");
        String digits = raw.replaceAll("\\D", "");
        String national = explicitCountryCode ? digits
            : "61" + (digits.startsWith("0") ? digits.substring(1) : digits);
        if (national.length() < 8 || national.length() > 15 || !nationalLengthValid(national)) {
            throw new IllegalArgumentException("Telephone cannot form a valid E.164 number: " + raw);
        }
        return "+" + national;
    }

    /**
     * Checks the national-number length against a country code that fixes it: {@code '+61'} requires
     * 9 national digits and {@code '+1'} requires 10. Other country codes are unconstrained here.
     *
     * @param national the country code followed by the national digits
     * @return {@code true} unless {@code national}'s country code mandates a different length
     */
    private static boolean nationalLengthValid(String national) {
        if (national.startsWith("61")) {
            return national.length() - 2 == 9;
        }
        if (national.startsWith("1")) {
            return national.length() - 1 == 10;
        }
        return true;
    }

}
