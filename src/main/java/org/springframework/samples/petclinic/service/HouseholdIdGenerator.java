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

import org.springframework.stereotype.Component;

/**
 * Derives a household's stable, shared identifier from its last name and address.
 *
 * <p>Owners belong to the same household when they share a last name and address,
 * compared case-insensitively and ignoring differences in surrounding or repeated
 * whitespace. Because the identifier is a deterministic hash of that normalized
 * identity, every owner in a household maps to the same value, independently and
 * across requests.
 */
@Component
public class HouseholdIdGenerator {

    private static final String PREFIX = "HH-";

    private static final int ID_LENGTH = 12;

    /**
     * Build the shared household identifier for the household identified by the given
     * last name and address.
     *
     * @param lastName the household's last name
     * @param address  the household's address
     * @return the stable household identifier, e.g. {@code "HH-0A1B2C3D4E5F"}
     */
    public String generate(String lastName, String address) {
        String identity = normalize(lastName) + "\n" + normalize(address);
        return PREFIX + Sha256.hex(identity).substring(0, ID_LENGTH).toUpperCase(Locale.ROOT);
    }

    /**
     * Normalize a household identity value for comparison: trim, collapse each run of
     * whitespace to a single space and lower-case, so that differences in case or
     * spacing do not split what is really one household.
     */
    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.strip().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
