package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidPostcodeException;

/**
 * Step of the owner pipelines that validates the optional postcode against the owner's city region.
 * A postcode is validated only WHEN PRESENT: an absent (null/blank) postcode passes untouched, so
 * an owner supplied without a postcode is still accepted. When present it must be 4 digits and, for
 * a city with a known region, within that region's range (see {@link Postcode}); otherwise
 * {@link InvalidPostcodeException} is thrown (handled as 400).
 */
public class RequirePostcode {

    public void service(@Val OwnerFieldsDto request) throws InvalidPostcodeException {
        Postcode.validate(request.getPostcode(), request.getCity());
    }
}
