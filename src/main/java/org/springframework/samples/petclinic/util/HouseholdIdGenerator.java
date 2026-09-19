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

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Derives the stable {@code householdId} shared by owners that live in the same household,
 * i.e. that have the same last name and address. The identifier is a deterministic function
 * of those two values (compared case- and whitespace-insensitively via {@link TextNormalizer}),
 * so every member of a household independently derives the same identifier regardless of the
 * order in which they are created.
 */
public final class HouseholdIdGenerator {

    /** Number of hex characters kept from the digest; ample to avoid realistic collisions. */
    private static final int LENGTH = 12;

    private HouseholdIdGenerator() {
    }

    /**
     * Compute the stable household identifier for the given last name and address.
     *
     * @param lastName the household's last name
     * @param address  the household's address
     * @return an upper-case hex identifier that is identical for any two owners whose last name
     * and address normalize to the same values
     */
    public static String generate(String lastName, String address) {
        String key = TextNormalizer.normalize(lastName) + "\n" + TextNormalizer.normalize(address);
        return shaHex(key).substring(0, LENGTH).toUpperCase();
    }

    private static String shaHex(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is required but unavailable", e);
        }
    }
}
