package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Map;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Single source of truth for which 4-digit postcode is valid for an owner's city. The
 * postcode's acceptable range is keyed by the city's region (via
 * {@link Owner#regionForCity(String)}): NSW 2000-2099, VIC 3000-3099, QLD 4000-4099. A
 * city whose region is not in the range table (including the {@code "UNKNOWN"} region)
 * accepts any 4-digit postcode. Postcode format (exactly 4 digits) is enforced upstream
 * by the {@code @Pattern} constraint on the request DTO; this helper only range-checks an
 * already-well-formed postcode.
 */
final class OwnerPostcodes {

    /** Region -> inclusive 4-digit postcode range {low, high}. */
    private static final Map<String, int[]> REGION_RANGES = Map.of(
        "NSW", new int[] {2000, 2099},
        "VIC", new int[] {3000, 3099},
        "QLD", new int[] {4000, 4099});

    private OwnerPostcodes() {
    }

    /**
     * Whether the (well-formed, 4-digit) postcode is valid for the given city. True when
     * the city's region has no known range (any 4-digit postcode is accepted), otherwise
     * true only when the postcode falls within that region's inclusive range.
     */
    static boolean isValidForCity(String postcode, String city) {
        int[] range = REGION_RANGES.get(Owner.regionForCity(city));
        if (range == null) {
            return true;
        }
        int value = Integer.parseInt(postcode);
        return value >= range[0] && value <= range[1];
    }
}
