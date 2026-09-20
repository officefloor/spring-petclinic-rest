package org.springframework.samples.petclinic.util;

import java.util.Collection;

/**
 * The owner member-id format, {@code <REGION><FY><HASH8><CHK>}: the canonical region (see
 * {@link CityLocality}), the two-digit {@link FiscalYear fiscal year}, the first eight
 * upper-case hex characters of a SHA-256 fingerprint, and a single {@link LuhnCheckDigit
 * Luhn check digit} computed over the digits of {@code <REGION><FY><HASH8>} (e.g.
 * {@code NSW261A2B3C4D7}). This class owns the format so the two concerns that depend on
 * it — composing the member-id at creation and reading the region back out for locality —
 * stay in step.
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

    /**
     * The region component of a member-id, i.e. the leading letters before the fiscal-year
     * digits. Robust to the {@code -<n>} de-duplication suffix, which sits after the region.
     *
     * @param memberId a member-id, or {@code null}
     * @return the region, or {@code null} when the id is absent or has no region part
     */
    public static String regionOf(String memberId) {
        if (memberId == null) {
            return null;
        }
        int end = 0;
        while (end < memberId.length() && !Character.isDigit(memberId.charAt(end))) {
            end++;
        }
        return end > 0 ? memberId.substring(0, end) : null;
    }
}
