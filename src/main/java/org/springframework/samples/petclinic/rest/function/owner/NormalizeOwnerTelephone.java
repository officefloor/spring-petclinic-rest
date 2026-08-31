package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.escalation.InvalidTelephoneException;

/**
 * On create, reduces the telephone to its digits and requires exactly ten of them.
 * Mutates the built {@link Owner} in place so later steps store and return the 10-digit value.
 */
public class NormalizeOwnerTelephone {

    public void service(@Val Owner owner) throws InvalidTelephoneException {
        String digits = owner.getTelephone() == null ? "" : owner.getTelephone().replaceAll("\\D", "");
        if (digits.length() != 10) {
            throw new InvalidTelephoneException("Telephone must contain exactly 10 digits");
        }
        owner.setTelephone(digits);
    }
}
