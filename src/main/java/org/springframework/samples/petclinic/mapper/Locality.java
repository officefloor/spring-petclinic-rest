package org.springframework.samples.petclinic.mapper;

import java.util.Map;

/**
 * Derives an owner's canonical region ('locality') from its city via a fixed
 * city-to-region table.
 *
 * <p>Kept as a standalone helper (referenced from {@link OwnerMapper}'s
 * {@code locality} expression) rather than a mapper {@code default} method: a
 * {@code String}-to-{@code String} method on the mapper interface would be
 * picked up by MapStruct as an automatic conversion for every String property.
 */
final class Locality {

    /** City -&gt; canonical region. Anything not listed derives {@link #UNKNOWN}. */
    private static final Map<String, String> CITY_REGION = Map.of(
            "Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    static final String UNKNOWN = "UNKNOWN";

    private Locality() {
    }

    /** The canonical region for {@code city}, or {@code "UNKNOWN"} when it is not in the table. */
    static String of(String city) {
        return city == null ? UNKNOWN : CITY_REGION.getOrDefault(city, UNKNOWN);
    }
}
