package org.springframework.samples.petclinic.util;

import java.util.Map;

/**
 * Fixed region-to-timezone lookup. Maps each canonical region to its IANA timezone name
 * (NSW-&gt;Australia/Sydney, VIC-&gt;Australia/Melbourne, QLD-&gt;Australia/Brisbane); every
 * other region (including {@link CityRegion#UNKNOWN}) resolves to {@code null}.
 */
public final class RegionTimezone {

    /** The pinned region-to-IANA-timezone table. */
    private static final Map<String, String> TABLE = Map.of(
            "NSW", "Australia/Sydney",
            "VIC", "Australia/Melbourne",
            "QLD", "Australia/Brisbane");

    private RegionTimezone() {
    }

    /**
     * The IANA timezone name for the given {@code region}, or {@code null} when the region is
     * not in the table (including a null region).
     */
    public static String of(String region) {
        return region == null ? null : TABLE.get(region);
    }
}
