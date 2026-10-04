package org.springframework.samples.petclinic.util;

import java.util.Map;

/**
 * Validates an owner's postcode against the region derived from its city.
 *
 * <p>A postcode is a 4-digit string. When present it must fall within the inclusive
 * range fixed for the region of the owner's city, keyed via a fixed city-to-region
 * table:
 * <ul>
 *     <li>Sydney -&gt; NSW, postcodes 2000-2099;</li>
 *     <li>Melbourne -&gt; VIC, postcodes 3000-3099;</li>
 *     <li>Brisbane -&gt; QLD, postcodes 4000-4099.</li>
 * </ul>
 *
 * <p>A city with no known region accepts any 4-digit postcode. The postcode is
 * optional, so a {@code null} postcode is always accepted; the 4-digit format is
 * enforced separately by the field's pattern constraint, so a value that is not a
 * 4-digit number is left for that constraint to reject.
 */
public final class Postcodes {

    /**
     * Known city-to-region mappings, keyed by the exact city name.
     */
    private static final Map<String, String> CITY_REGIONS = Map.of(
        "Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    /**
     * Region to its inclusive 4-digit postcode range {@code {low, high}}.
     */
    private static final Map<String, int[]> REGION_RANGES = Map.of(
        "NSW", new int[] {2000, 2099},
        "VIC", new int[] {3000, 3099},
        "QLD", new int[] {4000, 4099});

    private Postcodes() {
    }

    /**
     * Whether the given postcode is acceptable for the given city. Returns {@code true} when the
     * postcode is {@code null} (optional), when it is not a 4-digit value (deferred to the format
     * constraint), or when the city has no known region. Otherwise the 4-digit value must lie within
     * the inclusive range for the city's region.
     */
    public static boolean isValidForCity(String postcode, String city) {
        if (postcode == null || !postcode.matches("[0-9]{4}")) {
            return true;
        }
        String region = city == null ? null : CITY_REGIONS.get(city);
        int[] range = region == null ? null : REGION_RANGES.get(region);
        if (range == null) {
            return true;
        }
        int value = Integer.parseInt(postcode);
        return value >= range[0] && value <= range[1];
    }
}
