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
 * last name and address (compared case-insensitively with collapsed whitespace). Because the
 * identifier is a deterministic digest of that normalized pair, every member of a household computes
 * the same value, and it never changes as members join or leave.
 */
public final class HouseholdIdGenerator {

    /** Number of leading hex characters of the digest kept as the identifier. */
    private static final int LENGTH = 16;

    private HouseholdIdGenerator() {
    }

    /**
     * Produce the household identifier for the given last name and address.
     *
     * @param lastName the household's last name
     * @param address the household's address
     * @return a stable, non-blank identifier shared by every owner in the household
     */
    public static String generate(String lastName, String address) {
        String key = TextNormalizer.normalizeForComparison(lastName) + '\n'
            + TextNormalizer.normalizeForComparison(address);
        return Sha256.hexPrefix(key, LENGTH);
    }
}
