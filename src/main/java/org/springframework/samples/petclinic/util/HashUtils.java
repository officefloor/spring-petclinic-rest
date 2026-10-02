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
 * Shared SHA-256 hashing helper. Generic hashing logic with no dependency on any particular domain
 * object, kept here so every value derived from a SHA-256 digest (the owner's {@code identityKey},
 * {@code householdId} and {@code memberId} {@code HASH8} segment) shares one digest/hex implementation
 * rather than each copying the loop and its exception handling.
 */
public final class HashUtils {

    private HashUtils() {
    }

    /**
     * Returns the full lower-case hex SHA-256 digest of the UTF-8 bytes of the given input. Callers
     * that need an upper-cased or truncated form derive it from this value.
     *
     * @param input the value to hash
     * @return the 64-character lower-case hex digest
     */
    public static String sha256Hex(String input) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }
}
