package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.escalation.InvalidTelephoneException;

/**
 * Normalizes the owner's telephone by removing every non-digit character, then requires
 * exactly 10 digits. The normalized 10-digit value is stored back on the owner (so it is
 * persisted and returned); anything else escalates as a 400 via
 * {@link InvalidTelephoneException}.
 */
public class NormalizeOwnerTelephone {

    public void service(@Val Owner owner) throws InvalidTelephoneException {
        String raw = owner.getTelephone();
        String digits = raw == null ? "" : raw.replaceAll("\\D", "");
        if (digits.length() != 10) {
            throw new InvalidTelephoneException(
                    "Telephone must be exactly 10 digits after removing non-digit characters");
        }
        owner.setTelephone(digits);
    }
}
