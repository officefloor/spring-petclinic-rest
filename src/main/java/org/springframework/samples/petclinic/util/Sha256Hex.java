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
 * Hex-encoded SHA-256 digests of a string's UTF-8 bytes, used to derive deterministic identifiers
 * (identity keys, household ids, customer-code hashes). Keeps the {@link MessageDigest} plumbing and
 * hex formatting in one place instead of duplicating it across callers.
 */
public final class Sha256Hex {

    private Sha256Hex() {
    }

    /** The full 64-character lower-case hex SHA-256 of {@code value}'s UTF-8 bytes. */
    public static String lowerHex(String value) {
        return hex(digest(value), false, Integer.MAX_VALUE);
    }

    /** The first {@code chars} upper-case hex characters of the SHA-256 of {@code value}'s UTF-8 bytes. */
    public static String upperHex(String value, int chars) {
        return hex(digest(value), true, chars);
    }

    private static byte[] digest(String value) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    private static String hex(byte[] hash, boolean upper, int chars) {
        String format = upper ? "%02X" : "%02x";
        StringBuilder sb = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            sb.append(String.format(format, b));
        }
        int length = Math.min(chars, sb.length());
        return sb.substring(0, length);
    }
}
