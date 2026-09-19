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

package org.springframework.samples.petclinic.util;

import java.util.Optional;

/**
 * Formats a stored E.164 telephone number for human reading: the calling code, a space, then
 * the national digits grouped in threes (e.g. {@code "+61412345678"} becomes
 * {@code "+61 412 345 678"}).
 *
 * <p>The calling code is identified via {@link CountryCallingCode}. A number whose calling code
 * is not recognised has no known code/national boundary and is returned unchanged.
 */
public final class TelephoneDisplayFormatter {

    /** Size of each national-digit group in the formatted display. */
    private static final int GROUP_SIZE = 3;

    private TelephoneDisplayFormatter() {
    }

    /**
     * Format the given stored E.164 telephone for display.
     *
     * @param e164 the stored E.164 telephone (a {@code '+'} followed by digits), may be
     * {@code null}
     * @return the display form ({@code "+<code> <grouped national digits>"}), or {@code null}
     * when {@code e164} is {@code null} or blank
     */
    public static String format(String e164) {
        if (e164 == null || e164.isBlank()) {
            return null;
        }
        String digits = e164.startsWith("+") ? e164.substring(1) : e164;
        Optional<CountryCallingCode> country = CountryCallingCode.forDigits(digits);
        if (country.isEmpty()) {
            return e164;
        }
        String code = country.get().code();
        String national = digits.substring(code.length());
        return "+" + code + " " + groupInThrees(national);
    }

    private static String groupInThrees(String national) {
        StringBuilder grouped = new StringBuilder();
        for (int i = 0; i < national.length(); i++) {
            if (i > 0 && i % GROUP_SIZE == 0) {
                grouped.append(' ');
            }
            grouped.append(national.charAt(i));
        }
        return grouped.toString();
    }
}
