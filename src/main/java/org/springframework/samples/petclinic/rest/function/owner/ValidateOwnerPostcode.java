package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidPostcodeException;
import org.springframework.samples.petclinic.util.PostcodeRange;

/**
 * Validates the optional owner postcode. Postcode is optional, so a null or blank value
 * is left untouched; when present it must be exactly 4 digits and — for a city with a
 * known region — fall within that region's range (otherwise a 400 via
 * {@link InvalidPostcodeException}). A city with no known region accepts any 4-digit
 * postcode. The value is stored and returned unchanged.
 */
public class ValidateOwnerPostcode {

    public void service(@Val OwnerFieldsDto request) throws InvalidPostcodeException {
        String postcode = request.getPostcode();
        if (postcode == null || postcode.isBlank()) {
            return;
        }
        if (!postcode.matches("[0-9]{4}")) {
            throw new InvalidPostcodeException(postcode, "must be exactly 4 digits");
        }
        if (!PostcodeRange.isValidForCity(request.getCity(), postcode)) {
            throw new InvalidPostcodeException(postcode,
                "out of range for city '" + request.getCity() + "'");
        }
    }
}
