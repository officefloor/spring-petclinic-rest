/*
 * Copyright 2002-2017 the original author or authors.
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
 * Renders SHA-256 digests as hex. The single home for the {@code MessageDigest} acquisition and the
 * "SHA-256 not available" error, shared by every derived identifier built from a SHA-256 hash (e.g.
 * the owner's {@code identityKey}, {@code householdId} and customer-code hash), so the digest
 * handling and that error live in one place.
 */
public final class Sha256 {

    private Sha256() {
    }

    /**
     * Compute the full lower-case hex SHA-256 over the UTF-8 bytes of {@code input} (64 hex
     * characters).
     */
    public static String hex(String input) {
        return hexPrefix(input, 32, false);
    }

    /**
     * Compute the leading hex of SHA-256 over the UTF-8 bytes of {@code input}: the first
     * {@code bytes} bytes of the digest rendered as {@code 2 * bytes} hex characters, upper-case when
     * {@code upperCase} is {@code true}, otherwise lower-case.
     */
    public static String hexPrefix(String input, int bytes, boolean upperCase) {
        byte[] digest;
        try {
            digest = MessageDigest.getInstance("SHA-256")
                .digest(input.getBytes(StandardCharsets.UTF_8));
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is required but not available", e);
        }
        String format = upperCase ? "%02X" : "%02x";
        StringBuilder hex = new StringBuilder(bytes * 2);
        for (int i = 0; i < bytes; i++) {
            hex.append(String.format(format, digest[i]));
        }
        return hex.toString();
    }
}
