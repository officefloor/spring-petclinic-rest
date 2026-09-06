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

package org.springframework.samples.petclinic.rest.controller.v1;

/**
 * Composes an owner's customer code. Kept apart from {@link OwnerRestControllerV1} so the request
 * handler stays focused on orchestration while the rule for how a customer code is composed lives in
 * one place, alongside the other owner-field derivations. The controller supplies the region (derived
 * from the owner's postcode) and the identity fields; this helper assembles the code.
 *
 * <p>The code is formatted {@code <REGION>-<HASH8>}: REGION is the owner's region code (derived from
 * the postcode) and HASH8 is the first eight upper-case hex characters of the SHA-256 digest over the
 * owner's normalized telephone followed by its last name (for example {@code NSW-1A2B3C4D}).
 */
final class CustomerCode {

    /** Number of upper-case hex characters of the SHA-256 digest that form the HASH8 segment. */
    private static final int HASH_LENGTH = 8;

    private CustomerCode() {
    }

    /**
     * Assembles the customer code for an owner from its region and identity fields. The HASH8 segment
     * is the first eight upper-case hex characters of {@code SHA-256(normalizedTelephone + lastName)}.
     *
     * @param region              the owner's region code, forming the REGION segment
     * @param normalizedTelephone the owner's already-normalized telephone, hashed with the last name
     * @param lastName            the owner's last name, hashed with the normalized telephone
     * @return the assembled customer code
     */
    static String forRegionAndIdentity(String region, String normalizedTelephone, String lastName) {
        String hash8 = Sha256.hex(normalizedTelephone + lastName).substring(0, HASH_LENGTH).toUpperCase();
        return region + "-" + hash8;
    }

}
