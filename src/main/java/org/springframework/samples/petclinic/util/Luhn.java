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
 * Computes the Luhn check digit over the digits contained in a string.
 *
 * <p>Non-digit characters are ignored, so the algorithm can be applied directly to
 * formatted identifiers (such as a hyphenated customer code). Standard Luhn: working
 * from the rightmost digit, every second digit is doubled (subtracting 9 when the
 * result exceeds 9), the digits are summed, and the check digit is the amount that
 * rounds the sum up to the next multiple of ten.
 */
public final class Luhn {

    private Luhn() {
    }

    /**
     * Return the Luhn check digit (0-9) over the digits contained in {@code value}.
     *
     * @param value the source string; non-digit characters are ignored
     * @return the single check digit, from 0 to 9
     */
    public static int checkDigit(String value) {
        int sum = 0;
        boolean doubling = true;
        for (int i = value.length() - 1; i >= 0; i--) {
            char c = value.charAt(i);
            if (c < '0' || c > '9') {
                continue;
            }
            int d = c - '0';
            if (doubling) {
                d *= 2;
                if (d > 9) {
                    d -= 9;
                }
            }
            sum += d;
            doubling = !doubling;
        }
        return (10 - (sum % 10)) % 10;
    }
}
