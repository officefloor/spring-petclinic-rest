package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Decides which 4-digit postcode is valid for an owner's city. The acceptable range is
 * keyed by the city's region (via {@link Owner#regionForCity(String)}): NSW 2000-2099,
 * VIC 3000-3099, QLD 4000-4099. A city whose region is not in the range table (including
 * the {@code "UNKNOWN"} region) accepts any 4-digit postcode. Both the region ranges and
 * the postcode-to-region lookup live in {@link Owner} as the single source of truth; this
 * helper only combines them. Postcode format (exactly 4 digits) is enforced upstream by
 * the {@code @Pattern} constraint on the request DTO, so this helper range-checks an
 * already-well-formed postcode.
 */
final class OwnerPostcodes {

    private OwnerPostcodes() {
    }

    /**
     * Whether the (well-formed, 4-digit) postcode is valid for the given city. True when
     * the city's region has no known range (any 4-digit postcode is accepted), otherwise
     * true only when the postcode falls within that region's inclusive range — i.e. when
     * the postcode itself resolves to the city's region.
     */
    static boolean isValidForCity(String postcode, String city) {
        String region = Owner.regionForCity(city);
        if (!Owner.hasPostcodeRange(region)) {
            return true;
        }
        return region.equals(Owner.regionForPostcode(postcode));
    }
}
