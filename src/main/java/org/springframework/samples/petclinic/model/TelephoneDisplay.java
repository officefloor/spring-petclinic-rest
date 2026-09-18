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

/**
 * Formats a stored E.164 telephone number for humans, inverting the canonical form produced
 * by the E.164 normaliser: the country code, a space, then the national digits grouped in
 * threes, e.g. {@code "+61412345678"} becomes {@code "+61 412 345 678"}. The country code is
 * split off using the shared {@link CountryDialingCode} table.
 */
public final class TelephoneDisplay {

    private TelephoneDisplay() {
    }

    /**
     * The given E.164 number formatted for display. Returns {@code null} for a {@code null}
     * number and echoes any value not in E.164 form ({@code '+'} then digits) unchanged.
     */
    public static String format(String e164) {
        if (e164 == null || !e164.startsWith("+")) {
            return e164;
        }
        String digits = e164.substring(1);
        String countryCode = CountryDialingCode.matching(digits).orElse("");
        String national = groupInThrees(digits.substring(countryCode.length()));
        return countryCode.isEmpty() ? "+" + national : "+" + countryCode + " " + national;
    }

    /** Groups a run of digits into space-separated groups of three, left to right. */
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
