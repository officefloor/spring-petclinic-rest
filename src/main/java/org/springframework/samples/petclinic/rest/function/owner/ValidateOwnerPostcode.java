package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.mapper.CityRegion;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidPostcodeException;

/**
 * Validates the owner's {@code postcode} WHEN PRESENT, so an owner created without a postcode stays
 * accepted (the request contract is backward-compatible). A supplied postcode must be exactly four
 * digits and, when the city has a known region, must fall within that region's fixed range
 * (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099); a city with no known region accepts any 4-digit
 * postcode. Anything else is rejected with 400 via {@link InvalidPostcodeException}.
 *
 * <p>Reads the body published by the preceding validate step via {@code @Val}, so it runs before the
 * owner is built/loaded and an invalid postcode is a 400 rather than being masked by a later step.
 */
public class ValidateOwnerPostcode {

    public void service(@Val OwnerFieldsDto request) throws InvalidPostcodeException {
        String postcode = request.getPostcode();
        if (postcode == null || postcode.isBlank()) {
            return; // optional when absent
        }
        String city = request.getCity();
        if (!postcode.matches("[0-9]{4}")) {
            throw new InvalidPostcodeException(postcode, city, "must be exactly four digits");
        }
        int[] range = CityRegion.postcodeRange(city);
        if (range == null) {
            return; // no known region for this city; any 4-digit postcode is valid
        }
        int value = Integer.parseInt(postcode);
        if (value < range[0] || value > range[1]) {
            throw new InvalidPostcodeException(postcode, city,
                    "out of range " + range[0] + "-" + range[1] + " for the city's region");
        }
    }
}
