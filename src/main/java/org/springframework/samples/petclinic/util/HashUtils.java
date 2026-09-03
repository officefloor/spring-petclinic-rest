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
 * Utility methods for the SHA-256 hashing used to derive owners' stable identity
 * values. Separate from the entity and controller classes so the single hashing
 * implementation can be shared by every rule that hashes owner data (the customer
 * code, the household id and the identity key) rather than being re-derived in each.
 *
 * @see org.springframework.samples.petclinic.model.Owner#getIdentityKey()
 */
public abstract class HashUtils {

    /**
     * The SHA-256 digest of {@code input}'s UTF-8 bytes, rendered as a lower-case
     * hexadecimal string (two hex characters per digest byte, so 64 characters in
     * all). This is the single place the hashing-and-hex-encoding is performed;
     * callers that need an upper-case rendering or a shorter opaque token upper-case
     * and/or take a prefix of the result.
     */
    public static String sha256Hex(String input) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(input.getBytes(StandardCharsets.UTF_8));
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
