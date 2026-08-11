package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Map;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.mapper.Locality;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidPostcodeException;

/**
 * Step of {@code POST /api/owners}: validates the optional {@code postcode} WHEN PRESENT. A
 * supplied postcode must be four digits and, for a city with a known region (derived through
 * {@link Locality}: Sydney&rarr;NSW, Melbourne&rarr;VIC, Brisbane&rarr;QLD), fall inside that
 * region's inclusive range (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099). A city with no known
 * region accepts any 4-digit postcode. An absent postcode is left untouched, so the request
 * contract stays backward-compatible; anything invalid is rejected 400 via
 * {@link InvalidPostcodeException}.
 */
public class RequirePostcode {

    /** Region -&gt; inclusive 4-digit postcode range {low, high}. */
    private static final Map<String, int[]> REGION_POSTCODES = Map.of(
        "NSW", new int[] {2000, 2099}, "VIC", new int[] {3000, 3099}, "QLD", new int[] {4000, 4099});

    public void service(@Val OwnerFieldsDto request) throws InvalidPostcodeException {
        String postcode = request.getPostcode();
        if (postcode == null || postcode.isBlank()) {
            return; // optional when absent
        }
        if (!postcode.matches("[0-9]{4}")) {
            throw new InvalidPostcodeException(postcode);
        }
        int[] range = REGION_POSTCODES.get(Locality.of(request.getCity()));
        if (range != null) {
            int value = Integer.parseInt(postcode);
            if (value < range[0] || value > range[1]) {
                throw new InvalidPostcodeException(postcode);
            }
        }
    }
}
