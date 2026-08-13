package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Map;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidPostcodeException;
import org.springframework.samples.petclinic.rest.function.common.Localities;

/**
 * Validates the owner's postcode WHEN PRESENT, reading the published request body. An absent or
 * blank postcode is accepted (the field is optional). When supplied it must be exactly four digits;
 * if the city has a known region ({@link Localities}), the code must also fall inside that region's
 * inclusive range (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099). A city with no known region
 * accepts any 4-digit postcode. Anything else is rejected with 400 via
 * {@link InvalidPostcodeException}.
 */
public class ValidateOwnerPostcode {

    /** Region -> inclusive 4-digit postcode range {low, high}. */
    private static final Map<String, int[]> REGION_POSTCODES = Map.of(
            "NSW", new int[] {2000, 2099},
            "VIC", new int[] {3000, 3099},
            "QLD", new int[] {4000, 4099});

    public void service(@Val OwnerFieldsDto request) throws InvalidPostcodeException {
        String postcode = request.getPostcode();
        if (postcode == null || postcode.isBlank()) {
            return; // optional when absent
        }
        if (!postcode.matches("[0-9]{4}")) {
            throw new InvalidPostcodeException(postcode);
        }
        int[] range = REGION_POSTCODES.get(Localities.of(request.getCity()));
        if (range == null) {
            return; // city with no known region accepts any 4-digit postcode
        }
        int value = Integer.parseInt(postcode);
        if (value < range[0] || value > range[1]) {
            throw new InvalidPostcodeException(postcode);
        }
    }
}
