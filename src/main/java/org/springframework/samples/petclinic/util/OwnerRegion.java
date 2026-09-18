package org.springframework.samples.petclinic.util;

/**
 * The canonical region an owner belongs to: derived from the {@link PostcodeRange postcode range}
 * first, falling back to the fixed {@link CityRegion city-to-region table} when the postcode is
 * absent or in no known range, and {@link CityRegion#UNKNOWN} when neither resolves a region.
 *
 * <p>This is the single source of an owner's region, used both to build the region segment of the
 * {@link MemberId member id} and to report the owner's locality.
 */
public final class OwnerRegion {

    private OwnerRegion() {
    }

    /** The region for the given {@code postcode} (preferred) or {@code city} (fallback). */
    public static String of(String postcode, String city) {
        String byPostcode = PostcodeRange.regionForPostcode(postcode);
        return byPostcode != null ? byPostcode : CityRegion.locality(city);
    }
}
