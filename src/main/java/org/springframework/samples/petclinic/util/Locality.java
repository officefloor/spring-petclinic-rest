package org.springframework.samples.petclinic.util;

import java.util.regex.Pattern;

/**
 * Derives an owner's locality (its canonical region). The postcode is preferred: the region whose
 * postcode range contains it (see {@link RegionPostcodes#regionOf(int)}) wins, which disambiguates
 * cities that share a name. Only when the postcode is absent, malformed or in no known range does it
 * fall back to the city-to-region table (see {@link CityRegion#of(String)}). Known cities therefore
 * resolve to the same region as before.
 */
public final class Locality {

    private static final Pattern FOUR_DIGITS = Pattern.compile("\\d{4}");

    private Locality() {
    }

    /**
     * The canonical region for {@code city} and {@code postcode}, preferring the postcode and falling
     * back to the city. Never {@code null}: an unresolved city yields {@link CityRegion#UNKNOWN}.
     */
    public static String of(String city, String postcode) {
        String fromPostcode = regionOfPostcode(postcode);
        return fromPostcode != null ? fromPostcode : CityRegion.of(city);
    }

    /** The region for a well-formed 4-digit postcode in a known range, else {@code null}. */
    private static String regionOfPostcode(String postcode) {
        if (postcode == null || !FOUR_DIGITS.matcher(postcode).matches()) {
            return null;
        }
        return RegionPostcodes.regionOf(Integer.parseInt(postcode));
    }
}
