package org.springframework.samples.petclinic.rest.validation;

import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.samples.petclinic.mapper.OwnerMapper;

/**
 * Validates optional owner postcodes against the postcode range of the owner's city region.
 */
public final class OwnerPostcode {

    private static final Pattern FOUR_DIGITS = Pattern.compile("^[0-9]{4}$");

    /** Inclusive postcode range per region, keyed by the regions of {@link OwnerMapper#CITY_REGIONS}. */
    public static final Map<String, int[]> RANGES_BY_REGION = Map.of(
        "NSW", new int[] {2000, 2099},
        "VIC", new int[] {3000, 3099},
        "QLD", new int[] {4000, 4099});

    private OwnerPostcode() {
    }

    /**
     * The region whose range in {@link #RANGES_BY_REGION} contains the postcode.
     *
     * @param postcode the postcode, may be {@code null}
     * @return the region, or {@code null} when the postcode is absent, not 4 digits or in no known range
     */
    public static String regionOf(String postcode) {
        if (postcode == null || !FOUR_DIGITS.matcher(postcode).matches()) {
            return null;
        }
        int value = Integer.parseInt(postcode);
        for (Map.Entry<String, int[]> entry : RANGES_BY_REGION.entrySet()) {
            if (value >= entry.getValue()[0] && value <= entry.getValue()[1]) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * Checks a postcode for the given city. An absent postcode is accepted; a present one must be
     * 4 digits and, when the city has a known region, lie within that region's range.
     *
     * @param postcode the postcode as supplied in the request, may be {@code null}
     * @param city the owner's city
     * @throws InvalidPostcodeException if the postcode is malformed or out of range for the city's region
     */
    public static void validate(String postcode, String city) {
        if (postcode == null) {
            return;
        }
        if (!FOUR_DIGITS.matcher(postcode).matches()) {
            throw new InvalidPostcodeException(postcode, "Postcode must be 4 digits");
        }
        String region = city == null ? null : OwnerMapper.CITY_REGIONS.get(city);
        int[] range = region == null ? null : RANGES_BY_REGION.get(region);
        if (range == null) {
            return;
        }
        int value = Integer.parseInt(postcode);
        if (value < range[0] || value > range[1]) {
            throw new InvalidPostcodeException(postcode, "Postcode for " + city + " (" + region
                + ") must be between " + range[0] + " and " + range[1]);
        }
    }
}
