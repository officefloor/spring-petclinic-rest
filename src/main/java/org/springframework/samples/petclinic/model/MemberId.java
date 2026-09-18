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

import org.springframework.samples.petclinic.util.Luhn;
import org.springframework.samples.petclinic.util.Sha256;

/**
 * The owner member-id format {@code '<REGION><FY><HASH8><CHK>'}: the owner's canonical region, the
 * two-digit fiscal year of its registration date, {@code HASH8} — the first 8 upper-case hex
 * characters of SHA-256 over the owner's normalized telephone concatenated with its last name — and
 * a single Luhn {@code CHK} check digit computed over the digits of {@code '<REGION><FY><HASH8>'}.
 *
 * <p>The member id is a stable identity derived purely from owner fields, with no sequence numbers,
 * so every value the application builds from it (the {@linkplain #fiscalYearSegmentOf(String) fiscal
 * year} and the owner's {@linkplain #regionOf(String) locality}) follows from the same identity.
 * This class owns that format: it builds a member id and reads its region and fiscal-year segments
 * back. The region, fiscal year and check-digit segments carry no separators, so the region and
 * fiscal year are read back by their fixed offsets from the end of the identity; a {@code '-<n>'}
 * suffix appended to de-duplicate a colliding id is the only separator that ever appears.
 */
public final class MemberId {

    /** Number of leading hex characters of the digest kept as {@code HASH8}. */
    private static final int HASH_LENGTH = 8;

    /** Number of digits in the two-digit fiscal-year segment. */
    private static final int FISCAL_YEAR_LENGTH = 2;

    /** Number of Luhn check digits appended as {@code CHK}. */
    private static final int CHECK_LENGTH = 1;

    /** Combined length of the {@code '<FY><HASH8><CHK>'} part that trails the region. */
    private static final int TRAILING_LENGTH = FISCAL_YEAR_LENGTH + HASH_LENGTH + CHECK_LENGTH;

    /** Separator introduced only to disambiguate a colliding member id. */
    private static final char COLLISION_SEPARATOR = '-';

    private MemberId() {
    }

    /**
     * Build the {@code '<REGION><FY><HASH8><CHK>'} member id for the given region and identity inputs.
     *
     * @param region              the owner's canonical region
     * @param fiscalYearSegment   the two-digit segment of the owner's registration fiscal year
     * @param normalizedTelephone the owner's normalized (E.164) telephone
     * @param lastName            the owner's last name
     * @return the member id
     */
    public static String of(String region, int fiscalYearSegment, String normalizedTelephone, String lastName) {
        String base = region
            + String.format("%0" + FISCAL_YEAR_LENGTH + "d", fiscalYearSegment)
            + Sha256.hexPrefix(normalizedTelephone + lastName, HASH_LENGTH);
        return base + Luhn.checkDigit(base);
    }

    /**
     * Return the region component of a member id — everything preceding the fixed-length
     * {@code '<FY><HASH8><CHK>'} tail, ignoring any {@code '-<n>'} de-duplication suffix.
     *
     * @param memberId the member id (may be {@code null})
     * @return the region, or {@code null} when {@code memberId} is {@code null} or too short to carry one
     */
    public static String regionOf(String memberId) {
        String identity = identity(memberId);
        if (identity == null || identity.length() <= TRAILING_LENGTH) {
            return null;
        }
        return identity.substring(0, identity.length() - TRAILING_LENGTH);
    }

    /**
     * Return the two-digit fiscal-year segment of a member id.
     *
     * @param memberId the member id (may be {@code null})
     * @return the fiscal-year segment, or {@code null} when {@code memberId} is {@code null} or too
     * short to carry one
     */
    public static Integer fiscalYearSegmentOf(String memberId) {
        String identity = identity(memberId);
        if (identity == null || identity.length() <= TRAILING_LENGTH) {
            return null;
        }
        int start = identity.length() - TRAILING_LENGTH;
        return Integer.parseInt(identity.substring(start, start + FISCAL_YEAR_LENGTH));
    }

    /**
     * The identity portion of a member id: the part before any {@code '-<n>'} de-duplication suffix.
     */
    private static String identity(String memberId) {
        if (memberId == null) {
            return null;
        }
        int separator = memberId.indexOf(COLLISION_SEPARATOR);
        return separator < 0 ? memberId : memberId.substring(0, separator);
    }
}
