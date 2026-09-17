package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Map;

/**
 * The single definition of the region-to-timezone mapping: the fixed table from an owner's
 * canonical region (see {@link Locality}) to its IANA timezone name (NSW-&gt;Australia/Sydney,
 * VIC-&gt;Australia/Melbourne, QLD-&gt;Australia/Brisbane). Any other region, including
 * {@link Locality#UNKNOWN}, has no timezone.
 *
 * <p>Lookup only; a pure function of the region.
 */
public final class Timezone {

    /** Region -> IANA timezone name. */
    private static final Map<String, String> REGION_TIMEZONE =
            Map.of("NSW", "Australia/Sydney", "VIC", "Australia/Melbourne",
                    "QLD", "Australia/Brisbane");

    private Timezone() {
    }

    /** The IANA timezone name for {@code region} via the fixed table, or {@code null} when the
     *  region has no mapped timezone. */
    public static String of(String region) {
        return REGION_TIMEZONE.get(region);
    }
}
