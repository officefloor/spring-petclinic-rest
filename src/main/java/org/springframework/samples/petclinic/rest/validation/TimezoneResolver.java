package org.springframework.samples.petclinic.rest.validation;

import java.util.Map;

/**
 * Maps a canonical region to its IANA timezone name via a fixed region-to-timezone table
 * ({@code NSW -> Australia/Sydney}, {@code VIC -> Australia/Melbourne},
 * {@code QLD -> Australia/Brisbane}).
 *
 * <p>The region is the locality derived by {@link LocalityResolver}; any region not present in the
 * table — including {@link LocalityResolver#UNKNOWN} and a {@code null} region — has no known
 * timezone.
 */
public final class TimezoneResolver {

    /** Region -> IANA timezone; anything not listed has no known timezone. */
    private static final Map<String, String> REGION_TIMEZONE = Map.of(
        "NSW", "Australia/Sydney",
        "VIC", "Australia/Melbourne",
        "QLD", "Australia/Brisbane");

    private TimezoneResolver() {
    }

    /**
     * Resolve the IANA timezone name for a region.
     *
     * @param region the canonical region, possibly {@code null}
     * @return the region's IANA timezone name, or {@code null} when the region has no known timezone
     */
    public static String resolve(String region) {
        return REGION_TIMEZONE.get(region);
    }
}
