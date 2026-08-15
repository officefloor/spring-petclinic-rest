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

/**
 * Helpers for the "household" a pet owner belongs to. Two owners share a household when they have
 * the same last name and postcode; the identity is derived deterministically from the normalized
 * last name (surrounding whitespace trimmed, internal whitespace runs collapsed to a single space,
 * lower-cased) and the postcode, so values that differ only in letter case or spacing of the last
 * name map to one household.
 */
public final class Households {

    private Households() {
    }

    /**
     * Normalizes a value for household comparison: {@code null} becomes an empty string, surrounding
     * whitespace is trimmed, internal runs of whitespace collapse to a single space and the result is
     * lower-cased.
     */
    public static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    /**
     * Returns the stable, shared identifier for the household of the owner with the given last name
     * and postcode. The value is the first 12 hex characters of the SHA-256 digest of the normalized
     * last name, a {@code '|'} separator and the postcode, so every owner sharing a last name and
     * postcode (the two values the household is keyed on) is deterministically assigned the same
     * identifier.
     *
     * @param lastName the owner's last name
     * @param postcode the owner's postcode, or {@code null} when none
     * @return the household identifier
     */
    public static String householdId(String lastName, String postcode) {
        String key = normalize(lastName) + "|" + (postcode == null ? "" : postcode);
        return sha256hex(key).substring(0, 12);
    }

    /**
     * Builds an owner's {@code identityKey}: the single derived value all duplicate detection is
     * expressed through, formatted {@code '<telephone>|<email or empty>|<householdId>'} (a
     * {@code null} telephone or email contributes an empty segment). Two owners are duplicates only
     * when their whole identity keys are equal, so household members sharing a {@code householdId} but
     * carrying different telephones have different keys and are not duplicates.
     *
     * @param telephone   the owner's normalized (E.164) telephone
     * @param email       the owner's normalized email, or {@code null} when none
     * @param householdId the owner's household identifier (see {@link #householdId})
     * @return the derived identity key
     */
    public static String identityKey(String telephone, String email, String householdId) {
        return (telephone == null ? "" : telephone) + "|"
            + (email == null ? "" : email) + "|"
            + (householdId == null ? "" : householdId);
    }

    private static String sha256hex(String s) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));
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
}
