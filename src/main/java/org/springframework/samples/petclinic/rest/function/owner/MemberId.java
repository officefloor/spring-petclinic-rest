package org.springframework.samples.petclinic.rest.function.owner;

/**
 * The single source of truth for an owner's unified {@code memberId}, formatted
 * {@code <REGION><FY><HASH8><CHK>}: REGION is the region code derived from the postcode
 * (NSW/VIC/QLD, else {@code UNKNOWN}), FY the two-digit fiscal year, HASH8 the first 8 upper-case
 * hex characters of SHA-256 over {@code normalizedTelephone + lastName} (the same HASH8 the
 * region-and-hash identity uses — see {@link OwnerIdentity#customerHash}), and CHK a single Luhn
 * check digit over the decimal digits of {@code <REGION><FY><HASH8>}.
 *
 * <p>{@link #of} assembles the id from its parts when an owner is created; {@link #regionOf} and
 * {@link #fiscalYearCodeOf} read the REGION and FY segments back out of an assigned id, which is how
 * the owner's locality, timezone and fiscal year all resolve straight off the memberId. A collision
 * with an existing owner's memberId is de-duplicated by appending {@code -<n>} (see
 * {@link AssignMemberId}); the suffix is not part of the segments the readers below extract.
 */
public final class MemberId {

    /** The fixed-width tail after the REGION prefix: FY (2) + HASH8 (8) + CHK (1). */
    private static final int TAIL_LENGTH = 11;

    private MemberId() {
    }

    /**
     * The base memberId {@code <REGION><FY><HASH8><CHK>} assembled from its parts (before any
     * collision suffix): CHK is the Luhn check digit over the decimal digits of
     * {@code region + fiscalYearCode + hash8}.
     */
    public static String of(String region, String fiscalYearCode, String hash8) {
        String core = region + fiscalYearCode + hash8;
        return core + OwnerIdentity.luhnCheckDigit(core);
    }

    /**
     * The REGION prefix of {@code memberId} — one of the known regions (NSW, VIC, QLD), else
     * {@code UNKNOWN} (which also covers an absent or malformed id).
     */
    public static String regionOf(String memberId) {
        if (memberId != null) {
            for (String region : OwnerRegion.knownRegions()) {
                if (memberId.startsWith(region)) {
                    return region;
                }
            }
        }
        return "UNKNOWN";
    }

    /**
     * The two-digit FY segment of {@code memberId}: the two characters immediately after the REGION
     * prefix. Null when the id is absent or too short to carry the FY segment (before the HASH8/CHK
     * tail).
     */
    public static String fiscalYearCodeOf(String memberId) {
        if (memberId == null) {
            return null;
        }
        int start = regionOf(memberId).length();
        if (memberId.length() < start + TAIL_LENGTH) {
            return null;
        }
        return memberId.substring(start, start + 2);
    }
}
