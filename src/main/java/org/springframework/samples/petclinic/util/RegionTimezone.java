package org.springframework.samples.petclinic.util;

import java.util.Map;

/**
 * Fixed lookup from an owner's region (its "locality", see {@link OwnerRegion}) to its
 * IANA timezone name. The table is pinned; a region with no entry — including
 * {@link CityRegion#UNKNOWN} — has no known timezone and returns {@code null}.
 */
public final class RegionTimezone {

    /** Region to its IANA timezone name. */
    private static final Map<String, String> REGION_TIMEZONE = Map.of(
        "NSW", "Australia/Sydney",
        "VIC", "Australia/Melbourne",
        "QLD", "Australia/Brisbane");

    private RegionTimezone() {
    }

    /**
     * The IANA timezone name for the given region, or {@code null} when the region has no
     * entry in the pinned table.
     *
     * @param region the owner's region (its locality)
     * @return the IANA timezone name, or {@code null} when none applies
     */
    public static String of(String region) {
        return REGION_TIMEZONE.get(region);
    }
}
