package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Map;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidPostcodeException;
import org.springframework.samples.petclinic.util.Locality;

/**
 * Validates the optional 'postcode' WHEN PRESENT. A supplied postcode must be exactly four digits and,
 * for a city whose region is known ({@link Locality}: Sydney->NSW, Melbourne->VIC, Brisbane->QLD), must
 * fall within that region's inclusive range (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099). A city with
 * no known region (locality {@code UNKNOWN}) accepts any 4-digit postcode. An out-of-range or malformed
 * postcode is rejected by throwing {@link InvalidPostcodeException} (handled as 400). When no postcode is
 * supplied the request is left untouched, keeping the create contract backward-compatible.
 */
public class ValidateOwnerPostcode {

    /** Region -> inclusive 4-digit postcode range {low, high}. */
    private static final Map<String, int[]> REGION_POSTCODES = Map.of(
            "NSW", new int[] {2000, 2099},
            "VIC", new int[] {3000, 3099},
            "QLD", new int[] {4000, 4099});

    public void service(@Val OwnerFieldsDto request) throws InvalidPostcodeException {
        String postcode = request.getPostcode();
        if (postcode == null) {
            return; // optional: absent postcode is not validated
        }
        if (!postcode.matches("[0-9]{4}")) {
            throw new InvalidPostcodeException(postcode, "Postcode must be four digits: " + postcode);
        }
        int[] range = REGION_POSTCODES.get(Locality.of(request.getCity()));
        if (range == null) {
            return; // city with no known region accepts any 4-digit postcode
        }
        int value = Integer.parseInt(postcode);
        if (value < range[0] || value > range[1]) {
            throw new InvalidPostcodeException(postcode, String.format(
                    "Postcode %s is out of range %d-%d for city '%s'",
                    postcode, range[0], range[1], request.getCity()));
        }
    }
}
