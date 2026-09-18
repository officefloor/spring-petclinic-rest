package org.springframework.samples.petclinic.rest.function.owner;

import java.util.regex.Pattern;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.CityRegion;
import org.springframework.samples.petclinic.model.RegionPostcodes;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidPostcodeException;
import org.springframework.util.StringUtils;

/**
 * Validates the optional postcode of an owner request. When present it must be a 4-digit value that
 * is valid for the region derived from the owner's city ({@link CityRegion} then
 * {@link RegionPostcodes}): a city with no known region accepts any 4-digit postcode. An absent (or
 * blank) postcode is left untouched, since postcode is optional. Rejects a present-but-invalid
 * postcode with 400.
 */
public class ValidateOwnerPostcode {

    private static final Pattern FOUR_DIGITS = Pattern.compile("[0-9]{4}");

    public void service(@Val OwnerFieldsDto request) throws InvalidPostcodeException {
        String postcode = request.getPostcode();
        if (!StringUtils.hasText(postcode)) {
            return;
        }
        if (!FOUR_DIGITS.matcher(postcode).matches()
                || !RegionPostcodes.isInRange(CityRegion.of(request.getCity()), Integer.parseInt(postcode))) {
            throw new InvalidPostcodeException(postcode, request.getCity());
        }
    }
}
