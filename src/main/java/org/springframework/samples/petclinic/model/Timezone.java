package org.springframework.samples.petclinic.model;

import java.util.Map;

/**
 * Derives an owner's IANA timezone name from its {@link Locality locality/region} via a
 * fixed region-to-timezone table (NSW -> Australia/Sydney, VIC -> Australia/Melbourne,
 * QLD -> Australia/Brisbane). Returns {@code null} when the region is unknown or has no
 * mapped timezone.
 */
public final class Timezone {

    /** Region -> IANA timezone name. Anything not listed has no timezone. */
    private static final Map<String, String> REGION_TIMEZONE = Map.of(
            "NSW", "Australia/Sydney", "VIC", "Australia/Melbourne", "QLD", "Australia/Brisbane");

    private Timezone() {
    }

    /**
     * The IANA timezone name for the given region, or {@code null} when the region is
     * null or has no mapped timezone.
     */
    public static String of(String region) {
        if (region == null) {
            return null;
        }
        return REGION_TIMEZONE.get(region);
    }

    /**
     * The owner's IANA timezone name, derived from its locality/region.
     * {@code null} when no timezone could be derived.
     */
    public static String of(Owner owner) {
        return of(Locality.of(owner));
    }
}
