package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Map;

/**
 * Derives the {@code locality} (canonical region) from an owner's postcode, falling back
 * to a fixed city-to-region table when the postcode is absent or in no known range.
 * Cities not in the table resolve to {@code UNKNOWN}.
 */
public final class Locality {

    private static final Map<String, String> CITY_REGION =
            Map.of("Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    private static final Map<String, int[]> REGION_RANGE = Map.of(
            "NSW", new int[] {2000, 2099}, "VIC", new int[] {3000, 3099}, "QLD", new int[] {4000, 4099});

    private Locality() {
    }

    public static String of(String city) {
        return CITY_REGION.getOrDefault(city, "UNKNOWN");
    }

    public static String of(String city, String postcode) {
        String region = regionForPostcode(postcode);
        return region != null ? region : of(city);
    }

    private static String regionForPostcode(String postcode) {
        if (postcode == null || !postcode.matches("[0-9]{4}")) {
            return null;
        }
        int value = Integer.parseInt(postcode);
        for (Map.Entry<String, int[]> range : REGION_RANGE.entrySet()) {
            if (value >= range.getValue()[0] && value <= range.getValue()[1]) {
                return range.getKey();
            }
        }
        return null;
    }
}
