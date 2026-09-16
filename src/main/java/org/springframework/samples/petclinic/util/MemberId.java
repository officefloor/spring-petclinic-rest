package org.springframework.samples.petclinic.util;

import java.util.Locale;
import java.util.function.Predicate;

/**
 * The owner's unified member id, formatted {@code <REGION><FY><HASH8><CHK>}: the owner's
 * region (see {@link OwnerRegion}), the 2-digit fiscal year of its registration date (see
 * {@link FiscalYear#shortLabel(java.time.LocalDate)}), the first 8 upper-case hex
 * characters of SHA-256 over the normalized telephone concatenated with the last name (the
 * region-and-hash identity's HASH8), and a single Luhn check digit over the digits of
 * {@code <REGION><FY><HASH8>} (e.g. {@code NSW261A2B3C4D5}).
 *
 * <p>This is the single source of an owner's identity: the audit record is built from it,
 * and both the locality (the REGION read back out) and the fiscal year (the FY read back
 * out) are derived from it, so there is one identity concept rather than several
 * derivations that could drift.
 */
public final class MemberId {

    /** Separator introducing the de-duplication suffix; never appears in a base member id. */
    private static final String SEPARATOR = "-";

    /** Number of leading hex characters of the digest kept as the HASH8 segment. */
    private static final int HASH_LENGTH = 8;

    /** Number of characters in the 2-digit fiscal-year segment. */
    private static final int FY_LENGTH = 2;

    /** Number of characters in the Luhn check-digit segment. */
    private static final int CHECK_LENGTH = 1;

    /** Fixed-width tail after the variable-width REGION: {@code <FY><HASH8><CHK>}. */
    private static final int TAIL_LENGTH = FY_LENGTH + HASH_LENGTH + CHECK_LENGTH;

    private MemberId() {
    }

    /**
     * The {@code <REGION><FY><HASH8><CHK>} member id for the given region, fiscal year and
     * identity inputs.
     *
     * @param region         the owner's region, used verbatim as the REGION segment
     * @param fiscalYearShort the 2-digit fiscal year of the registration date (e.g. "26")
     * @param telephone      the owner's normalized (E.164) telephone
     * @param lastName       the owner's last name
     */
    public static String of(String region, String fiscalYearShort, String telephone, String lastName) {
        String base = region + fiscalYearShort + hash8(telephone, lastName);
        return base + Luhn.checkDigit(base);
    }

    /**
     * A member id unique among the already-taken ids: the given id itself when it does not
     * collide, otherwise {@code <memberId>-<n>} with the smallest {@code n} of 2 or more
     * that is not taken.
     *
     * @param memberId the computed {@code <REGION><FY><HASH8><CHK>} id
     * @param taken    tests whether an id is already in use by an existing owner
     */
    public static String deduplicate(String memberId, Predicate<String> taken) {
        if (!taken.test(memberId)) {
            return memberId;
        }
        for (int n = 2; ; n++) {
            String candidate = memberId + SEPARATOR + n;
            if (!taken.test(candidate)) {
                return candidate;
            }
        }
    }

    /**
     * The REGION segment of a member id: the leading characters before the fixed-width
     * {@code <FY><HASH8><CHK>} tail (any de-duplication suffix stripped first), or
     * {@code null} when the owner has no member id.
     */
    public static String regionOf(String memberId) {
        String base = base(memberId);
        return base == null ? null : base.substring(0, base.length() - TAIL_LENGTH);
    }

    /**
     * The 2-digit fiscal-year segment of a member id (e.g. "26"), or {@code null} when the
     * owner has no member id.
     */
    public static String fiscalYearShortOf(String memberId) {
        String base = base(memberId);
        return base == null ? null
                : base.substring(base.length() - TAIL_LENGTH, base.length() - TAIL_LENGTH + FY_LENGTH);
    }

    /** The member id with any de-duplication suffix removed, or {@code null} when absent. */
    private static String base(String memberId) {
        if (memberId == null) {
            return null;
        }
        int separator = memberId.indexOf(SEPARATOR);
        return separator < 0 ? memberId : memberId.substring(0, separator);
    }

    /** First {@value #HASH_LENGTH} upper-case hex characters of SHA-256(telephone + lastName). */
    private static String hash8(String telephone, String lastName) {
        return Sha256.hex(telephone + lastName).substring(0, HASH_LENGTH).toUpperCase(Locale.ROOT);
    }
}
