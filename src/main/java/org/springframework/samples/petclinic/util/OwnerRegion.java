package org.springframework.samples.petclinic.util;

import org.springframework.samples.petclinic.model.Owner;

/**
 * The canonical region ("locality") of an owner: the region derived from its postcode
 * first (see {@link PostcodeRange#regionOf(String)}), falling back to the city-to-region
 * table when the postcode is absent or in no known range, or {@link CityRegion#UNKNOWN}
 * for a city not in the table.
 *
 * <p>This region is the {@code REGION} segment of the owner's customer-code identity (see
 * {@link CustomerCode}); the identity is built from it on creation and the locality read
 * back off it, so there is one region concept rather than two derivations that could drift.
 */
public final class OwnerRegion {

    private OwnerRegion() {
    }

    /** The region for the given owner, derived from its postcode then its city. */
    public static String of(Owner owner) {
        String byPostcode = PostcodeRange.regionOf(owner.getPostcode());
        return byPostcode != null ? byPostcode : CityRegion.localityOf(owner.getCity());
    }
}
