package org.springframework.samples.petclinic.util;

import org.springframework.samples.petclinic.model.Owner;

/**
 * The owner's plain locality region, read back from the REGION segment of its
 * {@link MemberId member id} and decoded from the identity version tag (see
 * {@link IdentityVersion#plainRegion(String)}), so the version tag stored inside the
 * identifier never surfaces in the locality; {@code null} when the owner has no member id.
 *
 * <p>Single source of the derived locality: both the response mapper and the owner-created
 * audit event read it here, so the locality and everything derived from it (the timezone,
 * the owner segment) stay consistent rather than re-deriving the region separately.
 */
public final class OwnerLocality {

    private OwnerLocality() {
    }

    /** The plain locality region for the given owner. */
    public static String of(Owner owner) {
        return IdentityVersion.plainRegion(MemberId.regionOf(owner.getMemberId()));
    }
}
