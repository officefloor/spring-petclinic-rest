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
 * Computes an owner's {@code checkDigit}: the Luhn check digit (0-9) over the
 * digits contained in the {@code customerCode}, ignoring any non-digit characters.
 */
public final class CheckDigit {

    private CheckDigit() {
    }

    /**
     * @param customerCode the owner's customer code (may be {@code null})
     * @return the Luhn check digit, or {@code null} if {@code customerCode} is null
     */
    public static Integer of(String customerCode) {
        if (customerCode == null) {
            return null;
        }
        int sum = 0;
        boolean dbl = true;
        for (int i = customerCode.length() - 1; i >= 0; i--) {
            char c = customerCode.charAt(i);
            if (c < '0' || c > '9') {
                continue;
            }
            int d = c - '0';
            if (dbl && (d *= 2) > 9) {
                d -= 9;
            }
            sum += d;
            dbl = !dbl;
        }
        return (10 - (sum % 10)) % 10;
    }
}
