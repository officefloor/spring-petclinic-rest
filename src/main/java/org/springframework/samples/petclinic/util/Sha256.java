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
 * SHA-256 reduced to a lower-case hex string. Kept separate from the owner controller and the
 * {@code Owner} model so the single way this codebase turns a value into a stable hex digest lives in
 * one place, as a pure function with no web or persistence dependencies. Several owner values are
 * derived from such a digest and share this helper: the {@link CustomerCode} hash component and the
 * {@link HouseholdNormalizer} household id each keep a leading slice of it.
 */
public abstract class Sha256 {

    /**
     * The lower-case hex SHA-256 of the UTF-8 bytes of {@code value}: 64 hex characters, two per
     * digest byte. SHA-256 is a standard algorithm required to be present on every JVM, so its
     * absence is treated as a fatal misconfiguration rather than a recoverable error.
     *
     * @param value the value to digest (non-null)
     * @return the 64-character lower-case hex digest
     */
    public static String hex(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                hex.append(Character.forDigit((b >> 4) & 0xF, 16));
                hex.append(Character.forDigit(b & 0xF, 16));
            }
            return hex.toString();
        }
        catch (NoSuchAlgorithmException ex) {
            // SHA-256 is a standard algorithm required to be present on every JVM.
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }

}
