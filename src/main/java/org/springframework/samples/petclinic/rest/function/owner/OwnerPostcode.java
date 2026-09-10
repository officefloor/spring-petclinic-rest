package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.rest.function.common.Localities;

/**
 * Validates an owner's optional {@code postcode}. A postcode is validated only when present:
 * an absent (null or blank) postcode is accepted, keeping the create/update request contract
 * backward-compatible. When present it must be exactly 4 digits and, for a city whose region is
 * in the fixed table (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099), fall within that region's
 * range. A city with no known region accepts any 4-digit postcode.
 */
final class OwnerPostcode {

    private OwnerPostcode() {
    }

    /**
     * Validate the supplied postcode for the given city, throwing when it is present but invalid.
     * Does nothing when the postcode is absent.
     */
    static void validate(String postcode, String city) throws InvalidOwnerPostcodeException {
        if (postcode == null || postcode.isBlank()) {
            return; // optional when absent
        }
        if (!postcode.matches("[0-9]{4}")) {
            throw new InvalidOwnerPostcodeException("Postcode must be 4 digits, but was: '" + postcode + "'");
        }
        int[] range = Localities.postcodeRange(Localities.region(city));
        if (range == null) {
            return; // city with no known region accepts any 4-digit postcode
        }
        int value = Integer.parseInt(postcode);
        if (value < range[0] || value > range[1]) {
            throw new InvalidOwnerPostcodeException("Postcode '" + postcode + "' is out of range "
                    + range[0] + "-" + range[1] + " for city '" + city + "'");
        }
    }
}
