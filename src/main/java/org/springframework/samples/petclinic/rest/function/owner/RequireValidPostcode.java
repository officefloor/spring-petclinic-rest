package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Map;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidPostcodeException;

/**
 * Rejects a create request whose optional {@code postcode} is present but invalid: it
 * must be four digits and, for a city with a known region, within that region's range
 * (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099). A city with no known region accepts any
 * four-digit value. Absent postcode is accepted (validated only when present).
 */
public class RequireValidPostcode {

    private static final Map<String, int[]> REGION_RANGE = Map.of(
            "NSW", new int[] {2000, 2099}, "VIC", new int[] {3000, 3099}, "QLD", new int[] {4000, 4099});

    public void service(@Val OwnerFieldsDto request) throws InvalidPostcodeException {
        String postcode = request.getPostcode();
        if (postcode == null) {
            return;
        }
        if (!postcode.matches("[0-9]{4}")) {
            throw new InvalidPostcodeException(postcode);
        }
        int[] range = REGION_RANGE.get(CityLocality.of(request.getCity()));
        int value = Integer.parseInt(postcode);
        if (range != null && (value < range[0] || value > range[1])) {
            throw new InvalidPostcodeException(postcode);
        }
    }
}
