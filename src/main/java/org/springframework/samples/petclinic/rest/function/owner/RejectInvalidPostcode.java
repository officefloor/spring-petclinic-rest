package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Map;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.mapper.Locality;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidPostcodeException;

/**
 * Rejects a create body whose 4-digit postcode falls outside the range fixed for the
 * city's region (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099), so the endpoint responds
 * 400. The postcode is optional: an absent value is accepted, and a city with no known
 * region ({@link Locality#of} returns {@code UNKNOWN}) accepts any 4-digit value. The
 * 4-digit format itself is enforced by bean validation on the body.
 */
public class RejectInvalidPostcode {

    private static final Map<String, int[]> REGION_RANGE = Map.of(
            "NSW", new int[] {2000, 2099}, "VIC", new int[] {3000, 3099}, "QLD", new int[] {4000, 4099});

    public void service(@Val OwnerFieldsDto request) throws InvalidPostcodeException {
        String postcode = request.getPostcode();
        if (postcode == null) {
            return;
        }
        if (!postcode.matches("[0-9]{4}")) {
            throw new InvalidPostcodeException(postcode, request.getCity());
        }
        int[] range = REGION_RANGE.get(Locality.of(request.getCity()));
        if (range == null) {
            return;
        }
        int value = Integer.parseInt(postcode);
        if (value < range[0] || value > range[1]) {
            throw new InvalidPostcodeException(postcode, request.getCity());
        }
    }
}
