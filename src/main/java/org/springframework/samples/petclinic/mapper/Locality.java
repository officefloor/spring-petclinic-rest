package org.springframework.samples.petclinic.mapper;

import java.util.Map;

/** Derives an owner's locality (canonical region) from its city using a fixed table. */
public final class Locality {

    private static final Map<String, String> CITY_REGION =
        Map.of("Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    private static final Map<String, int[]> REGION_POSTCODES = Map.of(
        "NSW", new int[] {2000, 2099}, "VIC", new int[] {3000, 3099}, "QLD", new int[] {4000, 4099});

    private static final Map<String, String> REGION_TIMEZONE = Map.of(
        "NSW", "Australia/Sydney", "VIC", "Australia/Melbourne", "QLD", "Australia/Brisbane");

    private Locality() {
    }

    /** The canonical region for {@code city}, or "UNKNOWN" when it is not in the table. */
    public static String of(String city) {
        return city == null ? "UNKNOWN" : CITY_REGION.getOrDefault(city, "UNKNOWN");
    }

    /** Region by postcode range first (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099), falling
     * back to the city table when the postcode is absent or in no known range. */
    public static String of(String city, String postcode) {
        if (postcode != null && postcode.matches("\\d{4}")) {
            int value = Integer.parseInt(postcode);
            for (Map.Entry<String, int[]> region : REGION_POSTCODES.entrySet()) {
                int[] range = region.getValue();
                if (value >= range[0] && value <= range[1]) {
                    return region.getKey();
                }
            }
        }
        return of(city);
    }

    /** The IANA timezone for a region (NSW/VIC/QLD), or null when the region is not in the table. */
    public static String timezone(String region) {
        return REGION_TIMEZONE.get(region);
    }
}
