package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Map;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.escalation.InvalidPostcodeException;

/**
 * When an owner supplies a postcode, rejects it when it falls outside the inclusive range for the
 * city's region (Sydney/NSW 2000-2099, Melbourne/VIC 3000-3099, Brisbane/QLD 4000-4099). A city
 * with no known region accepts any 4-digit postcode, and an absent postcode is always accepted.
 * The 4-digit shape is already enforced by the request DTO, so a present postcode parses cleanly.
 */
public class ValidatePostcode {

    private static final Map<String, int[]> REGION_RANGES = Map.of(
            "NSW", new int[] {2000, 2099}, "VIC", new int[] {3000, 3099}, "QLD", new int[] {4000, 4099});

    private static final Map<String, String> CITY_REGION = Map.of(
            "Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    public void service(@Val Owner owner) throws InvalidPostcodeException {
        String postcode = owner.getPostcode();
        if (postcode == null) {
            return;
        }
        String region = CITY_REGION.get(owner.getCity());
        if (region == null) {
            return;
        }
        int[] range = REGION_RANGES.get(region);
        int value = Integer.parseInt(postcode);
        if (value < range[0] || value > range[1]) {
            throw new InvalidPostcodeException(owner.getCity(), postcode);
        }
    }
}
