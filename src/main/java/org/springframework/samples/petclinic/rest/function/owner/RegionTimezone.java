package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Map;

/**
 * Maps a canonical region to its IANA timezone via a fixed table
 * (NSW->Australia/Sydney, VIC->Australia/Melbourne, QLD->Australia/Brisbane);
 * any other region yields {@code null}.
 */
public final class RegionTimezone {

    private static final Map<String, String> REGION_TIMEZONE = Map.of(
            "NSW", "Australia/Sydney", "VIC", "Australia/Melbourne", "QLD", "Australia/Brisbane");

    private RegionTimezone() {
    }

    public static String of(String region) {
        return REGION_TIMEZONE.get(region);
    }
}
