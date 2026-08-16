package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;
import java.util.Map;

/**
 * Fixed city-to-region lookup used to derive an owner's {@code locality}.
 *
 * <p>The table is intentionally closed: only Sydney, Melbourne and Brisbane map to a
 * canonical region. Any other (or missing) city derives the locality {@code "UNKNOWN"}.
 */
public final class CityRegion {

    /** City (case-insensitive) -> canonical region. */
    private static final Map<String, String> CITY_REGION = Map.of(
        "sydney", "NSW",
        "melbourne", "VIC",
        "brisbane", "QLD");

    /** Region -> inclusive {low, high} 4-digit postcode range. */
    private static final Map<String, int[]> REGION_POSTCODES = Map.of(
        "NSW", new int[] {2000, 2099},
        "VIC", new int[] {3000, 3099},
        "QLD", new int[] {4000, 4099});

    /** Locality returned when the city is not in {@link #CITY_REGION}. */
    public static final String UNKNOWN = "UNKNOWN";

    private CityRegion() {
    }

    /** The canonical region for {@code city}, or {@code "UNKNOWN"} when it is not in the table. */
    public static String localityOf(String city) {
        if (city == null) {
            return UNKNOWN;
        }
        return CITY_REGION.getOrDefault(city.trim().toLowerCase(Locale.ROOT), UNKNOWN);
    }

    /**
     * The inclusive {@code {low, high}} postcode range for {@code city}'s region, or {@code null}
     * when the city has no known region (in which case any 4-digit postcode is accepted).
     */
    public static int[] postcodeRangeOf(String city) {
        return REGION_POSTCODES.get(localityOf(city));
    }
}
