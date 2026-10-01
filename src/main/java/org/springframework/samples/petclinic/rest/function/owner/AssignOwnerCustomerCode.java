package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Assigns the owner's {@code customerCode}, formatted {@code <REGION>-<HASH8>}, where {@code REGION}
 * is the region code derived from the postcode and {@code HASH8} is the first eight upper-case hex
 * characters of the SHA-256 of the normalized telephone concatenated with the last name (e.g.
 * {@code NSW-1A2B3C4D}). The shape and all of its parts are owned by {@link OwnerCustomerCode}.
 *
 * <p>The code carries no sequence number, so it depends only on the owner's own fields and needs no
 * repository lookup.
 */
public class AssignOwnerCustomerCode {

    public void service(@Val Owner owner) {
        owner.setCustomerCode(OwnerCustomerCode.customerCode(owner));
    }
}
