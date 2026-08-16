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

package org.springframework.samples.petclinic.util;

/**
 * Helpers for presenting an owner's stored E.164 telephone in a human-readable form. The stored
 * {@code telephone} stays raw E.164 (a {@code '+'} followed by the country code and national digits,
 * e.g. {@code "+61412345678"}); the {@code telephoneDisplay} is the same number formatted for humans as
 * the country code, a space, then the national digits grouped in threes (e.g. {@code "+61 412 345 678"}).
 */
public final class TelephoneFormats {

    private TelephoneFormats() {
    }

    /**
     * Formats a stored E.164 telephone for humans: the {@code '+'} and country code, a space, then the
     * national digits grouped left-to-right in blocks of three separated by spaces
     * (e.g. {@code "+61412345678" -> "+61 412 345 678"}). A {@code null} value, or a value that is not
     * a {@code '+'} followed by digits, is returned unchanged.
     *
     * @param e164 the stored E.164 telephone (a {@code '+'} followed by 8 to 15 digits)
     * @return the human-readable display form, or the input unchanged when it is not E.164
     */
    public static String telephoneDisplay(String e164) {
        if (e164 == null || !e164.matches("\\+[0-9]+")) {
            return e164;
        }
        String digits = e164.substring(1);
        int countryCodeLength = countryCodeLength(digits);
        String countryCode = digits.substring(0, countryCodeLength);
        String national = digits.substring(countryCodeLength);
        StringBuilder display = new StringBuilder("+").append(countryCode);
        for (int i = 0; i < national.length(); i += 3) {
            display.append(' ').append(national, i, Math.min(i + 3, national.length()));
        }
        return display.toString();
    }

    /**
     * Determines how many leading digits form the country code of an E.164 number. The country codes
     * with a fixed national-number length used by this application are recognized explicitly
     * ({@code +1} the NANP code, {@code +61} Australia); any other number falls back to a two-digit
     * country code (one digit for very short numbers).
     *
     * @param digits the E.164 digits (the country code followed by the national number, without {@code '+'})
     * @return the length of the leading country code
     */
    private static int countryCodeLength(String digits) {
        if (digits.startsWith("61")) {
            return 2;
        }
        if (digits.startsWith("1")) {
            return 1;
        }
        return digits.length() > 2 ? 2 : 1;
    }
}
