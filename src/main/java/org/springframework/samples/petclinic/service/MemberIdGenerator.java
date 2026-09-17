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

import java.time.LocalDate;
import java.util.Locale;
import java.util.function.Predicate;

import org.springframework.samples.petclinic.rest.validation.LocalityResolver;
import org.springframework.samples.petclinic.util.FiscalYear;
import org.springframework.samples.petclinic.util.Luhn;
import org.springframework.samples.petclinic.util.OwnerIdentityVersion;
import org.springframework.samples.petclinic.util.Sha256;
import org.springframework.stereotype.Component;

/**
 * Builds an owner's {@code memberId}, formatted {@code <REGION><FY><HASH8><CHK>} where REGION is the
 * version-2 {@linkplain OwnerIdentityVersion#regionCode(String) region code} (the canonical region
 * derived from the owner's postcode with the version tag mixed in), FY is the two-digit fiscal year
 * of the registration date, HASH8 is the first eight upper-case hex characters of the SHA-256 of the
 * owner's normalized telephone concatenated with the last name, and CHK is a single Luhn check digit
 * computed over the decimal digits of {@code <REGION><FY><HASH8>} (e.g. {@code "NSWV2261A2B3C4D9"}).
 */
@Component
public class MemberIdGenerator {

    /** Number of leading hex characters of the SHA-256 digest kept as the identity hash. */
    private static final int HASH_LENGTH = 8;

    /**
     * Build the member id for the given postcode, registration date, normalized telephone and last
     * name.
     *
     * @param postcode            the owner's postcode; its region forms the leading segment
     * @param registrationDate    the owner's registration date; its fiscal year forms the FY segment
     * @param normalizedTelephone the owner's telephone in canonical (E.164) form; hashed with the
     *                            last name to form the HASH8 segment
     * @param lastName            the owner's last name; hashed with the telephone
     * @return the formatted member id
     */
    public String generate(String postcode, LocalDate registrationDate, String normalizedTelephone,
            String lastName) {
        String region = OwnerIdentityVersion.regionCode(LocalityResolver.resolveFromPostcode(postcode));
        String fiscalYear = FiscalYear.twoDigit(registrationDate);
        String hash = Sha256.hex(normalizedTelephone + lastName)
            .substring(0, HASH_LENGTH).toUpperCase(Locale.ROOT);
        String payload = region + fiscalYear + hash;
        return payload + Luhn.checkDigit(payload);
    }

    /**
     * Return a member id that no existing owner already carries. If {@code memberId} is free it is
     * returned unchanged; otherwise {@code "-<n>"} is appended using the smallest {@code n} of two or
     * more that yields an unused id.
     *
     * @param memberId the freshly generated id that may collide with an existing owner's id
     * @param isTaken  predicate reporting whether a candidate id is already in use
     * @return an id guaranteed not to collide with an existing owner's id
     */
    public String deduplicate(String memberId, Predicate<String> isTaken) {
        if (!isTaken.test(memberId)) {
            return memberId;
        }
        for (int n = 2; ; n++) {
            String candidate = memberId + "-" + n;
            if (!isTaken.test(candidate)) {
                return candidate;
            }
        }
    }
}
