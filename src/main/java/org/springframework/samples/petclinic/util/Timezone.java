package org.springframework.samples.petclinic.util;

import java.util.Map;

/**
 * Maps a locality/region to its IANA timezone via a fixed region-to-timezone table
 * (NSW->Australia/Sydney, VIC->Australia/Melbourne, QLD->Australia/Brisbane).
 */
public final class Timezone {

    /** Region -> IANA timezone name. */
    private static final Map<String, String> REGION_TIMEZONE =
        Map.of("NSW", "Australia/Sydney", "VIC", "Australia/Melbourne", "QLD", "Australia/Brisbane");

    private Timezone() {
    }

    /** The IANA timezone for {@code region}, or {@code null} when it is not mapped. */
    public static String of(String region) {
        return REGION_TIMEZONE.get(region);
    }
}
