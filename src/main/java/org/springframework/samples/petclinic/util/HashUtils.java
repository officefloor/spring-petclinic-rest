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
 * Shared SHA-256 hex helpers used to derive the owner identity key, household id and member-id
 * hash. Centralises the digest-and-hex boilerplate so those derivations differ only in hex case and
 * truncation length rather than each carrying their own copy.
 */
public final class HashUtils {

    private HashUtils() {
    }

    /**
     * Returns the full lower-case hex SHA-256 digest of the UTF-8 bytes of {@code seed} (64 hex
     * characters).
     */
    public static String sha256Hex(String seed) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(seed.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    /**
     * Returns the first {@code length} upper-case hex characters of the SHA-256 digest of the UTF-8
     * bytes of {@code seed}.
     */
    public static String sha256UpperHex(String seed, int length) {
        return sha256Hex(seed).substring(0, length).toUpperCase();
    }
}
