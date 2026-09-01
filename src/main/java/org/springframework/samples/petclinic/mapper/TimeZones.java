package org.springframework.samples.petclinic.mapper;

import java.util.Map;

/**
 * Derives an IANA timezone name from a locality/region using a fixed
 * region-to-timezone table. Regions not in the table resolve to {@code null}.
 */
final class TimeZones {

    private static final Map<String, String> REGION_ZONE = Map.of(
        "NSW", "Australia/Sydney", "VIC", "Australia/Melbourne", "QLD", "Australia/Brisbane");

    private TimeZones() {
    }

    /** IANA timezone for {@code region}, or {@code null} when it is not in the table. */
    static String zoneOf(String region) {
        return region == null ? null : REGION_ZONE.get(region);
    }
}
