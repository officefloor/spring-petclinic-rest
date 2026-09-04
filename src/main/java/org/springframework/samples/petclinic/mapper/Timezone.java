package org.springframework.samples.petclinic.mapper;

import java.util.Map;

/**
 * Derives the owner's IANA timezone from the locality/region via a fixed
 * region-to-timezone table (NSW->Australia/Sydney, VIC->Australia/Melbourne,
 * QLD->Australia/Brisbane), or {@code null} when the region is not in the table.
 * The value is a pure function of the region, so it needs no stored state.
 */
public final class Timezone {

    private static final Map<String, String> REGION_ZONE = Map.of(
            "NSW", "Australia/Sydney", "VIC", "Australia/Melbourne", "QLD", "Australia/Brisbane");

    private Timezone() {
    }

    public static String of(String region) {
        return REGION_ZONE.get(region);
    }
}
