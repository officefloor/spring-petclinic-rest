package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Map;

/**
 * Derives an owner's 'locality' (canonical region) from their city using a fixed
 * city-to-region table. Any city not in the table — including a null city — yields
 * "UNKNOWN".
 */
public final class Locality {

    /** City -> canonical region. */
    private static final Map<String, String> CITY_REGION = Map.of(
            "Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    /** Region -> inclusive 4-digit postcode range {low, high}. */
    private static final Map<String, int[]> REGION_POSTCODES = Map.of(
            "NSW", new int[] {2000, 2099},
            "VIC", new int[] {3000, 3099},
            "QLD", new int[] {4000, 4099});

    /** Sentinel returned by {@link #of(String)} for a city with no known region. */
    public static final String UNKNOWN = "UNKNOWN";

    private Locality() {
    }

    /** Canonical region for {@code city}, or "UNKNOWN" when it is not in the table. */
    public static String of(String city) {
        return city == null ? UNKNOWN : CITY_REGION.getOrDefault(city, UNKNOWN);
    }

    /**
     * The inclusive 4-digit postcode range {low, high} for {@code region}, or {@code null}
     * when the region has no known range (so any 4-digit postcode is acceptable).
     */
    public static int[] postcodeRange(String region) {
        return REGION_POSTCODES.get(region);
    }
}
