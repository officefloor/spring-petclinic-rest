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
package org.springframework.samples.petclinic.service;

import java.util.Locale;

import org.springframework.stereotype.Component;

/**
 * Canonicalizes a free-text owner field (such as last name or address) for
 * household-identity comparison.
 *
 * <p>Two values are considered the same household field when they match after
 * trimming, collapsing every run of whitespace to a single space, and lower-casing.
 * This is the comparison used to detect owners living in the same household.
 */
@Component
public class HouseholdNormalizer {

    /**
     * Reduce {@code value} to its canonical form for case-insensitive,
     * whitespace-insensitive comparison.
     *
     * @param value the raw field value (may be {@code null})
     * @return the normalized value, or an empty string when {@code value} is {@code null}
     */
    public String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.strip().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
