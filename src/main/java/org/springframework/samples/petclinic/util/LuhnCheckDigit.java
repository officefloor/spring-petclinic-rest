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
 * Computation of the Luhn check digit over the digits contained in a string. Kept separate from the
 * owner controller and the {@code Owner} model so the rule lives in one place, as a pure function
 * with no web or persistence dependencies. Mirrors {@link LocalityResolver}, which does the same for
 * localities.
 *
 * <p>The standard Luhn algorithm is applied to the decimal digits found in the input (any non-digit
 * character is ignored): reading right to left, every second digit is doubled (subtracting 9 when the
 * doubled value exceeds 9) and all resulting values are summed. The check digit is the amount that
 * must be added to that sum to reach the next multiple of ten, i.e. {@code (10 - (sum % 10)) % 10},
 * a single digit in the range 0-9.
 */
public abstract class LuhnCheckDigit {

    /**
     * Compute the Luhn check digit over the decimal digits contained in {@code value}.
     *
     * @param value the string whose digits are checked (a {@code null} value is treated as containing
     *              no digits, yielding {@code 0})
     * @return the Luhn check digit, a single digit in the range 0-9
     */
    public static int of(String value) {
        if (value == null) {
            return 0;
        }
        int sum = 0;
        boolean doubleDigit = true;
        for (int i = value.length() - 1; i >= 0; i--) {
            char c = value.charAt(i);
            if (c < '0' || c > '9') {
                continue;
            }
            int digit = c - '0';
            if (doubleDigit) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }
            sum += digit;
            doubleDigit = !doubleDigit;
        }
        return (10 - (sum % 10)) % 10;
    }

}
