package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Map;

import org.springframework.samples.petclinic.mapper.LocalityLookup;

/**
 * Fixed region-to-postcode-range table used to validate an owner's postcode against its city.
 * The city's region is derived by {@link LocalityLookup#regionOf(String)}; a region absent from
 * this table (including the 'UNKNOWN' default for unmapped cities) accepts any 4-digit postcode.
 */
public final class PostcodeRegions {

    /** Region -> inclusive 4-digit postcode range {low, high}. */
    private static final Map<String, int[]> REGION_POSTCODES = Map.of(
            "NSW", new int[] {2000, 2099},
            "VIC", new int[] {3000, 3099},
            "QLD", new int[] {4000, 4099});

    private PostcodeRegions() {
    }

    /**
     * Whether {@code postcode} (already known to be four digits) is acceptable for {@code city}.
     * Cities whose region has no pinned range accept any 4-digit postcode.
     */
    public static boolean isValidFor(String city, String postcode) {
        int[] range = REGION_POSTCODES.get(LocalityLookup.regionOf(city));
        if (range == null) {
            return true;
        }
        int value = Integer.parseInt(postcode);
        return value >= range[0] && value <= range[1];
    }
}
