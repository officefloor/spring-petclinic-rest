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

import java.util.Comparator;
import java.util.Map;

/**
 * The recognised country calling codes: the single source of truth for which leading digits
 * of an E.164 number form its country code and how many national digits that country requires.
 * Shared by telephone normalization (validating the national-number length) and human-readable
 * formatting (splitting the country code from the national digits).
 */
public abstract class CountryCallingCodes {

    /**
     * Required national-number length for each recognised country code. A country code absent
     * from this map carries no national-length constraint beyond the generic E.164 bounds.
     */
    public static final Map<String, Integer> NATIONAL_NUMBER_LENGTHS = Map.of("61", 9, "1", 10);

    /**
     * Return the recognised country code that prefixes the given digits (the digits of an E.164
     * number without its leading '+'), longest match first so a '+61' number is never mistaken
     * for a shorter code, or {@code null} when no recognised code matches.
     */
    public static String prefixOf(String digits) {
        return NATIONAL_NUMBER_LENGTHS.keySet().stream()
            .filter(digits::startsWith)
            .max(Comparator.comparingInt(String::length))
            .orElse(null);
    }

}
