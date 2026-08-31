package org.springframework.samples.petclinic.util;

import java.util.Map;

/**
 * Maps a canonical region ('locality') to its IANA timezone via a fixed table:
 * {@code NSW -> Australia/Sydney}, {@code VIC -> Australia/Melbourne},
 * {@code QLD -> Australia/Brisbane}. Any other region (including {@code null})
 * yields {@code null}.
 */
public final class Timezones {

    private static final Map<String, String> REGION_TIMEZONE = Map.of(
        "NSW", "Australia/Sydney", "VIC", "Australia/Melbourne", "QLD", "Australia/Brisbane");

    private Timezones() {
    }

    /**
     * @param region the canonical region, may be {@code null}
     * @return the IANA timezone name, or {@code null} when the region is not in the table
     */
    public static String of(String region) {
        return REGION_TIMEZONE.get(region);
    }
}
