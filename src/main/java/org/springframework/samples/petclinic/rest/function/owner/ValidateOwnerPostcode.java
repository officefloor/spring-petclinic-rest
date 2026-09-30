package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Map;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidPostcodeException;

/**
 * Validates the owner's postcode against the city's region when a postcode is present. The 4-digit
 * shape is already enforced by the {@code OwnerFieldsDto} bean-validation pattern (a malformed
 * postcode is rejected 400 before this step); here the value is range-checked against the fixed
 * city-to-region postcode table (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099). A city with no known
 * region accepts any 4-digit postcode. When absent, postcode is optional and skipped entirely so the
 * request contract stays backward-compatible. An out-of-range postcode is rejected 400 via
 * {@link InvalidPostcodeException}.
 */
public class ValidateOwnerPostcode {

    /** Region -> inclusive 4-digit postcode range {low, high}. */
    private static final Map<String, int[]> REGION_POSTCODES = Map.of(
        "NSW", new int[] {2000, 2099}, "VIC", new int[] {3000, 3099}, "QLD", new int[] {4000, 4099});

    public void service(@Val OwnerFieldsDto request) throws InvalidPostcodeException {
        String postcode = request.getPostcode();
        if (postcode == null || postcode.isEmpty()) {
            return; // optional when absent
        }
        String region = Owner.regionOf(request.getCity());
        if (region == null) {
            return; // city with no known region accepts any 4-digit postcode
        }
        int[] range = REGION_POSTCODES.get(region);
        int value = Integer.parseInt(postcode); // 4-digit shape guaranteed by the DTO pattern
        if (value < range[0] || value > range[1]) {
            throw new InvalidPostcodeException(
                "Postcode " + postcode + " is not valid for city " + request.getCity());
        }
    }
}
