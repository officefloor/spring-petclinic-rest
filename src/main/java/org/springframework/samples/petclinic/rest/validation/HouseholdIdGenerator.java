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

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.util.OwnerIdentityVersion;
import org.springframework.samples.petclinic.util.Sha256Hex;
import org.springframework.stereotype.Component;

/**
 * Produces the stable identifier shared by the members of a household. The identifier is the
 * first 12 hex characters of the SHA-256 digest of the household's {@link HouseholdKey}
 * ({@code normalizedLastName + "|" + postcode}), so every owner with the same last name and
 * postcode maps to the same value regardless of creation order.
 * <p>
 * Under {@link OwnerIdentityVersion identity version 2} the {@link OwnerIdentityVersion#TAG version
 * tag} is mixed into the digest input, so the identifier never coincides with the version-1 value.
 * The household {@link HouseholdKey key} that decides membership is left untouched, so grouping is
 * unaffected.
 */
@Component
public class HouseholdIdGenerator {

    /** Number of leading hex characters of the digest that form the household identifier. */
    private static final int HOUSEHOLD_ID_LENGTH = 12;

    private final HouseholdKey householdKey;

    public HouseholdIdGenerator(HouseholdKey householdKey) {
        this.householdKey = householdKey;
    }

    /** @return the stable household identifier for {@code owner}, e.g. {@code "3F2A9C1E7B4D"}. */
    public String generate(Owner owner) {
        return Sha256Hex.upperHexPrefix(
            OwnerIdentityVersion.tag(householdKey.of(owner)), HOUSEHOLD_ID_LENGTH);
    }
}
