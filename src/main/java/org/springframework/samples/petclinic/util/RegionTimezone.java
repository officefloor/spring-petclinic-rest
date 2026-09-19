package org.springframework.samples.petclinic.util;

import java.util.Map;

/**
 * Maps a canonical region (see {@link CityLocality}) to its IANA timezone via a fixed
 * table: NSW->Australia/Sydney, VIC->Australia/Melbourne, QLD->Australia/Brisbane. This
 * class owns only that mapping, keeping the timezone concern separate from region
 * resolution and reusable wherever a region is known.
 *
 * <p>A region with no known timezone (including {@link CityLocality#UNKNOWN} or a null
 * region) resolves to {@code null}.
 */
public final class RegionTimezone {

    /** Region -> IANA timezone name. Anything not listed has no known timezone. */
    private static final Map<String, String> REGION_TIMEZONE = Map.of(
            "NSW", "Australia/Sydney",
            "VIC", "Australia/Melbourne",
            "QLD", "Australia/Brisbane");

    private RegionTimezone() {
    }

    /**
     * @param region the canonical region, as resolved by {@link CityLocality}
     * @return the IANA timezone name for {@code region}, or {@code null} when it has no
     *         known timezone
     */
    public static String forRegion(String region) {
        return REGION_TIMEZONE.get(region);
    }
}
