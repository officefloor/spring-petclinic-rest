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

package org.springframework.samples.petclinic.rest.validation;

import java.util.Locale;

/**
 * The canonical identity of a household: an owner's last name together with the postcode they live
 * at. Two owners belong to the same household when their keys are equal, and therefore derive the
 * same stable {@link #idFor(String, String) household id}. The last name is normalized (trimmed,
 * internal whitespace runs collapsed to a single space, lower-cased) so values that differ only in
 * letter case or spacing match; the postcode is used verbatim. This shared definition keeps
 * household matching consistent across every feature that keys off the household.
 */
public final class HouseholdKey {

    private HouseholdKey() {
    }

    /**
     * The canonical key for the household an owner with this last name and postcode belongs to,
     * formed as {@code normalizedLastName + '|' + postcode}.
     */
    public static String of(String lastName, String postcode) {
        return normalize(lastName) + "|" + orEmpty(postcode);
    }

    /**
     * The stable identifier for the household an owner with this last name and postcode belongs to:
     * the first 12 upper-case hex characters of the SHA-256 digest of its {@link #of(String, String)
     * key}. Deterministic, so every owner sharing a last name and postcode derives the same value.
     */
    public static String idFor(String lastName, String postcode) {
        return Sha256.hex(of(lastName, postcode)).substring(0, 12).toUpperCase(Locale.ROOT);
    }

    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    private static String orEmpty(String value) {
        return value == null ? "" : value;
    }
}
