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

package org.springframework.samples.petclinic.rest.controller.v1;

/**
 * Captures the rules for what makes two owners the same household. Kept apart from
 * {@link OwnerRestControllerV1} so the request handler stays focused on orchestration while the rule
 * for how a last name and postcode are canonicalized into a shared identifier lives in one place.
 *
 * <p>Two owners belong to the same household when their last names match - compared
 * case-insensitively after collapsing runs of whitespace to a single space and trimming - and their
 * postcodes are identical, so trivial differences in spacing or letter case in the last name still
 * count as a match. Because the identifier is derived purely from those two values, owners with the
 * same last name and postcode are automatically assigned the same {@link #householdId householdId}.
 */
final class HouseholdNormalizer {

    /**
     * Number of leading hex characters of the household digest kept as the {@link #householdId
     * household identifier}. The SHA-256 digest is far wider than a household id needs to be, so only
     * this many characters are retained.
     */
    private static final int ID_LENGTH = 12;

    private HouseholdNormalizer() {
    }

    /**
     * Derives the stable household identifier shared by every owner that belongs to the same
     * household - the first {@value #ID_LENGTH} hex characters of SHA-256 over the household
     * {@link #key(String, String) key}. Because it is computed purely from the last name and
     * postcode, two owners with the same normalized last name and postcode always derive the same
     * value, so joiners are assigned a shared identifier without coordinating through stored state.
     *
     * @param lastName the owner's last name
     * @param postcode the owner's postcode, or {@code null} when none was supplied
     * @return a stable, opaque hex identifier for the household
     */
    static String householdId(String lastName, String postcode) {
        return Sha256.hex(key(lastName, postcode)).substring(0, ID_LENGTH);
    }

    /**
     * Composes the canonical household key that is hashed to form the {@link #householdId household
     * identifier}: the {@link #normalize(String) normalized} last name and the postcode joined by a
     * {@code '|'} delimiter that a normalized last name can never contain, so two owners yield the
     * same key exactly when they belong to the same household.
     *
     * @param lastName the owner's last name
     * @param postcode the owner's postcode, or {@code null} when none was supplied
     * @return the canonical household key that identifies the household
     */
    private static String key(String lastName, String postcode) {
        return normalize(lastName) + "|" + (postcode == null ? "" : postcode);
    }

    /**
     * Normalizes a value for household comparison: leading and trailing whitespace is trimmed, every
     * internal run of whitespace is collapsed to a single space, and the result is lower-cased so the
     * comparison is case-insensitive.
     *
     * @param value the raw last name or address value, or {@code null}
     * @return the normalized value, or the empty string when {@code value} is {@code null}
     */
    static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase();
    }

}
