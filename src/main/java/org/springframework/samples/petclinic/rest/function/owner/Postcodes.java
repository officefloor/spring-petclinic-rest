package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Map;

import org.springframework.samples.petclinic.model.CityRegion;

/**
 * Postcode validity: a postcode must be exactly four digits, and when the owner's city has a
 * known region (see {@link CityRegion}) it must fall within that region's pinned inclusive
 * range (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099). A city with no known region accepts any
 * four-digit postcode.
 */
final class Postcodes {

    /** Region -> inclusive {low, high} 4-digit postcode range. */
    private static final Map<String, int[]> REGION_RANGES = Map.of(
        "NSW", new int[] {2000, 2099},
        "VIC", new int[] {3000, 3099},
        "QLD", new int[] {4000, 4099});

    private Postcodes() {
    }

    /**
     * Whether {@code postcode} is valid for {@code city}: it must be exactly four digits and,
     * when the city's region has a pinned range, fall inside that range. Cities with no known
     * region accept any four-digit postcode.
     */
    static boolean isValidForCity(String postcode, String city) {
        if (postcode == null || !postcode.matches("\\d{4}")) {
            return false;
        }
        int[] range = REGION_RANGES.get(CityRegion.localityOf(city));
        if (range == null) {
            return true;
        }
        int value = Integer.parseInt(postcode);
        return value >= range[0] && value <= range[1];
    }
}
