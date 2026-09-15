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
 * Formats a stored E.164 telephone for humans: the country code, a space, then the national
 * digits grouped in threes, e.g. {@code "+61412345678"} becomes {@code "+61 412 345 678"}. The
 * country code is recognised via {@link CountryCallingCodes}; when it is not recognised the whole
 * number after the '+' is treated as the national part.
 */
public abstract class TelephoneDisplayFormatter {

    /**
     * Return the human-readable form of the given E.164 telephone, or {@code null} when it is
     * absent.
     */
    public static String display(String telephone) {
        if (telephone == null || telephone.isBlank()) {
            return null;
        }
        String digits = telephone.startsWith("+") ? telephone.substring(1) : telephone;
        String countryCode = CountryCallingCodes.prefixOf(digits);
        if (countryCode == null) {
            return "+" + groupInThrees(digits);
        }
        return "+" + countryCode + " " + groupInThrees(digits.substring(countryCode.length()));
    }

    /**
     * Groups the given digits left to right in blocks of three separated by single spaces.
     */
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
