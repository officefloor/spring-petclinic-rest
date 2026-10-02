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

import java.util.List;

/**
 * Formats stored E.164 telephone numbers for human display. Generic presentation logic with no
 * dependency on any particular domain object, kept here so any holder of a telephone value can reuse
 * it rather than duplicating the calling-code table or the grouping algorithm.
 */
public final class TelephoneFormatter {

    /**
     * Known E.164 calling codes used to split a telephone into its country code and national digits.
     * Calling codes are prefix-free, so the longest match wins; a number whose code is not listed
     * falls back to a single-digit country code.
     */
    private static final List<String> KNOWN_CALLING_CODES = List.of("61", "1");

    private TelephoneFormatter() {
    }

    /**
     * Formats a stored E.164 telephone for humans: the {@code '+'} and country code, a single space,
     * then the national digits grouped in threes separated by spaces (e.g. {@code '+61 412 345 678'}
     * for the stored {@code '+61412345678'}). The country code is the longest leading digit run
     * matching a {@link #KNOWN_CALLING_CODES known calling code}; for any other code a single leading
     * digit is treated as the country code.
     *
     * @param telephone the stored telephone value, possibly {@code null}
     * @return the formatted display string; {@code null} when {@code telephone} is {@code null}, and
     *         the value unchanged when it is not a {@code '+'}-prefixed E.164 string
     */
    public static String toDisplay(String telephone) {
        if (telephone == null) {
            return null;
        }
        if (!telephone.startsWith("+")) {
            return telephone;
        }
        String digits = telephone.substring(1);
        if (digits.isEmpty()) {
            return telephone;
        }
        String countryCode = null;
        for (String code : KNOWN_CALLING_CODES) {
            if (digits.startsWith(code) && (countryCode == null || code.length() > countryCode.length())) {
                countryCode = code;
            }
        }
        if (countryCode == null) {
            countryCode = digits.substring(0, 1);
        }
        String national = digits.substring(countryCode.length());
        StringBuilder sb = new StringBuilder("+").append(countryCode);
        for (int i = 0; i < national.length(); i++) {
            if (i % 3 == 0) {
                sb.append(' ');
            }
            sb.append(national.charAt(i));
        }
        return sb.toString();
    }
}
