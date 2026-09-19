package org.springframework.samples.petclinic.util;

import java.util.Map;

/**
 * Resolves the IANA timezone name for a canonical region using a fixed region-to-timezone
 * table. Regions absent from the table (including {@link CityRegions#UNKNOWN}) have no known
 * timezone and resolve to {@code null}.
 */
public final class Timezones {

    /** Fixed, canonical region-to-IANA-timezone mapping. */
    private static final Map<String, String> REGION_TO_TIMEZONE = Map.of(
            "NSW", "Australia/Sydney",
            "VIC", "Australia/Melbourne",
            "QLD", "Australia/Brisbane");

    private Timezones() {
    }

    /**
     * Return the IANA timezone name for {@code region}, or {@code null} when the region is
     * absent from the table.
     */
    public static String of(String region) {
        return REGION_TO_TIMEZONE.get(region);
    }
}
