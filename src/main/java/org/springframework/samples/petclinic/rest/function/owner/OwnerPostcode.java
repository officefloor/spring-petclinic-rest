package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Map;

import org.springframework.samples.petclinic.rest.escalation.OwnerPostcodeInvalidException;

/**
 * Shared postcode handling for owner endpoints. An owner's postcode is optional; when supplied it
 * must be four digits and, when the city maps to a known region via the fixed city-to-region table
 * ({@code Sydney -> NSW}, {@code Melbourne -> VIC}, {@code Brisbane -> QLD}), it must fall within
 * that region's inclusive range ({@code NSW 2000-2099}, {@code VIC 3000-3099}, {@code QLD 4000-4099}).
 * A city with no known region accepts any 4-digit postcode. The value is stored and returned as
 * given.
 */
final class OwnerPostcode {

    /** City -&gt; canonical region. Anything not listed has no known region (any 4-digit is accepted). */
    private static final Map<String, String> CITY_REGION = Map.of(
            "Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    /** Region -&gt; inclusive 4-digit postcode range {@code {low, high}}. */
    private static final Map<String, int[]> REGION_RANGE = Map.of(
            "NSW", new int[] {2000, 2099}, "VIC", new int[] {3000, 3099}, "QLD", new int[] {4000, 4099});

    private OwnerPostcode() {
    }

    /**
     * Validates the supplied postcode for the given city. A null or blank postcode is accepted
     * (the field is optional). A non-blank postcode must be exactly four digits and, when the city
     * has a known region, must fall within that region's range.
     *
     * @throws OwnerPostcodeInvalidException when a supplied postcode is malformed or out of range.
     */
    static void validate(String postcode, String city) throws OwnerPostcodeInvalidException {
        if (postcode == null || postcode.isBlank()) {
            return;
        }
        if (!postcode.matches("[0-9]{4}")) {
            throw new OwnerPostcodeInvalidException(postcode);
        }
        String region = city == null ? null : CITY_REGION.get(city);
        int[] range = region == null ? null : REGION_RANGE.get(region);
        if (range != null) {
            int value = Integer.parseInt(postcode);
            if (value < range[0] || value > range[1]) {
                throw new OwnerPostcodeInvalidException(postcode);
            }
        }
    }
}
