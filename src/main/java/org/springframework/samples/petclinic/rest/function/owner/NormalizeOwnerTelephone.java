package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.escalation.InvalidTelephoneException;

/**
 * Normalizes a new owner's telephone: strips every non-digit character, then requires exactly
 * 10 digits. The stored (and later returned) value is the 10-digit string; anything else is a 400.
 * Mutates the built {@link Owner} in place so {@link SaveOwner} persists the normalized value.
 */
public class NormalizeOwnerTelephone {

    public void service(@Val Owner owner) throws InvalidTelephoneException {
        String telephone = owner.getTelephone();
        String digits = telephone == null ? "" : telephone.replaceAll("\\D", "");
        if (digits.length() != 10) {
            throw new InvalidTelephoneException(
                    "Telephone must contain exactly 10 digits after removing non-digit characters");
        }
        owner.setTelephone(digits);
    }
}
