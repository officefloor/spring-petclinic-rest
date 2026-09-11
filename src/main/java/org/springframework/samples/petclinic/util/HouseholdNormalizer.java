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

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

import org.springframework.stereotype.Component;

/**
 * Produces the canonical key identifying the household an owner belongs to.
 * <p>
 * Centralising this here keeps the REST controllers thin: they delegate to
 * {@link #householdKey(String, String)} both when deciding whether a new owner shares a
 * household with an existing one and (in future) when deriving the shared household
 * identifier, so the two always agree on what "the same household" means.
 * <p>
 * Two owners belong to the same household when they have the same last name and the same
 * address. Both fields are compared case-insensitively after trimming and collapsing runs
 * of whitespace to a single space, so values that differ only in letter case or in
 * incidental spacing map to the same key. The normalized fields are joined with a newline,
 * which the normalization can never itself produce, so the key uniquely determines the
 * (last name, address) pair it was built from.
 */
@Component
public class HouseholdNormalizer {

    /**
     * Builds the canonical household key for the given last name and address. Owners whose
     * last name and address normalize identically share a key and therefore a household.
     *
     * @param lastName the owner's last name (may be {@code null})
     * @param address  the owner's address (may be {@code null})
     * @return the canonical household key
     */
    public String householdKey(String lastName, String address) {
        return normalizeField(lastName) + '\n' + normalizeField(address);
    }

    /**
     * Derives the stable shared identifier for the household with the given last name and
     * address. The identifier is a pure function of the canonical {@link #householdKey key},
     * so every owner in the same household derives the identical value and it never changes
     * between calls (a "stable shared identifier"). It is formatted 'HH-' followed by the
     * first 12 upper-case hex characters of the SHA-256 of the household key.
     *
     * @param lastName the owner's last name (may be {@code null})
     * @param address  the owner's address (may be {@code null})
     * @return the stable household identifier
     */
    public String householdId(String lastName, String address) {
        return "HH-" + sha256Hex(householdKey(lastName, address)).substring(0, 12).toUpperCase(Locale.ROOT);
    }

    /**
     * Computes the lower-case hex SHA-256 digest of the UTF-8 bytes of the given value.
     *
     * @param value the value to hash
     * @return the hex-encoded digest
     */
    private String sha256Hex(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    /**
     * Normalizes a single household field by trimming, collapsing internal runs of
     * whitespace to a single space and lower-casing, so comparisons ignore case and
     * incidental whitespace differences.
     *
     * @param value the value to normalize (may be {@code null})
     * @return the normalized value, or the empty string if the input was {@code null}
     */
    private String normalizeField(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
