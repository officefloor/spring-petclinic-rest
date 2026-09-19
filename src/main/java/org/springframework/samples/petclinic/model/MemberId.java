package org.springframework.samples.petclinic.model;

import java.util.function.Predicate;

/**
 * The owner's member id, the single business identity every downstream value is derived from,
 * formatted {@code <REGION><FY><HASH8><CHK>} (no separators, e.g. {@code NSW261A2B3C4D5}):
 *
 * <ul>
 *   <li>REGION &mdash; the region code derived from the owner's postcode (falling back to its city,
 *       else {@link CityRegion#UNKNOWN}).</li>
 *   <li>FY &mdash; the two-digit fiscal year (starting 1 July) of the business-day-adjusted
 *       registration date (see {@link FiscalYear}).</li>
 *   <li>HASH8 &mdash; the first 8 upper-case hex characters of SHA-256 over the normalized telephone
 *       followed by the last name (the same hash used by the region-and-hash identity).</li>
 *   <li>CHK &mdash; a single {@link Luhn} check digit computed over the digits of
 *       {@code <REGION><FY><HASH8>}, guarding against transcription errors of the id.</li>
 * </ul>
 *
 * <p>The owner's {@link Owner#getLocality() locality} is the REGION read back out of it.
 */
public final class MemberId {

    /** Number of leading hex characters of the hash kept in the id. */
    private static final int HASH_LENGTH = 8;

    /** Fixed-width tail after REGION: FY (2) + HASH8 (8) + CHK (1). */
    private static final int TAIL_LENGTH = 2 + HASH_LENGTH + 1;

    private MemberId() {
    }

    /**
     * The member id for an owner in {@code region}, registered in fiscal year {@code fiscalYearTwoDigit}
     * (00&ndash;99), whose normalized telephone and last name are {@code telephone} and {@code lastName}.
     */
    public static String of(String region, int fiscalYearTwoDigit, String telephone, String lastName) {
        String hash8 = Sha256.hex(telephone + lastName).substring(0, HASH_LENGTH);
        String body = String.format("%s%02d%s", region, fiscalYearTwoDigit, hash8);
        return body + Luhn.checkDigit(body);
    }

    /**
     * A unique variant of {@code base}: {@code base} itself when {@code taken} reports it free,
     * otherwise {@code base-<n>} with the smallest {@code n >= 2} that {@code taken} reports free.
     * Used to de-duplicate a derived id against the ids already in use.
     */
    public static String deduplicate(String base, Predicate<String> taken) {
        if (!taken.test(base)) {
            return base;
        }
        for (int n = 2; ; n++) {
            String candidate = base + "-" + n;
            if (!taken.test(candidate)) {
                return candidate;
            }
        }
    }

    /**
     * The REGION portion of {@code memberId} &mdash; everything before its fixed-width
     * {@code <FY><HASH8><CHK>} tail (and before any de-duplication suffix) &mdash; or
     * {@link CityRegion#UNKNOWN} when the id is absent or carries no region.
     */
    public static String regionOf(String memberId) {
        if (memberId == null) {
            return CityRegion.UNKNOWN;
        }
        int dash = memberId.indexOf('-');
        String base = (dash < 0) ? memberId : memberId.substring(0, dash);
        return (base.length() <= TAIL_LENGTH) ? CityRegion.UNKNOWN : base.substring(0, base.length() - TAIL_LENGTH);
    }
}
