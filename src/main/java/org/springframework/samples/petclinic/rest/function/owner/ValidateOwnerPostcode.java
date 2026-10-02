package org.springframework.samples.petclinic.rest.function.owner;

import java.util.regex.Pattern;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidPostcodeException;

/**
 * Validates an owner's postcode when one is supplied, so an unacceptable value is a 400 before the
 * owner is ever persisted. Runs after the body has been bound and published (by {@link ValidateNewOwner}
 * on create / {@link ValidateOwner} on update) and before the owner is built or loaded.
 *
 * <p>Postcode is optional: a {@code null} or blank value is left untouched and accepted. When present it
 * must be exactly four digits; otherwise it is rejected. The city's region is derived through the single
 * {@link Owner#regionOf(String) city-to-region table}, and when the region is known the postcode must
 * fall within that region's inclusive range (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099). A city with
 * no known region accepts any four-digit postcode.
 */
public class ValidateOwnerPostcode {

    /** A postcode is exactly four digits. */
    private static final Pattern POSTCODE = Pattern.compile("^[0-9]{4}$");

    public void service(@Val OwnerFieldsDto request) throws InvalidPostcodeException {
        String postcode = request.getPostcode();
        if (postcode == null || postcode.isBlank()) {
            return; // optional when absent — request contract stays backward-compatible
        }
        if (!POSTCODE.matcher(postcode).matches()) {
            throw new InvalidPostcodeException(postcode, "must be four digits");
        }
        int[] range = Owner.postcodeRangeOf(Owner.regionOf(request.getCity()));
        if (range != null) {
            int value = Integer.parseInt(postcode);
            if (value < range[0] || value > range[1]) {
                throw new InvalidPostcodeException(postcode,
                        "out of range " + range[0] + "-" + range[1] + " for city '" + request.getCity() + "'");
            }
        }
    }
}
