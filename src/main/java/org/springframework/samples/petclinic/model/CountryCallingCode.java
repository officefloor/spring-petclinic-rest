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

package org.springframework.samples.petclinic.model;

import java.util.Map;
import java.util.Optional;

/**
 * Knowledge of E.164 country calling codes: which codes are recognised and how
 * many national-number digits each one requires. Country calling codes form a
 * prefix-free set, so at most one recognised code prefixes a given number. Codes
 * not listed here are unrecognised; callers apply only generic rules to them.
 */
public final class CountryCallingCode {

    private static final Map<String, Integer> NATIONAL_DIGITS_BY_CODE = Map.of("1", 10, "61", 9);

    private CountryCallingCode() {
    }

    /**
     * The recognised country calling code that prefixes the given E.164 digits
     * (the digits following the {@code '+'}), or empty when none is recognised.
     *
     * @param digits the E.164 digits (country code followed by the national number)
     * @return the matching country calling code, or {@link Optional#empty()}
     */
    public static Optional<String> of(String digits) {
        return NATIONAL_DIGITS_BY_CODE.keySet().stream()
            .filter(digits::startsWith)
            .findFirst();
    }

    /**
     * The exact national-number length required for a recognised country calling
     * code, or empty when the code is not recognised.
     *
     * @param countryCode the country calling code
     * @return the required national-number length, or {@link Optional#empty()}
     */
    public static Optional<Integer> nationalDigits(String countryCode) {
        return Optional.ofNullable(NATIONAL_DIGITS_BY_CODE.get(countryCode));
    }
}
