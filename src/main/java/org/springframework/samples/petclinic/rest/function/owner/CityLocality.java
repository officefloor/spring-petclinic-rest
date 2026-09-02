package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Map;

/**
 * Derives an owner's locality (canonical region), preferring the postcode: a four-digit
 * postcode within a known region range (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099)
 * wins. When the postcode is absent or in no known range, a fixed city-to-region table
 * (Sydney->NSW, Melbourne->VIC, Brisbane->QLD) is used; any other city derives
 * {@code "UNKNOWN"}.
 */
public final class CityLocality {

    private static final Map<String, String> CITY_REGION =
            Map.of("Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    private static final Map<String, int[]> REGION_RANGE = Map.of(
            "NSW", new int[] {2000, 2099}, "VIC", new int[] {3000, 3099}, "QLD", new int[] {4000, 4099});

    private CityLocality() {
    }

    public static String of(String city) {
        return CITY_REGION.getOrDefault(city, "UNKNOWN");
    }

    /** Region from the postcode range first, falling back to the city table. */
    public static String of(String city, String postcode) {
        String byPostcode = byPostcode(postcode);
        return byPostcode != null ? byPostcode : of(city);
    }

    private static String byPostcode(String postcode) {
        if (postcode == null || !postcode.matches("[0-9]{4}")) {
            return null;
        }
        int value = Integer.parseInt(postcode);
        for (Map.Entry<String, int[]> region : REGION_RANGE.entrySet()) {
            int[] range = region.getValue();
            if (value >= range[0] && value <= range[1]) {
                return region.getKey();
            }
        }
        return null;
    }
}
