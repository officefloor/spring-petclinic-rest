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

package org.springframework.samples.petclinic.rest.validation;

/**
 * Formats a canonical E.164 telephone (as produced by {@link TelephoneNormalizer}) for human
 * display: the country code, a space, then the national digits grouped in threes
 * (e.g. {@code '+61412345678'} becomes {@code '+61 412 345 678'}).
 */
public final class TelephoneFormatter {

    private static final int GROUP_SIZE = 3;

    private TelephoneFormatter() {
    }

    /**
     * Formats the given E.164 telephone for display.
     *
     * @param telephone the canonical E.164 telephone: {@code '+'} followed by its digits
     * @return the display form, or the input unchanged when it is not a recognised E.164 number
     */
    public static String format(String telephone) {
        if (telephone == null || !telephone.startsWith("+")) {
            return telephone;
        }
        String digits = telephone.substring(1);
        return E164Country.forDigits(digits)
            .map(country -> "+" + country.callingCode() + " "
                + groupInThrees(digits.substring(country.callingCode().length())))
            .orElse(telephone);
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
