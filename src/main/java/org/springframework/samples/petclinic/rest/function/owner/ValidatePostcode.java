package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Map;
import java.util.regex.Pattern;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidPostcodeException;
import org.springframework.samples.petclinic.rest.function.common.Localities;

/**
 * Postcode is optional. When absent (null or blank) the owner is left unchanged. When present it
 * must be exactly four digits and, for a city with a known region (see {@link Localities}), fall
 * within that region's inclusive range (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099). A city with
 * no known region accepts any 4-digit postcode. Throws {@link InvalidPostcodeException} for a 400
 * when the value is malformed or out of range for the city's region.
 */
public class ValidatePostcode {

    private static final Pattern FOUR_DIGITS = Pattern.compile("^[0-9]{4}$");

    /** Region -> inclusive 4-digit postcode range {low, high}. */
    private static final Map<String, int[]> REGION_POSTCODES = Map.of(
            "NSW", new int[] {2000, 2099},
            "VIC", new int[] {3000, 3099},
            "QLD", new int[] {4000, 4099});

    public void service(@Val OwnerFieldsDto request) throws InvalidPostcodeException {
        String postcode = request.getPostcode();
        if (postcode == null || postcode.isBlank()) {
            return;
        }
        if (!FOUR_DIGITS.matcher(postcode).matches()) {
            throw new InvalidPostcodeException(postcode);
        }
        int[] range = REGION_POSTCODES.get(Localities.region(request.getCity()));
        if (range != null) {
            int value = Integer.parseInt(postcode);
            if (value < range[0] || value > range[1]) {
                throw new InvalidPostcodeException(postcode);
            }
        }
    }
}
