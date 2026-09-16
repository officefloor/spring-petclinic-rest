package org.springframework.samples.petclinic.rest.function.owner;

import java.util.regex.Pattern;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Locality;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidPostcodeException;

/**
 * Validates an owner request's optional postcode. When absent the request is accepted
 * unchanged; when present it must be four digits and, for a city whose region has a pinned
 * postcode range, fall within that range (see {@link Locality#postcodeAllowedForCity}).
 * Otherwise a {@link InvalidPostcodeException} is thrown (400). Reads the request already
 * published by an earlier step, so it sees the same normalized city later steps persist.
 */
public class ValidateOwnerPostcode {

    private static final Pattern FOUR_DIGITS = Pattern.compile("[0-9]{4}");

    public void service(@Val OwnerFieldsDto request) throws InvalidPostcodeException {
        String postcode = request.getPostcode();
        if (postcode == null) {
            return;
        }
        if (!FOUR_DIGITS.matcher(postcode).matches()) {
            throw new InvalidPostcodeException("Postcode must be 4 digits, but was '" + postcode + "'");
        }
        if (!Locality.postcodeAllowedForCity(request.getCity(), Integer.parseInt(postcode))) {
            throw new InvalidPostcodeException(
                    "Postcode " + postcode + " is not valid for city '" + request.getCity() + "'");
        }
    }
}
