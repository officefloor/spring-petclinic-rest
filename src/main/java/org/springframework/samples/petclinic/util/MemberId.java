package org.springframework.samples.petclinic.util;

import java.time.LocalDate;
import java.util.Locale;
import java.util.Set;

/**
 * The owner's unified member id, formatted {@code <REGION><FY><HASH8><CHK>}: the version-2
 * {@link Postcodes#identityRegionCode identity region code} derived from the postcode, the
 * two-digit fiscal year of the registration date (see {@link FiscalYears}), the first eight
 * upper-case hex characters of the SHA-256 digest over the owner's normalized telephone followed
 * by their last name, and finally a single Luhn check digit computed over the digits of the
 * preceding {@code <REGION><FY><HASH8>}. This class owns the format; the user-facing derived
 * fields (locality, fiscal year) are computed from the owner's plain region and registration date,
 * not parsed back out of the identifier.
 */
public final class MemberId {

    /** Number of leading hex characters of the digest kept as the identity's hash component. */
    private static final int HASH_LENGTH = 8;

    /** Separator introducing the {@code -<n>} suffix a collision appends. */
    private static final char SEPARATOR = '-';

    private MemberId() {
    }

    /**
     * Build the member id {@code <REGION><FY><HASH8><CHK>} for an owner in {@code region}, whose
     * fiscal year is taken from {@code registrationDate} and whose normalized telephone and last
     * name hash to {@code HASH8}. {@code CHK} is the Luhn check digit over the digits of the
     * preceding segments.
     */
    public static String of(String region, LocalDate registrationDate, String normalizedTelephone,
            String lastName) {
        String fy = FiscalYears.yearOfCentury(registrationDate);
        String hash8 = Sha256.hex(normalizedTelephone + lastName)
                .substring(0, HASH_LENGTH).toUpperCase(Locale.ROOT);
        String base = region + fy + hash8;
        return base + Luhn.checkDigit(base);
    }

    /**
     * De-duplicate {@code memberId} against the ids already {@code taken}. When it is free the id
     * is returned unchanged; otherwise {@code -<n>} is appended with the smallest {@code n} of 2
     * or more that yields an id no owner already holds.
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
}
