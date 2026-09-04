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

import java.util.Locale;

/**
 * Comparison of owner households, an owner's {@code lastName} together with their {@code address}.
 * Kept separate from the owner controller and the {@code Owner} model so the rule for deciding when
 * two owners belong to the same household lives in one place, as pure functions with no web or
 * persistence dependencies. Mirrors {@link TelephoneNormalizer}, which does the same for telephones.
 *
 * <p>Two owners share a household when their {@linkplain #toComparisonKey(String, String) comparison
 * keys} are equal. Each field is reduced for comparison by trimming, collapsing every run of
 * whitespace to a single space and lower-casing, so households recorded in different letter cases or
 * with incidental spacing still compare equal (e.g. {@code "  Franklin "} / {@code "110  W.  Liberty St."}
 * matches {@code "franklin"} / {@code "110 W. Liberty St."}).
 */
public abstract class HouseholdNormalizer {

    /** A run of one or more whitespace characters, collapsed to a single space for comparison. */
    private static final String WHITESPACE_RUN = "\\s+";

    /** Separates the normalized fields in a key; whitespace-collapsing leaves no {@code '\n'} in a field. */
    private static final String KEY_SEPARATOR = "\n";

    /**
     * The key used to decide whether two owners belong to the same household: their normalized
     * {@code lastName} and {@code address} joined together, so households stored in different letter
     * cases or with incidental whitespace still compare equal.
     *
     * @param lastName the owner's last name (non-null)
     * @param address  the owner's postal address (non-null)
     * @return the comparison key
     */
    public static String toComparisonKey(String lastName, String address) {
        return normalizeField(lastName) + KEY_SEPARATOR + normalizeField(address);
    }

    /**
     * Reduce a single household field to its comparison form: trimmed, every run of whitespace
     * collapsed to a single space, and lower-cased.
     *
     * @param field a household field (non-null)
     * @return the field's comparison form
     */
    private static String normalizeField(String field) {
        return field.trim().replaceAll(WHITESPACE_RUN, " ").toLowerCase(Locale.ROOT);
    }

}
