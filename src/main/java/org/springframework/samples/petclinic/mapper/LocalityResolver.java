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
     * Returns the owner's canonical region ('locality'): the <em>plain</em> region code derived from
     * the owner's postcode (see {@link #regionCode(String)}), or {@link #UNKNOWN} when the owner is
     * {@code null} or its postcode maps to no known region. This is deliberately taken straight from
     * the postcode rather than parsed back out of the owner's {@code memberId}: the version-2
     * identifiers decorate their region segment with a {@code 'V2'} version tag (see
     * {@link IdentityKeyResolver#deriveMemberId}), whereas the user-facing locality — and the
     * {@code timezone} and owner-segment area derived from it — must stay the plain region code.
     *
     * @param owner the owner whose locality should be derived, may be {@code null}
     * @return the owner's plain region code, or {@link #UNKNOWN} when none is present
     */
    public static String deriveLocality(Owner owner) {
        return owner == null ? UNKNOWN : regionCode(owner.getPostcode());
    }
}
