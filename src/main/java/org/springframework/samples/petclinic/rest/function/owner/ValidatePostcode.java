package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Map;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.mapper.Locality;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidPostcodeException;

/**
 * Validates the create-owner {@code postcode} against the owner's city. The postcode is
 * optional: a request without one is accepted unchanged (the contract stays
 * backward-compatible). When present it has already passed the {@code ^[0-9]{4}$} bean
 * validation (so a malformed value is a 400 before this step). For a city whose region is
 * known (see {@link Locality}) the four digits must fall within that region's inclusive
 * range (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099); an out-of-range value raises
 * {@link InvalidPostcodeException} (400). A city with no known region accepts any 4-digit
 * postcode.
 */
public class ValidatePostcode {

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
        int[] range = REGION_POSTCODES.get(Locality.of(request.getCity()));
        if (range == null) {
            return; // city with no known region accepts any 4-digit postcode
        }
        int value = Integer.parseInt(postcode);
        if (value < range[0] || value > range[1]) {
            throw new InvalidPostcodeException(postcode, request.getCity());
        }
    }
}
