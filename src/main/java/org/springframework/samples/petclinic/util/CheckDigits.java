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
 * Helper for the Luhn check digit used within a pet owner's {@code memberId}. The check digit is
 * computed over the decimal digits contained in the {@code <REGION><FY><HASH8>} prefix of the
 * memberId; any non-digit characters (such as the region letters) are ignored.
 */
public final class CheckDigits {

    private CheckDigits() {
    }

    /**
     * Computes the Luhn check digit (0-9) over the decimal digits contained in {@code value}. Digits
     * are processed right to left with every second digit (starting from the rightmost) doubled and
     * its digits summed; non-digit characters are ignored.
     *
     * @param value the string whose digits are checked, or {@code null}
     * @return the Luhn check digit, from 0 to 9 ({@code null} and digit-free values yield 0)
     */
    public static int luhn(String value) {
        if (value == null) {
            return 0;
        }
        int sum = 0;
        boolean dbl = true;
        for (int i = value.length() - 1; i >= 0; i--) {
            char c = value.charAt(i);
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
