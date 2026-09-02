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
package org.springframework.samples.petclinic.model;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * The deterministic household identifier: the first 12 upper-case hex characters of SHA-256 over the
 * normalized last name and postcode joined with '|'. Owners with the same last name and postcode
 * share it automatically, so it is the single key for household membership and duplicate detection.
 */
public final class Household {

    private Household() {
    }

    /** The household id for the given last name and postcode, or null when either is absent. */
    public static String id(String lastName, String postcode) {
        if (lastName == null || postcode == null) {
            return null;
        }
        String key = "V2|" + lastName.strip().replaceAll("\\s+", " ").toLowerCase() + '|' + postcode;
        StringBuilder sb = new StringBuilder();
        for (byte b : sha256(key)) {
            sb.append(String.format("%02X", b));
        }
        return sb.substring(0, 12);
    }

    private static byte[] sha256(String value) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
