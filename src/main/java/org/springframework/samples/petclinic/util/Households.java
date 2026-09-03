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
import java.util.Collection;
import java.util.Locale;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Deterministic household identity: the id is the first 12 hex characters of SHA-256 over
 * {@code normalizedLastName + '|' + postcode}, so owners with the same last name and postcode
 * share a household automatically.
 */
public final class Households {

    private Households() {
    }

    /** The deterministic householdId for the given last name and postcode. */
    public static String id(String lastName, String postcode) {
        String key = normalize(lastName) + '|' + (postcode == null ? "" : postcode);
        byte[] digest = sha256(key);
        StringBuilder hex = new StringBuilder(12);
        for (int i = 0; i < 6; i++) {
            hex.append(String.format("%02x", digest[i]));
        }
        return hex.toString();
    }

    /** Whether any existing owner already belongs to the household identified by {@code householdId}. */
    public static boolean isDuplicate(Collection<Owner> existing, String householdId) {
        for (Owner other : existing) {
            if (householdId.equals(other.getHouseholdId())) {
                return true;
            }
        }
        return false;
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    private static byte[] sha256(String value) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
        }
        catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
