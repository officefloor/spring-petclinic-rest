package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidPostcodeException;

/**
 * When the create request carries a postcode, requires it to be 4 digits and, for a city with a
 * known region, to fall in that region's range (Sydney/NSW 2000-2099, Melbourne/VIC 3000-3099,
 * Brisbane/QLD 4000-4099). A city with no known region accepts any 4-digit value. A missing
 * postcode is left untouched; an invalid one is rejected with 400.
 */
public class ValidatePostcode {

    public void service(@Val OwnerFieldsDto request) throws InvalidPostcodeException {
        String postcode = request.getPostcode();
        if (postcode == null) {
            return;
        }
        boolean valid = postcode.matches("\\d{4}");
        if (valid) {
            int value = Integer.parseInt(postcode);
            String city = request.getCity();
            if ("Sydney".equals(city)) {
                valid = value >= 2000 && value <= 2099;
            }
            else if ("Melbourne".equals(city)) {
                valid = value >= 3000 && value <= 3099;
            }
            else if ("Brisbane".equals(city)) {
                valid = value >= 4000 && value <= 4099;
            }
        }
        if (!valid) {
            throw new InvalidPostcodeException(postcode);
        }
    }
}
