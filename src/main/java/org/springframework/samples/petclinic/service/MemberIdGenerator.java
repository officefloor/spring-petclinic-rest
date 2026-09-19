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

import org.springframework.samples.petclinic.model.FiscalYear;
import org.springframework.samples.petclinic.model.LuhnCheckDigit;
import org.springframework.samples.petclinic.model.Sha256Hex;
import org.springframework.stereotype.Component;

/**
 * Builds an owner's {@code memberId}, formatted {@code <REGION><FY><HASH8><CHK>}: the region code,
 * the two-digit fiscal year of the registration date, {@code HASH8} (the first eight upper-case
 * hexadecimal characters of the SHA-256 digest over the owner's normalized telephone followed by
 * their last name), and a single Luhn check digit computed over the digits of the
 * {@code <REGION><FY><HASH8>} prefix (e.g. {@code NSW261A2B3C4D7}).
 */
@Component
public class MemberIdGenerator {

    private static final int HASH_LENGTH = 8;

    /**
     * Build the member id for an owner.
     *
     * @param region           the owner's region code, forming the leading segment
     * @param telephone        the owner's normalized telephone; hashed together with the last name
     * @param lastName         the owner's last name; hashed together with the telephone
     * @param registrationDate the owner's registration date; its fiscal year supplies the two-digit FY segment
     * @return the formatted member id, e.g. {@code NSW261A2B3C4D7}
     */
    public String generate(String region, String telephone, String lastName, LocalDate registrationDate) {
        String hash8 = Sha256Hex.upperHexPrefix(telephone + lastName, HASH_LENGTH);
        String base = String.format("%s%02d%s", region, FiscalYear.of(registrationDate).getShortYear(), hash8);
        return base + LuhnCheckDigit.of(base);
    }
}
