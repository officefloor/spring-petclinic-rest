package org.springframework.samples.petclinic.model;

import java.util.Map;

/**
 * Maps a city to its canonical region using a fixed city-to-region table.
 */
public final class Locality {

    private static final Map<String, String> REGIONS = Map.of(
        "Sydney", "NSW",
        "Melbourne", "VIC",
        "Brisbane", "QLD");

    private Locality() {
    }

    /**
     * Returns the canonical region for the given city, or {@code "UNKNOWN"} when the city
     * is not in the table.
     */
    public static String of(String city) {
        return REGIONS.getOrDefault(city, "UNKNOWN");
    }

    /**
     * Returns the canonical region, preferring the postcode range (NSW 2000-2099,
     * VIC 3000-3099, QLD 4000-4099) and falling back to the city-to-region table when the
     * postcode is absent or in no known range.
     */
    public static String of(String city, String postcode) {
        String byPostcode = byPostcode(postcode);
        return byPostcode != null ? byPostcode : of(city);
    }

    private static String byPostcode(String postcode) {
        if (postcode == null || !postcode.matches("[0-9]{4}")) {
            return null;
        }
        int code = Integer.parseInt(postcode);
        if (code >= 2000 && code <= 2099) {
            return "NSW";
        }
        if (code >= 3000 && code <= 3099) {
            return "VIC";
        }
        if (code >= 4000 && code <= 4099) {
            return "QLD";
        }
        return null;
    }
}
