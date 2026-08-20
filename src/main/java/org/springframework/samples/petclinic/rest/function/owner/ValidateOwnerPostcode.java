package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Map;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.mapper.LocalityLookup;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidOwnerPostcodeException;

/**
 * Validates the owner's postcode WHEN PRESENT. An absent (null/blank) postcode is accepted,
 * keeping the request contract backward-compatible. When supplied it must be exactly 4 digits
 * and, if the city maps to a known region, fall within that region's inclusive range
 * (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099). A city with no known region accepts any
 * 4-digit postcode. Anything else is rejected with a 400 via
 * {@link InvalidOwnerPostcodeException}.
 */
public class ValidateOwnerPostcode {

    /** Region -> inclusive 4-digit postcode range {low, high}. */
    private static final Map<String, int[]> REGION_POSTCODES = Map.of(
            "NSW", new int[] {2000, 2099},
            "VIC", new int[] {3000, 3099},
            "QLD", new int[] {4000, 4099});

    public void service(@Val OwnerFieldsDto request) throws InvalidOwnerPostcodeException {
        String postcode = request.getPostcode();
        if (postcode == null || postcode.isBlank()) {
            return; // optional when absent
        }
        String city = request.getCity();
        if (!postcode.matches("[0-9]{4}")) {
            throw new InvalidOwnerPostcodeException(postcode, city);
        }
        int[] range = REGION_POSTCODES.get(LocalityLookup.regionFor(city));
        if (range == null) {
            return; // city with no known region accepts any 4-digit postcode
        }
        int value = Integer.parseInt(postcode);
        if (value < range[0] || value > range[1]) {
            throw new InvalidOwnerPostcodeException(postcode, city);
        }
    }
}
