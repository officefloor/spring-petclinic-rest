package org.springframework.samples.petclinic.mapper;

/**
 * Derives an owner's canonical region ('locality') from its {@code memberId}. The member id is formatted
 * {@code <REGION><FY><HASH8><CHK>} (see {@link IdentityKeyResolver#deriveMemberId}), so the locality is simply the
 * leading {@code REGION} segment — the run of letters before the two-digit fiscal-year segment — which is the region
 * derived from the owner's postcode, rather than being recomputed from the city and postcode. {@link #UNKNOWN} is
 * returned when the member id is absent or carries no region segment. Kept out of {@link OwnerMapper} so MapStruct
 * does not mistake it for an implicit {@code String -> String} property mapping method.
 */
public final class LocalityResolver {

    /** Locality returned for an owner whose member id carries no known {@link Region}. */
    public static final String UNKNOWN = "UNKNOWN";

    private LocalityResolver() {
    }

    /**
     * Returns the canonical region ('locality') for the given {@code memberId}, which is the leading {@code REGION}
     * segment of the {@code <REGION><FY><HASH8><CHK>} member id: the run of letters before the two-digit fiscal-year
     * segment (the region derived from the owner's postcode). Returns {@link #UNKNOWN} when the member id is
     * {@code null}, blank, or carries no region segment.
     *
     * @param memberId the owner's member id, may be {@code null}
     * @return the region segment of the member id, or {@link #UNKNOWN} when none is present
     */
    public static String deriveLocality(String memberId) {
        if (memberId == null) {
            return UNKNOWN;
        }
        int end = 0;
        while (end < memberId.length() && Character.isLetter(memberId.charAt(end))) {
            end++;
        }
        String region = memberId.substring(0, end);
        return region.isBlank() ? UNKNOWN : region;
    }
}
