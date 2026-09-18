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
package org.springframework.samples.petclinic.model;

import java.time.LocalDate;

/**
 * The owner member id, the single value that identifies an owner to the outside world,
 * formatted {@code <REGION><FY><HASH8><CHK>}:
 * <ul>
 * <li>{@code REGION} — the owner's region (derived from its postcode, falling back to its
 * city);</li>
 * <li>{@code FY} — the two-digit {@link FiscalYear fiscal year} the owner's registration date
 * falls in (the fiscal year starts on 1 July);</li>
 * <li>{@code HASH8} — the first 8 upper-case hex characters of the SHA-256 digest of the
 * owner's normalized telephone concatenated with its last name (the same value used by the
 * region-and-hash identity);</li>
 * <li>{@code CHK} — a single {@link Luhn} check digit over the digits of
 * {@code <REGION><FY><HASH8>}.</li>
 * </ul>
 * e.g. {@code "NSW261A2B3C4D9"}. A collision with an already-stored member id is
 * de-duplicated by appending {@code -<n>}; the fixed {@code FY}+{@code HASH8}+{@code CHK}
 * tail lets the region and fiscal-year segments be read back regardless.
 */
public final class MemberId {

    /** Number of leading hex characters of the identity hash kept in the member id. */
    private static final int HASH_LENGTH = 8;

    /** Number of characters of the two-digit fiscal-year segment. */
    private static final int FY_LENGTH = 2;

    /** Number of characters of the Luhn check-digit segment. */
    private static final int CHECK_LENGTH = 1;

    /** Length of the fixed {@code FY}+{@code HASH8}+{@code CHK} tail that follows the region. */
    private static final int TAIL_LENGTH = FY_LENGTH + HASH_LENGTH + CHECK_LENGTH;

    private MemberId() {
    }

    /**
     * The member id for an owner in {@code region}, registered on {@code registrationDate},
     * with the given normalized {@code telephone} and {@code lastName}. This is the base
     * value before any collision de-duplication.
     *
     * @param region           the owner's region ({@code REGION} segment)
     * @param registrationDate the owner's registration date (source of the {@code FY} segment)
     * @param telephone        the owner's normalized telephone (hashed into {@code HASH8})
     * @param lastName         the owner's last name (hashed into {@code HASH8})
     * @return the formatted member id
     */
    public static String of(String region, LocalDate registrationDate, String telephone, String lastName) {
        String fiscalYear = String.format("%0" + FY_LENGTH + "d", FiscalYear.startYearOf(registrationDate) % 100);
        String hash8 = Sha256Hex.prefix(telephone + lastName, HASH_LENGTH);
        String body = region + fiscalYear + hash8;
        return body + Luhn.checkDigit(body);
    }

    /**
     * The {@code REGION} segment of {@code memberId}: everything before the fixed
     * {@code FY}+{@code HASH8}+{@code CHK} tail, ignoring any {@code -<n>} de-duplication
     * suffix.
     *
     * @param memberId the member id to read
     * @return its region segment
     */
    public static String regionOf(String memberId) {
        String core = stripSuffix(memberId);
        return core.substring(0, core.length() - TAIL_LENGTH);
    }

    /**
     * The two-digit {@code FY} (fiscal-year) segment of {@code memberId}, ignoring any
     * {@code -<n>} de-duplication suffix.
     *
     * @param memberId the member id to read
     * @return its two-digit fiscal-year segment
     */
    public static String fiscalYearOf(String memberId) {
        String core = stripSuffix(memberId);
        int start = core.length() - TAIL_LENGTH;
        return core.substring(start, start + FY_LENGTH);
    }

    /** Strips the {@code -<n>} de-duplication suffix, if any, leaving the formatted core. */
    private static String stripSuffix(String memberId) {
        int dash = memberId.indexOf('-');
        return dash < 0 ? memberId : memberId.substring(0, dash);
    }
}
