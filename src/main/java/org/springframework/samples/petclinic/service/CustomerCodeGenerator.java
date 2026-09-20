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
package org.springframework.samples.petclinic.service;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Formats an owner's {@code customerCode} as {@code '<REGION>-<HASH8>'}, where {@code REGION}
 * is the owner's region code and {@code HASH8} is the first eight upper-case hex characters of
 * the SHA-256 digest over the owner's identity ({@code normalizedTelephone + lastName})
 * (e.g. {@code 'NSW-A1B2C3D4'}). The code no longer carries a sequence number.
 */
@Component
public class CustomerCodeGenerator {

    /** Number of leading hex characters of the SHA-256 digest kept as the hash segment. */
    private static final int HASH_LENGTH = 8;

    /**
     * Build the customer code from the owner's region and identity.
     *
     * @param region             the owner's region code (the leading {@code <REGION>} segment)
     * @param normalizedTelephone the owner's telephone in its canonical (E.164) form
     * @param lastName           the owner's last name
     * @return the formatted customer code {@code '<REGION>-<HASH8>'}
     */
    public String generate(String region, String normalizedTelephone, String lastName) {
        return region + "-" + hash(normalizedTelephone + lastName);
    }

    private String hash(String identity) {
        byte[] digest = sha256(identity);
        StringBuilder hex = new StringBuilder(HASH_LENGTH);
        for (int i = 0; hex.length() < HASH_LENGTH; i++) {
            hex.append(String.format("%02X", digest[i]));
        }
        return hex.substring(0, HASH_LENGTH);
    }

    private byte[] sha256(String value) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is required but unavailable", e);
        }
    }
}
