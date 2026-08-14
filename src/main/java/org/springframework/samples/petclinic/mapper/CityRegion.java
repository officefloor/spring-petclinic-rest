package org.springframework.samples.petclinic.mapper;

import java.util.Map;

/**
 * Derives the canonical region (locality) for a city from a fixed city-to-region table.
 *
 * <p>Kept as a standalone helper (rather than a method on {@link OwnerMapper}) so MapStruct does
 * not mistake it for a {@code String -> String} mapping method and apply it to unrelated fields.
 */
public final class CityRegion {

    /** City -> canonical region; anything not listed derives locality "UNKNOWN". */
    private static final Map<String, String> CITY_REGION = Map.of(
            "Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    /** Region -> inclusive {low, high} 4-digit postcode range. */
    private static final Map<String, int[]> REGION_POSTCODES = Map.of(
            "NSW", new int[] {2000, 2099}, "VIC", new int[] {3000, 3099}, "QLD", new int[] {4000, 4099});

    private CityRegion() {
    }

    /** The canonical region for {@code city}, or "UNKNOWN" when the city is not in the table. */
    public static String locality(String city) {
        return CITY_REGION.getOrDefault(city, "UNKNOWN");
    }

    /**
     * The inclusive {@code {low, high}} 4-digit postcode range valid for {@code city}'s region, or
     * {@code null} when the city has no known region (in which case any 4-digit postcode is valid).
     */
    public static int[] postcodeRange(String city) {
        return REGION_POSTCODES.get(locality(city));
    }
}
