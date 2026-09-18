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

/**
 * Formats a stored E.164 telephone number for humans. The recognised country code is separated from
 * the national number by a single space, and the national digits are grouped in threes, e.g.
 * {@code +61412345678} becomes {@code +61 412 345 678}. When the country code is not recognised the
 * digits after the {@code '+'} are grouped in threes without a separate country-code segment.
 */
public final class TelephoneFormatter {

    private static final int GROUP_SIZE = 3;

    private TelephoneFormatter() {
    }

    /**
     * Formats the given E.164 telephone for display.
     *
     * @param e164 the stored telephone in E.164 form, e.g. {@code +61412345678}
     * @return the human-readable telephone, e.g. {@code +61 412 345 678}, or {@code null} when
     *         {@code e164} is {@code null}
     */
    public static String format(String e164) {
        if (e164 == null) {
            return null;
        }
        String digits = e164.startsWith("+") ? e164.substring(1) : e164;
        String countryCode = CountryDialingCodes.matching(digits);
        if (countryCode == null) {
            return "+" + groupInThrees(digits);
        }
        String national = digits.substring(countryCode.length());
        return "+" + countryCode + " " + groupInThrees(national);
    }

    /**
     * Groups the given digits in blocks of three, separated by a single space; a trailing block may
     * be shorter.
     */
    private static String groupInThrees(String digits) {
        StringBuilder grouped = new StringBuilder(digits.length() + digits.length() / GROUP_SIZE);
        for (int i = 0; i < digits.length(); i++) {
            if (i > 0 && i % GROUP_SIZE == 0) {
                grouped.append(' ');
            }
            grouped.append(digits.charAt(i));
        }
        return grouped.toString();
    }
}
