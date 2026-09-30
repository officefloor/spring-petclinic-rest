package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.CityRegions;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidPostcodeException;

/**
 * Validates the owner's postcode against the city's region, but only WHEN PRESENT — a
 * request with no postcode passes through unchanged (the create/update contract stays
 * backward-compatible). Its 4-digit shape is already enforced by the {@code OwnerFields}
 * schema pattern; this step applies the region-specific range rule. For a city with a known
 * region (Sydney=NSW 2000-2099, Melbourne=VIC 3000-3099, Brisbane=QLD 4000-4099) the postcode
 * must fall within that region's inclusive range; a city with no known region accepts any
 * 4-digit postcode. An out-of-range postcode is rejected 400 via {@link InvalidPostcodeException}.
 */
public class EnsurePostcodeValidForCity {

    public void service(@Val OwnerFieldsDto request) throws InvalidPostcodeException {
        String postcode = request.getPostcode();
        if (postcode == null || postcode.isEmpty()) {
            return; // optional: validated only when present
        }
        String region = CityRegions.regionOf(request.getCity());
        if (region == null) {
            return; // city has no known region -> any 4-digit postcode is accepted
        }
        int[] range = CityRegions.postcodeRange(region); // single shared source of the range fact
        int value = Integer.parseInt(postcode);
        if (value < range[0] || value > range[1]) {
            throw new InvalidPostcodeException(request.getCity(), postcode);
        }
    }
}
