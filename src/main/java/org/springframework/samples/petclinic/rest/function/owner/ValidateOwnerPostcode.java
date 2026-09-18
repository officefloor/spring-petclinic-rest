package org.springframework.samples.petclinic.rest.function.owner;

import java.util.List;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidOwnerFieldsException;
import org.springframework.samples.petclinic.util.PostcodeRange;

/**
 * Runs after {@link ValidateOwnerFields}, reading the already-validated body as a variable.
 * When a postcode is present (already known to be 4 digits by the DTO's pattern), it must be
 * valid for the owner's city per the fixed region ranges; otherwise this throws
 * {@link InvalidOwnerFieldsException} naming {@code postcode} for a 400. A city with no known
 * region accepts any 4-digit postcode, and an absent postcode is left untouched (optional).
 */
public class ValidateOwnerPostcode {

    public void service(@Val OwnerFieldsDto request) throws InvalidOwnerFieldsException {
        String postcode = request.getPostcode();
        if (postcode == null || postcode.isBlank()) {
            return; // optional when absent
        }
        if (!PostcodeRange.isValidForCity(request.getCity(), postcode)) {
            throw new InvalidOwnerFieldsException(List.of("postcode"));
        }
    }
}
