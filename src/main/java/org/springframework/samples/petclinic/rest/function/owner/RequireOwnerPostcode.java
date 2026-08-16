package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Map;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.OwnerPostcodeInvalidException;

/**
 * Validates the owner's optional {@code postcode} WHEN PRESENT. A supplied postcode must be
 * exactly four digits and, when the owner's city maps to a known region, must fall within that
 * region's inclusive range (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099). A city with no known
 * region accepts any four-digit postcode. An absent postcode is left untouched — postcode is
 * optional, so the request contract stays backward-compatible.
 *
 * <p>Reads the body published by {@link RequireOwnerFields}; runs before {@link BuildOwner} so an
 * out-of-range postcode is a 400 via {@link OwnerPostcodeInvalidException}, never stored.
 */
public class RequireOwnerPostcode {

    /** City -> region, using the same fixed table as the owner locality mapping. */
    private static final Map<String, String> CITY_REGION =
            Map.of("Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    /** Region -> inclusive {low, high} 4-digit postcode range. */
    private static final Map<String, int[]> REGION_POSTCODES = Map.of(
            "NSW", new int[] {2000, 2099},
            "VIC", new int[] {3000, 3099},
            "QLD", new int[] {4000, 4099});

    public void service(@Val OwnerFieldsDto request) throws OwnerPostcodeInvalidException {
        String postcode = request.getPostcode();
        if (postcode == null) {
            return; // optional when absent
        }
        if (!postcode.matches("[0-9]{4}")) {
            throw new OwnerPostcodeInvalidException(postcode);
        }
        String region = CITY_REGION.get(request.getCity());
        int[] range = region == null ? null : REGION_POSTCODES.get(region);
        if (range != null) {
            int value = Integer.parseInt(postcode);
            if (value < range[0] || value > range[1]) {
                throw new OwnerPostcodeInvalidException(postcode);
            }
        }
    }
}
