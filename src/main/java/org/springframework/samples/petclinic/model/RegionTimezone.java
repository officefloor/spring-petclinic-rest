package org.springframework.samples.petclinic.model;

import java.util.Map;

/**
 * Fixed region-to-timezone lookup used to derive an owner's IANA timezone from its
 * {@link Owner#getLocality() locality} (see {@link CityRegion}). A region not listed here — including
 * {@link CityRegion#UNKNOWN} — has no known timezone.
 */
public final class RegionTimezone {

    /** Region -> IANA timezone name. */
    private static final Map<String, String> REGION_TO_TIMEZONE = Map.of(
        "NSW", "Australia/Sydney",
        "VIC", "Australia/Melbourne",
        "QLD", "Australia/Brisbane");

    private RegionTimezone() {
    }

    /** The IANA timezone name for {@code region}, or {@code null} when the region has no known timezone. */
    public static String of(String region) {
        return REGION_TO_TIMEZONE.get(region);
    }
}
