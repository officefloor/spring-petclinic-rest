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

package org.springframework.samples.petclinic.rest.controller;

import org.springframework.stereotype.Component;

/**
 * Normalizes an owner's telephone number on create.
 *
 * <p>Every non-digit character (spaces, parentheses, hyphens, a leading {@code +},
 * and so on) is stripped, after which the value must consist of exactly ten digits.
 * The resulting ten-digit string is what gets stored and returned.
 */
@Component
public class TelephoneNormalizer {

    private static final int REQUIRED_DIGITS = 10;

    /**
     * Strips every non-digit character from {@code raw} and requires exactly ten digits.
     *
     * @param raw the submitted telephone value
     * @return the normalized ten-digit telephone number
     * @throws InvalidTelephoneException if the stripped value is not exactly ten digits
     */
    public String normalize(String raw) {
        String digits = raw == null ? "" : raw.replaceAll("\\D", "");
        if (digits.length() != REQUIRED_DIGITS) {
            throw new InvalidTelephoneException(raw);
        }
        return digits;
    }
}
