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
 * Small stateless helpers for the SHA-256 hex derivations that back the application's
 * identity values (customer codes, household ids).
 *
 * <p>Keeping the derivation in one place means every identity value built from a truncated
 * hex digest reads the same computation and they can never drift apart.
 */
public abstract class HashUtils {

    /**
     * The full, lower-case hex encoding of the SHA-256 digest of the UTF-8 bytes of
     * {@code source} (64 hex characters).
     *
     * @param source the string to digest
     * @return the 64-character lower-case hex SHA-256 digest of {@code source}
     */
    public static String sha256Hex(String source) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(source.getBytes(StandardCharsets.UTF_8));
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
     * The first {@code length} upper-case hex characters of the SHA-256 digest of the UTF-8
     * bytes of {@code source}.
     *
     * @param source the string to digest
     * @param length the number of leading hex characters to return
     * @return the first {@code length} upper-case hex characters of the SHA-256 digest
     */
    public static String sha256HexPrefix(String source, int length) {
        return sha256Hex(source).substring(0, length).toUpperCase(Locale.ROOT);
    }
}
