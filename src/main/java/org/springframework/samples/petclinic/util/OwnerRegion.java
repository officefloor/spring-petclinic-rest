package org.springframework.samples.petclinic.util;

import org.springframework.samples.petclinic.model.Owner;

/**
 * The canonical region an owner belongs to: derived from the {@link PostcodeRange postcode range}
 * first, falling back to the fixed {@link CityRegion city-to-region table} when the postcode is
 * absent or in no known range, and {@link CityRegion#UNKNOWN} when neither resolves a region.
 *
 * <p>This is the single source of an owner's region. The plain region is what the user-facing
 * locality, timezone and {@link OwnerSegment owner segment} report; the {@link #forIdentifier
 * identifier region} mixes in the {@link OwnerIdentityVersion#TAG version tag} for use inside the
 * {@link MemberId member id}.
 */
public final class OwnerRegion {

    private OwnerRegion() {
    }

    /** The region for the given {@code postcode} (preferred) or {@code city} (fallback). */
    public static String of(String postcode, String city) {
        String byPostcode = PostcodeRange.regionForPostcode(postcode);
        return byPostcode != null ? byPostcode : CityRegion.locality(city);
    }

    /** The plain region for the given {@code owner}, derived from its postcode then city. */
    public static String of(Owner owner) {
        return of(owner.getPostcode(), owner.getCity());
    }

    /**
     * The region code embedded inside the owner's version-2 identifiers: the {@link #of(String,
     * String) plain region} with the fixed {@link OwnerIdentityVersion#TAG version tag} mixed in, so
     * an identifier's region can never collide with a version-1 one. Confined to the identifiers —
     * the user-facing locality, timezone and owner segment use the plain region.
     */
    public static String forIdentifier(String postcode, String city) {
        return OwnerIdentityVersion.TAG + of(postcode, city);
    }
}
