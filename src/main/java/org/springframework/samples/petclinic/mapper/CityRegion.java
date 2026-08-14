package org.springframework.samples.petclinic.mapper;

import java.util.Map;

/**
 * Derives the canonical region (locality) for a city from a fixed city-to-region table.
 *
 * <p>Kept as a standalone helper (rather than a method on {@link OwnerMapper}) so MapStruct does
 * not mistake it for a {@code String -> String} mapping method and apply it to unrelated fields.
 */
public final class CityRegion {

    /** City -> canonical region; anything not listed derives locality "UNKNOWN". */
    private static final Map<String, String> CITY_REGION = Map.of(
            "Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    /** Region -> inclusive {low, high} 4-digit postcode range. */
    private static final Map<String, int[]> REGION_POSTCODES = Map.of(
            "NSW", new int[] {2000, 2099}, "VIC", new int[] {3000, 3099}, "QLD", new int[] {4000, 4099});

    /** Region -> IANA timezone name. */
    private static final Map<String, String> REGION_TIMEZONE = Map.of(
            "NSW", "Australia/Sydney", "VIC", "Australia/Melbourne", "QLD", "Australia/Brisbane");

    private CityRegion() {
    }

    /**
     * The canonical region, preferring the postcode: if {@code postcode} is a 4-digit code inside a
     * known region's range it wins, disambiguating cities that share a name. Otherwise (postcode
     * absent or in no known range) fall back to the city-to-region table, or "UNKNOWN".
     */
    public static String locality(String postcode, String city) {
        String byPostcode = regionForPostcode(postcode);
        return byPostcode != null ? byPostcode : locality(city);
    }

    /** The canonical region for {@code city}, or "UNKNOWN" when the city is not in the table. */
    public static String locality(String city) {
        return CITY_REGION.getOrDefault(city, "UNKNOWN");
    }

    /**
     * The IANA timezone name for {@code region} via the fixed region-to-timezone table
     * (NSW->Australia/Sydney, VIC->Australia/Melbourne, QLD->Australia/Brisbane), or {@code null}
     * when the region is not one of these.
     */
    public static String timezone(String region) {
        return REGION_TIMEZONE.get(region);
    }

    /** The region whose postcode range contains {@code postcode}, or {@code null} if none does. */
    private static String regionForPostcode(String postcode) {
        if (postcode == null || !postcode.matches("\\d{4}")) {
            return null;
        }
        int code = Integer.parseInt(postcode);
        for (Map.Entry<String, int[]> entry : REGION_POSTCODES.entrySet()) {
            int[] range = entry.getValue();
            if (code >= range[0] && code <= range[1]) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * The inclusive {@code {low, high}} 4-digit postcode range valid for {@code city}'s region, or
     * {@code null} when the city has no known region (in which case any 4-digit postcode is valid).
     */
    public static int[] postcodeRange(String city) {
        return REGION_POSTCODES.get(locality(city));
    }
}
