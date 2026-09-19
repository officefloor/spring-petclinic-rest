package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidPostcodeException;
import org.springframework.samples.petclinic.util.CityLocality;
import org.springframework.samples.petclinic.util.Postcodes;

/**
 * Validates an owner request's optional postcode: absent or blank is left untouched
 * (the field is optional and the contract stays backward-compatible). When present it
 * must be four digits and, for a city in a known region, fall within that region's
 * range; otherwise the request is rejected with a 400. A city with no known region
 * accepts any 4-digit postcode. The value is stored verbatim, so nothing is mutated.
 */
public class ValidateOwnerPostcode {

    public void service(@Val OwnerFieldsDto request) throws InvalidPostcodeException {
        String postcode = request.getPostcode();
        if (postcode == null || postcode.isBlank()) {
            return;
        }
        if (!Postcodes.hasValidFormat(postcode)) {
            throw new InvalidPostcodeException(postcode, "must be four digits");
        }
        String region = CityLocality.forCity(request.getCity());
        if (!Postcodes.isAllowedForRegion(region, postcode)) {
            throw new InvalidPostcodeException(postcode,
                    "outside the postcode range for region " + region);
        }
    }
}
