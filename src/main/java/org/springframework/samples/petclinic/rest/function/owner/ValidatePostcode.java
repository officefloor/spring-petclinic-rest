package org.springframework.samples.petclinic.rest.function.owner;

import java.util.List;
import java.util.Map;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Locality;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.MissingFieldsException;

/**
 * Validates an owner's optional postcode when present: it must be four digits and, when the city's
 * region is known, fall within that region's fixed range (NSW 2000-2099, VIC 3000-3099,
 * QLD 4000-4099). A city with no known region accepts any 4-digit postcode. An absent postcode is
 * left untouched, keeping the create request backward-compatible. A bad postcode is a 400 via
 * {@link MissingFieldsException}.
 */
public class ValidatePostcode {

    /** Region -> inclusive {low, high} 4-digit postcode range. */
    private static final Map<String, int[]> REGION_RANGES = Map.of(
        "NSW", new int[] {2000, 2099},
        "VIC", new int[] {3000, 3099},
        "QLD", new int[] {4000, 4099});

    public void service(@Val OwnerFieldsDto request) throws MissingFieldsException {
        String postcode = request.getPostcode();
        if (postcode == null) {
            return;
        }
        int[] range = REGION_RANGES.get(Locality.of(request.getCity()));
        if (!postcode.matches("[0-9]{4}") || (range != null
                && (Integer.parseInt(postcode) < range[0] || Integer.parseInt(postcode) > range[1]))) {
            throw new MissingFieldsException(List.of("postcode"));
        }
    }
}
