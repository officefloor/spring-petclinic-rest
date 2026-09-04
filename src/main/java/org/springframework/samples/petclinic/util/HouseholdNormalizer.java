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
 * Comparison of owner households, an owner's {@code lastName} together with their {@code postcode}.
 * Kept separate from the owner controller and the {@code Owner} model so the rule for deciding when
 * two owners belong to the same household lives in one place, as pure functions with no web or
 * persistence dependencies. Mirrors {@link TelephoneNormalizer}, which does the same for telephones.
 *
 * <p>Two owners share a household when their {@linkplain #toComparisonKey(String, String) comparison
 * keys} are equal. The last name is reduced for comparison by trimming, collapsing every run of
 * whitespace to a single space and lower-casing, so households recorded in different letter cases or
 * with incidental spacing still compare equal (e.g. {@code "  Franklin "} / {@code "2000"} matches
 * {@code "franklin"} / {@code "2000"}); the postcode is a fixed 4-digit code and is compared as
 * given.
 */
public abstract class HouseholdNormalizer {

    /** A run of one or more whitespace characters, collapsed to a single space for comparison. */
    private static final String WHITESPACE_RUN = "\\s+";

    /** Separates the normalized {@code lastName} and {@code postcode} in a key. */
    private static final String KEY_SEPARATOR = "|";

    /** The number of leading hex characters of the digest kept as the household id. */
    private static final int HOUSEHOLD_ID_LENGTH = 12;

    /**
     * Fixed version tag mixed into the hashed household id under the version-2 identity algorithm, so
     * every household id differs from the value the version-1 algorithm produced. Two owners still
     * share a household exactly when their {@linkplain #toComparisonKey comparison keys} are equal; the
     * tag only shifts the derived identifier, not the rule for when two owners match.
     */
    private static final String VERSION_TAG = "V2";

    /**
     * The key used to decide whether two owners belong to the same household: their normalized
     * {@code lastName} and {@code postcode} joined together, so households stored in different letter
     * cases or with incidental whitespace still compare equal.
     *
     * @param lastName the owner's last name (non-null)
     * @param postcode the owner's postcode, or null when absent
     * @return the comparison key
     */
    public static String toComparisonKey(String lastName, String postcode) {
        return normalizeField(lastName) + KEY_SEPARATOR + (postcode == null ? "" : postcode);
    }

    /**
     * A stable, shared identifier for the household an owner belongs to. It is a pure function of the
     * {@linkplain #toComparisonKey(String, String) comparison key}, so every owner with the same
     * {@code lastName} (compared case-insensitively with collapsed whitespace) and {@code postcode}
     * derives the same identifier without any coordination or stored state, and the identifier stays
     * the same across restarts. The value is the first {@value #HOUSEHOLD_ID_LENGTH} hex characters
     * of the SHA-256 of the fixed {@code 'V2'} version tag followed by the comparison key.
     *
     * @param lastName the owner's last name (non-null)
     * @param postcode the owner's postcode, or null when absent
     * @return the household's stable shared identifier
     */
    public static String toHouseholdId(String lastName, String postcode) {
        return Sha256.hex(VERSION_TAG + toComparisonKey(lastName, postcode)).substring(0, HOUSEHOLD_ID_LENGTH);
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
