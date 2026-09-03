package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Map;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.escalation.InvalidPostcodeException;

/**
 * Rejects a create when the owner supplies a postcode that is out of range for the
 * region derived from its city (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099). A city
 * with no known region, or an owner with no postcode, is accepted unchanged.
 */
public class EnsureOwnerPostcodeWithinRegion {

    private static final Map<String, int[]> REGION_RANGE = Map.of(
            "NSW", new int[] {2000, 2099}, "VIC", new int[] {3000, 3099}, "QLD", new int[] {4000, 4099});

    public void service(@Val Owner owner) throws InvalidPostcodeException {
        String postcode = owner.getPostcode();
        int[] range = REGION_RANGE.get(Locality.of(owner));
        if (postcode == null || range == null) {
            return;
        }
        int value = Integer.parseInt(postcode);
        if (value < range[0] || value > range[1]) {
            throw new InvalidPostcodeException("Postcode " + postcode + " is not valid for this city");
        }
    }
}
