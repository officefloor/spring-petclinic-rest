package org.springframework.samples.petclinic.util;

import java.util.Map;

/**
 * Pinned region-to-timezone reference table. Each canonical region (see {@link CityRegion}) maps to
 * its IANA timezone name; a region not in the table — including {@link CityRegion#UNKNOWN} — has no
 * timezone. Pure lookup with no dependency on other owners, so it is derived at response time rather
 * than stored on the entity.
 */
public final class RegionTimezone {

    /** Region -> IANA timezone name. */
    private static final Map<String, String> REGION_TIMEZONES = Map.of(
            "NSW", "Australia/Sydney",
            "VIC", "Australia/Melbourne",
            "QLD", "Australia/Brisbane");

    private RegionTimezone() {
    }

    /**
     * The IANA timezone name for {@code region}, or {@code null} when the region is {@code null} or not
     * in the table.
     */
    public static String of(String region) {
        return region == null ? null : REGION_TIMEZONES.get(region);
    }
}
