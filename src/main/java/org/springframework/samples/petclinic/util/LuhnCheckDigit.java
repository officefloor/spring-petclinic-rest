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

/**
 * Computes a single Luhn check digit over the digits contained in a string.
 */
public final class LuhnCheckDigit {

    private LuhnCheckDigit() {
    }

    /**
     * Compute the Luhn check digit (0-9) over the digits contained in {@code value}, ignoring any
     * non-digit characters. Doubling starts from the rightmost digit.
     *
     * @param value the value whose digits are checked, or {@code null}
     * @return the Luhn check digit, or {@code null} when {@code value} is {@code null}
     */
    public static Integer compute(String value) {
        if (value == null) {
            return null;
        }
        int sum = 0;
        boolean doubleDigit = true;
        for (int i = value.length() - 1; i >= 0; i--) {
            char c = value.charAt(i);
            if (c < '0' || c > '9') {
                continue;
            }
            int d = c - '0';
            if (doubleDigit) {
                d *= 2;
                if (d > 9) {
                    d -= 9;
                }
            }
            sum += d;
            doubleDigit = !doubleDigit;
        }
        return (10 - (sum % 10)) % 10;
    }
}
