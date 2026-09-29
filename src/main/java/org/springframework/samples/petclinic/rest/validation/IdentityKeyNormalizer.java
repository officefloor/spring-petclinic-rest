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

package org.springframework.samples.petclinic.rest.validation;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Utility for deriving an owner's {@code identityKey}, the single value into which all
 * duplicate detection is consolidated. The key is
 * {@code normalizedTelephone + '|' + (email or empty) + '|' + householdId}: the owner's
 * telephone in {@link TelephoneNormalizer E.164} form, the (already lower-cased) e-mail
 * address or an empty string when absent, and the shared household identifier or an empty
 * string when the owner does not belong to a household.
 *
 * <p>Because the telephone is part of the key, two members of the same household (the same
 * {@code householdId}) with different telephones have different identity keys; only an exact
 * whole-key match between a new owner and an existing one is a duplicate.
 */
public final class IdentityKeyNormalizer {

    private IdentityKeyNormalizer() {
    }

    /**
     * Derive the owner's identity key.
     *
     * @param owner the owner (may be {@code null})
     * @return the derived identity key, or {@code null} when the owner is {@code null}
     */
    public static String derive(Owner owner) {
        if (owner == null) {
            return null;
        }
        String telephone = TelephoneNormalizer.toComparable(owner.getTelephone());
        String email = owner.getEmail() == null ? "" : owner.getEmail();
        String householdId = owner.getHouseholdId() == null ? "" : owner.getHouseholdId();
        return telephone + "|" + email + "|" + householdId;
    }
}
