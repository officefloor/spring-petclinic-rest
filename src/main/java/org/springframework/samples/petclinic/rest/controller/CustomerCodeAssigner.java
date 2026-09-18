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

package org.springframework.samples.petclinic.rest.controller;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.Sha256Hex;
import org.springframework.stereotype.Component;

/**
 * Assigns an owner's customer code on create.
 * <p>
 * The code is formatted {@code <REGION>-<HASH8>} where {@code REGION} is the owner's
 * {@link Owner#getRegion() region} (derived from its postcode, falling back to its city)
 * and {@code HASH8} is the first 8 upper-case hex characters of the SHA-256 digest of the
 * owner's normalized telephone concatenated with its last name (e.g. {@code "NSW-1A2B3C4D"}).
 */
@Component
public class CustomerCodeAssigner {

    /** Number of leading hex characters of the identity hash kept in the customer code. */
    private static final int HASH_LENGTH = 8;

    /**
     * Assigns {@code owner}'s customer code from its region and the hash of its normalized
     * telephone and last name. Call this after the telephone has been normalized and before
     * the owner is saved.
     *
     * @param owner the owner being created
     */
    public void assign(Owner owner) {
        String hash = Sha256Hex.prefix(owner.getTelephone() + owner.getLastName(), HASH_LENGTH);
        owner.setCustomerCode(owner.getRegion() + "-" + hash);
    }
}
