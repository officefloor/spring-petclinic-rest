/*
 * Copyright 2002-2013 the original author or authors.
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

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * E.164 telephone knowledge shared across the domain: the known country codes with the
 * national-number length each requires, and the human-readable display form derived from a stored
 * E.164 number.
 */
public final class Telephone {

    /**
     * Required national-number length for each known country code, ordered longest prefix first so
     * the most specific country code is matched against a number.
     */
    private static final Map<String, Integer> NATIONAL_LENGTH_BY_COUNTRY_CODE = new LinkedHashMap<>();

    static {
        NATIONAL_LENGTH_BY_COUNTRY_CODE.put("61", 9);
        NATIONAL_LENGTH_BY_COUNTRY_CODE.put("1", 10);
    }

    private Telephone() {
    }

    /**
     * @param digits an E.164 digit string with no leading {@code '+'}
     * @return the known country code that prefixes {@code digits}, matched longest prefix first, or
     * {@code null} when none is recognised
     */
    public static String countryCodeOf(String digits) {
        for (String countryCode : NATIONAL_LENGTH_BY_COUNTRY_CODE.keySet()) {
            if (digits.startsWith(countryCode)) {
                return countryCode;
            }
        }
        return null;
    }

    /**
     * @param countryCode a country code
     * @return the national-number length that {@code countryCode} requires, or {@code null} when the
     * country code is not recognised
     */
    public static Integer nationalLength(String countryCode) {
        return NATIONAL_LENGTH_BY_COUNTRY_CODE.get(countryCode);
    }

    /**
     * Format a stored E.164 number for humans as the country code, a space, then the national digits
     * grouped in threes (e.g. {@code "+61412345678"} becomes {@code "+61 412 345 678"}). When the
     * country code is not recognised the whole number after the {@code '+'} is grouped. A {@code null}
     * input is returned unchanged, as is a value that is not in E.164 form.
     *
     * @param e164 the stored E.164 number (a {@code '+'} followed by digits)
     * @return the display form
     */
    public static String forDisplay(String e164) {
        if (e164 == null || !e164.startsWith("+")) {
            return e164;
        }
        String digits = e164.substring(1);
        String countryCode = countryCodeOf(digits);
        if (countryCode == null) {
            return "+" + groupInThrees(digits);
        }
        String national = digits.substring(countryCode.length());
        return "+" + countryCode + " " + groupInThrees(national);
    }

    private static String groupInThrees(String digits) {
        StringBuilder grouped = new StringBuilder();
        for (int i = 0; i < digits.length(); i++) {
            if (i > 0 && i % 3 == 0) {
                grouped.append(' ');
            }
            grouped.append(digits.charAt(i));
        }
        return grouped.toString();
    }
}
