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
 * Computes the SHA-256 digest of a string as a lower-case hex string. Kept apart from
 * {@link OwnerRestControllerV1} so the request handler stays focused on orchestration, and so the
 * single piece of SHA-256 boilerplate lives in one place: the several owner values that are derived
 * by hashing (such as the {@link HouseholdNormalizer#householdId household identifier}) share this
 * one helper rather than each repeating the digest-and-hex-encode dance.
 */
final class Sha256 {

    private Sha256() {
    }

    /**
     * Returns the SHA-256 digest of the UTF-8 bytes of {@code input} as a 64-character lower-case
     * hex string. Callers that need a shorter, opaque token take a prefix of the result.
     *
     * @param input the string to hash
     * @return the digest as 64 lower-case hex characters
     */
    static String hex(String input) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException ex) {
            // SHA-256 is a required algorithm on every JVM, so this cannot happen.
            throw new IllegalStateException("SHA-256 is not available", ex);
        }
    }

}
