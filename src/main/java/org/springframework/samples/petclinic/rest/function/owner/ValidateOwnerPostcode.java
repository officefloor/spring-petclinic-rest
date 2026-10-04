package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Create-time guard for {@code POST /owners}: validates {@code postcode} <em>only when present</em>.
 * A supplied postcode must be exactly four digits and, when the city maps to a known region, fall
 * within that region's inclusive range; otherwise the request is rejected with 400 via
 * {@link InvalidPostcodeException}. A city with no known region accepts any 4-digit postcode, and
 * an absent (null/blank) postcode is accepted unchanged — so the request contract stays
 * backward-compatible.
 *
 * <p>The city-to-region decision comes from {@link OwnerLocality}, the single shared table also
 * used by {@code OwnerMapper.locality} for the read-time {@code locality}, so the two sides of the
 * rule cannot drift.
 */
public class ValidateOwnerPostcode {

    public void service(@Val OwnerFieldsDto request) throws InvalidPostcodeException {
        String postcode = request.getPostcode();
        if (postcode == null || postcode.isBlank()) {
            return; // optional when absent
        }
        if (!postcode.matches("[0-9]{4}")) {
            throw new InvalidPostcodeException(postcode);
        }
        String region = OwnerLocality.regionForCity(request.getCity());
        int[] range = OwnerLocality.postcodeRange(region);
        if (range != null) {
            int value = Integer.parseInt(postcode);
            if (value < range[0] || value > range[1]) {
                throw new InvalidPostcodeException(postcode);
            }
        }
    }
}
