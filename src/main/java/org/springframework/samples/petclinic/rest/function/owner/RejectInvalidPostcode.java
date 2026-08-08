package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.escalation.InvalidPostcodeException;

/**
 * Validates a supplied postcode against the region derived from the owner's city.
 * The postcode is optional: when absent (null/blank) nothing is checked. When present
 * it has already been shape-checked (4 digits) by bean validation, so this step only
 * enforces the region range — NSW 2000-2099, VIC 3000-3099, QLD 4000-4099 — throwing a
 * checked {@link InvalidPostcodeException} (turned into a 400 by the escalation handler)
 * when out of range. A city with no known region accepts any 4-digit postcode.
 */
public class RejectInvalidPostcode {

    public void service(@Val Owner owner) throws InvalidPostcodeException {
        String postcode = owner.getPostcode();
        if (postcode == null || postcode.isBlank()) {
            return; // optional when absent
        }
        int value;
        try {
            value = Integer.parseInt(postcode.trim());
        }
        catch (NumberFormatException ex) {
            throw new InvalidPostcodeException("Postcode must be 4 digits: '" + postcode + "'");
        }
        if (!PostcodeRegions.isValid(owner.getCity(), value)) {
            throw new InvalidPostcodeException(
                    "Postcode '" + postcode + "' is not valid for city '" + owner.getCity() + "'");
        }
    }
}
