/*
 * Copyright 2002-2017 the original author or authors.
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
 * The Luhn check-digit algorithm.
 *
 * <p>Computes the single check digit (0-9) over the decimal digits contained in a
 * string, ignoring any non-digit characters. This is the single source of truth for
 * that computation, so callers never repeat it.
 */
public final class LuhnCheckDigit {

    private LuhnCheckDigit() {
    }

    /**
     * Compute the Luhn check digit over the digits contained in {@code value}.
     *
     * @param value the string whose digits are checked; non-digit characters are ignored
     * @return the check digit (0-9) that, appended to the digits, makes them Luhn-valid
     */
    public static int of(String value) {
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
