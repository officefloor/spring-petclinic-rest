package org.springframework.samples.petclinic.mapper;

import java.util.Map;

/**
 * Derives an owner's IANA timezone ('timezone') from its canonical region
 * ('locality') via the fixed region-to-timezone table
 * ({@code NSW -> Australia/Sydney}, {@code VIC -> Australia/Melbourne},
 * {@code QLD -> Australia/Brisbane}). Regions outside the table (including
 * {@link Locality#UNKNOWN}) have no known timezone and yield {@code null}.
 *
 * <p>Kept as a standalone helper (referenced from {@link OwnerMapper}'s
 * {@code timezone} expression) rather than a mapper {@code default} method to
 * avoid MapStruct picking it up as an automatic conversion.
 */
final class Timezone {

    /** Region -&gt; IANA timezone. Anything not listed has no known timezone. */
    private static final Map<String, String> REGION_TIMEZONE = Map.of(
            "NSW", "Australia/Sydney", "VIC", "Australia/Melbourne", "QLD", "Australia/Brisbane");

    private Timezone() {
    }

    /**
     * The IANA timezone name for the given region, or {@code null} when the region
     * is absent or has no entry in the fixed table.
     */
    static String of(String region) {
        return region == null ? null : REGION_TIMEZONE.get(region);
    }
}
