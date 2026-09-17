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

package org.springframework.samples.petclinic.rest.assignment;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

import org.springframework.samples.petclinic.rest.validation.HouseholdKey;
import org.springframework.stereotype.Component;

/**
 * Derives an owner's {@code householdId}: a stable identifier shared by every owner in the same
 * household. Because it is a pure function of the {@link HouseholdKey}, owners in the same household
 * always yield the same value, formatted {@code 'HH-<12 upper-case hex>'}.
 */
@Component
public class HouseholdIdGenerator {

    private final HouseholdKey householdKey;

    public HouseholdIdGenerator(HouseholdKey householdKey) {
        this.householdKey = householdKey;
    }

    /**
     * @param lastName the owner's last name
     * @param address  the owner's address
     * @return the stable household identifier for that last name and address
     */
    public String generate(String lastName, String address) {
        String key = this.householdKey.of(lastName, address);
        return "HH-" + sha256(key).substring(0, 12).toUpperCase(Locale.ROOT);
    }

    private static String sha256(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        }
        catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is required but unavailable", ex);
        }
    }
}
