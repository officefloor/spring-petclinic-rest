package org.springframework.samples.petclinic.mapper;

import java.util.Map;

/**
 * Derives the owner's locality (region). The postcode is preferred: a 4-digit
 * postcode is mapped to its region by range (NSW 2000-2099, VIC 3000-3099,
 * QLD 4000-4099). When the postcode is absent or falls in no known range, it
 * falls back to a fixed city-to-region table (Sydney->NSW, Melbourne->VIC,
 * Brisbane->QLD), or {@code UNKNOWN} when the city is not in the table. The value
 * is a pure function of postcode and city, so it needs no stored state.
 */
public final class Locality {

    private static final Map<String, String> CITY_REGION = Map.of(
            "Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    private static final Map<String, int[]> REGION_RANGE = Map.of(
            "NSW", new int[] {2000, 2099}, "VIC", new int[] {3000, 3099}, "QLD", new int[] {4000, 4099});

    private Locality() {
    }

    public static String of(String city) {
        return CITY_REGION.getOrDefault(city, "UNKNOWN");
    }

    public static String of(String postcode, String city) {
        String byPostcode = byPostcode(postcode);
        return byPostcode != null ? byPostcode : of(city);
    }

    private static String byPostcode(String postcode) {
        if (postcode == null || !postcode.matches("[0-9]{4}")) {
            return null;
        }
        int value = Integer.parseInt(postcode);
        for (Map.Entry<String, int[]> entry : REGION_RANGE.entrySet()) {
            int[] range = entry.getValue();
            if (value >= range[0] && value <= range[1]) {
                return entry.getKey();
            }
        }
        return null;
    }
}
