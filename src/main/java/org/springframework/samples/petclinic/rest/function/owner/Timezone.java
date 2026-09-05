package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Map;

/**
 * Maps an owner's {@code locality} (canonical region, see {@link Locality}) to its IANA timezone name
 * via a fixed region-to-timezone table (NSW -> Australia/Sydney, VIC -> Australia/Melbourne,
 * QLD -> Australia/Brisbane). Any region not in the table (including {@code "UNKNOWN"} or null)
 * derives no timezone ({@code null}). The mapping is pinned so it is stable across requests and needs
 * no persisted column.
 */
public final class Timezone {

    /** Region -> IANA timezone; anything not listed derives no timezone (null). */
    private static final Map<String, String> REGION_TIMEZONE = Map.of(
            "NSW", "Australia/Sydney", "VIC", "Australia/Melbourne", "QLD", "Australia/Brisbane");

    private Timezone() {
    }

    /** The IANA timezone for {@code region} from the fixed table, or {@code null} when unlisted. */
    public static String of(String region) {
        return REGION_TIMEZONE.get(region);
    }
}
