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

import org.springframework.samples.petclinic.mapper.LuhnCheckDigit;

/**
 * Composes an owner's unified member id. Kept apart from {@link OwnerRestControllerV1} so the request
 * handler stays focused on orchestration while the rule for how a member id is composed lives in one
 * place, alongside the other owner-field derivations. The controller supplies the region (derived from
 * the owner's postcode), the two-digit fiscal year and the identity fields; this helper assembles the id.
 *
 * <p>The id is formatted {@code <REGION><FY><HASH8><CHK>}: REGION is the owner's region code (derived
 * from the postcode), FY is the two-digit fiscal year of the owner's registration date, HASH8 is the
 * first eight upper-case hex characters of the SHA-256 digest over the owner's normalized telephone
 * followed by its last name, and CHK is a single Luhn check digit over the digits of
 * {@code <REGION><FY><HASH8>} (for example {@code NSW261A2B3C4D3}).
 */
final class MemberId {

    /** Number of upper-case hex characters of the SHA-256 digest that form the HASH8 segment. */
    private static final int HASH_LENGTH = 8;

    private MemberId() {
    }

    /**
     * Assembles the member id for an owner from its region, fiscal year and identity fields. The HASH8
     * segment is the first eight upper-case hex characters of {@code SHA-256(normalizedTelephone +
     * lastName)}, and the trailing CHK is the Luhn check digit over the digits of the assembled
     * {@code <REGION><FY><HASH8>}.
     *
     * @param region              the owner's region code, forming the REGION segment
     * @param fiscalYearTwoDigit  the two-digit fiscal year of the owner's registration date (0-99)
     * @param normalizedTelephone the owner's already-normalized telephone, hashed with the last name
     * @param lastName            the owner's last name, hashed with the normalized telephone
     * @return the assembled member id
     */
    static String forRegionYearAndIdentity(String region, int fiscalYearTwoDigit,
                                           String normalizedTelephone, String lastName) {
        String base = region + String.format("%02d", fiscalYearTwoDigit) + hash8(normalizedTelephone, lastName);
        return base + LuhnCheckDigit.forDigits(base);
    }

    /**
     * Computes the HASH8 segment of an owner's member id: the first {@value #HASH_LENGTH} upper-case hex
     * characters of {@code SHA-256(normalizedTelephone + lastName)}. Exposed on its own, rather than only
     * being inlined into {@link #forRegionYearAndIdentity}, so the single definition of the identity hash
     * is reused wherever the id's hash segment is needed rather than being re-derived.
     *
     * @param normalizedTelephone the owner's already-normalized telephone, hashed with the last name
     * @param lastName            the owner's last name, hashed with the normalized telephone
     * @return the HASH8 segment, as {@value #HASH_LENGTH} upper-case hex characters
     */
    static String hash8(String normalizedTelephone, String lastName) {
        return Sha256.hex(normalizedTelephone + lastName).substring(0, HASH_LENGTH).toUpperCase();
    }

}
