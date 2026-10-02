package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Canonical postcode rules for the owner pipelines, the single authority shared by the create
 * and update steps so both agree.
 *
 * <p>A postcode is optional. When present it must be exactly 4 digits. Each known city maps to a
 * region (Sydney->NSW, Melbourne->VIC, Brisbane->QLD) and each region to an inclusive 4-digit
 * range (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099); a present postcode must fall within its
 * city's region range. A city with no known region accepts any 4-digit postcode.
 *
 * <p>The city->region and region->range tables are not kept here: they are the single authority
 * on {@link Owner} ({@link Owner#regionForCity} and {@link Owner#regionForPostcode}), reused so
 * postcode validation and the region-and-hash identity cannot drift apart.
 */
final class OwnerPostcode {

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
        String region = Owner.regionForCity(city);
        if (region == null) {
            return true;
        }
        // The postcode is valid for the city exactly when its own region range matches the
        // city's region; regions have distinct ranges, so a mismatch (or no containing range)
        // is out of range.
        return region.equals(Owner.regionForPostcode(postcode));
    }
}
