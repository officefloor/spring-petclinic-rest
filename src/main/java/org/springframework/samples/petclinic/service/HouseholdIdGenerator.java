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

import org.springframework.samples.petclinic.util.Sha256;
import org.springframework.stereotype.Component;

/**
 * Owns the notion of a "household" key and its stable identifier. A household groups owners
 * that share the same last name and postcode; this component normalizes the last name (so
 * values differing only in casing or spacing are treated as equal) and derives a stable,
 * shared {@code householdId} from the normalized last name and the postcode.
 *
 * <p>The id is deterministic: the same (last name, postcode) household always maps to the same
 * id, independent of which member is registered first, so members share it automatically.
 */
@Component
public class HouseholdIdGenerator {

    /**
     * Derive the stable household identifier for the given last name and postcode: the first 12
     * hex characters of the SHA-256 of {@code normalizedLastName + '|' + postcode}.
     *
     * @param lastName the household's last name
     * @param postcode the household's postcode
     * @return the stable, shared household id
     */
    public String generate(String lastName, String postcode) {
        String key = canonical(lastName) + "|" + (postcode == null ? "" : postcode);
        return Sha256.hex(key).substring(0, 12);
    }

    /**
     * Canonicalize the last name for household identity: trim, collapse internal runs of
     * whitespace to a single space and lower-case, so values differing only in casing or
     * spacing are treated as the same household.
     */
    public String canonical(String value) {
        return value == null ? "" : value.strip().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
