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
package org.springframework.samples.petclinic.rest;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.springframework.samples.petclinic.util.FiscalYearResolver;
import org.springframework.samples.petclinic.util.LocalityResolver;
import org.springframework.samples.petclinic.util.LuhnCheckDigit;
import org.springframework.samples.petclinic.util.Sha256Hex;
import org.springframework.stereotype.Component;

/**
 * Builds an owner's member id, formatted {@code '<REGION><FY><HASH8><CHK>'}: the region code
 * derived from the postcode, the 2-digit fiscal year of the (business-day-adjusted) registration
 * date, the first 8 upper-case hex characters of SHA-256 over the normalized telephone concatenated
 * with the last name, and a single Luhn check digit computed over the digits of the preceding
 * {@code <REGION><FY><HASH8>} body, e.g. {@code 'NSW273F2A9C1E4'}.
 */
@Component
public class MemberIdGenerator {

    /** Separator introducing the numeric suffix a collision appends to an otherwise-taken id. */
    private static final char SEPARATOR = '-';

    /** Number of hex characters taken from the SHA-256 digest for the hash segment. */
    private static final int HASH_LENGTH = 8;

    /** Number of digits in the fiscal-year segment. */
    private static final int FISCAL_YEAR_LENGTH = 2;

    /** Number of characters in the trailing check-digit segment. */
    private static final int CHECK_LENGTH = 1;

    /** Combined length of the fixed trailing segments {@code <FY><HASH8><CHK>} after the region. */
    private static final int TRAILING_LENGTH = FISCAL_YEAR_LENGTH + HASH_LENGTH + CHECK_LENGTH;

    /**
     * Generate the member id for a new owner.
     *
     * @param region           the region code derived from the owner's postcode; forms the prefix
     * @param registrationDate the owner's (business-day-adjusted) registration date; its fiscal
     *                         year forms the {@code FY} segment
     * @param telephone        the owner's normalized telephone; hashed with the last name
     * @param lastName         the owner's last name; hashed with the telephone
     * @return the formatted member id
     */
    public String generate(String region, LocalDate registrationDate, String telephone, String lastName) {
        String body = region
            + String.format("%02d", FiscalYearResolver.yearOf(registrationDate) % 100)
            + Sha256Hex.upperHexPrefix(telephone + lastName, HASH_LENGTH);
        return body + LuhnCheckDigit.of(body);
    }

    /**
     * De-duplicate a member id against those already in use. When {@code baseId} does not collide it
     * is returned unchanged; otherwise {@code '-<n>'} is appended with the smallest {@code n} of 2 or
     * more that yields an id not present in {@code takenIds}.
     *
     * @param baseId   the member id produced by {@link #generate}
     * @param takenIds the member ids already assigned to existing owners
     * @return {@code baseId} when unique, otherwise the first available {@code '<baseId>-<n>'}
     */
    public String deduplicate(String baseId, Collection<String> takenIds) {
        Set<String> taken = new HashSet<>(takenIds);
        if (!taken.contains(baseId)) {
            return baseId;
        }
        for (int n = 2; ; n++) {
            String candidate = baseId + SEPARATOR + n;
            if (!taken.contains(candidate)) {
                return candidate;
            }
        }
    }

    /**
     * Extract the region segment from a member id, i.e. everything preceding the fixed trailing
     * {@code <FY><HASH8><CHK>} segments. Yields {@link LocalityResolver#UNKNOWN} when the id is
     * absent or carries no region segment.
     *
     * @param memberId a member id produced by {@link #generate}, or {@code null}
     * @return the region code embedded in {@code memberId}
     */
    public static String regionOf(String memberId) {
        String body = body(memberId);
        if (body == null || body.length() <= TRAILING_LENGTH) {
            return LocalityResolver.UNKNOWN;
        }
        return body.substring(0, body.length() - TRAILING_LENGTH);
    }

    /**
     * Extract the fiscal-year label from a member id, formatted {@code 'FY<YY>'} where {@code YY} is
     * the id's {@code FY} segment, e.g. {@code "FY27"}. Returns {@code null} when the id is absent or
     * too short to carry a fiscal-year segment.
     *
     * @param memberId a member id produced by {@link #generate}, or {@code null}
     * @return the fiscal-year label embedded in {@code memberId}
     */
    public static String fiscalYearOf(String memberId) {
        String body = body(memberId);
        if (body == null || body.length() < TRAILING_LENGTH) {
            return null;
        }
        int fyStart = body.length() - TRAILING_LENGTH;
        return "FY" + body.substring(fyStart, fyStart + FISCAL_YEAR_LENGTH);
    }

    /**
     * The member id stripped of any collision suffix, i.e. the {@code <REGION><FY><HASH8><CHK>} body
     * that carries the embedded segments. Returns {@code null} when {@code memberId} is {@code null}.
     */
    private static String body(String memberId) {
        if (memberId == null) {
            return null;
        }
        int suffix = memberId.indexOf(SEPARATOR);
        return suffix < 0 ? memberId : memberId.substring(0, suffix);
    }
}
