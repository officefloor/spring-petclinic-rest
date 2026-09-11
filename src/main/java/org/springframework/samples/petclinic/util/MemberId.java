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

/**
 * Defines the layout of an owner's member id, formatted {@code "<REGION><FY><HASH8><CHK>"}: the
 * canonical region (see {@link LocalityResolver}), the 2-digit fiscal year of the registration date
 * (see {@link FiscalYear}), an 8 upper-hex-character hash of the owner's stable identity fields (see
 * {@link Sha256Hex}), and a single Luhn check digit (see {@link LuhnCheckDigit}) computed over the
 * decimal digits of {@code "<REGION><FY><HASH8>"}. The trailing segments are fixed-width — FY (2),
 * HASH8 (8) and CHK (1) — so each segment is recovered from the right regardless of the region's
 * length. Keeping the id's format in one place gathers its construction — done at creation time,
 * before any uniqueness suffix is applied — and the extraction of its segments — done on read — into
 * a single home, rather than spreading the segment widths across the controller that builds the id
 * and the mapper that reads it back.
 */
public final class MemberId {

    /** Separates a uniqueness suffix appended on a collision (e.g. {@code "NSW26A1B2C3D45-2"}). */
    private static final String SEPARATOR = "-";

    /** Width of the single Luhn check-digit (CHK) segment. */
    private static final int CHK_LENGTH = 1;

    /** Width of the identity-hash (HASH8) segment. */
    private static final int HASH8_LENGTH = 8;

    /** Width of the 2-digit fiscal-year (FY) segment. */
    private static final int FY_LENGTH = 2;

    private MemberId() {
    }

    /**
     * Formats the member-id core {@code "<REGION><FY><HASH8><CHK>"} from its segments, where CHK is
     * the single Luhn check digit over the decimal digits of {@code "<REGION><FY><HASH8>"}. This is
     * the id before any uniqueness suffix is appended for a collision.
     *
     * @param region the canonical region segment (e.g. {@code "NSW"})
     * @param fy     the 2-digit fiscal-year segment (e.g. {@code "26"})
     * @param hash8  the 8 upper-hex-character identity hash segment
     * @return the formatted member-id core, including its check digit
     */
    public static String format(String region, String fy, String hash8) {
        String core = region + fy + hash8;
        return core + LuhnCheckDigit.compute(core);
    }

    /**
     * Returns the REGION segment of a formatted member id: the text before the fixed-width
     * {@code FY + HASH8 + CHK} suffix, so neither those trailing segments nor a uniqueness suffix
     * (e.g. {@code "NSW26A1B2C3D45-2"}) affects the result. Returns {@code null} when
     * {@code memberId} is {@code null}.
     *
     * @param memberId a formatted member id, or {@code null}
     * @return the id's REGION segment, or {@code null} when {@code memberId} is {@code null}
     */
    public static String region(String memberId) {
        if (memberId == null) {
            return null;
        }
        String core = core(memberId);
        return core.substring(0, core.length() - (FY_LENGTH + HASH8_LENGTH + CHK_LENGTH));
    }

    /**
     * Returns the fiscal-year value carried in the FY segment of a formatted member id, formatted
     * {@code "FY<YY>"} (e.g. {@code "FY26"}), so a reader takes the fiscal year from the id itself.
     * A uniqueness suffix does not affect the result. Returns {@code null} when {@code memberId} is
     * {@code null}.
     *
     * @param memberId a formatted member id, or {@code null}
     * @return the fiscal-year label carried in the id, or {@code null} when {@code memberId} is
     *         {@code null}
     */
    public static String fiscalYear(String memberId) {
        if (memberId == null) {
            return null;
        }
        String core = core(memberId);
        int fyEnd = core.length() - (HASH8_LENGTH + CHK_LENGTH);
        return "FY" + core.substring(fyEnd - FY_LENGTH, fyEnd);
    }

    /**
     * Returns the member-id core (the part before any {@code "-<n>"} uniqueness suffix), so the
     * fixed-width segment offsets are taken over the core alone.
     *
     * @param memberId a formatted member id
     * @return the member-id core, without any uniqueness suffix
     */
    private static String core(String memberId) {
        int separator = memberId.indexOf(SEPARATOR);
        return separator < 0 ? memberId : memberId.substring(0, separator);
    }
}
