package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Assigns the owner's {@code customerCode}, formatted {@code <REGION>-<HASH8>} where REGION is the
 * region derived from the owner's postcode (falling back to the city, then {@code "UNKNOWN"}) and
 * HASH8 is the first eight upper-case hex characters of {@code SHA-256(normalizedTelephone + lastName)}.
 * The identity depends only on the owner's own fields; there are no sequence numbers.
 */
public class AssignCustomerCode {

    public void service(@Val Owner owner) {
        owner.setCustomerCode(CustomerCodes.build(owner));
    }
}
