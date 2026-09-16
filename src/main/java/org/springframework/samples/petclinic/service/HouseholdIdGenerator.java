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

import java.util.Locale;

import org.springframework.samples.petclinic.model.IdentityVersion;
import org.springframework.samples.petclinic.model.Sha256;
import org.springframework.stereotype.Component;

/**
 * Derives a household's stable, shared identifier from its last name and postcode.
 *
 * <p>Owners belong to the same household when they share a last name (compared
 * case-insensitively and ignoring differences in surrounding or repeated whitespace)
 * and postcode. Because the identifier is a deterministic hash of that normalized
 * identity, every owner in a household maps to the same value automatically,
 * independently and across requests, without any owner having to opt in.
 */
@Component
public class HouseholdIdGenerator {

    private static final int ID_LENGTH = 12;

    /**
     * Build the shared household identifier for the household identified by the given
     * last name and postcode: the first {@value #ID_LENGTH} hex characters of the version-2
     * SHA-256 digest of {@code 'V2' + '|' + normalizedLastName + '|' + postcode} (see
     * {@link IdentityVersion}). Mixing in the fixed version tag makes every version-2
     * household id differ from its version-1 value, while owners still share it purely on
     * last name and postcode.
     *
     * <p>An owner without a postcode belongs to no shared household, so this returns
     * {@code null} in that case.
     *
     * @param lastName the household's last name
     * @param postcode the household's postcode
     * @return the stable household identifier, e.g. {@code "0a1b2c3d4e5f"}, or
     * {@code null} when no postcode is supplied
     */
    public String generate(String lastName, String postcode) {
        if (postcode == null || postcode.isBlank()) {
            return null;
        }
        String identity = IdentityVersion.TAG + "|" + normalize(lastName) + "|" + postcode;
        return Sha256.hex(identity).substring(0, ID_LENGTH);
    }

    /**
     * Normalize the last name for comparison: trim, collapse each run of whitespace to
     * a single space and lower-case, so that differences in case or spacing do not
     * split what is really one household.
     */
    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.strip().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
