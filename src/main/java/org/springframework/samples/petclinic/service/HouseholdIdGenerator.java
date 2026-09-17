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

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

import org.springframework.stereotype.Component;

/**
 * Owns the notion of a "household" key and its stable identifier. A household groups owners
 * that share the same last name and address; this component canonicalizes those fields (so
 * values differing only in casing or spacing are treated as equal) and derives a stable,
 * shared {@code householdId} from them.
 *
 * <p>The id is deterministic: the same canonical household always maps to the same id,
 * independent of which member is registered first.
 */
@Component
public class HouseholdIdGenerator {

    /**
     * Derive the stable household identifier for the given last name and address, formatted
     * {@code "HH-<12 upper-case hex>"} of the SHA-256 of the canonical household key.
     *
     * @param lastName the household's last name
     * @param address  the household's address
     * @return the stable, shared household id
     */
    public String generate(String lastName, String address) {
        String key = canonical(lastName) + "\n" + canonical(address);
        return "HH-" + sha256Hex(key).substring(0, 12).toUpperCase(Locale.ROOT);
    }

    /**
     * Canonicalize a free-text field for household identity: trim, collapse internal runs of
     * whitespace to a single space and lower-case, so values differing only in casing or
     * spacing are treated as the same household.
     */
    public String canonical(String value) {
        return value == null ? "" : value.strip().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    private static String sha256Hex(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
