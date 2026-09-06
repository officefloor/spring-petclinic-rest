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

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Captures the rules for what makes two owners the same household. Kept apart from
 * {@link OwnerRestControllerV1} so the request handler stays focused on orchestration while the rules
 * for how a last name and address are canonicalized, and when two of them match, live in one place.
 *
 * <p>Two owners belong to the same household when their last names match and their addresses match,
 * each compared case-insensitively after collapsing runs of whitespace to a single space and
 * trimming, so trivial differences in spacing or letter case still count as a match.
 */
final class HouseholdNormalizer {

    private HouseholdNormalizer() {
    }

    /**
     * Determines whether two owners, identified by their last name and address, belong to the same
     * household. Both fields are compared using {@link #normalize(String)}, so the match ignores letter
     * case and trivial differences in whitespace.
     *
     * @param lastNameA the first owner's last name
     * @param addressA  the first owner's address
     * @param lastNameB the second owner's last name
     * @param addressB  the second owner's address
     * @return {@code true} if both owners share the same normalized last name and address
     */
    static boolean sameHousehold(String lastNameA, String addressA, String lastNameB, String addressB) {
        return normalize(lastNameA).equals(normalize(lastNameB))
            && normalize(addressA).equals(normalize(addressB));
    }

    /**
     * Derives the stable household identifier shared by every owner that belongs to the same
     * household. Because it is computed purely from the {@link #normalize(String) normalized} last
     * name and address, two owners that {@link #sameHousehold match} always derive the same value,
     * so joiners can be assigned a shared identifier without coordinating through stored state.
     *
     * @param lastName the owner's last name
     * @param address  the owner's address
     * @return a stable, opaque hex identifier for the household
     */
    static String householdId(String lastName, String address) {
        String key = normalize(lastName) + "\n" + normalize(address);
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(key.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(32);
            for (int i = 0; i < 16; i++) {
                sb.append(String.format("%02x", digest[i]));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException ex) {
            // SHA-256 is a required algorithm on every JVM, so this cannot happen.
            throw new IllegalStateException("SHA-256 is not available", ex);
        }
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
