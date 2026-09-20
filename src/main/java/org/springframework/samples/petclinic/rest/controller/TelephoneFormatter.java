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

package org.springframework.samples.petclinic.rest.controller;

import org.springframework.stereotype.Component;

/**
 * Formats a stored E.164 telephone number for human display.
 * <p>
 * The country calling code (recognised via {@link E164CountryRules}) is kept with its
 * leading {@code '+'} and separated from the national number by a single space; the
 * national digits are then grouped in threes from the left (e.g. {@code "+61412345678"}
 * becomes {@code "+61 412 345 678"}). When no calling code is recognised the whole digit
 * run is grouped in threes behind the {@code '+'} with no country-code split.
 */
@Component
public class TelephoneFormatter {

    private static final int GROUP_SIZE = 3;

    private final E164CountryRules countryRules;

    public TelephoneFormatter(E164CountryRules countryRules) {
        this.countryRules = countryRules;
    }

    /**
     * Render an E.164 telephone for display.
     *
     * @param e164Telephone the stored telephone (a leading {@code '+'} followed by digits)
     * @return the human-formatted telephone, or {@code null} when {@code e164Telephone} is absent
     */
    public String format(String e164Telephone) {
        if (e164Telephone == null || e164Telephone.isBlank()) {
            return null;
        }
        String digits = e164Telephone.startsWith("+") ? e164Telephone.substring(1) : e164Telephone;
        String callingCode = countryRules.callingCodeOf(digits);
        if (callingCode == null) {
            return "+" + groupInThrees(digits);
        }
        String national = digits.substring(callingCode.length());
        return "+" + callingCode + " " + groupInThrees(national);
    }

    /**
     * Group a run of digits into space-separated triples from the left, e.g.
     * {@code "412345678"} becomes {@code "412 345 678"}.
     */
    private String groupInThrees(String digits) {
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
