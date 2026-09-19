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
package org.springframework.samples.petclinic.service;

import org.springframework.samples.petclinic.model.Sha256Hex;
import org.springframework.stereotype.Component;

/**
 * Builds the stable, shared {@code householdId} for owners living in the same
 * household.
 *
 * <p>The identifier is derived deterministically from the household's identity
 * (last name, canonicalized by {@link HouseholdNormalizer}, and postcode), so every
 * owner sharing a last name and postcode resolves to the same value automatically,
 * regardless of registration order.
 */
@Component
public class HouseholdIdGenerator {

    private static final int ID_LENGTH = 12;

    private final HouseholdNormalizer householdNormalizer;

    public HouseholdIdGenerator(HouseholdNormalizer householdNormalizer) {
        this.householdNormalizer = householdNormalizer;
    }

    /**
     * Build the household identifier shared by owners with the given last name and
     * postcode: the first {@value #ID_LENGTH} upper-case hex characters of SHA-256 over
     * {@code normalize(lastName) + "|" + postcode}.
     *
     * @param lastName the household's last name
     * @param postcode the household's postcode
     * @return a stable, upper-case hexadecimal identifier for the household
     */
    public String generate(String lastName, String postcode) {
        String key = householdNormalizer.normalize(lastName) + "|" + postcode;
        return Sha256Hex.upperHexPrefix(key, ID_LENGTH);
    }
}
