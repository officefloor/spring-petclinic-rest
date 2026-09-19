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

package org.springframework.samples.petclinic.util;

/**
 * Derives the stable {@code householdId} shared by owners that live in the same household,
 * i.e. that have the same last name and postcode. The identifier is a deterministic function
 * of those two values (the last name compared case- and whitespace-insensitively via
 * {@link TextNormalizer}), so every member of a household independently derives the same
 * identifier regardless of the order in which they are created.
 */
public final class HouseholdIdGenerator {

    /** Number of hex characters kept from the digest; ample to avoid realistic collisions. */
    private static final int LENGTH = 12;

    /** Separates the two key components so distinct pairs cannot collide by concatenation. */
    private static final String SEPARATOR = "|";

    private HouseholdIdGenerator() {
    }

    /**
     * Compute the stable household identifier for the given last name and postcode: the first
     * {@value #LENGTH} hex characters of SHA-256 over {@code normalizedLastName + '|' + postcode}.
     *
     * @param lastName the household's last name
     * @param postcode the household's postcode (a {@code null} postcode contributes an empty component)
     * @return an upper-case hex identifier that is identical for any two owners whose last name
     * normalizes to the same value and whose postcode is equal
     */
    public static String generate(String lastName, String postcode) {
        String key = TextNormalizer.normalize(lastName) + SEPARATOR + (postcode == null ? "" : postcode);
        return Sha256.hex(key).substring(0, LENGTH).toUpperCase();
    }
}
