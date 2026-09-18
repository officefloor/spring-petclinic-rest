/*
 * Copyright 2016 the original author or authors.
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
 * Renders SHA-256 digests as upper-case hexadecimal. Shared by the stable identifiers the
 * application derives from owner fields — the household id and the customer code — so the digest
 * and hex-encoding logic lives in one place.
 */
public final class Sha256 {

    private Sha256() {
    }

    /**
     * Produce the first {@code length} upper-case hex characters of the SHA-256 digest of the UTF-8
     * bytes of {@code input}.
     *
     * @param input the value to hash
     * @param length the number of leading hex characters to keep
     * @return the upper-case hex prefix of the digest
     */
    public static String hexPrefix(String input, int length) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(length);
            for (int i = 0; hex.length() < length; i++) {
                hex.append(String.format("%02X", digest[i]));
            }
            return hex.substring(0, length);
        } catch (NoSuchAlgorithmException ex) {
            // SHA-256 is required to be available on every JVM, so this cannot happen.
            throw new IllegalStateException("SHA-256 algorithm not available", ex);
        }
    }
}
