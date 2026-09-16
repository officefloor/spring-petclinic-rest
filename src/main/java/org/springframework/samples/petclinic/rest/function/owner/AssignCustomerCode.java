package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.util.CustomerCode;
import org.springframework.samples.petclinic.util.OwnerRegion;

/**
 * Assigns a newly built owner its {@code customerCode}, formatted {@code <REGION>-<HASH8>}
 * where REGION is the owner's region (derived from its postcode, see {@link OwnerRegion})
 * and HASH8 the first 8 upper-case hex characters of SHA-256 over the normalized telephone
 * concatenated with the last name (e.g. {@code NSW-1A2B3C4D}). Runs after
 * {@link NormalizeOwnerTelephone} (so the telephone is canonical) and before
 * {@link SaveOwner}, mutating the built owner in place so the code is stored and returned
 * with the owner.
 */
public class AssignCustomerCode {

    public void service(@Val Owner owner) {
        String region = OwnerRegion.of(owner);
        owner.setCustomerCode(CustomerCode.of(region, owner.getTelephone(), owner.getLastName()));
    }
}
