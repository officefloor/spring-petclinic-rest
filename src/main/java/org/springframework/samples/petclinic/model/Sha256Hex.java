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
 * SHA-256 helper producing the leading upper-case hex characters of a value's digest.
 */
public final class Sha256Hex {

    private Sha256Hex() {
    }

    /**
     * The first {@code length} upper-case hex characters of the SHA-256 digest of the
     * UTF-8 bytes of {@code value}.
     *
     * @param value  the string to hash
     * @param length how many leading hex characters to return
     * @return the leading {@code length} upper-case hex characters of the digest
     */
    public static String prefix(String value, int length) {
        byte[] digest = digest(value);
        StringBuilder sb = new StringBuilder(length + 1);
        for (int i = 0; sb.length() < length; i++) {
            sb.append(String.format("%02X", digest[i]));
        }
        return sb.substring(0, length);
    }

    /**
     * The full lower-case hex encoding of the SHA-256 digest of the UTF-8 bytes of
     * {@code value}: 64 hex characters.
     *
     * @param value the string to hash
     * @return the 64-character lower-case hex digest
     */
    public static String hex(String value) {
        byte[] digest = digest(value);
        StringBuilder sb = new StringBuilder(digest.length * 2);
        for (byte b : digest) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private static byte[] digest(String value) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
        }
        catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is required but unavailable", ex);
        }
    }
}
