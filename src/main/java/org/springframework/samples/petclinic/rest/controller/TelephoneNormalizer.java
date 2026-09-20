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
 * Normalizes a raw telephone input into its canonical, storable form.
 * <p>
 * Every non-digit character is stripped and the result must then be exactly
 * {@link #REQUIRED_DIGITS} digits; anything else is rejected with an
 * {@link InvalidTelephoneException} (surfaced as {@code 400 Bad Request}).
 */
@Component
public class TelephoneNormalizer {

    private static final int REQUIRED_DIGITS = 10;

    /**
     * Strip every non-digit character from {@code rawTelephone} and require the
     * remaining value to be exactly ten digits.
     *
     * @param rawTelephone the telephone as supplied by the client (may contain formatting)
     * @return the ten-digit telephone to store and return
     * @throws InvalidTelephoneException if fewer or more than ten digits remain after stripping
     */
    public String normalize(String rawTelephone) {
        String digits = rawTelephone == null ? "" : rawTelephone.replaceAll("\\D", "");
        if (digits.length() != REQUIRED_DIGITS) {
            throw new InvalidTelephoneException(rawTelephone);
        }
        return digits;
    }
}
