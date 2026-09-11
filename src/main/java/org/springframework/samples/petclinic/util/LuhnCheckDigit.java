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
 * Computes the Luhn check digit over the decimal digits contained in a string. Non-digit
 * characters are ignored, so the check digit is taken over the digits of an owner's customer code
 * (e.g. {@code "SMI-0007"} contributes the digits {@code 0007}).
 */
public final class LuhnCheckDigit {

    private LuhnCheckDigit() {
    }

    /**
     * Returns the Luhn check digit (0-9) over the decimal digits contained in {@code s}. Starting
     * from the right-most digit, every second digit is doubled (subtracting 9 when the result
     * exceeds 9) before being summed; the check digit is the amount needed to bring the running
     * total to the next multiple of ten.
     *
     * @param s the source string whose digits are used (must not be {@code null})
     * @return the Luhn check digit, a single value from 0 to 9
     */
    public static int compute(String s) {
        int sum = 0;
        boolean dbl = true;
        for (int i = s.length() - 1; i >= 0; i--) {
            char c = s.charAt(i);
            if (c < '0' || c > '9') {
                continue;
            }
            int d = c - '0';
            if (dbl) {
                d *= 2;
                if (d > 9) {
                    d -= 9;
                }
            }
            sum += d;
            dbl = !dbl;
        }
        return (10 - (sum % 10)) % 10;
    }
}
