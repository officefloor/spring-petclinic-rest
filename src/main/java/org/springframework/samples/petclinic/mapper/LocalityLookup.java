package org.springframework.samples.petclinic.mapper;

import java.util.Map;

/**
 * Derives an owner's {@code locality} from its postcode and city. The postcode
 * range is preferred: it is looked up against the fixed region ranges first, and
 * only when the postcode is absent or falls in no known range does the derivation
 * fall back to the fixed city-to-region table. Kept out of the mapper interface
 * deliberately: MapStruct treats any single-argument method on a {@code @Mapper}
 * interface as a candidate mapping method, so a {@code String -> String} helper
 * there would be applied to every String property. This static lookup is
 * referenced only from an explicit mapping expression instead.
 */
public final class LocalityLookup {

    /** City -> canonical region; an exact, case-sensitive mapping. */
    private static final Map<String, String> CITY_REGION = Map.of(
        "Sydney", "NSW",
        "Melbourne", "VIC",
        "Brisbane", "QLD");

    /** Region -> inclusive 4-digit postcode range {low, high}. */
    private static final Map<String, int[]> REGION_POSTCODES = Map.of(
        "NSW", new int[] {2000, 2099},
        "VIC", new int[] {3000, 3099},
        "QLD", new int[] {4000, 4099});

    /** Region -> IANA timezone name; a fixed, exact mapping. */
    private static final Map<String, String> REGION_TIMEZONE = Map.of(
        "NSW", "Australia/Sydney",
        "VIC", "Australia/Melbourne",
        "QLD", "Australia/Brisbane");

    private LocalityLookup() {
    }

    /**
     * Returns the canonical region for {@code city} from the fixed table, or
     * {@code 'UNKNOWN'} when the city is not in the table.
     *
     * @param city the owner's city, possibly {@code null}
     * @return the canonical region, or {@code 'UNKNOWN'} if the city is unknown
     */
    public static String regionFor(String city) {
        return CITY_REGION.getOrDefault(city, "UNKNOWN");
    }

    /**
     * Returns the canonical region, preferring the postcode over the city. The
     * postcode is matched against the fixed region ranges first; only when it is
     * absent or in no known range does the lookup fall back to the city table.
     * This yields the same region for known cities but disambiguates cities that
     * share a name.
     *
     * @param postcode the owner's postcode, possibly {@code null}
     * @param city the owner's city, possibly {@code null}
     * @return the canonical region, or {@code 'UNKNOWN'} if neither resolves
     */
    public static String regionFor(String postcode, String city) {
        String fromPostcode = regionForPostcode(postcode);
        return fromPostcode != null ? fromPostcode : regionFor(city);
    }

    /**
     * Returns the IANA timezone name for the region derived from {@code postcode}
     * and {@code city}, from the fixed region-to-timezone table, or {@code null}
     * when the region is not in the table.
     *
     * @param postcode the owner's postcode, possibly {@code null}
     * @param city the owner's city, possibly {@code null}
     * @return the IANA timezone name, or {@code null} if the region is unknown
     */
    public static String timezoneFor(String postcode, String city) {
        return REGION_TIMEZONE.get(regionFor(postcode, city));
    }

    /**
     * Returns the canonical region whose postcode range contains {@code postcode},
     * or {@code null} when the postcode is absent, non-numeric, or in no known range.
     */
    private static String regionForPostcode(String postcode) {
        if (postcode == null) {
            return null;
        }
        int value;
        try {
            value = Integer.parseInt(postcode.trim());
        }
        catch (NumberFormatException ex) {
            return null;
        }
        for (Map.Entry<String, int[]> entry : REGION_POSTCODES.entrySet()) {
            int[] range = entry.getValue();
            if (value >= range[0] && value <= range[1]) {
                return entry.getKey();
            }
        }
        return null;
    }
}
