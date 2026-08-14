package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Map;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.escalation.InvalidPostcodeException;

/**
 * Runs in the create-owner pipeline after {@link BuildOwner}. Validates the owner's postcode ONLY
 * when one is present: an absent (or blank) postcode is optional and accepted, keeping the request
 * contract backward-compatible. When present it must be exactly four digits and, when the owner's
 * city maps to a known region ({@link Owner#getLocality()}), fall within that region's inclusive
 * postcode range (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099). A city with no known region
 * ('UNKNOWN') accepts any 4-digit postcode. Anything else is rejected with a 400.
 */
public class ValidateOwnerPostcode {

    /** Region -> inclusive 4-digit postcode range {low, high}. */
    private static final Map<String, int[]> REGION_POSTCODES = Map.of(
            "NSW", new int[] {2000, 2099},
            "VIC", new int[] {3000, 3099},
            "QLD", new int[] {4000, 4099});

    public void service(@Val Owner owner) throws InvalidPostcodeException {
        String postcode = owner.getPostcode();
        if (postcode == null || postcode.isBlank()) {
            return; // optional when absent
        }
        if (!postcode.matches("[0-9]{4}")) {
            throw new InvalidPostcodeException(postcode);
        }
        int[] range = REGION_POSTCODES.get(owner.getLocality());
        if (range != null) {
            int value = Integer.parseInt(postcode);
            if (value < range[0] || value > range[1]) {
                throw new InvalidPostcodeException(postcode);
            }
        }
    }
}
