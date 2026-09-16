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

package org.springframework.samples.petclinic.model;

/**
 * Formats a stored E.164 telephone number for human display: the country calling
 * code, a space, then the national digits grouped in threes (for example
 * {@code "+61412345678"} becomes {@code "+61 412 345 678"}). When the country
 * calling code is not recognised (see {@link CountryCallingCode}) only the
 * digits are grouped, with no country-code split.
 */
public final class TelephoneFormatter {

    private static final int GROUP_SIZE = 3;

    private TelephoneFormatter() {
    }

    /**
     * Format an E.164 telephone number for human display.
     *
     * @param e164 the stored E.164 number (e.g. {@code "+61412345678"}); may be {@code null}
     * @return the human-readable form (e.g. {@code "+61 412 345 678"}), or the
     *         input unchanged when it is {@code null} or not in E.164 form
     */
    public static String toDisplay(String e164) {
        if (e164 == null || !e164.startsWith("+")) {
            return e164;
        }
        String digits = e164.substring(1);
        String countryCode = CountryCallingCode.of(digits).orElse("");
        String national = groupInThrees(digits.substring(countryCode.length()));
        return countryCode.isEmpty() ? "+" + national : "+" + countryCode + " " + national;
    }

    private static String groupInThrees(String digits) {
        StringBuilder grouped = new StringBuilder();
        for (int i = 0; i < digits.length(); i++) {
            if (i > 0 && i % GROUP_SIZE == 0) {
                grouped.append(' ');
            }
            grouped.append(digits.charAt(i));
        }
        return grouped.toString();
    }
}
