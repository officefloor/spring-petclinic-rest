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
 * Computes the standard Luhn check digit (0-9) over the decimal digits contained in a string.
 * <p>
 * Non-digit characters are ignored, so a value such as {@code "SMI-0007"} is checked over its
 * digits {@code "0007"}. Digits are weighted from the rightmost, doubling every second one and
 * subtracting 9 from any doubled value above 9; the check digit is what makes the weighted sum
 * a multiple of 10.
 */
public final class LuhnCheckDigit {

    private LuhnCheckDigit() {
    }

    /** @return the Luhn check digit (0-9) over the digits contained in {@code value}. */
    public static int of(String value) {
        int sum = 0;
        boolean doubled = true;
        for (int i = value.length() - 1; i >= 0; i--) {
            char c = value.charAt(i);
            if (c < '0' || c > '9') {
                continue;
            }
            int d = c - '0';
            if (doubled) {
                d *= 2;
                if (d > 9) {
                    d -= 9;
                }
            }
            sum += d;
            doubled = !doubled;
        }
        return (10 - (sum % 10)) % 10;
    }
}
