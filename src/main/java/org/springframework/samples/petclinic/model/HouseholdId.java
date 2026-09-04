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
package org.springframework.samples.petclinic.model;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Derives a household's stable, shared identifier from the last name and postcode
 * that define it. Owners sharing a household (same last name and postcode) resolve to
 * the same value, so the household key is deterministic rather than explicitly linked.
 */
public final class HouseholdId {

    private HouseholdId() {
    }

    /**
     * @param lastName the household's last name (normalized case-insensitively)
     * @param postcode the household's postcode
     * @return a stable 12-character hex identifier, or {@code null} if either input is null
     */
    public static String of(String lastName, String postcode) {
        if (lastName == null || postcode == null) {
            return null;
        }
        String key = normalize(lastName) + "|" + postcode;
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(key.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 6; i++) {
                sb.append(String.format("%02X", digest[i]));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String normalize(String value) {
        return value.trim().replaceAll("\\s+", " ").toLowerCase();
    }
}
