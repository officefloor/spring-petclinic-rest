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

/**
 * Derives the stable identifier shared by owners who belong to the same household, i.e. who share a
 * last name and postcode. The last name is normalized (case-insensitively, with collapsed
 * whitespace) and joined to the postcode with {@code '|'}; the identifier is the leading hex
 * characters of the SHA-256 digest of that pair. Because it is a deterministic digest, every owner
 * with the same last name and postcode computes the same value automatically, and it never changes
 * as members join or leave.
 */
public final class HouseholdIdGenerator {

    /** Number of leading hex characters of the digest kept as the identifier. */
    private static final int LENGTH = 12;

    private HouseholdIdGenerator() {
    }

    /**
     * Produce the household identifier for the given last name and postcode, or {@code null} when
     * no postcode is on file (an owner without a postcode belongs to no shared household).
     *
     * @param lastName the household's last name
     * @param postcode the household's postcode
     * @return the identifier shared by every owner with this last name and postcode, or
     *         {@code null} when the postcode is absent
     */
    public static String generate(String lastName, String postcode) {
        if (postcode == null || postcode.isBlank()) {
            return null;
        }
        String key = TextNormalizer.normalizeForComparison(lastName) + '|' + postcode;
        return Sha256.hexPrefix(key, LENGTH);
    }
}
