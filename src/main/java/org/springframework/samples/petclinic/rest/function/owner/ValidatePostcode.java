package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Map;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.escalation.InvalidPostcodeException;

/**
 * Validates the owner's postcode WHEN PRESENT, rejecting with 400 via
 * {@link InvalidPostcodeException} when it is not valid for the owner's city.
 *
 * <p>Rules: an absent (null/blank) postcode is accepted (optional, backward
 * compatible). When present it must be a 4-digit value. For a city with a known
 * region it must fall within that region's inclusive postcode range; a city with no
 * known region accepts any 4-digit postcode.
 */
public class ValidatePostcode {

    /** Fixed city-to-region table, keyed by city name. */
    private static final Map<String, String> CITY_REGION = Map.of(
            "Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    /** Region-to-inclusive 4-digit postcode range {low, high}. */
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
            throw new InvalidPostcodeException("Postcode must be a 4-digit value");
        }
        String region = CITY_REGION.get(owner.getCity());
        if (region == null) {
            return; // a city with no known region accepts any 4-digit postcode
        }
        int[] range = REGION_POSTCODES.get(region);
        int value = Integer.parseInt(postcode);
        if (value < range[0] || value > range[1]) {
            throw new InvalidPostcodeException("Postcode '" + postcode + "' is not valid for city '"
                    + owner.getCity() + "' (region " + region + " expects " + range[0] + "-" + range[1] + ")");
        }
    }
}
