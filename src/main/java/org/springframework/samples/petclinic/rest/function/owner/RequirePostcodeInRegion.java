package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.PostcodeRange;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidPostcodeException;

/**
 * Rejects a create-owner request whose supplied postcode is malformed (not exactly four digits)
 * or out of range for the owner's city region, responding 400 via {@link InvalidPostcodeException}.
 * Postcode is validated only WHEN PRESENT: an absent postcode is accepted, keeping the request
 * contract backward-compatible. Region membership is checked against the pinned
 * {@link PostcodeRange} table; a city with no known region accepts any 4-digit postcode.
 */
public class RequirePostcodeInRegion {

    private static final String FOUR_DIGITS = "[0-9]{4}";

    public void service(@Val OwnerFieldsDto request) throws InvalidPostcodeException {
        String postcode = request.getPostcode();
        if (postcode == null) {
            return;
        }
        if (!postcode.matches(FOUR_DIGITS)
                || !PostcodeRange.isValidForCity(request.getCity(), postcode)) {
            throw new InvalidPostcodeException(request.getCity(), postcode);
        }
    }
}
