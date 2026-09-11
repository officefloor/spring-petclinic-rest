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

/**
 * Computes SHA-256 digests rendered as hexadecimal. Used to derive the hash segment of an owner's
 * customer code from stable identity fields, so the segment is a deterministic function of that
 * input rather than a mutable sequence.
 */
public final class Sha256Hex {

    private Sha256Hex() {
    }

    /**
     * Returns the first {@code length} upper-case hexadecimal characters of the SHA-256 digest of
     * the UTF-8 bytes of {@code input}.
     *
     * @param input  the source string to hash (must not be {@code null})
     * @param length the number of leading hex characters to return
     * @return the first {@code length} upper-case hex characters of SHA-256({@code input})
     */
    public static String upperHexPrefix(String input, int length) {
        byte[] digest = digest(input);
        StringBuilder sb = new StringBuilder(digest.length * 2);
        for (byte b : digest) {
            sb.append(String.format("%02X", b));
        }
        return sb.substring(0, length);
    }

    private static byte[] digest(String input) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(input.getBytes(StandardCharsets.UTF_8));
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is required but unavailable", e);
        }
    }
}
