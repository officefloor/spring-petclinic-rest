package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Assigns a new owner's {@code customerCode} before it is saved. The code is
 * {@code <REGION>-<HASH8>} (see {@link OwnerCustomerCodes}): the region derived from the owner's
 * postcode and a hash of their telephone and last name, so it is a stable function of the owner's
 * own identity rather than a per-city sequence.
 */
public class AssignOwnerCustomerCode {

    public void service(@Val Owner owner) {
        owner.setCustomerCode(OwnerCustomerCodes.forOwner(owner));
    }
}
