package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Map;

/**
 * Maps a canonical region (see {@link Locality}) to its IANA timezone name via a fixed
 * table: NSW -> Australia/Sydney, VIC -> Australia/Melbourne, QLD -> Australia/Brisbane.
 * A region with no mapping yields {@code null}.
 */
public final class Timezone {

    private static final Map<String, String> REGION_TIMEZONE = Map.of(
            "NSW", "Australia/Sydney", "VIC", "Australia/Melbourne", "QLD", "Australia/Brisbane");

    private Timezone() {
    }

    public static String of(String region) {
        return REGION_TIMEZONE.get(region);
    }
}
