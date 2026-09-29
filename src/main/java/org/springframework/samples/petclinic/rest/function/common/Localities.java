package org.springframework.samples.petclinic.rest.function.common;

import java.util.Map;

/**
 * Derives an owner's canonical region ("locality") from their city using a fixed
 * city-to-region table. The mapping is intentionally pinned so both the Spring and
 * OfficeFloor arms produce identical values; anything not in the table is "UNKNOWN".
 */
public final class Localities {

    /** City -> canonical region. */
    private static final Map<String, String> CITY_REGION = Map.of(
            "Sydney", "NSW",
            "Melbourne", "VIC",
            "Brisbane", "QLD");

    /** Locality returned for any city not in {@link #CITY_REGION}. */
    public static final String UNKNOWN = "UNKNOWN";

    /** Region -> inclusive 4-digit postcode range {low, high}. Regions absent here (UNKNOWN) accept
     *  any 4-digit postcode. Pinned to match the acceptance spec's fixed table. */
    private static final Map<String, int[]> REGION_POSTCODES = Map.of(
            "NSW", new int[] {2000, 2099},
            "VIC", new int[] {3000, 3099},
            "QLD", new int[] {4000, 4099});

    private Localities() {
    }

    /** The canonical region for {@code city}, or "UNKNOWN" when the city is not in the table. */
    public static String of(String city) {
        return CITY_REGION.getOrDefault(city, UNKNOWN);
    }

    /** The inclusive {low, high} postcode range for {@code city}'s region, or {@code null} when the
     *  city has no known region (any 4-digit postcode is then accepted). */
    public static int[] postcodeRange(String city) {
        return REGION_POSTCODES.get(of(city));
    }
}
