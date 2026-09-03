package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Map;

/**
 * Maps an owner's locality/region code to its IANA timezone name via a fixed table
 * (NSW->Australia/Sydney, VIC->Australia/Melbourne, QLD->Australia/Brisbane). Any
 * other region has no known timezone.
 */
public class Timezone {

    private static final Map<String, String> REGION_TIMEZONE = Map.of(
            "NSW", "Australia/Sydney", "VIC", "Australia/Melbourne", "QLD", "Australia/Brisbane");

    public static String of(String region) {
        return REGION_TIMEZONE.get(region);
    }
}
