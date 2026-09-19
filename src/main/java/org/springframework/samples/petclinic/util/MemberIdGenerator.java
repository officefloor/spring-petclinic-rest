/*
 * Copyright 2002-2013 the original author or authors.
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

package org.springframework.samples.petclinic.util;

import java.time.LocalDate;
import java.util.function.Predicate;

/**
 * Builds and parses an owner's {@code memberId}, the single unified identity formatted as
 * {@code "<REGION><FY><HASH8><CHK>"} where:
 * <ul>
 * <li>{@code REGION} is the owner's canonical region (for example {@code "NSW"}),</li>
 * <li>{@code FY} is the two-digit {@link FiscalYear fiscal year} of the registration date,</li>
 * <li>{@code HASH8} is the first eight upper-case hex characters of
 * {@code SHA-256(normalizedTelephone + lastName)} (the region-and-hash identity), and</li>
 * <li>{@code CHK} is a single {@link LuhnCheckDigit Luhn check digit} over the digits of
 * {@code <REGION><FY><HASH8>}.</li>
 * </ul>
 */
public final class MemberIdGenerator {

    /** Separator introducing the numeric suffix appended when de-duplicating a member id. */
    private static final String SEPARATOR = "-";

    /** Number of upper-case hex characters kept from the digest. */
    private static final int HASH_LENGTH = 8;

    private MemberIdGenerator() {
    }

    /**
     * Format a member id as {@code "<REGION><FY><HASH8><CHK>"} (for example
     * {@code "NSW261A2B3C4D7"}).
     *
     * @param region              the owner's canonical region
     * @param registrationDate    the owner's registration date, whose fiscal year supplies {@code FY}
     * @param normalizedTelephone the owner's normalized (E.164) telephone
     * @param lastName            the owner's last name
     * @return the formatted member id
     */
    public static String format(String region, LocalDate registrationDate, String normalizedTelephone,
            String lastName) {
        String base = region + FiscalYear.twoDigit(registrationDate) + hash8(normalizedTelephone, lastName);
        return base + LuhnCheckDigit.compute(base);
    }

    /**
     * De-duplicate a formatted member id against the ids already in use. When {@code baseMemberId}
     * is free it is returned unchanged; otherwise {@code "-<n>"} is appended using the smallest
     * {@code n >= 2} that yields an id {@code isTaken} reports as free.
     *
     * @param baseMemberId the freshly formatted member id (see {@link #format})
     * @param isTaken      tests whether a candidate id already belongs to another owner
     * @return a member id that {@code isTaken} reports as free
     */
    public static String deduplicate(String baseMemberId, Predicate<String> isTaken) {
        if (!isTaken.test(baseMemberId)) {
            return baseMemberId;
        }
        for (int n = 2; ; n++) {
            String candidate = baseMemberId + SEPARATOR + n;
            if (!isTaken.test(candidate)) {
                return candidate;
            }
        }
    }

    /**
     * Extract the {@code REGION} prefix from a member id produced by {@link #format}: its leading
     * run of letters, which precedes the numeric fiscal-year segment.
     *
     * @param memberId the member id, may be {@code null}
     * @return the region prefix, or {@code null} when the member id is {@code null}
     */
    public static String region(String memberId) {
        if (memberId == null) {
            return null;
        }
        int end = 0;
        while (end < memberId.length() && Character.isLetter(memberId.charAt(end))) {
            end++;
        }
        return memberId.substring(0, end);
    }

    private static String hash8(String normalizedTelephone, String lastName) {
        String key = orEmpty(normalizedTelephone) + orEmpty(lastName);
        return Sha256.hex(key).substring(0, HASH_LENGTH).toUpperCase();
    }

    private static String orEmpty(String value) {
        return value == null ? "" : value;
    }
}
