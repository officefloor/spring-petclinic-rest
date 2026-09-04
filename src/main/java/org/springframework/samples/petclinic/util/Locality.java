package org.springframework.samples.petclinic.util;

import java.util.Map;

/**
 * Derives an owner's {@code locality}: the canonical region. The postcode is preferred,
 * looked up by range (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099); when it is absent or
 * in no known range the fixed city-to-region table is used instead. Anything unresolved
 * resolves to {@code "UNKNOWN"}.
 */
public final class Locality {

    private static final Map<String, String> CITY_REGION =
        Map.of("Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    /** Postcode "hundreds" band (postcode / 100) -> region. */
    private static final Map<Integer, String> BAND_REGION =
        Map.of(20, "NSW", 30, "VIC", 40, "QLD");

    private Locality() {
    }

    /** The canonical region for {@code city}, or {@code "UNKNOWN"} when it is not mapped. */
    public static String of(String city) {
        return CITY_REGION.getOrDefault(city, "UNKNOWN");
    }

    /** The region for {@code postcode} (preferred), falling back to {@code city}. */
    public static String of(String city, String postcode) {
        String region = byPostcode(postcode);
        return region != null ? region : of(city);
    }

    /** The region whose range contains {@code postcode}, or {@code null} when none does. */
    private static String byPostcode(String postcode) {
        if (postcode == null) {
            return null;
        }
        return BAND_REGION.get(Integer.parseInt(postcode) / 100);
    }
}
