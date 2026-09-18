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

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Fixed knowledge of the country dialling codes the application understands: each known code
 * (without its {@code '+'}) and the exact number of national digits that country uses. This
 * is the single source both the E.164 normaliser (to validate a number's national length) and
 * the display formatter (to split a stored number into country code and national part) rely on.
 */
public final class CountryDialingCode {

    /**
     * Country code (without {@code '+'}) to the exact national-number length it requires.
     * Codes not listed here are unknown to the application.
     */
    private static final Map<String, Integer> NATIONAL_LENGTHS = Map.of("61", 9, "1", 10);

    /** Known country codes, longest first, so a code is matched greedily. */
    private static final List<String> CODES = NATIONAL_LENGTHS.keySet().stream()
        .sorted(Comparator.comparingInt(String::length).reversed())
        .toList();

    private CountryDialingCode() {
    }

    /**
     * The known country code that the given digit string starts with, matched longest-first,
     * or empty when the digits begin with no known country code.
     */
    public static Optional<String> matching(String digits) {
        return CODES.stream().filter(digits::startsWith).findFirst();
    }

    /**
     * The exact national-number length a known country code requires, or empty when the code
     * is not one of the known codes.
     */
    public static Optional<Integer> nationalLength(String countryCode) {
        return Optional.ofNullable(NATIONAL_LENGTHS.get(countryCode));
    }
}
