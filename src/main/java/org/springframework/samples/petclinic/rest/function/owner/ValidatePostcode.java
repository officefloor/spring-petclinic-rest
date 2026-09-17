package org.springframework.samples.petclinic.rest.function.owner;

import java.util.regex.Pattern;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidPostcodeException;
import org.springframework.samples.petclinic.util.CityRegion;
import org.springframework.samples.petclinic.util.RegionPostcodes;

/**
 * Validates an owner's postcode <em>only when present</em>: a supplied postcode must be four digits
 * and fall within the range its city's region permits (see {@link RegionPostcodes}), with a city of
 * no known region ({@link CityRegion}) accepting any 4-digit code. A missing or blank postcode is
 * allowed, keeping the create/update contract backward-compatible. An offending postcode is a 400 via
 * {@link InvalidPostcodeException}.
 */
public class ValidatePostcode {

    private static final Pattern FOUR_DIGITS = Pattern.compile("\\d{4}");

    public void service(@Val OwnerFieldsDto request) throws InvalidPostcodeException {
        String postcode = request.getPostcode();
        if (postcode == null || postcode.isBlank()) {
            return;
        }
        if (!FOUR_DIGITS.matcher(postcode).matches()
                || !RegionPostcodes.allows(CityRegion.of(request.getCity()), Integer.parseInt(postcode))) {
            throw new InvalidPostcodeException(postcode, request.getCity());
        }
    }
}
