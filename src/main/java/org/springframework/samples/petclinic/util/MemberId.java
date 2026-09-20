package org.springframework.samples.petclinic.util;

import java.util.Collection;

/**
 * The owner member-id format, {@code <REGION><FY><HASH8><CHK>}: the canonical region (see
 * {@link CityLocality}), the two-digit {@link FiscalYear fiscal year}, the first eight
 * upper-case hex characters of a SHA-256 fingerprint, and a single {@link LuhnCheckDigit
 * Luhn check digit} computed over the digits of {@code <REGION><FY><HASH8>} (e.g.
 * {@code V2NSW261A2B3C4D7}). This class owns the format so member-id composition at creation
 * has a single home. The {@code <REGION>} component is the version-2 region code (see
 * {@link OwnerIdentityVersion#regionCode(String)}); the user-facing region is derived
 * independently from the stored postcode and city, so the version tag never leaks into it.
 */
public final class MemberId {

    private static final char DEDUP_SEPARATOR = '-';

    private MemberId() {
    }

    /**
     * Composes a member-id from its region, fiscal-year and hash components, appending the
     * Luhn check digit computed over the digits of {@code <REGION><FY><HASH8>}.
     *
     * @param region          the canonical region
     * @param shortFiscalYear the two-digit fiscal year (0-99)
     * @param hash8           the eight-character hash
     * @return the {@code <REGION><FY><HASH8><CHK>} member-id
     */
    public static String of(String region, int shortFiscalYear, String hash8) {
        String body = String.format("%s%02d%s", region, shortFiscalYear, hash8);
        return body + LuhnCheckDigit.of(body);
    }

    /**
     * De-duplicates a member-id against the ids already in use. If the id is unused it is
     * returned unchanged; otherwise {@code -<n>} is appended with the smallest {@code n} of
     * two or more that yields an id not already taken.
     *
     * @param memberId the composed member-id
     * @param existing the member-ids already in use
     * @return the {@code memberId}, or a {@code <memberId>-<n>} variant unique among
     *         {@code existing}
     */
    public static String deduplicate(String memberId, Collection<String> existing) {
        String candidate = memberId;
        for (int n = 2; existing.contains(candidate); n++) {
            candidate = memberId + DEDUP_SEPARATOR + n;
        }
        return candidate;
    }
}
