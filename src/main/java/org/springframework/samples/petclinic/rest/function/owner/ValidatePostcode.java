package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Map;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidPostcodeException;

/**
 * Validates the create-owner request's {@code postcode} when one is supplied.
 *
 * <p>Postcode is optional: an absent (or blank) postcode passes untouched, keeping the
 * request contract backward-compatible. When present it must be four digits and, when the
 * owner's city maps to a known region, must fall inside that region's inclusive range per
 * the fixed table (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099). A city with no known
 * region accepts any 4-digit postcode. Anything else is rejected with an
 * {@link InvalidPostcodeException} (400). The valid, supplied value is left on the body so
 * {@link BuildOwner} stores and later steps return it.
 */
public class ValidatePostcode {

    /** City -> canonical region, matching the fixed city-to-region table. */
    private static final Map<String, String> CITY_REGION =
            Map.of("Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

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
            throw new InvalidPostcodeException("Postcode must be four digits");
        }
        String region = CITY_REGION.get(request.getCity());
        if (region == null) {
            return; // unknown region accepts any 4-digit postcode
        }
        int[] range = REGION_POSTCODES.get(region);
        int value = Integer.parseInt(postcode);
        if (value < range[0] || value > range[1]) {
            throw new InvalidPostcodeException(
                    "Postcode " + postcode + " is not valid for region " + region);
        }
    }
}
