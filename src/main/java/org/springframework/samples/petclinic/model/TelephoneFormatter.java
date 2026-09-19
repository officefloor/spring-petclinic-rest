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
 * Formats a stored E.164 telephone number for human display: the calling code (kept with its
 * leading {@code +}), a space, then the national digits grouped in threes from the left and
 * separated by spaces, e.g. {@code "+61412345678"} becomes {@code "+61 412 345 678"}.
 *
 * <p>The calling code is identified from the {@linkplain E164CountryCode recognized country codes};
 * a number whose calling code is not recognized (so the boundary between calling code and national
 * digits is unknown) is returned unchanged.
 */
public final class TelephoneFormatter {

    private static final int GROUP_SIZE = 3;

    private TelephoneFormatter() {
    }

    /**
     * Format an E.164 telephone number for display.
     *
     * @param e164 the stored E.164 number (a leading {@code +} followed by digits), or {@code null}
     * @return the display form ({@code "+<callingCode> <national digits grouped in threes>"}), or
     *         the input unchanged when it is {@code null}, not E.164, or carries an unrecognized
     *         calling code
     */
    public static String toDisplay(String e164) {
        if (e164 == null || !e164.startsWith("+")) {
            return e164;
        }
        String digits = e164.substring(1);
        return E164CountryCode.forE164Digits(digits)
            .map(country -> {
                String callingCode = country.callingCode();
                String national = digits.substring(callingCode.length());
                return "+" + callingCode + " " + groupInThrees(national);
            })
            .orElse(e164);
    }

    /** Group the digits into space-separated runs of {@value #GROUP_SIZE}, from the left. */
    private static String groupInThrees(String national) {
        StringBuilder grouped = new StringBuilder(national.length() + national.length() / GROUP_SIZE);
        for (int i = 0; i < national.length(); i++) {
            if (i > 0 && i % GROUP_SIZE == 0) {
                grouped.append(' ');
            }
            grouped.append(national.charAt(i));
        }
        return grouped.toString();
    }
}
