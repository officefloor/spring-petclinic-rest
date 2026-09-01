package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Map;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.mapper.Locality;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.escalation.InvalidPostcodeException;

/**
 * When a postcode is supplied it must be exactly 4 digits and, for a city whose region is
 * known, fall inside that region's range (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099). A
 * city with no known region accepts any 4-digit postcode, and an absent postcode is left
 * untouched. Rejects an out-of-range or malformed value with a 400.
 */
public class EnsureOwnerPostcode {

    private static final Map<String, int[]> REGION_RANGES = Map.of(
            "NSW", new int[] {2000, 2099}, "VIC", new int[] {3000, 3099}, "QLD", new int[] {4000, 4099});

    public void service(@Val Owner owner) throws InvalidPostcodeException {
        String postcode = owner.getPostcode();
        if (postcode == null) {
            return;
        }
        if (!postcode.matches("\\d{4}")) {
            throw new InvalidPostcodeException("Postcode must be 4 digits");
        }
        int[] range = REGION_RANGES.get(Locality.of(owner.getCity()));
        if (range != null) {
            int value = Integer.parseInt(postcode);
            if (value < range[0] || value > range[1]) {
                throw new InvalidPostcodeException("Postcode is not valid for the owner's city");
            }
        }
    }
}
