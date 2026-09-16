package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.CustomerCode;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Assigns the owner's customer code, formatted {@code <REGION>-<HASH8>} (see
 * {@link CustomerCode}): REGION derived from the postcode and HASH8 the first 8 upper-case
 * hex characters of SHA-256 over the normalized telephone plus last name. Runs after the
 * telephone has been normalized and mutates the built {@link Owner} in place.
 */
public class AssignOwnerCustomerCode {

    public void service(@Val Owner owner) {
        owner.setCustomerCode(CustomerCode.forOwner(owner));
    }
}
