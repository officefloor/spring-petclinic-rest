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

import org.springframework.stereotype.Component;

/**
 * Produces the deterministic identifier of the household an owner belongs to.
 * <p>
 * Centralising this here keeps the REST controllers thin: they delegate to
 * {@link #householdId(String, String)} both when deciding whether a new owner shares a
 * household with an existing one and when deriving the shared household identifier, so the
 * two always agree on what "the same household" means.
 * <p>
 * Two owners belong to the same household when they have the same last name and the same
 * postcode. The last name is compared case-insensitively after trimming and collapsing runs
 * of whitespace to a single space, so values that differ only in letter case or in incidental
 * spacing map to the same household. The household identifier is a pure function of those two
 * fields, so every owner with the same last name and postcode derives the identical value
 * automatically without any explicit linking step.
 */
@Component
public class HouseholdNormalizer {

    /**
     * The fixed identity version tag mixed into the hashed household fields so the version-2
     * household id differs from every value produced under version 1. It is prepended to the joined
     * fields before hashing; because the same tag is mixed into every id, owners with the same last
     * name and postcode still derive the identical value.
     */
    private static final String IDENTITY_VERSION_TAG = "V2";

    /**
     * Derives the deterministic shared identifier for the household with the given last name and
     * postcode. The identifier is a pure function of the normalized last name and the postcode, so
     * every owner with the same last name and postcode derives the identical value and it never
     * changes between calls. It is the first 12 hexadecimal characters of the SHA-256 of
     * {@code "V2" + '|' + normalizedLastName + '|' + postcode} — the fixed {@code "V2"} identity
     * version tag mixed in so the value differs from its version-1 form — hashed via the shared
     * {@link Sha256Hex} helper so every hash-derived identifier in the app is computed one way.
     * Owners with the same last name and postcode therefore share the household id automatically.
     *
     * @param lastName the owner's last name (may be {@code null})
     * @param postcode the owner's postcode (may be {@code null})
     * @return the deterministic household identifier
     */
    public String householdId(String lastName, String postcode) {
        return Sha256Hex.upperHexPrefix(
            IDENTITY_VERSION_TAG + '|' + normalizeLastName(lastName) + '|' + (postcode == null ? "" : postcode), 12);
    }

    /**
     * Normalizes the last name by trimming, collapsing internal runs of whitespace to a single
     * space and lower-casing, so households match regardless of case and incidental whitespace
     * differences.
     *
     * @param value the value to normalize (may be {@code null})
     * @return the normalized value, or the empty string if the input was {@code null}
     */
    private String normalizeLastName(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
