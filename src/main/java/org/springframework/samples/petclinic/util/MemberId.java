package org.springframework.samples.petclinic.util;

import java.util.Locale;
import java.util.Set;

/**
 * The owner's member id, {@code '<REGION><FY><HASH8><CHK>'}: the owner's {@link OwnerRegion region},
 * the 2-digit {@link FiscalYear fiscal year} FY, an 8-character hash HASH8 that identifies the
 * person, and a single {@link Luhn} check digit CHK. HASH8 is the first 8 upper-case hex characters
 * of {@code SHA-256(normalizedTelephone + lastName)} (the same hash the region-and-hash identity
 * uses), so the same person in the same region and fiscal year always resolves to the same id
 * without any sequence number (e.g. {@code 'NSW271A2B3C4D5'}). CHK is the Luhn check digit computed
 * over the digits of {@code <REGION><FY><HASH8>}.
 */
public final class MemberId {

    private static final String SEPARATOR = "-";

    /** Number of leading hex characters of the digest kept as HASH8. */
    private static final int HASH_LENGTH = 8;

    /** Number of digits in the FY segment. */
    private static final int FISCAL_YEAR_LENGTH = 2;

    /** Length of the fixed core following the region: {@code FY(2) + HASH8(8) + CHK(1)}. */
    private static final int CORE_SUFFIX_LENGTH = FISCAL_YEAR_LENGTH + HASH_LENGTH + 1;

    private MemberId() {
    }

    /**
     * The member id for the given {@code region} and {@code fiscalYear} (identified by the calendar
     * year it ends in, e.g. {@code 2027}), hashing the person's {@code normalizedTelephone} and
     * {@code lastName} into HASH8 and appending the Luhn check digit over the preceding digits.
     */
    public static String of(String region, int fiscalYear, String normalizedTelephone, String lastName) {
        String body = region + String.format("%02d", fiscalYear % 100)
                + hash8(orEmpty(normalizedTelephone) + orEmpty(lastName));
        return body + Luhn.checkDigit(body);
    }

    /**
     * De-duplicates {@code memberId} against the ids already {@code taken}: if it is unused it is
     * returned unchanged, otherwise {@code '-<n>'} is appended with the smallest {@code n >= 2} that
     * yields an id not already taken (e.g. {@code 'NSW271A2B3C4D5'} → {@code 'NSW271A2B3C4D5-2'}).
     */
    public static String deduplicate(String memberId, Set<String> taken) {
        if (!taken.contains(memberId)) {
            return memberId;
        }
        for (int n = 2; ; n++) {
            String candidate = memberId + SEPARATOR + n;
            if (!taken.contains(candidate)) {
                return candidate;
            }
        }
    }

    /** The REGION segment of a member id (everything before the fixed {@code <FY><HASH8><CHK>} core). */
    public static String region(String memberId) {
        String core = beforeSuffix(memberId);
        return core.substring(0, core.length() - CORE_SUFFIX_LENGTH);
    }

    /** The FY segment (2-digit fiscal year) of a member id. */
    public static String fiscalYear(String memberId) {
        String core = beforeSuffix(memberId);
        int start = core.length() - CORE_SUFFIX_LENGTH;
        return core.substring(start, start + FISCAL_YEAR_LENGTH);
    }

    /** The member id without any {@code '-<n>'} de-duplication suffix. */
    private static String beforeSuffix(String memberId) {
        int separator = memberId.indexOf(SEPARATOR);
        return separator < 0 ? memberId : memberId.substring(0, separator);
    }

    private static String hash8(String source) {
        return Sha256.hex(source).substring(0, HASH_LENGTH).toUpperCase(Locale.ROOT);
    }

    private static String orEmpty(String value) {
        return value == null ? "" : value;
    }
}
