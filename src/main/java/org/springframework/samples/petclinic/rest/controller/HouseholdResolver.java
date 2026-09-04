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

package org.springframework.samples.petclinic.rest.controller;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.util.HashUtils;
import org.springframework.samples.petclinic.util.IdentityUtils;
import org.springframework.stereotype.Component;

/**
 * Owns the household-identity concern: the single place that derives the shared household id
 * owners carry.
 *
 * <p>The household id is deterministic: the first twelve hex characters of the SHA-256 digest
 * of the normalized last name and the postcode joined by '|'. Owners that share a normalized
 * last name and postcode therefore derive the same id automatically, without any explicit
 * link. The id no longer participates in duplicate detection (which now rests solely on the
 * identity key); it feeds the household membership-level ceiling and household-size derivations.
 */
@Component
public class HouseholdResolver {

    /**
     * The stable, shared household identifier for {@code owner}: the first twelve hex
     * characters of {@code SHA-256(normalizedLastName + '|' + postcode)}. Owners in the same
     * household (same normalized last name and postcode) derive an identical id.
     *
     * @param owner the owner whose household id is derived
     * @return the deterministic household id
     */
    public String householdId(Owner owner) {
        String postcode = owner.getPostcode() == null ? "" : owner.getPostcode();
        String key = IdentityUtils.normalizeIdentity(owner.getLastName()) + "|" + postcode;
        return HashUtils.sha256HexPrefix(key, 12);
    }
}
