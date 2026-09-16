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

import java.time.LocalDate;
import java.util.Locale;

import org.springframework.samples.petclinic.model.FiscalYear;

/**
 * Derives an owner's member id, the single identifier that unifies the former customer code and
 * membership number. It is formatted {@code <REGION><FY><HASH8><CHK>} where:
 * <ul>
 *   <li>REGION is the owner's region code (derived from the postcode, see
 *       {@link org.springframework.samples.petclinic.model.RegionResolver RegionResolver});</li>
 *   <li>FY is the two last digits, zero-padded, of the fiscal year of the registration date (the
 *       same fiscal year exposed as the {@code FY<YY>} label);</li>
 *   <li>HASH8 is the first eight upper-case hexadecimal characters of the SHA-256 digest of the
 *       owner's normalized telephone concatenated with the last name — the region-and-hash
 *       identity's hash segment;</li>
 *   <li>CHK is a single Luhn check digit computed over the digits of {@code <REGION><FY><HASH8>}.</li>
 * </ul>
 * For example {@code NSW261A2B3C4D5}. The id carries no sequence number, so it is stable for a given
 * region, fiscal year and (telephone, last name) pair.
 */
public final class MemberIdGenerator {

    /** Number of leading hex characters of the SHA-256 digest kept as the id's hash segment. */
    private static final int HASH_LENGTH = 8;

    private MemberIdGenerator() {
    }

    /**
     * Builds the member id for a new owner.
     *
     * @param region              the owner's region code
     * @param normalizedTelephone the owner's telephone in its normalized storage form
     * @param lastName            the owner's last name
     * @param registrationDate    the owner's registration date
     * @return the member id, e.g. {@code "NSW261A2B3C4D5"}
     */
    public static String generate(String region, String normalizedTelephone, String lastName,
                                  LocalDate registrationDate) {
        String hash8 = Sha256.hex(orEmpty(normalizedTelephone) + orEmpty(lastName))
            .substring(0, HASH_LENGTH).toUpperCase(Locale.ROOT);
        String fy = String.format("%02d", FiscalYear.of(registrationDate) % 100);
        String base = region + fy + hash8;
        return base + LuhnCheckDigit.of(base);
    }

    /**
     * Return the REGION segment of a member id: its leading run of non-digit characters, since the
     * region code is alphabetic and the following FY segment always begins with a digit.
     *
     * @param memberId a member id produced by {@link #generate}
     * @return the region segment
     */
    public static String regionOf(String memberId) {
        int end = 0;
        while (end < memberId.length() && !Character.isDigit(memberId.charAt(end))) {
            end++;
        }
        return memberId.substring(0, end);
    }

    private static String orEmpty(String value) {
        return value == null ? "" : value;
    }
}
