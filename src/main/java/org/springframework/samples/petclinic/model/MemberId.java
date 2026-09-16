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
package org.springframework.samples.petclinic.model;

/**
 * The owner's unified member id, formatted {@code '<REGION><FY><HASH8><CHK>'}: the
 * canonical region (see {@link RegionResolver}), the two-digit fiscal year (see
 * {@link FiscalYear}), the eight upper-case hexadecimal HASH8 of the region-and-hash
 * identity, and a single Luhn check digit (see {@link LuhnCheckDigit}) computed over the
 * digits of the preceding {@code <REGION><FY><HASH8>}.
 *
 * <p>Single source of truth for the member-id format, so callers build and read its
 * segments the same way and never repeat the layout. The region is a run of letters and
 * the fiscal year the two digits immediately after it, so the leading segments can be
 * read back even when a collision suffix is appended to the id.
 */
public final class MemberId {

    private MemberId() {
    }

    /**
     * Build a member id from its parts: the {@code <REGION><FY><HASH8>} body followed by
     * the Luhn check digit computed over that body's digits.
     *
     * @param region          the owner's canonical region
     * @param fiscalYearShort the two-digit fiscal year
     * @param hash8           the eight upper-case hex characters of the region-and-hash identity
     * @return the formatted member id, e.g. {@code "NSW261A2B3C4D5"}
     */
    public static String format(String region, int fiscalYearShort, String hash8) {
        String body = region + String.format("%02d", fiscalYearShort) + hash8;
        return body + LuhnCheckDigit.of(body);
    }

    /**
     * The region segment of a member id: its leading run of letters, which ends where the
     * two-digit fiscal year begins.
     */
    public static String regionOf(String memberId) {
        int end = 0;
        while (end < memberId.length() && Character.isLetter(memberId.charAt(end))) {
            end++;
        }
        return memberId.substring(0, end);
    }

    /**
     * The two-digit fiscal-year segment of a member id: the two digits immediately
     * following its {@link #regionOf(String) region}.
     */
    public static int fiscalYearShortOf(String memberId) {
        int start = regionOf(memberId).length();
        return Integer.parseInt(memberId.substring(start, start + 2));
    }
}
