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
package org.springframework.samples.petclinic.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

/**
 * Computes upper-case hexadecimal SHA-256 digests. Shared by the identifier generators
 * so the digest-to-hex logic lives in exactly one place.
 */
public final class Sha256Hex {

    private Sha256Hex() {
    }

    /**
     * Digest {@code input} with SHA-256 and return the first {@code length} characters of its
     * hexadecimal representation, upper-cased.
     *
     * @param input  the string whose UTF-8 bytes are digested
     * @param length the number of leading hex characters to return
     * @return the upper-case hexadecimal prefix of the digest
     */
    public static String upperHexPrefix(String input, int length) {
        return fullHex(input).substring(0, length).toUpperCase(Locale.ROOT);
    }

    private static String fullHex(String input) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is required for identifier generation", e);
        }
    }
}
