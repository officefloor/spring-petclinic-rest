package org.springframework.samples.petclinic.util;

import java.time.LocalDate;
import java.util.Collection;

/**
 * The owner's unified member id — the single identity that replaces the former customerCode and
 * membershipNumber. It is formatted {@code <REGION><FY><HASH8><CHK>}:
 * <ul>
 * <li>REGION — the region derived from the owner's postcode (falling back to the city; see
 * {@link Locality}),</li>
 * <li>FY — the two-digit {@link FiscalYear fiscal year} of the registration date,</li>
 * <li>HASH8 — the first eight upper-case hex characters of SHA-256 over the normalized telephone
 * concatenated with the last name (see {@link Sha256}), the same hash the region-and-hash identity
 * used,</li>
 * <li>CHK — a single {@link CheckDigit Luhn check digit} computed over the digits of
 * {@code <REGION><FY><HASH8>}.</li>
 * </ul>
 * The identity carries no per-owner sequence, so it does not depend on other owners; the same region,
 * fiscal year, telephone and last name always yield the same id. Every value that keys off the
 * owner's identity — the locality (and, through it, the timezone and owner segment), the create audit
 * record — therefore shares this single id. Pure functions, so exposed as plain helpers.
 */
public final class MemberId {

    /** Number of hex characters in the hash portion of the id. */
    private static final int HASH_LENGTH = 8;

    /** Number of digits in the fiscal-year portion of the id. */
    private static final int FY_LENGTH = 2;

    /** Number of characters in the check-digit portion of the id. */
    private static final int CHECK_LENGTH = 1;

    /** Length of the fixed FY + HASH8 + CHK tail that follows the variable-length region. */
    private static final int TAIL_LENGTH = FY_LENGTH + HASH_LENGTH + CHECK_LENGTH;

    private MemberId() {
    }

    /**
     * Builds the member id {@code <REGION><FY><HASH8><CHK>}. The hash is taken over the normalized
     * telephone followed by the last name; a {@code null} telephone or last name contributes an empty
     * string. The check digit is the Luhn digit over the digits of the {@code <REGION><FY><HASH8>}
     * body.
     */
    public static String of(String region, LocalDate registrationDate, String normalizedTelephone,
            String lastName) {
        String fy = String.format("%02d", FiscalYear.of(registrationDate) % 100);
        String hash8 = Sha256.prefix(orEmpty(normalizedTelephone) + orEmpty(lastName), HASH_LENGTH);
        String body = region + fy + hash8;
        return body + CheckDigit.of(body);
    }

    /**
     * De-duplicates {@code candidate} against the {@code existing} member ids. Returns the candidate
     * unchanged when it does not collide; otherwise appends {@code -<n>} with the smallest {@code n} of
     * 2 or more that yields a value absent from {@code existing}.
     */
    public static String dedupe(String candidate, Collection<String> existing) {
        if (!existing.contains(candidate)) {
            return candidate;
        }
        for (int n = 2; ; n++) {
            String deduped = candidate + "-" + n;
            if (!existing.contains(deduped)) {
                return deduped;
            }
        }
    }

    /** The REGION portion of a member id, or {@code null} when the id is absent. */
    public static String regionOf(String memberId) {
        if (memberId == null) {
            return null;
        }
        String base = base(memberId);
        return base.length() > TAIL_LENGTH ? base.substring(0, base.length() - TAIL_LENGTH) : base;
    }

    /** The member id without any {@code -<n>} de-duplication suffix. */
    private static String base(String memberId) {
        int dash = memberId.indexOf('-');
        return dash < 0 ? memberId : memberId.substring(0, dash);
    }

    private static String orEmpty(String value) {
        return value == null ? "" : value;
    }
}
