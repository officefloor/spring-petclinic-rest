package org.springframework.samples.petclinic.mapper;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's canonical region code ('locality'): the plain region the owner belongs to
 * (NSW, VIC or QLD), or {@link #UNKNOWN} when it belongs to no known region.
 *
 * <p>This is the single home of the owner's <em>plain</em> region code. The same region also
 * prefixes the owner's identifiers (see {@link IdentityKeyResolver#deriveMemberId}), but the two
 * are kept as distinct concerns so they can diverge: an identifier may decorate its region segment
 * (for example with a version tag), whereas the user-facing locality — and the {@code timezone} and
 * owner-segment area derived from it — stays the plain region code. Callers therefore ask for the
 * region by owner (see {@link #deriveLocality(Owner)}) rather than parsing it back out of a
 * particular identifier.
 *
 * <p>Kept out of {@link OwnerMapper} so MapStruct does not mistake it for an implicit
 * {@code String -> String} property mapping method.
 */
public final class LocalityResolver {

    /** Locality returned for an owner that belongs to no known {@link Region}. */
    public static final String UNKNOWN = "UNKNOWN";

    private LocalityResolver() {
    }

    /**
     * Returns the plain region code for a postcode: the name of the {@link Region} whose inclusive
     * range contains it (see {@link Region#forPostcode}), or {@link #UNKNOWN} when the postcode is
     * absent or maps to no known region. This is the shared building block of the owner's region
     * code, used both for the owner's locality and for the region segment its identifiers are
     * prefixed with.
     *
     * @param postcode the owner's postcode, may be {@code null}
     * @return the region code, or {@link #UNKNOWN} when the postcode maps to no known region
     */
    public static String regionCode(String postcode) {
        Region region = Region.forPostcode(postcode);
        return region == null ? UNKNOWN : region.name();
    }

    /**
     * Returns the owner's canonical region ('locality'): the region taken from the {@code REGION}
     * segment of the owner's {@code memberId} (the run of letters before the two-digit fiscal-year
     * segment), which is the region derived from the owner's postcode. Returns {@link #UNKNOWN} when
     * the owner has no member id, or its member id carries no region segment.
     *
     * @param owner the owner whose locality should be derived, may be {@code null}
     * @return the owner's region code, or {@link #UNKNOWN} when none is present
     */
    public static String deriveLocality(Owner owner) {
        return owner == null ? UNKNOWN : regionOfMemberId(owner.getMemberId());
    }

    /**
     * Extracts the leading {@code REGION} segment of a {@code <REGION><FY><HASH8><CHK>} member id:
     * the run of letters before the two-digit fiscal-year segment. Returns {@link #UNKNOWN} when the
     * member id is {@code null}, blank, or carries no region segment.
     *
     * @param memberId the owner's member id, may be {@code null}
     * @return the region segment of the member id, or {@link #UNKNOWN} when none is present
     */
    private static String regionOfMemberId(String memberId) {
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
