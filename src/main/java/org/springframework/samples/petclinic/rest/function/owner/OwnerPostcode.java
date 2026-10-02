package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Map;

/**
 * Canonical postcode rules for the owner pipelines, the single authority shared by the create
 * and update steps so both agree.
 *
 * <p>A postcode is optional. When present it must be exactly 4 digits. Each known city maps to a
 * region (Sydney->NSW, Melbourne->VIC, Brisbane->QLD) and each region to an inclusive 4-digit
 * range (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099); a present postcode must fall within its
 * city's region range. A city with no known region accepts any 4-digit postcode.
 */
final class OwnerPostcode {

    /** City -> canonical region. Matched exactly, mirroring {@code Owner.getLocality()}. */
    private static final Map<String, String> CITY_REGION = Map.of(
            "Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    /** Region -> inclusive 4-digit postcode range {low, high}. */
    private static final Map<String, int[]> REGION_RANGE = Map.of(
            "NSW", new int[] {2000, 2099},
            "VIC", new int[] {3000, 3099},
            "QLD", new int[] {4000, 4099});

    private OwnerPostcode() {
    }

    /**
     * Whether the given postcode is acceptable for the city. An absent (null/blank) postcode is
     * always acceptable — validation applies only when present. A present postcode must be 4
     * digits and, for a city with a known region, within that region's range; a city with no
     * known region accepts any 4-digit postcode.
     */
    static boolean isValid(String postcode, String city) {
        if (postcode == null || postcode.isBlank()) {
            return true;
        }
        if (!postcode.matches("[0-9]{4}")) {
            return false;
        }
        String region = city == null ? null : CITY_REGION.get(city);
        if (region == null) {
            return true;
        }
        int[] range = REGION_RANGE.get(region);
        if (range == null) {
            return true;
        }
        int value = Integer.parseInt(postcode);
        return value >= range[0] && value <= range[1];
    }
}
