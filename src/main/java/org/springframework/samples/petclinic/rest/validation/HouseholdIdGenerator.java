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

package org.springframework.samples.petclinic.rest.validation;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.stereotype.Component;

/**
 * Produces the stable identifier shared by the members of a household. The identifier is
 * derived deterministically from the household's {@link HouseholdKey}, so every owner in the
 * same household maps to the same value regardless of creation order.
 */
@Component
public class HouseholdIdGenerator {

    private final HouseholdKey householdKey;

    public HouseholdIdGenerator(HouseholdKey householdKey) {
        this.householdKey = householdKey;
    }

    /** @return the stable household identifier for {@code owner}, e.g. {@code "HH-3F2A9C1E7B4D6058"}. */
    public String generate(Owner owner) {
        return "HH-" + shaHex(householdKey.of(owner), 16);
    }

    /** First {@code n} upper-case hex characters of the SHA-256 digest of {@code value}. */
    private String shaHex(String value, int n) {
        byte[] digest = sha256(value.getBytes(StandardCharsets.UTF_8));
        StringBuilder hex = new StringBuilder(digest.length * 2);
        for (byte b : digest) {
            hex.append(String.format("%02X", b));
        }
        return hex.substring(0, n);
    }

    private byte[] sha256(byte[] bytes) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(bytes);
        }
        catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }
}
