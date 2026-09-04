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

package org.springframework.samples.petclinic.util;

import java.time.LocalDate;

/**
 * Derivation of an owner's {@code memberId}, the single unified identity every other owner value is
 * now built from. It replaces the previously separate {@code customerCode} and {@code
 * membershipNumber}. Kept separate from the owner controller and the {@code Owner} model so the rule
 * lives in one place, as pure functions with no web or persistence dependencies. Mirrors
 * {@link LocalityResolver} (which supplies the region), {@link FiscalYear} (the fiscal year) and
 * {@link LuhnCheckDigit} (the check digit).
 *
 * <p>The id is {@code '<REGION><FY><HASH8><CHK>'} (no separators): REGION is the region derived from
 * the owner's postcode (see {@link LocalityResolver#resolveFromPostcode}); FY is the two-digit fiscal
 * year the owner's registration date falls in (see {@link FiscalYear#yearOf}); HASH8 is the first
 * eight upper-case hexadecimal characters of the SHA-256 digest of the owner's normalized telephone
 * concatenated with the last name (the region-and-hash identity's hash component); and CHK is a
 * single Luhn check digit computed over the digits of {@code <REGION><FY><HASH8>}. On a collision
 * with an existing owner's id, {@code '-<n>'} is appended (handled by the service), so the REGION and
 * FY components are always the leading run of letters followed by the two fiscal-year digits.
 */
public abstract class MemberId {

    /** Number of leading hex characters of the SHA-256 digest kept as the HASH8 component. */
    private static final int HASH_LENGTH = 8;

    /** Number of digits in the fiscal-year (FY) component. */
    private static final int FY_LENGTH = 2;

    /**
     * Build a member id {@code '<REGION><FY><HASH8><CHK>'} from an owner's region, registration date,
     * telephone and last name.
     *
     * @param region           the region code (e.g. {@code NSW}, or {@code UNKNOWN})
     * @param registrationDate the owner's business-day-adjusted registration date (must not be null)
     * @param telephone        the owner's normalized telephone, or null
     * @param lastName         the owner's last name, or null
     * @return the member id
     */
    public static String of(String region, LocalDate registrationDate, String telephone, String lastName) {
        String base = region + fyDigits(registrationDate) + hash8(telephone, lastName);
        return base + LuhnCheckDigit.of(base);
    }

    /**
     * The HASH8 component alone: the first eight upper-case hex characters of the SHA-256 digest of the
     * owner's normalized telephone concatenated with the last name, each part treated as the empty
     * string when absent. This is the region-and-hash identity's hash component.
     *
     * @param telephone the owner's normalized telephone, or null
     * @param lastName  the owner's last name, or null
     * @return the eight-character upper-case hex HASH8
     */
    public static String hash8(String telephone, String lastName) {
        String telephonePart = telephone == null ? "" : telephone;
        String lastNamePart = lastName == null ? "" : lastName;
        return Sha256.hex(telephonePart + lastNamePart).substring(0, HASH_LENGTH).toUpperCase();
    }

    /**
     * The two-digit fiscal-year (FY) component for a date: the last two digits of the fiscal year the
     * date falls in, zero-padded (see {@link FiscalYear#yearOf}).
     */
    private static String fyDigits(LocalDate date) {
        return String.format("%02d", FiscalYear.yearOf(date) % 100);
    }

    /**
     * The region (the leading run of letters) of a member id, or {@code null} when the id is
     * {@code null}. This is how the owner's locality is now derived from its identity. The region is
     * followed by the two fiscal-year digits, so it ends at the first digit.
     *
     * @param memberId a member id, or null
     * @return the region component, or null
     */
    public static String region(String memberId) {
        if (memberId == null) {
            return null;
        }
        int i = 0;
        while (i < memberId.length() && !Character.isDigit(memberId.charAt(i))) {
            i++;
        }
        return memberId.substring(0, i);
    }

    /**
     * The owner's {@code FY<YY>} fiscal-year label derived from a member id, or {@code null} when the id
     * is {@code null} or too short to carry the FY component. {@code YY} is the two fiscal-year digits
     * that follow the {@linkplain #region region} in the id.
     *
     * @param memberId a member id, or null
     * @return the fiscal-year label (e.g. {@code FY27}), or null
     */
    public static String fiscalYearLabel(String memberId) {
        if (memberId == null) {
            return null;
        }
        int regionLength = region(memberId).length();
        if (memberId.length() < regionLength + FY_LENGTH) {
            return null;
        }
        return "FY" + memberId.substring(regionLength, regionLength + FY_LENGTH);
    }

}
