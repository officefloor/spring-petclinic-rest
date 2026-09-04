package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidPostcodeException;

/**
 * Validates the postcode on create WHEN PRESENT. A supplied postcode must be exactly four digits
 * and, for a city with a known region, fall within that region's range (see {@link PostcodeRegions});
 * otherwise the request is rejected 400 via {@link InvalidPostcodeException}. Postcode is optional:
 * when absent (null or blank) this step does nothing, keeping the create contract backward-compatible.
 * Reads the already-bound {@link OwnerFieldsDto}; the value is left untouched for {@link BuildOwner}
 * to map so it is stored and returned as given.
 */
public class ValidateOwnerPostcode {

    public void service(@Val OwnerFieldsDto request) throws InvalidPostcodeException {
        String postcode = request.getPostcode();
        if (postcode == null || postcode.isBlank()) {
            return;
        }
        if (!postcode.matches("^[0-9]{4}$")) {
            throw new InvalidPostcodeException("Postcode must be four digits: " + postcode);
        }
        if (!PostcodeRegions.isValidFor(request.getCity(), postcode)) {
            throw new InvalidPostcodeException(
                    "Postcode " + postcode + " is not valid for city " + request.getCity());
        }
    }
}
