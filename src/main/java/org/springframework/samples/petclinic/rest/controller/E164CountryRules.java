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

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

/**
 * Per-country length rules for the national portion of an E.164 number.
 * <p>
 * Each known country calling code fixes how many digits the national number must carry
 * (e.g. {@code +61} requires 9, {@code +1} requires 10). Given the digit string that
 * follows the {@code '+'}, this splits off the longest matching calling code and checks
 * the remaining national digits against that country's required length. A mismatch is
 * rejected with an {@link InvalidTelephoneException} (surfaced as {@code 400 Bad Request}).
 * Digit strings whose calling code is not recognised carry no per-country rule and pass.
 */
@Component
public class E164CountryRules {

    /** Country calling code -> exact number of national digits it requires. */
    private static final Map<String, Integer> NATIONAL_LENGTHS = Map.of(
        "61", 9,
        "1", 10);

    /** Calling codes tried longest-first so a longer code is preferred over a shorter prefix. */
    private static final List<String> CALLING_CODES = NATIONAL_LENGTHS.keySet().stream()
        .sorted(Comparator.comparingInt(String::length).reversed())
        .toList();

    /**
     * Validate the national-number length of an E.164 digit string against its country.
     *
     * @param e164Digits the digits following the {@code '+'} (calling code plus national number)
     * @param rejectedValue the original client input, echoed in the rejection message
     * @throws InvalidTelephoneException if the national number is the wrong length for its country
     */
    public void validateNationalLength(String e164Digits, String rejectedValue) {
        for (String code : CALLING_CODES) {
            if (e164Digits.startsWith(code)) {
                int required = NATIONAL_LENGTHS.get(code);
                int actual = e164Digits.length() - code.length();
                if (actual != required) {
                    throw InvalidTelephoneException.wrongNationalLength(rejectedValue, code, required, actual);
                }
                return;
            }
        }
    }
}
