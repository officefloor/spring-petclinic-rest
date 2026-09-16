package org.springframework.samples.petclinic.model;

import java.util.function.Predicate;

/**
 * The single definition of an owner's member id: {@code <REGION><FY><HASH8><CHK>} where REGION is
 * the version-2 region — the region derived from the owner's postcode prefixed with the
 * {@link IdentityVersion#TAG} version tag — FY the two-digit fiscal year of the owner's
 * registration date (see {@link FiscalYear}), HASH8 the first 8 upper-case hex characters of
 * SHA-256 over the owner's normalized telephone plus last name (the same digest used by the
 * region-and-hash identity), and CHK a single Luhn check digit (see {@link CheckDigit}) over the
 * digits of {@code <REGION><FY><HASH8>}. Building the id and reading its region back both live here
 * so the format is defined in one place.
 */
public final class MemberId {

    /** Number of leading hex characters of the telephone-and-name digest kept in the id. */
    private static final int HASH_LENGTH = 8;

    /** Trailing characters after REGION: FY (2) + HASH8 (8) + CHK (1). */
    private static final int SUFFIX_LENGTH = 2 + HASH_LENGTH + 1;

    private static final String SEPARATOR = "-";

    private MemberId() {
    }

    /**
     * The member id for {@code owner}. The owner's telephone must already be normalized and its
     * registration date resolved (see the create pipeline), since they feed the hash and the
     * fiscal year.
     *
     * @param owner the owner whose identity to derive
     * @return the {@code <REGION><FY><HASH8><CHK>} member id
     */
    public static String forOwner(Owner owner) {
        String region = IdentityVersion.TAG + Locality.regionForPostcode(owner.getPostcode());
        String hash = Sha256.upperHex(owner.getTelephone() + owner.getLastName(), HASH_LENGTH);
        String base = String.format("%s%02d%s", region, FiscalYear.of(owner.getRegistrationDate()) % 100, hash);
        return base + CheckDigit.luhn(base);
    }

    /**
     * De-duplicates a member id against the ids already in use. Returns {@code memberId} unchanged
     * when it is not taken; otherwise appends {@code -<n>} with the smallest {@code n} of 2 or more
     * that yields an id not reported as taken.
     *
     * @param memberId the base {@code <REGION><FY><HASH8><CHK>} id
     * @param taken tells whether a candidate id is already in use
     * @return an id not reported as taken by {@code taken}
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
     * The REGION segment of a member id: everything ahead of the trailing FY, HASH8 and CHK
     * (and ahead of any de-duplication suffix).
     *
     * @param memberId the member id (may be {@code null})
     * @return the leading region, or {@code null} when the id is null
     */
    public static String region(String memberId) {
        if (memberId == null) {
            return null;
        }
        int separator = memberId.indexOf(SEPARATOR);
        String base = separator < 0 ? memberId : memberId.substring(0, separator);
        return base.length() <= SUFFIX_LENGTH ? base : base.substring(0, base.length() - SUFFIX_LENGTH);
    }
}
