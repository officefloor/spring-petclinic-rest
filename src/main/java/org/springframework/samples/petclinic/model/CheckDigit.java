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
 * Computes an owner's {@code checkDigit}: a single Luhn check digit (0-9) over the digits
 * contained in the owner's {@code customerCode}. Non-digit characters are ignored, so the
 * digit depends only on the numeric content of the code.
 */
public final class CheckDigit {

    private CheckDigit() {
    }

    /**
     * Returns the Luhn check digit over the digits of the owner's customer code, or
     * {@code null} when the owner is {@code null} or has no customer code assigned yet.
     */
    public static Integer forOwner(Owner owner) {
        if (owner == null || owner.getCustomerCode() == null) {
            return null;
        }
        return luhn(owner.getCustomerCode());
    }

    /**
     * Computes the Luhn check digit (0-9) over the decimal digits contained in {@code value},
     * ignoring any non-digit characters.
     */
    public static int luhn(String value) {
        int sum = 0;
        boolean doubling = true;
        for (int i = value.length() - 1; i >= 0; i--) {
            char c = value.charAt(i);
            if (c < '0' || c > '9') {
                continue;
            }
            int digit = c - '0';
            if (doubling) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }
            sum += digit;
            doubling = !doubling;
        }
        return (10 - (sum % 10)) % 10;
    }
}
