package org.springframework.samples.petclinic.util;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * Validates an owner's 4-digit postcode against the postcode range of their city's region.
 * Ranges are keyed by the canonical region resolved via {@link CityRegions}; a city whose
 * region is {@link CityRegions#UNKNOWN} has no range and accepts any well-formed 4-digit
 * postcode.
 */
public final class Postcodes {

    /** A well-formed postcode is exactly four digits. */
    private static final Pattern FOUR_DIGITS = Pattern.compile("[0-9]{4}");

    /** Region -> inclusive 4-digit postcode range {low, high}. */
    private static final Map<String, int[]> REGION_RANGE = Map.of(
            "NSW", new int[] {2000, 2099},
            "VIC", new int[] {3000, 3099},
            "QLD", new int[] {4000, 4099});

    private Postcodes() {
    }

    /** Whether {@code postcode} is exactly four digits. */
    public static boolean isWellFormed(String postcode) {
        return postcode != null && FOUR_DIGITS.matcher(postcode).matches();
    }

    /**
     * Whether {@code postcode} is valid for {@code city}: it must be four digits and, when the
     * city's region has a known range, fall within it. A city with no known region accepts any
     * four-digit postcode.
     */
    public static boolean isValidForCity(String city, String postcode) {
        if (!isWellFormed(postcode)) {
            return false;
        }
        int[] range = REGION_RANGE.get(CityRegions.localityOf(city));
        if (range == null) {
            return true;
        }
        int value = Integer.parseInt(postcode);
        return value >= range[0] && value <= range[1];
    }
}
