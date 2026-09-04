package org.springframework.samples.petclinic.rest.function.owner;

/**
 * The single source of truth for an owner's unified {@code memberId}, formatted
 * {@code <REGION><V2><FY><HASH8><CHK>}: REGION is the region code derived from the postcode
 * (NSW/VIC/QLD, else {@code UNKNOWN}) carrying the fixed version-2 tag (e.g. {@code NSWV2}, see
 * {@link OwnerRegion#identifierRegionCode}), FY the two-digit fiscal year, HASH8 the first 8 upper-case
 * hex characters of SHA-256 over {@code normalizedTelephone + lastName} (see
 * {@link OwnerIdentity#customerHash}), and CHK a single Luhn check digit over the decimal digits of
 * {@code <REGIONV2><FY><HASH8>}.
 *
 * <p>{@link #of} assembles the id from its parts when an owner is created; {@link #regionOf} and
 * {@link #fiscalYearCodeOf} read the REGION and FY segments back out of an assigned id, which is how
 * the owner's locality, timezone and fiscal year all resolve straight off the memberId. {@link #regionOf}
 * strips the version tag and returns the <em>plain</em> region (e.g. {@code NSW}), so the version tag
 * never leaks into the user-facing locality. A collision with an existing owner's memberId is
 * de-duplicated by appending {@code -<n>} (see {@link AssignMemberId}); the suffix is not part of the
 * segments the readers below extract.
 */
public final class MemberId {

    /** The fixed-width tail after the version-tagged REGION segment: FY (2) + HASH8 (8) + CHK (1). */
    private static final int TAIL_LENGTH = 11;

    private MemberId() {
    }

    /**
     * The base memberId {@code <REGIONV2><FY><HASH8><CHK>} assembled from its parts (before any
     * collision suffix): {@code regionCode} is the version-tagged region code (e.g. {@code NSWV2}) and
     * CHK is the Luhn check digit over the decimal digits of {@code regionCode + fiscalYearCode + hash8}.
     */
    public static String of(String regionCode, String fiscalYearCode, String hash8) {
        String core = regionCode + fiscalYearCode + hash8;
        return core + OwnerIdentity.luhnCheckDigit(core);
    }

    /**
     * The <em>plain</em> region of {@code memberId} — one of the known regions (NSW, VIC, QLD), else
     * {@code UNKNOWN} (which also covers an absent or malformed id). The version tag carried by the
     * memberId's region segment (e.g. {@code NSWV2}) is stripped, so the user-facing locality never
     * exposes it.
     */
    public static String regionOf(String memberId) {
        if (memberId != null) {
            for (String region : OwnerRegion.knownRegions()) {
                if (memberId.startsWith(region + OwnerIdentity.VERSION_TAG)) {
                    return region;
                }
            }
        }
        return "UNKNOWN";
    }

    /** The length of the version-tagged region segment at the start of {@code memberId}. */
    private static int regionSegmentLength(String memberId) {
        return regionOf(memberId).length() + OwnerIdentity.VERSION_TAG.length();
    }

    /**
     * The two-digit FY segment of {@code memberId}: the two characters immediately after the
     * version-tagged REGION segment. Null when the id is absent or too short to carry the FY segment
     * (before the HASH8/CHK tail).
     */
    public static String fiscalYearCodeOf(String memberId) {
        if (memberId == null) {
            return null;
        }
        int start = regionSegmentLength(memberId);
        if (memberId.length() < start + TAIL_LENGTH) {
            return null;
        }
        return memberId.substring(start, start + 2);
    }
}
