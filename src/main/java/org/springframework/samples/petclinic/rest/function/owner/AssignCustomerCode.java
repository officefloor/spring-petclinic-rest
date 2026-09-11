package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Assigns the owner's {@code customerCode}, formatted {@code <REGION>-<HASH8>} where
 * REGION is the region code derived from the postcode and HASH8 is the first 8
 * upper-case hex characters of SHA-256 over (normalizedTelephone + lastName) — see
 * {@link CustomerCodes}. The identity is a pure function of the owner's own fields, so
 * this no longer depends on the owners already stored.
 */
public class AssignCustomerCode {

    public void service(@Val Owner owner) {
        owner.setCustomerCode(CustomerCodes.forOwner(owner));
    }
}
